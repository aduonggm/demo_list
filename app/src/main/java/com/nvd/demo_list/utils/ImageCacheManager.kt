package com.nvd.demo_list.utils

import android.graphics.Bitmap
import android.util.Log
import android.util.LruCache

/**
 * Singleton cache manager để lưu trữ bitmap đã load
 * Sử dụng LruCache để tự động quản lý bộ nhớ
 */
object ImageCacheManager {
    private const val TAG = "ImageCacheManager"

    // Tính toán cache size (3/4 của max memory để cache tối thiểu 10 ảnh)
    // Với typical Android device có 512MB heap, cache sẽ là ~384MB
    private val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
    private val cacheSize = (maxMemory * 3) / 4

    // Max size cho mỗi bitmap (giảm xuống 30MB để cache nhiều ảnh hơn)
    // Với cache 384MB, có thể lưu ~12 ảnh
    private const val MAX_BITMAP_SIZE_KB = 30 * 1024 // 30MB per image

    // LruCache với key là URL và value là Bitmap
    private val memoryCache = object : LruCache<String, Bitmap>(cacheSize) {
        override fun sizeOf(key: String, bitmap: Bitmap): Int {
            // Tính size của bitmap theo KB
            val sizeKB = bitmap.byteCount / 1024
            return if (sizeKB == 0) 1 else sizeKB // Đảm bảo tối thiểu là 1KB
        }

        override fun entryRemoved(evicted: Boolean, key: String, oldValue: Bitmap, newValue: Bitmap?) {
            super.entryRemoved(evicted, key, oldValue, newValue)
            if (evicted) {
                Log.w(TAG, "⚠️ Bitmap evicted from cache: $key, size: ${oldValue.byteCount / 1024}KB")
                Log.d(TAG, "Cache info: ${getCacheInfo()}")
            }
        }
    }

    init {
        Log.d(TAG, "ImageCacheManager initialized. Max memory: ${maxMemory}KB, Cache size: ${cacheSize}KB")
    }

    /**
     * Lấy bitmap từ cache
     */
    fun getBitmap(url: String): Bitmap? {
        val bitmap = memoryCache.get(url)
        if (bitmap != null) {
            Log.d(TAG, "✅ Cache HIT: $url")
        } else {
            Log.d(TAG, "❌ Cache MISS: $url")
        }
        return bitmap
    }

    /**
     * Lưu bitmap vào cache
     * Tự động downscale nếu bitmap quá lớn để tiết kiệm bộ nhớ
     */
    fun putBitmap(url: String, bitmap: Bitmap) {
        val bitmapSize = bitmap.byteCount / 1024
        val existingBitmap = memoryCache.get(url)

        if (existingBitmap == null) {
            // Kiểm tra nếu bitmap quá lớn, downscale để tiết kiệm bộ nhớ
            val finalBitmap = if (bitmapSize > MAX_BITMAP_SIZE_KB) {
                // Tính scale factor để giảm size xuống MAX_BITMAP_SIZE_KB
                val scaleFactor = Math.sqrt(MAX_BITMAP_SIZE_KB.toDouble() / bitmapSize).toFloat()
                val newWidth = (bitmap.width * scaleFactor).toInt()
                val newHeight = (bitmap.height * scaleFactor).toInt()

                Log.d(TAG, "⚠️ Bitmap too large (${bitmapSize}KB), downscaling from ${bitmap.width}x${bitmap.height} to ${newWidth}x${newHeight}")

                // Downscale bitmap
                val scaledBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
                val scaledSize = scaledBitmap.byteCount / 1024

                Log.d(TAG, "✅ Downscaled bitmap size: ${scaledSize}KB (reduced by ${((1 - scaledSize.toFloat() / bitmapSize) * 100).toInt()}%)")

                scaledBitmap
            } else {
                bitmap
            }

            memoryCache.put(url, finalBitmap)
            val finalSize = finalBitmap.byteCount / 1024
            Log.d(TAG, "💾 Saved to cache: $url, size: ${finalSize}KB")
            Log.d(TAG, "Cache usage: ${memoryCache.size()}KB / ${cacheSize}KB (${(memoryCache.size() * 100 / cacheSize)}%)")
        } else {
            Log.d(TAG, "⏭️ Already in cache: $url")
        }
    }

    /**
     * Xóa bitmap khỏi cache
     */
    fun removeBitmap(url: String) {
        memoryCache.remove(url)
        Log.d(TAG, "🗑️ Removed from cache: $url")
    }

    /**
     * Xóa toàn bộ cache
     */
    fun clearCache() {
        memoryCache.evictAll()
        Log.d(TAG, "🗑️ Cache cleared")
    }

    /**
     * Lấy thông tin cache hiện tại
     */
    fun getCacheInfo(): String {
        return "Cache size: ${memoryCache.size()} KB / $cacheSize KB, " +
                "Hit count: ${memoryCache.hitCount()}, " +
                "Miss count: ${memoryCache.missCount()}"
    }

    /**
     * Kiểm tra xem URL có trong cache không
     */
    fun contains(url: String): Boolean {
        return memoryCache.get(url) != null
    }
}

