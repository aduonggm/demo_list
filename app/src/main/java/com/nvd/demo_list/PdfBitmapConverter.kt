package com.nvd.demo_list

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import androidx.annotation.RawRes
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import androidx.core.graphics.createBitmap
import androidx.core.net.toUri
import java.io.File
import java.io.FileOutputStream

class PdfBitmapConverter(private val context: Context) {
    var renderer: PdfRenderer? = null

    suspend fun pdfToBitmaps(contentUri: Uri): List<Bitmap> {
        return withContext(Dispatchers.IO) {
            renderer?.close()

            context
                .contentResolver
                .openFileDescriptor(contentUri, "r")
                ?.use { descriptor ->
                    with(PdfRenderer(descriptor)) {
                        renderer = this

                        return@withContext (0 until pageCount).map { index ->
                            async {
                                openPage(index).use { page ->
                                    val bitmap = createBitmap(page.width, page.height)

                                    val canvas = Canvas(bitmap).apply {
                                        drawColor(Color.WHITE)
                                        drawBitmap(bitmap, 0f, 0f, null)
                                    }

                                    page.render(
                                        bitmap,
                                        null,
                                        null,
                                        PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
                                    )

                                    bitmap
                                }
                            }
                        }.awaitAll()
                    }
                }
            return@withContext emptyList()
        }
    }

    suspend fun pdfFromRaw(@RawRes rawResId: Int): List<Bitmap> {
        val file = File(context.cacheDir, "${System.currentTimeMillis()}_temp_pdf_from_raw.pdf")
        context.resources.openRawResource(rawResId).use { it.copyTo(FileOutputStream(file)) }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        return pdfToBitmaps(file.toUri())
    }

    suspend fun pdfFromAssets(assetName: String): List<Bitmap> {
        val file = File(context.cacheDir, "temp_pdf_from_assets.pdf")
        context.assets.open(assetName).use { it.copyTo(FileOutputStream(file)) }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        return pdfToBitmaps(uri)
    }
}