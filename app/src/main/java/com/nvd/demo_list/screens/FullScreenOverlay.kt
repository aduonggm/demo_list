package com.nvd.demo_list.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import nl.birdly.zoombox.gesture.condition.WithinXBoundsTouchCondition
import nl.birdly.zoombox.gesture.transform.TransformGestureHandler
import nl.birdly.zoombox.rememberMutableZoomState
import nl.birdly.zoombox.zoomable


@Composable
fun FullscreenZoomOverlay(
    imageUrl: String,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()

    // Animatable states for smooth Sony-like animations
    val scale = remember { Animatable(1f) }
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }

    val zoomState = rememberMutableZoomState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Color.Black
            )
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = "Zoomed Image",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .zoomable(
                    zoomRange =  1f..4f,
                    zoomState = zoomState,
                    transformGestureHandler = TransformGestureHandler(
                        onCondition = WithinXBoundsTouchCondition()
                    )

                )


        )
    }
}
