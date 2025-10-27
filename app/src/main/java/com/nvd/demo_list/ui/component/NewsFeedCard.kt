package com.nvd.demo_list.ui.component

import android.util.Log
import android.view.MotionEvent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.unit.dp
import com.nvd.demo_list.R
import com.nvd.demo_list.models.NewsFeedItem
import com.nvd.demo_list.screens.ActionButtons
import com.nvd.demo_list.screens.ItemSelected
import com.nvd.demo_list.screens.PostContent
import com.nvd.demo_list.screens.PostHeader
import com.nvd.demo_list.screens.ReactionsAndStats
import nl.birdly.zoombox.MutableZoomState
import nl.birdly.zoombox.ZoomState
import nl.birdly.zoombox.gesture.condition.TouchCondition
import nl.birdly.zoombox.gesture.transform.TransformGestureHandler
import nl.birdly.zoombox.rememberMutableZoomState
import nl.birdly.zoombox.zoomable
import java.io.File


const val LONG_PRESS_TIME = 500L

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun NewsFeedPdfCardOld(
    item: NewsFeedItem,
    index: Int,
    onClick: (ItemSelected) -> Unit,
    isVisible: Boolean = true,
    onLongClick: () -> Unit = {},
    onZoomChange: (Boolean) -> Unit = {},
    onResetZoom: (() -> Unit) -> Unit = {},
    onSourceFileReady: (File?) -> Unit = {},
    onZoomStateUpdate: (ZoomState) -> Unit = {},
    onPdfViewerSizeChanged: (width: Int, height: Int, offsetY: Int) -> Unit = { _, _, _ -> }
) {
    val zoomState = rememberMutableZoomState()
    val isZooming = zoomState.value.scale > 1f

    // Notify parent about zoom state
    onZoomChange(isZooming)

    // Notify parent about zoom state changes for cropping
    onZoomStateUpdate(zoomState.value)

    // Expose reset function to parent
    onResetZoom {
        zoomState.value = ZoomState()
    }

    // Post Header
    PostHeader(item)

    // Post Content
    PostContent(item)

    // Track long press state
    var downTime by remember { mutableLongStateOf(0L) }
    var isLongPressTriggered by remember { mutableStateOf(false) }
    var isSingleTouch by remember { mutableStateOf(true) }

    PdfViewer(
        modifier = Modifier
            .fillMaxWidth()
            .onGloballyPositioned { coordinates ->
                val position = coordinates.positionInWindow()
                val size = coordinates.size
                onPdfViewerSizeChanged(size.width, size.height, position.y.toInt())
            }
            .zoomable(
                zoomState = zoomState,
                zoomRange = 1f..3f,
                transformGestureHandler = TransformGestureHandler(
                    onCondition = object : TouchCondition {
                        override fun invoke(
                            zoomStateProvider: () -> ZoomState,
                            pointerInputScope: PointerInputScope,
                            pointerEvent: PointerEvent
                        ): Boolean {
                            val motionEvent = pointerEvent.motionEvent
                            val pointerCount = motionEvent?.pointerCount ?: 0
                            val composeTouchCount = pointerEvent.changes.size

                            // Detect if this is multi-touch (zoom gesture)
                            val isMultiTouch = pointerCount > 1 || composeTouchCount > 1

                            when (motionEvent?.action) {
                                MotionEvent.ACTION_DOWN -> {
                                    // Record down time only for single touch
                                    if (!isMultiTouch) {
                                        downTime = System.currentTimeMillis()
                                        isLongPressTriggered = false
                                        isSingleTouch = true
                                        Log.d(
                                            "LongPress",
                                            "ACTION_DOWN (single touch) at $downTime"
                                        )
                                    } else {
                                        // Multi-touch detected, reset long press state
                                        downTime = 0L
                                        isSingleTouch = false
                                        Log.d(
                                            "LongPress",
                                            "ACTION_DOWN (multi-touch) - long press disabled"
                                        )
                                    }
                                }

                                MotionEvent.ACTION_POINTER_DOWN -> {
                                    // Second finger down - this is zoom gesture, cancel long press
                                    downTime = 0L
                                    isLongPressTriggered = false
                                    isSingleTouch = false
                                    Log.d(
                                        "LongPress",
                                        "ACTION_POINTER_DOWN - multi-touch detected, long press cancelled"
                                    )
                                }

                                MotionEvent.ACTION_UP -> {
                                    // Calculate press duration when finger lifts
                                    val upTime = System.currentTimeMillis()
                                    val pressDuration = upTime - downTime

                                    Log.d(
                                        "LongPress",
                                        "ACTION_UP at $upTime, duration: ${pressDuration}ms, isSingleTouch: $isSingleTouch"
                                    )

                                    // Check if press duration >= 2 seconds AND it was single touch throughout
                                    if (pressDuration >= LONG_PRESS_TIME && !isLongPressTriggered && isSingleTouch && !isMultiTouch) {
                                        isLongPressTriggered = true
                                        Log.d(
                                            "LongPress",
                                            "Long press detected! Enabling crop overlay"
                                        )
                                        onLongClick()
                                    }

                                    // Reset down time
                                    downTime = 0L
                                    isSingleTouch = true
                                }

                                MotionEvent.ACTION_MOVE -> {
                                    // If multi-touch detected during move, cancel long press
                                    if (isMultiTouch) {
                                        downTime = 0L
                                        isLongPressTriggered = false
                                        isSingleTouch = false
                                        Log.d(
                                            "LongPress",
                                            "ACTION_MOVE with multi-touch - long press cancelled"
                                        )
                                    } else if (downTime > 0 && !isLongPressTriggered && isSingleTouch) {
                                        // Check if user has been holding for 2 seconds with single touch
                                        val currentTime = System.currentTimeMillis()
                                        val pressDuration = currentTime - downTime

                                        if (pressDuration >= LONG_PRESS_TIME) {
                                            isLongPressTriggered = true
                                            Log.d(
                                                "LongPress",
                                                "Long press detected during single-touch move! Enabling crop overlay"
                                            )
                                            onLongClick()
                                        }
                                    }
                                }

                                MotionEvent.ACTION_CANCEL -> {
                                    // Reset if touch is cancelled
                                    downTime = 0L
                                    isLongPressTriggered = false
                                    Log.d("LongPress", "ACTION_CANCEL - reset")
                                }
                            }

                            // Original condition for zoom
                            return pointerEvent.changes.size > 1 || zoomStateProvider().scale > 1f
                        }
                    }
                )
            ),
        rawResId = if (index % 2 == 0) R.raw.test_1 else R.raw.test_2,
        onImageFileReady = onSourceFileReady
    )


    // Reactions and Stats
    ReactionsAndStats(item)

    HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))

    // Action Buttons
    ActionButtons(item)
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun NewsFeedPdfCardNew(
    item: NewsFeedItem,
    onLongClick: () -> Unit = {},
    onZoomChange: (Boolean) -> Unit = {},
    onResetZoom: (() -> Unit) -> Unit = {},
    onZoomStateUpdate: (MutableZoomState) -> Unit = {},
) {
    val zoomState = rememberMutableZoomState()
    val isZooming = zoomState.value.scale > 1f

    // Notify parent about zoom state
    onZoomChange(isZooming)

    // Notify parent about zoom state changes for cropping
    onZoomStateUpdate(zoomState)

    // Expose reset function to parent
    onResetZoom {
        zoomState.value = ZoomState()
    }

    // Post Header
    PostHeader(item)

    // Post Content
    PostContent(item)

    // Track long press state
    var downTime by remember { mutableLongStateOf(0L) }
    var isLongPressTriggered by remember { mutableStateOf(false) }
    var isSingleTouch by remember { mutableStateOf(true) }

    PdfViewerNew(
        modifier = Modifier
            .fillMaxWidth()
            .zoomable(
                zoomState = zoomState,
                zoomRange = 1f..3f,
                transformGestureHandler = TransformGestureHandler(
                    onCondition = object : TouchCondition {
                        override fun invoke(
                            zoomStateProvider: () -> ZoomState,
                            pointerInputScope: PointerInputScope,
                            pointerEvent: PointerEvent
                        ): Boolean {
                            val motionEvent = pointerEvent.motionEvent
                            val pointerCount = motionEvent?.pointerCount ?: 0
                            val composeTouchCount = pointerEvent.changes.size

                            // Detect if this is multi-touch (zoom gesture)
                            val isMultiTouch = pointerCount > 1 || composeTouchCount > 1

                            when (motionEvent?.action) {
                                MotionEvent.ACTION_DOWN -> {
                                    // Record down time only for single touch
                                    if (!isMultiTouch) {
                                        downTime = System.currentTimeMillis()
                                        isLongPressTriggered = false
                                        isSingleTouch = true
                                        Log.d(
                                            "LongPress",
                                            "ACTION_DOWN (single touch) at $downTime"
                                        )
                                    } else {
                                        // Multi-touch detected, reset long press state
                                        downTime = 0L
                                        isSingleTouch = false
                                        Log.d(
                                            "LongPress",
                                            "ACTION_DOWN (multi-touch) - long press disabled"
                                        )
                                    }
                                }

                                MotionEvent.ACTION_POINTER_DOWN -> {
                                    // Second finger down - this is zoom gesture, cancel long press
                                    downTime = 0L
                                    isLongPressTriggered = false
                                    isSingleTouch = false
                                    Log.d(
                                        "LongPress",
                                        "ACTION_POINTER_DOWN - multi-touch detected, long press cancelled"
                                    )
                                }

                                MotionEvent.ACTION_UP -> {
                                    // Calculate press duration when finger lifts
                                    val upTime = System.currentTimeMillis()
                                    val pressDuration = upTime - downTime

                                    Log.d(
                                        "LongPress",
                                        "ACTION_UP at $upTime, duration: ${pressDuration}ms, isSingleTouch: $isSingleTouch"
                                    )

                                    // Check if press duration >= 2 seconds AND it was single touch throughout
                                    if (pressDuration >= LONG_PRESS_TIME && !isLongPressTriggered && isSingleTouch && !isMultiTouch) {
                                        isLongPressTriggered = true
                                        Log.d(
                                            "LongPress",
                                            "Long press detected! Enabling crop overlay"
                                        )
                                        onLongClick()
                                    }

                                    // Reset down time
                                    downTime = 0L
                                    isSingleTouch = true
                                }

                                MotionEvent.ACTION_MOVE -> {
                                    // If multi-touch detected during move, cancel long press
                                    if (isMultiTouch) {
                                        downTime = 0L
                                        isLongPressTriggered = false
                                        isSingleTouch = false
                                        Log.d(
                                            "LongPress",
                                            "ACTION_MOVE with multi-touch - long press cancelled"
                                        )
                                    } else if (downTime > 0 && !isLongPressTriggered && isSingleTouch) {
                                        // Check if user has been holding for 2 seconds with single touch
                                        val currentTime = System.currentTimeMillis()
                                        val pressDuration = currentTime - downTime

                                        if (pressDuration >= LONG_PRESS_TIME) {
                                            isLongPressTriggered = true
                                            Log.d(
                                                "LongPress",
                                                "Long press detected during single-touch move! Enabling crop overlay"
                                            )
                                            onLongClick()
                                        }
                                    }
                                }

                                MotionEvent.ACTION_CANCEL -> {
                                    // Reset if touch is cancelled
                                    downTime = 0L
                                    isLongPressTriggered = false
                                    Log.d("LongPress", "ACTION_CANCEL - reset")
                                }
                            }

                            // Original condition for zoom
                            return pointerEvent.changes.size > 1 || zoomStateProvider().scale > 1f
                        }
                    }
                )
            ),
        pdfUrl = item.postImage,
    )


    // Reactions and Stats
    ReactionsAndStats(item)

    HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))

    // Action Buttons
    ActionButtons(item)
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun NewsFeedImageCard(
    item: NewsFeedItem,
    onLongClick: () -> Unit = {},
    onZoomChange: (Boolean) -> Unit = {},
    onResetZoom: (() -> Unit) -> Unit = {},
    onZoomStateUpdate: (ZoomState) -> Unit = {},
    onPdfViewerSizeChanged: (width: Int, height: Int, offsetY: Int) -> Unit = { _, _, _ -> }
) {
    val zoomState = rememberMutableZoomState()
    val isZooming = zoomState.value.scale > 1f

    // Notify parent about zoom state
    onZoomChange(isZooming)

    // Notify parent about zoom state changes for cropping
    onZoomStateUpdate(zoomState.value)

    // Expose reset function to parent
    onResetZoom {
        zoomState.value = ZoomState()
    }

    // Post Header
    PostHeader(item)

    // Post Content
    PostContent(item)

    item.postImage?.let {
        ZoomableImage(
            imageUrl = it,
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coordinates ->
                    val position = coordinates.positionInWindow()
                    val size = coordinates.size
                    onPdfViewerSizeChanged(size.width, size.height, position.y.toInt())
                },
            onLongClick = onLongClick,
            onZoomChange = onZoomChange,
            onResetZoom = onResetZoom
        )
    }

    // Reactions and Stats
    ReactionsAndStats(item)

    HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))

    // Action Buttons
    ActionButtons(item)
}

