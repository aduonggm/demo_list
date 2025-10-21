package com.nvd.demo_list.screens

import android.util.Log
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeGesturesPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.nvd.demo_list.models.NewsFeedData
import nl.birdly.zoombox.ZoomState
import nl.birdly.zoombox.gesture.condition.TouchCondition
import nl.birdly.zoombox.gesture.transform.TransformGestureHandler
import nl.birdly.zoombox.rememberMutableZoomState
import nl.birdly.zoombox.zoomable

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun DetailsScreen(
    index: Int,

    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    onBackPressed: () -> Unit
) {
    val newsFeedItems = remember { NewsFeedData.getSampleData() }

    val zoomState = rememberMutableZoomState()
    val image by remember(newsFeedItems) { mutableStateOf(newsFeedItems[index].postImage ?: "") }
    Log.d("=======>>>>>>> ", "DetailsScreen:  $image")
    println("image detail is  $image")

    with(sharedTransitionScope) {
        Box(
            Modifier
                .fillMaxSize()
                .background(color = Color.Black)
                .clickable {
//                    zoomState.value = ZoomState()
                    onBackPressed()
                },
            contentAlignment = Alignment.Center
        ) {


            AsyncImage(
                model = image,
                contentDescription = "Zoomed Image",
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .fillMaxWidth()
                    .sharedElement(
                        sharedTransitionScope.rememberSharedContentState(key = image),
                        animatedVisibilityScope = animatedContentScope
                    )
                    .zoomable(

                        zoomState = zoomState,
                        zoomRange = 1f..4f,
                        transformGestureHandler = TransformGestureHandler(

                            onCondition = object : TouchCondition {
                                override fun invoke(
                                    zoomStateProvider: () -> ZoomState,
                                    pointerInputScope: PointerInputScope,
                                    pointerEvent: PointerEvent
                                ): Boolean {
                                    return pointerEvent.changes.size > 1 || zoomStateProvider.invoke().scale > 1f
                                }

                            }
                        )

                    )
                    .background(color = Color.Red)
            )


            IconButton(
                onBackPressed,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .safeGesturesPadding()
                    .padding(16.dp)
                    .background(color = Color.White.copy(alpha = 0.2f), shape = CircleShape)

            ) {
                Icon(
                    Icons.Default.Clear, contentDescription = null,
                    tint = Color.White
                )
            }

        }
    }
}