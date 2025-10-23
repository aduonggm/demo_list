package com.nvd.demo_list.screens

import android.net.Uri
import android.util.Log
import androidx.annotation.RawRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.nvd.demo_list.PdfBitmapConverter
import java.io.File

@Composable
fun PdfViewer(
    modifier: Modifier = Modifier,
    assetName: String? = null,
    @RawRes rawResId: Int? = null,
    uri: Uri? = null
) {


    val context = LocalContext.current
    var imageFiles by remember { mutableStateOf<List<File>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(assetName, rawResId, uri) {
        val converter = PdfBitmapConverter(context)
        imageFiles = when {
            assetName != null -> converter.pdfFromAssets(assetName)
            rawResId != null -> converter.pdfFromRaw(rawResId)
            uri != null -> converter.pdfToImageFiles(uri, "")
            else -> emptyList()
        }
        loading = false
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
            AsyncImage(
                file,
                contentDescription = "Page ${0 + 1}",
                contentScale = ContentScale.FillWidth,
                modifier = modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )
        }
    }
}
