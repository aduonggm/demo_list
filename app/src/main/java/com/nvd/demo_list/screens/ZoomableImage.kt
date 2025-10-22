package com.nvd.demo_list.screens

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.nvd.demo_list.R
import nl.birdly.zoombox.ZoomState
import nl.birdly.zoombox.gesture.condition.TouchCondition
import nl.birdly.zoombox.gesture.transform.TransformGestureHandler
import nl.birdly.zoombox.rememberMutableZoomState
import nl.birdly.zoombox.zoomable

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ZoomableImage(
    imageUrl: String,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    onClick: () -> Unit
) {

    val zoomState = rememberMutableZoomState()
   with(sharedTransitionScope){
       AsyncImage(
           imageUrl,
           contentDescription = "Page ${0 + 1}",
           contentScale = ContentScale.FillWidth,
           modifier = Modifier
               .fillMaxWidth()
               .sharedElement(
                   sharedContentState = sharedTransitionScope.rememberSharedContentState(key = imageUrl),
                   animatedVisibilityScope = animatedContentScope
               )
               .clickable(onClick = onClick)
               .aspectRatio(1f)
               .padding(8.dp)
       )
   }

//    PdfViewer(
//
//        rawResId = R.raw.test,
//        modifier = Modifier
//            .zoomable(
//                zoomState = zoomState,
//                zoomRange = 1f..4f,
//                transformGestureHandler = TransformGestureHandler(
//
//                    onCondition = object : TouchCondition {
//                        override fun invoke(
//                            zoomStateProvider: () -> ZoomState,
//                            pointerInputScope: PointerInputScope,
//                            pointerEvent: PointerEvent
//                        ): Boolean {
//                            return pointerEvent.changes.size > 1 || zoomStateProvider.invoke().scale > 1f
//                        }
//
//                    }
//                )
//
//            )
//
//    )


}
