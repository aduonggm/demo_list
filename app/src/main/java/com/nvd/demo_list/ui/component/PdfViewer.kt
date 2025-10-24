package com.nvd.demo_list.ui.component

import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.annotation.RawRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.nvd.demo_list.PdfBitmapConverter
import com.nvd.demo_list.utils.PdfCacheManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL

@Composable
fun PdfViewer(
    modifier: Modifier = Modifier,
    assetName: String? = null,
    @RawRes rawResId: Int? = null,
    uri: Uri? = null,
    onImageFileReady: (File?) -> Unit = {}
) {


    val context = LocalContext.current
    var imageFiles by remember { mutableStateOf<List<File>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    // State cho zoom và pan
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    LaunchedEffect(assetName, rawResId, uri) {
        val converter = PdfBitmapConverter(context)
        imageFiles = when {
            assetName != null -> converter.pdfFromAssets(assetName)
            rawResId != null -> converter.pdfFromRaw(rawResId)
            uri != null -> converter.pdfToImageFiles(uri)
            else -> emptyList()
        }
        loading = false
        onImageFileReady(imageFiles.getOrNull(0))
    }

    Log.d(
        "========>>>>>>>> ",
        "PdfViewer: file found  ${imageFiles.map { " ${it.length()} ${it.path}" }}"
    )

    if (loading) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        val file = imageFiles.getOrNull(0)
        file?.let {
            // Load bitmap trực tiếp để tránh Coil downscale
            val bitmap = remember(file) {
                BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap()
            }

            bitmap?.let { imageBitmap ->
                Image(
                    bitmap = imageBitmap,
                    contentDescription = "Page ${0 + 1}",
                    contentScale = ContentScale.Fit,
                    modifier = modifier
                        .fillMaxWidth()
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offset.x,
                            translationY = offset.y
                        )
                )
                /*Box(
                    modifier = modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                scale = (scale * zoom).coerceIn(1f, 10f) // Cho phép zoom đến 10x

                                // Tính toán offset với giới hạn
                                val maxX = (size.width * (scale - 1)) / 2
                                val maxY = (size.height * (scale - 1)) / 2
                                offset = Offset(
                                    x = (offset.x + pan.x).coerceIn(-maxX, maxX),
                                    y = (offset.y + pan.y).coerceIn(-maxY, maxY)
                                )
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap = imageBitmap,
                        contentDescription = "Page ${0 + 1}",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer(
                                scaleX = scale,
                                scaleY = scale,
                                translationX = offset.x,
                                translationY = offset.y
                            )
                    )
                }*/
            }
        }
    }
}

