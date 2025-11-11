package com.nvd.demo_list.utils

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.nvd.demo_list.repository.SplashRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

data class SplashManagerState(
    val cachedImageFile: File? = null,
    val isChecking: Boolean = false,
    val isDownloading: Boolean = false
)

/**
 * Singleton manager để quản lý splash image ở app level
 * Tự động check và download splash image khi app khởi động
 */
object SplashManager {
    private const val TAG = "SplashManager"
    private val repository = SplashRepository()
    
    // App-level coroutine scope
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    private val _state = MutableStateFlow<SplashManagerState>(SplashManagerState())
    val state: StateFlow<SplashManagerState> = _state.asStateFlow()

    /**
     * Khởi tạo và bắt đầu check API
     * Nên gọi từ Application.onCreate()
     */
    fun initialize(context: Context) {
        Log.d(TAG, "SplashManager initialized")
        
        // Load cached image ngay lập tức
        val cachedImageFile = SplashCacheManager.getSplashImageFile(context)
        _state.value = _state.value.copy(cachedImageFile = cachedImageFile)
        
        // Bắt đầu check API ở background
        checkAndUpdateSplash(context)
    }

    /**
     * Check API và update splash image nếu cần
     */
    private fun checkAndUpdateSplash(context: Context) {
        appScope.launch {
            try {
                _state.value = _state.value.copy(isChecking = true)
                Log.d(TAG, "Checking splash API...")

                val apiResult = repository.getSplashUrl()
                
                apiResult.onSuccess { newSplashUrl ->
                    // Kiểm tra xem URL có thay đổi không
                    val urlChanged = SplashCacheManager.hasUrlChanged(context, newSplashUrl)
                    val cachedImageFile = SplashCacheManager.getSplashImageFile(context)
                    val cachedUrl = SplashCacheManager.getCachedUrl(context)
                    
                    // Nếu URL thay đổi hoặc không có file cache (kể cả khi có URL trong SharedPreferences)
                    if (urlChanged || cachedImageFile == null) {
                        // URL đã thay đổi hoặc file cache không tồn tại, cần download ảnh mới
                        if (cachedUrl != null && cachedImageFile == null) {
                            Log.d(TAG, "⚠URL exists in SharedPreferences but image file missing, downloading...")
                        } else {
                            Log.d(TAG, "Downloading new splash image...")
                        }
                        downloadAndCacheImage(context, newSplashUrl)
                    } else {
                        // URL không đổi và file cache tồn tại, sử dụng cache hiện tại
                        Log.d(TAG, "Splash image is up to date")
                        _state.value = _state.value.copy(
                            cachedImageFile = cachedImageFile,
                            isChecking = false
                        )
                    }
                }.onFailure { error ->
                    Log.e(TAG, "Error fetching splash URL: ${error.message}")
                    // Giữ nguyên cache nếu có
                    _state.value = _state.value.copy(isChecking = false)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error: ${e.message}", e)
                _state.value = _state.value.copy(isChecking = false)
            }
        }
    }

    /**
     * Download và cache ảnh mới
     */
    private fun downloadAndCacheImage(context: Context, imageUrl: String) {
        appScope.launch {
            try {
                _state.value = _state.value.copy(isDownloading = true)
                
                val downloadResult = repository.downloadImage(imageUrl)
                
                downloadResult.onSuccess { tempFile ->
                    // Lưu vào cache
                    val saved = SplashCacheManager.saveSplashImage(context, tempFile, imageUrl)
                    
                    if (saved) {
                        val cachedFile = SplashCacheManager.getSplashImageFile(context)
                        _state.value = _state.value.copy(
                            cachedImageFile = cachedFile,
                            isDownloading = false,
                            isChecking = false
                        )
                        Log.d(TAG, "Splash image cached successfully")
                        
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                context.applicationContext,
                                "Download image splash success!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        _state.value = _state.value.copy(
                            isDownloading = false,
                            isChecking = false
                        )
                        Log.e(TAG, "Failed to save splash image")
                    }
                    
                    // Xóa temp file
                    tempFile.delete()
                }.onFailure { error ->
                    Log.e(TAG, "Error downloading image: ${error.message}")
                    // Giữ nguyên cache nếu có
                    val cachedImageFile = SplashCacheManager.getSplashImageFile(context)
                    _state.value = _state.value.copy(
                        cachedImageFile = cachedImageFile,
                        isDownloading = false,
                        isChecking = false
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error downloading: ${e.message}", e)
                val cachedImageFile = SplashCacheManager.getSplashImageFile(context)
                _state.value = _state.value.copy(
                    cachedImageFile = cachedImageFile,
                    isDownloading = false,
                    isChecking = false
                )
            }
        }
    }

    /**
     * Lấy cached image file hiện tại
     */
    fun getCachedImageFile(): File? {
        return _state.value.cachedImageFile
    }
}

