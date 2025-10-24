package com.nvd.demo_list.screens.transform

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import kotlin.math.PI
import kotlin.math.abs

class TransformController {
    var scale by mutableStateOf(1f)
        private set
    var offset by mutableStateOf(Offset.Zero)
        private set
    var rotation by mutableStateOf(0f)
        private set

    var bounds by mutableStateOf<Rect?>(null)
    private val minScale = 1f
    private val maxScale = 3f

    fun onTransform(zoomChange: Float, panChange: Offset, rotationChange: Float) {
        scale = (scale * zoomChange).coerceIn(minScale, maxScale)
        if (scale > 1f) {
            offset += panChange
            rotation += rotationChange
        }
    }

    fun touchUp() {
        if (scale <= 1f) {
            reset()
        }
    }

    fun reset() {
        scale = 1f
        offset = Offset.Zero
        rotation = 0f
    }

    val isZooming get() = scale > 1f
}
