package com.nvd.demo_list.screens.splash

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nvd.demo_list.utils.SplashCacheManager
import com.nvd.demo_list.utils.SplashManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

data class SplashState(
    val cachedImageFile: File? = null,
    val shouldNavigate: Boolean = false
)

class SplashViewModel(private val context: Context) : ViewModel() {
    private val _state = MutableStateFlow(SplashState())
    val state: StateFlow<SplashState> = _state.asStateFlow()

    init {
        // Load cached image ngay lập tức từ cache (không đợi SplashManager)
        val cachedImageFile = SplashCacheManager.getSplashImageFile(context)
        if (cachedImageFile != null) {
            Log.d("SplashViewModel", "Loaded cached image immediately: ${cachedImageFile.absolutePath}")
            _state.value = _state.value.copy(cachedImageFile = cachedImageFile)
        } else {
            Log.d("SplashViewModel", "No cached image found")
        }
        
        // Đọc state từ SplashManager để cập nhật khi có thay đổi
        viewModelScope.launch {
            SplashManager.state.collect { managerState ->
                if (managerState.cachedImageFile != null) {
                    Log.d("SplashViewModel", "Updated from SplashManager: ${managerState.cachedImageFile.absolutePath}")
                    _state.value = _state.value.copy(
                        cachedImageFile = managerState.cachedImageFile
                    )
                }
            }
        }
        
        // Navigate sau 2 giây
        navigateAfterDelay()
    }

    /**
     * Navigate sau delay
     */
    private fun navigateAfterDelay() {
        viewModelScope.launch {
            kotlinx.coroutines.delay(2000) // 2 giây
            _state.value = _state.value.copy(shouldNavigate = true)
        }
    }
}

