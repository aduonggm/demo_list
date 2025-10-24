package com.nvd.demo_list.ui.component

import android.graphics.BitmapFactory
import android.util.Log
import android.view.MotionEvent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.nvd.demo_list.screens.ItemSelected
import com.nvd.demo_list.utils.ImageCacheManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.birdly.zoombox.ZoomState
import nl.birdly.zoombox.gesture.condition.TouchCondition
import nl.birdly.zoombox.gesture.transform.TransformGestureHandler
import nl.birdly.zoombox.rememberMutableZoomState
import nl.birdly.zoombox.zoomable
import java.io.File
import java.net.URL

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

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ZoomableImage(
    imageUrl: String,
    modifier: Modifier,
    onLongClick: () -> Unit = {},
    onZoomChange: (Boolean) -> Unit = {},
    onResetZoom: (() -> Unit) -> Unit = {}
) {
    // Khởi tạo bitmap từ cache ngay trong remember để tránh flash loading khi scroll
    var bitmap by remember(imageUrl) {
        mutableStateOf(ImageCacheManager.getBitmap(imageUrl))
    }

    // Loading chỉ true nếu chưa có trong cache
    var loading by remember(imageUrl) {
        mutableStateOf(ImageCacheManager.getBitmap(imageUrl) == null)
    }

    val zoomState = rememberMutableZoomState()
    val isZooming = zoomState.value.scale > 1f

    // Track long press state
    var downTime by remember { mutableLongStateOf(0L) }
    var isLongPressTriggered by remember { mutableStateOf(false) }
    var isSingleTouch by remember { mutableStateOf(true) }

    // Notify parent about zoom state
    onZoomChange(isZooming)

    // Expose reset function to parent
    onResetZoom {
        zoomState.value = ZoomState()
    }

    // Load bitmap directly to maintain quality when zooming (similar to PdfViewer)
    // Sử dụng cache để tránh load lại khi scroll
    LaunchedEffect(imageUrl) {
        Log.d("ZoomableImage", "🔄 LaunchedEffect started for: $imageUrl")

        // Kiểm tra cache trước
        val cachedBitmap = ImageCacheManager.getBitmap(imageUrl)

        if (cachedBitmap != null && !cachedBitmap.isRecycled) {
            // Nếu có trong cache và bitmap chưa bị recycle, sử dụng luôn
            bitmap = cachedBitmap
            loading = false
            Log.d("ZoomableImage", "✅ Using cached bitmap for: $imageUrl")
        } else {
            if (cachedBitmap?.isRecycled == true) {
                Log.w("ZoomableImage", "⚠️ Cached bitmap was recycled, reloading: $imageUrl")
                ImageCacheManager.removeBitmap(imageUrl)
            }

            // Nếu chưa có trong cache hoặc đã bị recycle, load từ nguồn
            loading = true
            Log.d("ZoomableImage", "⬇️ Loading from source: $imageUrl")

            val loadedBitmap = withContext(Dispatchers.IO) {
                try {
                    when {
                        imageUrl.startsWith("http://") || imageUrl.startsWith("https://") -> {
                            // Load from URL
                            val url = URL(imageUrl)
                            BitmapFactory.decodeStream(url.openConnection().getInputStream())
                        }

                        imageUrl.startsWith("file://") -> {
                            // Load from file URI
                            BitmapFactory.decodeFile(imageUrl.removePrefix("file://"))
                        }

                        else -> {
                            // Load from file path
                            val file = File(imageUrl)
                            if (file.exists()) {
                                BitmapFactory.decodeFile(file.absolutePath)
                            } else {
                                null
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("ZoomableImage", "❌ Error loading image: ${e.message}", e)
                    null
                }
            }

            // Lưu vào cache nếu load thành công
            if (loadedBitmap != null) {
                ImageCacheManager.putBitmap(imageUrl, loadedBitmap)
                bitmap = loadedBitmap
                Log.d("ZoomableImage", "✅ Loaded and cached: $imageUrl")
            } else {
                Log.e("ZoomableImage", "❌ Failed to load: $imageUrl")
            }

            loading = false
        }
    }

    if (loading) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        bitmap?.let { bmp ->
            Image(
                bitmap = bmp.asImageBitmap(),
                contentDescription = imageUrl,
                contentScale = ContentScale.FillWidth,
                modifier = modifier
                    .fillMaxWidth()
                    .zIndex(if (isZooming) 10f else 0f)
                    .aspectRatio(1f)
                    .padding(8.dp)
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

                                    return pointerEvent.changes.size > 1 || zoomStateProvider.invoke().scale > 1f
                                }
                            }
                        )
                    )
            )
        }
    }
}
