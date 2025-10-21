package com.nvd.demo_list

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import androidx.annotation.RawRes
import androidx.core.graphics.createBitmap
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class PdfBitmapConverter(private val context: Context) {
    suspend fun pdfToImageFiles(contentUri: Uri): List<File> = withContext(Dispatchers.IO) {
        val imageFiles = mutableListOf<File>()

        context.contentResolver.openFileDescriptor(contentUri, "r")?.use { descriptor ->
            PdfRenderer(descriptor).use { renderer ->
                for (i in 0 until renderer.pageCount) {
                    val page = renderer.openPage(i)
                    val bitmap = createBitmap(page.width, page.height)
                    val canvas = Canvas(bitmap)
                    canvas.drawColor(Color.WHITE)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

                    val file = File(context.cacheDir, "pdf_page_$i.png")
                    FileOutputStream(file).use { out ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                    }
                    imageFiles.add(file)

                    page.close()
                    bitmap.recycle()
                }
            }
        }

        imageFiles
    }

    suspend fun pdfFromAssets(assetName: String): List<File> {
        val file = File(context.cacheDir, "$assetName.pdf")
        context.assets.open(assetName).use { it.copyTo(FileOutputStream(file)) }
        return pdfToImageFiles(file.toUri())
    }

    suspend fun pdfFromRaw(@RawRes rawResId: Int): List<File> {
        val file = File(context.cacheDir, "${rawResId}.pdf")
        context.resources.openRawResource(rawResId).use { it.copyTo(FileOutputStream(file)) }
        return pdfToImageFiles(file.toUri())
    }
}