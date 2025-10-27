package com.nvd.demo_list.screens.transform

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.zIndex

@Composable
fun TransformableBox(
    controller: TransformController,
    modifier: Modifier = Modifier,
    content: @Composable (Modifier) -> Unit
) {
    content(
        modifier
            .graphicsLayer {
                scaleX = controller.scale
                scaleY = controller.scale
                translationX = controller.offset.x
                translationY = controller.offset.y
            }
            .zIndex(if (controller.isZooming) 3f else 0f)
            .onGloballyPositioned {
                controller.bounds = it.boundsInRoot()
   }
    )

}
