package com.nvd.demo_list.ui.component

import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.zIndex
import nl.birdly.zoombox.MutableZoomState
import nl.birdly.zoombox.ZoomState
import kotlin.math.abs

@Composable
fun CropOverlay(
    context: Context,
    imageUrl: String,
    modifier: Modifier = Modifier,
    zoomState: MutableZoomState? = null,
    isVisible: Boolean = true,
    onCancel: () -> Unit = {},
    onCrop: (Rect) -> Unit = {}
) {
    var cropRect by remember { mutableStateOf(Rect.Zero) }
    var isDragging by remember { mutableStateOf(false) }
    var dragHandle by remember { mutableIntStateOf(-1) }
    var isInitialized by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .zIndex(5f)
    ) {
        // Canvas overlay with drag gestures
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { centroid, pan, zoom, rotation ->
                        if (zoomState != null) {
                            val current = zoomState.value
                            val newScale = (current.scale * zoom).coerceIn(1f, 5f)
                            val panAdjusted = pan
                            val newOffset = if (zoom != 1f) {
                                val scaleDiff = newScale / current.scale
                                ((current.offset + centroid - panAdjusted) * scaleDiff - centroid)
                            } else {
                                current.offset - panAdjusted
                            }

                            if (newScale > 1f) {
                                zoomState.value = ZoomState(
                                    scale = newScale,
                                    offset = newOffset
                                )
                            } else {
                                zoomState.value = ZoomState()
                            }

                        }
                    }
                }
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)

                        // Kiểm tra ngay khi chạm xuống xem có chạm handle không
                        dragHandle = getHandleAtPosition(down.position, cropRect)
                        isDragging = dragHandle >= 0
                        if (!isDragging) return@awaitEachGesture // Nếu không chạm handle, kết thúc gesture luôn

                        do {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull() ?: break
                            val pointerCount = event.changes.count { it.pressed }

                            if (pointerCount == 1 && isDragging && dragHandle >= 0) {
                                val dragAmount = change.positionChange()
                                val imageRect = Rect(
                                    left = 0f,
                                    top = 0f,
                                    right = size.width.toFloat(),
                                    bottom = size.height.toFloat()
                                )
                                cropRect = updateCropRect(
                                    cropRect,
                                    dragHandle,
                                    dragAmount,
                                    size.toSize(),
                                    imageRect
                                )
                                change.consume()
                            }

                            // Reset khi thả tay
                            if (event.changes.none { it.pressed }) {
                                isDragging = false
                                dragHandle = -1
                            }

                        } while (event.changes.any { it.pressed })
                    }
                    /*detectDragGestures(
                        onDragStart = { offset ->
                            dragHandle = getHandleAtPosition(offset, cropRect)
                            isDragging = dragHandle >= 0
                        },
                        onDragEnd = {
                            isDragging = false
                            dragHandle = -1
                        },
                        onDrag = { _, dragAmount ->
                            if (isDragging && dragHandle >= 0) {
                                val imageRect = Rect(
                                    left = 0f,
                                    top = 0f,
                                    right = size.width.toFloat(),
                                    bottom = size.height.toFloat()
                                )
                                cropRect = updateCropRect(
                                    cropRect,
                                    dragHandle,
                                    dragAmount,
                                    size.toSize(),
                                    imageRect
                                )
                            }
                        }
                    )*/
                }
        ) {
            val screenWidth = size.width
            val screenHeight = size.height

            // Initialize crop rect only once
            if (!isInitialized && screenWidth > 0 && screenHeight > 0) {
                val margin = 55.dp.toPx()
                val centerX = screenWidth / 2
                val centerY = screenHeight / 2
                val rectSize = minOf(screenWidth, screenHeight) - 2 * margin

                cropRect = Rect(
                    left = centerX - rectSize / 2,
                    top = centerY - rectSize / 2,
                    right = centerX + rectSize / 2,
                    bottom = centerY + rectSize / 2
                )
                isInitialized = true
            }

            if (isInitialized && cropRect != Rect.Zero && isVisible) {
                // Draw black overlay on top (above crop area)
                if (cropRect.top > 0) {
                    drawRect(
                        color = Color.Black.copy(alpha = 0.7f),
                        topLeft = Offset(0f, 0f),
                        size = Size(screenWidth, cropRect.top)
                    )
                }

                // Draw black overlay on bottom (below crop area)
                if (cropRect.bottom < screenHeight) {
                    drawRect(
                        color = Color.Black.copy(alpha = 0.7f),
                        topLeft = Offset(0f, cropRect.bottom),
                        size = Size(screenWidth, screenHeight - cropRect.bottom)
                    )
                }

                // Draw black overlay on left (beside crop area)
                if (cropRect.left > 0) {
                    drawRect(
                        color = Color.Black.copy(alpha = 0.7f),
                        topLeft = Offset(0f, cropRect.top),
                        size = Size(cropRect.left, cropRect.height)
                    )
                }

                // Draw black overlay on right (beside crop area)
                if (cropRect.right < screenWidth) {
                    drawRect(
                        color = Color.Black.copy(alpha = 0.7f),
                        topLeft = Offset(cropRect.right, cropRect.top),
                        size = Size(screenWidth - cropRect.right, cropRect.height)
                    )
                }

                // Draw crop frame
                drawCropFrame(cropRect)

                // Draw resize handles
                drawResizeHandles(cropRect)
            }
        }

        // Buttons row - positioned at bottom
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .align(Alignment.BottomCenter)
                .background(Color.White),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = onCancel,
                modifier = Modifier.padding(horizontal = 32.dp)
            ) {
                Text(
                    text = "Cancel",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            TextButton(
                onClick = { onCrop(cropRect) },
                modifier = Modifier.padding(horizontal = 32.dp)
            ) {
                Text(
                    text = "Crop",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1877F2)
                )
            }
        }
    }
}