@Composable
fun PdfViewerNew(
    modifier: Modifier = Modifier,
    pdfUrl: String? = null,
    onImageFileReady: (File?) -> Unit = {}
) {
    val context = LocalContext.current
    var imageFiles by remember { mutableStateOf<List<File>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var downloadProgress by remember { mutableStateOf(0f) }

    // State cho zoom và pan
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    LaunchedEffect(pdfUrl) {
        if (pdfUrl.isNullOrEmpty()) {
            loading = false
            errorMessage = "PDF URL is empty"
            return@LaunchedEffect
        }

        loading = true
        errorMessage = null
        downloadProgress = 0f

        try {
            val startTime = System.currentTimeMillis()
            Log.d("PdfViewerNew", "🔄 Processing PDF: $pdfUrl")

            // Kiểm tra xem PDF images đã có trong cache chưa
            val cachedImageFiles = PdfCacheManager.getImageFiles(pdfUrl)
            if (cachedImageFiles != null) {
                // Sử dụng cache, không cần download lại
                imageFiles = cachedImageFiles
                onImageFileReady(cachedImageFiles.getOrNull(0))
                val elapsed = System.currentTimeMillis() - startTime
                Log.d(
                    "PdfViewerNew",
                    "✅ Using cached PDF images (${cachedImageFiles.size} files) - ${elapsed}ms"
                )
            } else {
                // Chưa có trong cache, cần download và convert
                Log.d("PdfViewerNew", "📥 Downloading PDF from: $pdfUrl")

                val pdfFile = withContext(Dispatchers.IO) {
                    val url = URL(pdfUrl)
                    val connection = url.openConnection() as java.net.HttpURLConnection

                    // Thêm timeout để tránh hang
                    connection.connectTimeout = 15000 // 15 seconds
                    connection.readTimeout = 30000 // 30 seconds
                    connection.connect()

                    val fileLength = connection.contentLength
                    Log.d("PdfViewerNew", "📦 PDF size: ${fileLength / 1024}KB")

                    // Tạo permanent file trong cache dir với tên duy nhất dựa trên URL
                    val fileName = "pdf_${pdfUrl.hashCode()}_${System.currentTimeMillis()}.pdf"
                    val pdfCacheFile = File(context.cacheDir, fileName)

                    // Streaming download với progress và buffer lớn hơn
                    connection.inputStream.use { input ->
                        pdfCacheFile.outputStream().buffered(8192).use { output ->
                            val buffer = ByteArray(8192) // 8KB buffer
                            var bytesRead: Int
                            var totalBytesRead = 0L

                            while (input.read(buffer).also { bytesRead = it } != -1) {
                                output.write(buffer, 0, bytesRead)
                                totalBytesRead += bytesRead

                                // Update progress
                                if (fileLength > 0) {
                                    downloadProgress = totalBytesRead.toFloat() / fileLength
                                }
                            }
                        }
                    }

                    val downloadTime = System.currentTimeMillis() - startTime
                    Log.d(
                        "PdfViewerNew",
                        "✅ PDF downloaded: ${pdfCacheFile.length() / 1024}KB in ${downloadTime}ms"
                    )
                    pdfCacheFile
                }

                // Lưu PDF file vào cache
                PdfCacheManager.putPdfFile(pdfUrl, pdfFile)

                // Convert PDF thành image files
                val convertStartTime = System.currentTimeMillis()
                val converter = PdfBitmapConverter(context)
                val convertedImageFiles = converter.pdfToImageFiles(Uri.fromFile(pdfFile))

                val convertTime = System.currentTimeMillis() - convertStartTime
                Log.d(
                    "PdfViewerNew",
                    "✅ PDF converted to ${convertedImageFiles.size} page(s) in ${convertTime}ms"
                )

                if (convertedImageFiles.isNotEmpty()) {
                    Log.d(
                        "PdfViewerNew",
                        "📄 First page: ${convertedImageFiles[0].length() / 1024}KB"
                    )
                }

                // Lưu image files vào cache
                PdfCacheManager.putImageFiles(pdfUrl, convertedImageFiles)

                imageFiles = convertedImageFiles
                onImageFileReady(convertedImageFiles.getOrNull(0))

                val totalTime = System.currentTimeMillis() - startTime
                Log.d(
                    "PdfViewerNew",
                    "⏱️ Total time: ${totalTime}ms (download: ${totalTime - convertTime}ms, convert: ${convertTime}ms)"
                )
            }

        } catch (e: java.net.SocketTimeoutException) {
            Log.e("PdfViewerNew", "⏰ Timeout loading PDF: ${e.message}", e)
            errorMessage = "Timeout: Server took too long to respond"
        } catch (e: java.io.IOException) {
            Log.e("PdfViewerNew", "🌐 Network error loading PDF: ${e.message}", e)
            errorMessage = "Network error: ${e.message}"
        } catch (e: Exception) {
            Log.e("PdfViewerNew", "❌ Error loading PDF from URL: ${e.message}", e)
            errorMessage = "Failed to load PDF: ${e.message}"
        } finally {
            loading = false
            downloadProgress = 0f
        }
    }

    if (loading) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            androidx.compose.foundation.layout.Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
            ) {
                CircularProgressIndicator()
                if (downloadProgress > 0f) {
                    Text(
                        text = "Downloading: ${(downloadProgress * 100).toInt()}%",
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    } else if (errorMessage != null) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = errorMessage ?: "Unknown error",
                color = androidx.compose.ui.graphics.Color.Red
            )
        }
    } else {
        val file = imageFiles.getOrNull(0)
        file?.let {
            // Load bitmap trực tiếp để tránh Coil downscale
            val bitmap = remember(file) {
                BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap()
            }

            bitmap?.let { imageBitmap ->
                Image(
                    bitmap = imageBitmap,
                    contentDescription = "PDF Page ${0 + 1}",
                    contentScale = ContentScale.Fit,
                    modifier = modifier
                        .fillMaxWidth()
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offset.x,
                            translationY = offset.y
                        )
                )
            }
        }
    }
}
