package com.nvd.demo_list.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class ImageManager(private val context: Context) {
    
    private val imagesDir = File(context.getExternalFilesDir(null), "Pictures")
    
    init {
        if (!imagesDir.exists()) {
            imagesDir.mkdirs()
        }
    }
    
    fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_${timeStamp}_"
        return File.createTempFile(imageFileName, ".jpg", imagesDir)
    }
    
    fun getImageUri(file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
    }
    
    fun saveBitmapToFile(bitmap: Bitmap, fileName: String): File? {
        return try {
            val file = File(imagesDir, fileName)
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
            file
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
    
    fun getAllImages(): List<File> {
        return imagesDir.listFiles()?.filter { file ->
            file.isFile && (file.name.endsWith(".jpg") || file.name.endsWith(".jpeg") || file.name.endsWith(".png"))
        }?.sortedByDescending { it.lastModified() } ?: emptyList()
    }
    
    fun deleteImage(file: File): Boolean {
        return try {
            file.delete()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    fun getBitmapFromFile(file: File): Bitmap? {
        return try {
            BitmapFactory.decodeFile(file.absolutePath)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Copy image from content URI to app storage and return the FileProvider URI
     * This ensures the image persists even after the app is closed
     */
    fun copyUriToAppStorage(sourceUri: Uri): Uri? {
        return try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val imageFileName = "memo_${timeStamp}_${UUID.randomUUID()}.jpg"
            val destFile = File(imagesDir, imageFileName)
            
            // Copy the file
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }
            
            // Return FileProvider URI
            getImageUri(destFile)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Check if URI is a content URI (temporary) or file URI (persistent)
     */
    fun isContentUri(uriString: String): Boolean {
        return uriString.startsWith("content://")
    }
}
