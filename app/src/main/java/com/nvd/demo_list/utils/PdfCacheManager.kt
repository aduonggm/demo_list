package com.nvd.demo_list.utils

import android.util.Log
import java.io.File

/**
 * Singleton cache manager để lưu trữ PDF files đã download
 */
object PdfCacheManager {
    private const val TAG = "PdfCacheManager"

    // Cache map: URL -> List of converted image files
    private val pdfImageCache = mutableMapOf<String, List<File>>()

    // Cache map: URL -> Downloaded PDF file
    private val pdfFileCache = mutableMapOf<String, File>()

    /**
     * Lấy danh sách image files từ cache
     */
    fun getImageFiles(url: String): List<File>? {
        val files = pdfImageCache[url]
        if (files != null) {
            // Kiểm tra xem files còn tồn tại không
            val allExist = files.all { it.exists() }
            if (allExist) {
                Log.d(TAG, "✅ Cache HIT for PDF images: $url (${files.size} files)")
                return files
            } else {
                // Nếu có file bị xóa, remove khỏi cache
                Log.w(TAG, "⚠️ Cache invalid, some files missing: $url")
                pdfImageCache.remove(url)
                pdfFileCache[url]?.delete()
                pdfFileCache.remove(url)
            }
        } else {
            Log.d(TAG, "❌ Cache MISS for PDF images: $url")
        }
        return null
    }

    /**
     * Lưu danh sách image files vào cache
     */
    fun putImageFiles(url: String, imageFiles: List<File>) {
        pdfImageCache[url] = imageFiles
        Log.d(TAG, "💾 Saved PDF images to cache: $url (${imageFiles.size} files)")
        Log.d(TAG, "Cache size: ${pdfImageCache.size} PDFs")
    }

    /**
     * Lấy PDF file đã download từ cache
     */
    fun getPdfFile(url: String): File? {
        val file = pdfFileCache[url]
        if (file != null && file.exists()) {
            Log.d(TAG, "✅ Cache HIT for PDF file: $url")
            return file
        } else {
            if (file != null && !file.exists()) {
                Log.w(TAG, "⚠️ Cached PDF file missing: $url")
                pdfFileCache.remove(url)
            } else {
                Log.d(TAG, "❌ Cache MISS for PDF file: $url")
            }
        }
        return null
    }

    /**
     * Lưu PDF file đã download vào cache
     */
    fun putPdfFile(url: String, pdfFile: File) {
        pdfFileCache[url] = pdfFile
        Log.d(TAG, "💾 Saved PDF file to cache: $url, size: ${pdfFile.length()} bytes")
    }

    /**
     * Xóa cache cho một URL cụ thể
     */
    fun removeCache(url: String) {
        pdfImageCache.remove(url)
        pdfFileCache[url]?.delete()
        pdfFileCache.remove(url)
        Log.d(TAG, "🗑️ Removed cache for: $url")
    }

    /**
     * Xóa toàn bộ cache
     */
    fun clearCache() {
        pdfImageCache.values.forEach { files ->
            files.forEach { it.delete() }
        }
        pdfFileCache.values.forEach { it.delete() }

        pdfImageCache.clear()
        pdfFileCache.clear()
        Log.d(TAG, "🗑️ All PDF cache cleared")
    }

    /**
     * Lấy thông tin cache hiện tại
     */
    fun getCacheInfo(): String {
        return "PDF Cache: ${pdfImageCache.size} PDFs cached, " +
                "Total files: ${pdfImageCache.values.sumOf { it.size }}"
    }
}

