package com.nvd.demo_list.screens

import android.net.Uri
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ImageDetailScreen(
    itemId: String,
    imageUrl: String,
    navController: NavController,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope
) {
    val decodedUrl = remember { Uri.decode(imageUrl) }
    val scope = rememberCoroutineScope()
    
    // State for gesture handling
    var offsetY by remember { mutableFloatStateOf(0f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }
    var shouldDismiss by remember { mutableStateOf(false) }
    
    // Animated values
    val animatedOffsetY = remember { Animatable(0f) }
    val animatedOffsetX = remember { Animatable(0f) }
    val animatedAlpha = remember { Animatable(1f) }
    val animatedScale = remember { Animatable(1f) }
    
    // Background alpha based on drag distance
    val dragThreshold = 300f
    val backgroundAlpha = remember(offsetY, offsetX) {
        val dragDistance = kotlin.math.sqrt(offsetY * offsetY + offsetX * offsetX)
        (1f - (dragDistance / dragThreshold).coerceIn(0f, 1f))
    }
    
    // Handle dismiss navigation
    LaunchedEffect(shouldDismiss) {
        if (shouldDismiss) {
            navController.popBackStack()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = backgroundAlpha))
    ) {
        // Top app bar with back button
        TopAppBar(
            title = { },
            navigationIcon = {
                IconButton(
                    onClick = { navController.popBackStack() }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            ),
            modifier = Modifier.alpha(backgroundAlpha)
        )
        
        // Image with shared transition and gesture handling
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = {
                            isDragging = true
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            offsetY += dragAmount.y
                            offsetX += dragAmount.x
                            
                            // Update animated values during drag
                            scope.launch {
                                animatedOffsetY.snapTo(offsetY)
                                animatedOffsetX.snapTo(offsetX)
                                
                                // Calculate alpha and scale based on drag distance
                                val dragDistance = kotlin.math.sqrt(offsetY * offsetY + offsetX * offsetX)
                                val progress = (dragDistance / dragThreshold).coerceIn(0f, 1f)
                                animatedAlpha.snapTo(1f - progress * 0.5f)
                                animatedScale.snapTo(1f - progress * 0.3f)
                            }
                        },
                        onDragEnd = {
                            isDragging = false
                            val dragDistance = kotlin.math.sqrt(offsetY * offsetY + offsetX * offsetX)
                            
                            scope.launch {
                                if (dragDistance > dragThreshold) {
                                    // Dismiss - animate to the drag direction and pop back
                                    launch {
                                        animatedOffsetY.animateTo(
                                            targetValue = offsetY * 2f,
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                stiffness = Spring.StiffnessLow
                                            )
                                        )
                                    }
                                    launch {
                                        animatedOffsetX.animateTo(
                                            targetValue = offsetX * 2f,
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                stiffness = Spring.StiffnessLow
                                            )
                                        )
                                    }
                                    launch {
                                        animatedAlpha.animateTo(
                                            targetValue = 0f,
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                stiffness = Spring.StiffnessLow
                                            )
                                        )
                                    }
                                    launch {
                                        animatedScale.animateTo(
                                            targetValue = 0.5f,
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                stiffness = Spring.StiffnessLow
                                            )
                                        )
                                    }
                                    
                                    // Delay before dismissing to show animation
                                    kotlinx.coroutines.delay(300)
                                    shouldDismiss = true
                                } else {
                                    // Snap back to original position
                                    launch {
                                        animatedOffsetY.animateTo(
                                            targetValue = 0f,
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                stiffness = Spring.StiffnessMedium
                                            )
                                        )
                                    }
                                    launch {
                                        animatedOffsetX.animateTo(
                                            targetValue = 0f,
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                stiffness = Spring.StiffnessMedium
                                            )
                                        )
                                    }
                                    launch {
                                        animatedAlpha.animateTo(
                                            targetValue = 1f,
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                stiffness = Spring.StiffnessMedium
                                            )
                                        )
                                    }
                                    launch {
                                        animatedScale.animateTo(
                                            targetValue = 1f,
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                stiffness = Spring.StiffnessMedium
                                            )
                                        )
                                    }
                                    
                                    offsetY = 0f
                                    offsetX = 0f
                                }
                            }
                        },
                        onDragCancel = {
                            isDragging = false
                            scope.launch {
                                launch { animatedOffsetY.animateTo(0f) }
                                launch { animatedOffsetX.animateTo(0f) }
                                launch { animatedAlpha.animateTo(1f) }
                                launch { animatedScale.animateTo(1f) }
                                offsetY = 0f
                                offsetX = 0f
                            }
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            with(sharedTransitionScope) {
                Image(
                    painter = rememberAsyncImagePainter(decodedUrl),
                    contentDescription = "Detailed image",
                    modifier = Modifier
                        .fillMaxSize()
                        .offset {
                            IntOffset(
                                animatedOffsetX.value.roundToInt(),
                                animatedOffsetY.value.roundToInt()
                            )
                        }
                        .graphicsLayer {
                            alpha = animatedAlpha.value
                            scaleX = animatedScale.value
                            scaleY = animatedScale.value
                        }
                        .sharedElement(
                            sharedContentState = rememberSharedContentState(key = "image-$itemId"),
                            animatedVisibilityScope = animatedContentScope
                        ),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}

