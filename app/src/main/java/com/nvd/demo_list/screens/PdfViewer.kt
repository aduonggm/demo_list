package com.nvd.demo_list.screens

import android.graphics.Bitmap
import android.net.Uri
import androidx.annotation.RawRes
import androidx.compose.foundation.Image
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
import com.nvd.demo_list.PdfBitmapConverter
import kotlinx.coroutines.launch

@Composable
fun PdfViewer(
    modifier: Modifier = Modifier,
    assetName: String? = null,
    @RawRes rawResId: Int? = null,
    uri: Uri? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var bitmaps by remember { mutableStateOf<List<Bitmap>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(assetName, rawResId, uri) {
        loading = true
        val converter = PdfBitmapConverter(context)
        bitmaps = when {
            assetName != null -> converter.pdfFromAssets(assetName)
            rawResId != null -> converter.pdfFromRaw(rawResId)
            uri != null -> converter.pdfToBitmaps(uri)
            else -> emptyList()
        }
        loading = false
    }

    if (loading) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        val bitmap = bitmaps.getOrNull(0)
        bitmap?.let {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Page ${0 + 1}",
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )
        }
//        LazyColumn(
//            modifier = modifier.fillMaxSize(),
//            verticalArrangement = Arrangement.spacedBy(8.dp),
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            items(bitmaps.size) { index ->
//                val bitmap = bitmaps[index]
//
//            }
//        }
    }
}
