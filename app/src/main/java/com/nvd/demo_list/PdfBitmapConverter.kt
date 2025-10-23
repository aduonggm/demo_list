package com.nvd.demo_list

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
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
    // Tăng DPI để có độ phân giải cao hơn - 72 DPI là mặc định, tăng lên 300-600 DPI
    private val dpi = 300 // Có thể điều chỉnh: 150, 300, 600
    private val defaultDpi = 72f
    private val scale = dpi / defaultDpi

    suspend fun pdfToImageFiles(contentUri: Uri): List<File> = withContext(Dispatchers.IO) {
        val imageFiles = mutableListOf<File>()

        context.contentResolver.openFileDescriptor(contentUri, "r")?.use { descriptor ->
            PdfRenderer(descriptor).use { renderer ->
                for (i in 0 until renderer.pageCount) {
                    val page = renderer.openPage(i)

                    // Tính toán kích thước với DPI cao
                    val width = (page.width * scale).toInt()
                    val height = (page.height * scale).toInt()

                    // Tạo bitmap với độ phân giải cao (ARGB_8888 cho chất lượng tốt nhất)
                    val bitmap = createBitmap(width, height)

                    // Vẽ nền trắng
                    bitmap.eraseColor(Color.WHITE)

                    // Tạo matrix để scale
                    val matrix = Matrix().apply {
                        setScale(scale, scale)
                    }

                    // Render với chế độ PRINT và matrix transform
                    page.render(
                        bitmap,
                        null,
                        matrix,
                        PdfRenderer.Page.RENDER_MODE_FOR_PRINT
                    )

                    // Lưu file với compression tốt nhất
                    val file =
                        File(context.cacheDir, "pdf_page_${i}_${System.currentTimeMillis()}.png")
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
        val file = File(context.cacheDir, "${System.currentTimeMillis()}_${rawResId}.pdf")
        context.resources.openRawResource(rawResId).use { it.copyTo(FileOutputStream(file)) }
        return pdfToImageFiles(file.toUri())
    }
}