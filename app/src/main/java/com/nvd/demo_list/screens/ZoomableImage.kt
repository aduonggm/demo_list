package com.nvd.demo_list.screens

import android.util.Log
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
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
    onClick: (ItemSelected) -> Unit,
    isVisible: Boolean = true

) {
    var itemOffset by remember { mutableStateOf(IntOffset.Zero) }

    val zoomState = rememberMutableZoomState()
    val density = LocalDensity.current
    val statusBarHeight = WindowInsets.statusBars.getTop(density)
    var heightItem = remember { 0 }

    AsyncImage(
        imageUrl,
        contentDescription = "Page ${0 + 1}",
        contentScale = ContentScale.FillWidth,
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (isVisible) 1f else 0f)
            .onGloballyPositioned { layoutCoordinates ->
                val windowPos = layoutCoordinates.localToRoot(Offset.Zero).round()
                heightItem = layoutCoordinates.size.height // px
                itemOffset = IntOffset(windowPos.x, windowPos.y)
            }
            .clickable {

                Log.d(
                    "ItemClick",
                    "  ${statusBarHeight} Item click tại: $itemOffset  $heightItem"
                )
                // bạn có thể gọi callback truyền ra ngoài ở đây
                onClick(ItemSelected(imageUrl, itemOffset, heightItem))
            }
    )


}
