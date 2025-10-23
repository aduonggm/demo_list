package com.nvd.demo_list.screens

import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.annotation.RawRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.nvd.demo_list.PdfBitmapConverter
import java.io.File

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
//                        .graphicsLayer(
//                            scaleX = scale,
//                            scaleY = scale,
//                            translationX = offset.x,
//                            translationY = offset.y
//                        )
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
