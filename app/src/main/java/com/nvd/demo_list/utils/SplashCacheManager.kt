package com.nvd.demo_list.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import java.io.File
import androidx.core.content.edit

/**
 * Singleton cache manager để lưu trữ splash image đã download
 */
object SplashCacheManager {
    private const val TAG = "SplashCacheManager"
    private const val SPLASH_CACHE_DIR = "splash_cache"
    private const val SPLASH_IMAGE_FILE = "splash_image.jpg"
    private const val PREF_NAME = "splash_prefs"
    private const val PREF_KEY_SPLASH_URL = "splash_url"
    
    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Lấy splash image file từ cache
     */
    fun getSplashImageFile(context: Context): File? {
        val cacheDir = File(context.cacheDir, SPLASH_CACHE_DIR)
        val imageFile = File(cacheDir, SPLASH_IMAGE_FILE)
        
        if (imageFile.exists() && imageFile.length() > 0) {
            Log.d(TAG, "Splash image found in cache: ${imageFile.absolutePath}, size: ${imageFile.length()} bytes")
            return imageFile
        } else {
            Log.d(TAG, "Splash image not found in cache")
            return null
        }
    }

    /**
     * Lưu splash image vào cache
     */
    fun saveSplashImage(context: Context, imageFile: File, splashUrl: String): Boolean {
        return try {
            val cacheDir = File(context.cacheDir, SPLASH_CACHE_DIR)
            if (!cacheDir.exists()) {
                cacheDir.mkdirs()
            }

            val targetFile = File(cacheDir, SPLASH_IMAGE_FILE)

            // Copy image file
            imageFile.inputStream().use { input ->
                targetFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            // Save URL to SharedPreferences
            val prefs = getSharedPreferences(context)
            prefs.edit().putString(PREF_KEY_SPLASH_URL, splashUrl).apply()

            Log.d(TAG, "Splash image saved to cache: ${targetFile.absolutePath}, size: ${targetFile.length()} bytes")
            Log.d(TAG, "Splash URL saved to SharedPreferences: $splashUrl")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error saving splash image: ${e.message}", e)
            false
        }
    }

    /**
     * Lấy URL đã cache từ SharedPreferences
     * Kiểm tra xem file cache có tồn tại không
     */
    fun getCachedUrl(context: Context): String? {
        return try {
            val prefs = getSharedPreferences(context)
            val url = prefs.getString(PREF_KEY_SPLASH_URL, null)
            
            if (url != null && url.isNotEmpty()) {
                // Kiểm tra xem file cache có tồn tại không
                val imageFile = getSplashImageFile(context)
                if (imageFile != null) {
                    Log.d(TAG, "Cached URL found and image file exists: $url")
                    url
                } else {
                    Log.w(TAG, "⚠Cached URL found but image file missing: $url")
                    // URL có nhưng file không tồn tại, xóa URL để download lại
                    prefs.edit { remove(PREF_KEY_SPLASH_URL) }
                    null
                }
            } else {
                Log.d(TAG, "Cached URL not found in SharedPreferences")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error reading cached URL: ${e.message}", e)
            null
        }
    }

    /**
     * Kiểm tra xem URL có thay đổi không
     */
    fun hasUrlChanged(context: Context, newUrl: String): Boolean {
        val cachedUrl = getCachedUrl(context)
        val changed = cachedUrl != newUrl
        if (changed) {
            Log.d(TAG, "URL changed: $cachedUrl -> $newUrl")
        } else {
            Log.d(TAG, "URL unchanged: $newUrl")
        }
        return changed
    }

    /**
     * Xóa cache splash image và URL
     */
    fun clearCache(context: Context) {
        try {
            // Xóa file cache
            val cacheDir = File(context.cacheDir, SPLASH_CACHE_DIR)
            if (cacheDir.exists()) {
                cacheDir.deleteRecursively()
                Log.d(TAG, "🗑️ Splash cache files cleared")
            }
            
            // Xóa URL từ SharedPreferences
            val prefs = getSharedPreferences(context)
            prefs.edit { remove(PREF_KEY_SPLASH_URL) }
            Log.d(TAG, "Splash URL cleared from SharedPreferences")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing splash cache: ${e.message}", e)
        }
    }

    /**
     * Kiểm tra xem có splash image trong cache không
     * Kiểm tra cả file cache và URL trong SharedPreferences
     */
    fun hasCachedImage(context: Context): Boolean {
        val imageFile = getSplashImageFile(context)
        val cachedUrl = getCachedUrl(context)
        // Cả file và URL đều phải tồn tại
        return imageFile != null && cachedUrl != null
    }
}

