package com.nvd.demo_list.screens

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.nvd.demo_list.R

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ZoomableImage(
    imageUrl: String,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    onClick: () -> Unit
) {
    with(sharedTransitionScope) {
        // Only show the thumbnail in list
        Box(
            contentAlignment = Alignment.Center
        ) {
//            PdfViewer(
//
//                rawResId = R.raw.test,
//                modifier = Modifier
//                    .clickable(onClick = onClick)
//
//            )

            AsyncImage(
                model = imageUrl,
                contentDescription = "Zoomable Image",
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .fillMaxWidth()
                    .sharedElement(
                        sharedTransitionScope.rememberSharedContentState(key = imageUrl),
                        animatedVisibilityScope = animatedContentScope,
                    )
                    .clickable(onClick = onClick)
                    .drawWithContent {
                        drawContent()
                        drawRect(Color.Black.copy(alpha = 0.3f))
                    }
            )
//
//            Icon(
//                painterResource(R.drawable.baseline_zoom_in_24),
//                contentDescription = null,
//                modifier = Modifier.size(60.dp),
//                tint = Color.White
//            )
        }
    }
}