private fun DrawScope.drawCropFrame(cropRect: Rect) {
    val strokeWidth = 2.dp.toPx()

    // Draw complete frame
    drawRect(
        color = Color.White,
        topLeft = Offset(cropRect.left, cropRect.top),
        size = Size(cropRect.width, cropRect.height),
        style = Stroke(width = strokeWidth)
    )
}

private fun DrawScope.drawResizeHandles(cropRect: Rect) {
    val handleSize = 15.dp.toPx()
    val handleColor = Color.White
    val handleStroke = 2.dp.toPx()

    val centerX = cropRect.left + cropRect.width / 2
    val centerY = cropRect.top + cropRect.height / 2

    val handles = listOf(
        // Corner handles
        Offset(cropRect.left, cropRect.top),      // 0: top-left
        Offset(cropRect.right, cropRect.top),     // 1: top-right
        Offset(cropRect.left, cropRect.bottom),   // 2: bottom-left
        Offset(cropRect.right, cropRect.bottom),  // 3: bottom-right
        // Edge handles
        Offset(centerX, cropRect.top),            // 4: top-center
        Offset(centerX, cropRect.bottom),         // 5: bottom-center
        Offset(cropRect.left, centerY),           // 6: left-center
        Offset(cropRect.right, centerY)           // 7: right-center
    )

    handles.forEach { handle ->
        // Draw white border
        drawRect(
            color = handleColor,
            topLeft = Offset(
                handle.x - handleSize / 2,
                handle.y - handleSize / 2
            ),
            size = Size(handleSize, handleSize),
            style = Stroke(width = handleStroke)
        )

        // Draw black semi-transparent background
        drawRect(
            color = Color.Black.copy(alpha = 0.6f),
            topLeft = Offset(
                handle.x - handleSize / 2 + handleStroke,
                handle.y - handleSize / 2 + handleStroke
            ),
            size = Size(handleSize - 2 * handleStroke, handleSize - 2 * handleStroke)
        )

        // Draw white dot in center
        drawCircle(
            color = Color.White,
            radius = 3.dp.toPx(),
            center = handle
        )
    }
}

