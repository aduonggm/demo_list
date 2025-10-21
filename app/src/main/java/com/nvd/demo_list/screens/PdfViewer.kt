package com.nvd.demo_list.screens

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.annotation.RawRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.nvd.demo_list.PdfBitmapConverter
import kotlinx.coroutines.launch
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
            uri != null -> converter.pdfToImageFiles(uri)
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
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .padding(8.dp)
            )
        }
    }
}