private fun getHandleAtPosition(position: Offset, cropRect: Rect): Int {
    val touchThreshold = 60f

    val centerX = cropRect.left + cropRect.width / 2
    val centerY = cropRect.top + cropRect.height / 2

    val handles = listOf(
        Offset(cropRect.left, cropRect.top),      // 0: top-left
        Offset(cropRect.right, cropRect.top),     // 1: top-right
        Offset(cropRect.left, cropRect.bottom),   // 2: bottom-left
        Offset(cropRect.right, cropRect.bottom),  // 3: bottom-right
        Offset(centerX, cropRect.top),            // 4: top-center
        Offset(centerX, cropRect.bottom),         // 5: bottom-center
        Offset(cropRect.left, centerY),           // 6: left-center
        Offset(cropRect.right, centerY)           // 7: right-center
    )

    // Check handles with larger touch area
    handles.forEachIndexed { index, handle ->
        if (abs(position.x - handle.x) <= touchThreshold && abs(position.y - handle.y) <= touchThreshold) {
            return index
        }
    }

    val edgeThreshold = 20f

    // Check if touching edges
    if (position.x >= cropRect.left - touchThreshold && position.x <= cropRect.right + touchThreshold &&
        position.y >= cropRect.top - touchThreshold && position.y <= cropRect.bottom + touchThreshold
    ) {
        // Touching near top/bottom edge
        if ((abs(position.y - cropRect.top) <= edgeThreshold || abs(position.y - cropRect.bottom) <= edgeThreshold) &&
            position.x >= cropRect.left - edgeThreshold && position.x <= cropRect.right + edgeThreshold
        ) {
            return if (abs(position.y - cropRect.top) <= edgeThreshold) 4 else 5
        }

        // Touching near left/right edge
        if ((abs(position.x - cropRect.left) <= edgeThreshold || abs(position.x - cropRect.right) <= edgeThreshold) &&
            position.y >= cropRect.top - edgeThreshold && position.y <= cropRect.bottom + edgeThreshold
        ) {
            return if (abs(position.x - cropRect.left) <= edgeThreshold) 6 else 7
        }

        // Touching center to move entire rect
        if (position.x >= cropRect.left + edgeThreshold && position.x <= cropRect.right - edgeThreshold &&
            position.y >= cropRect.top + edgeThreshold && position.y <= cropRect.bottom - edgeThreshold
        ) {
            return 8 // center drag
        }
    }

    return -1
}

private fun updateCropRect(
    currentRect: Rect,
    handleIndex: Int,
    dragAmount: Offset,
    canvasSize: Size,
    imageRect: Rect
): Rect {
    val minSize = 50f
    var newRect = currentRect

    when (handleIndex) {
        0 -> { // top-left
            val newLeft = (currentRect.left + dragAmount.x).coerceIn(
                imageRect.left,
                currentRect.right - minSize
            )
            val newTop = (currentRect.top + dragAmount.y).coerceIn(
                imageRect.top,
                currentRect.bottom - minSize
            )
            newRect = currentRect.copy(left = newLeft, top = newTop)
        }

        1 -> { // top-right
            val newRight = (currentRect.right + dragAmount.x).coerceIn(
                currentRect.left + minSize,
                imageRect.right
            )
            val newTop = (currentRect.top + dragAmount.y).coerceIn(
                imageRect.top,
                currentRect.bottom - minSize
            )
            newRect = currentRect.copy(right = newRight, top = newTop)
        }

        2 -> { // bottom-left
            val newLeft = (currentRect.left + dragAmount.x).coerceIn(
                imageRect.left,
                currentRect.right - minSize
            )
            val newBottom = (currentRect.bottom + dragAmount.y).coerceIn(
                currentRect.top + minSize,
                imageRect.bottom
            )
            newRect = currentRect.copy(left = newLeft, bottom = newBottom)
        }

        3 -> { // bottom-right
            val newRight = (currentRect.right + dragAmount.x).coerceIn(
                currentRect.left + minSize,
                imageRect.right
            )
            val newBottom = (currentRect.bottom + dragAmount.y).coerceIn(
                currentRect.top + minSize,
                imageRect.bottom
            )
            newRect = currentRect.copy(right = newRight, bottom = newBottom)
        }

        4 -> { // top-center
            val newTop = (currentRect.top + dragAmount.y).coerceIn(
                imageRect.top,
                currentRect.bottom - minSize
            )
            newRect = currentRect.copy(top = newTop)
        }

        5 -> { // bottom-center
            val newBottom = (currentRect.bottom + dragAmount.y).coerceIn(
                currentRect.top + minSize,
                imageRect.bottom
            )
            newRect = currentRect.copy(bottom = newBottom)
        }

        6 -> { // left-center
            val newLeft = (currentRect.left + dragAmount.x).coerceIn(
                imageRect.left,
                currentRect.right - minSize
            )
            newRect = currentRect.copy(left = newLeft)
        }

        7 -> { // right-center
            val newRight = (currentRect.right + dragAmount.x).coerceIn(
                currentRect.left + minSize,
                imageRect.right
            )
            newRect = currentRect.copy(right = newRight)
        }

        8 -> { // center drag (move entire rect)
            val deltaX = dragAmount.x
            val deltaY = dragAmount.y
            val newLeft = (currentRect.left + deltaX).coerceIn(
                imageRect.left,
                imageRect.right - currentRect.width
            )
            val newTop = (currentRect.top + deltaY).coerceIn(
                imageRect.top,
                imageRect.bottom - currentRect.height
            )
            newRect = Rect(
                left = newLeft,
                top = newTop,
                right = newLeft + currentRect.width,
                bottom = newTop + currentRect.height
            )
        }
    }

    return newRect
}