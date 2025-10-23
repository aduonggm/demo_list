package com.nvd.demo_list.screens

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.MotionEvent
import android.view.PixelCopy
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeGesturesPadding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.zIndex
import androidx.core.graphics.createBitmap
import coil.compose.AsyncImage
import com.nvd.demo_list.R
import com.nvd.demo_list.models.NewsFeedData
import com.nvd.demo_list.models.NewsFeedItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nl.birdly.zoombox.ZoomState
import nl.birdly.zoombox.gesture.condition.TouchCondition
import nl.birdly.zoombox.gesture.transform.TransformGestureHandler
import nl.birdly.zoombox.rememberMutableZoomState
import nl.birdly.zoombox.zoomable
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.math.abs

/**
 * Capture the crop area (transparent square region) from screen and save to MediaStore
 * Uses PixelCopy API to handle hardware bitmaps properly
 */
suspend fun captureAndSaveCropArea(
    context: Context,
    rootView: View,
    viewWidth: Int,
    viewHeight: Int
): Boolean {
    return withContext(Dispatchers.IO) {
        try {
            // Use actual view dimensions instead of displayMetrics
            val screenWidth = viewWidth
            val screenHeight = viewHeight

            // Calculate crop area (square with size = screen width, centered vertically)
            val cropSize = screenWidth
            val cropX = 0
            val cropY = maxOf(0, (screenHeight - cropSize) / 2)

            // Ensure crop dimensions are exactly square
            val cropWidth = cropSize
            val cropHeight = cropSize  // Always square, not minOf(...)

            Log.d("CaptureImage", "View size: ${screenWidth}x${screenHeight}")
            Log.d("CaptureImage", "Crop area: x=$cropX, y=$cropY, w=$cropWidth, h=$cropHeight")

            // Capture using PixelCopy API (Android 8+) or fallback
            val fullBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                captureScreenWithPixelCopy(context, rootView, screenWidth, screenHeight)
            } else {
                captureScreenLegacy(rootView, screenWidth, screenHeight)
            }

            if (fullBitmap == null) {
                Log.e("CaptureImage", "Failed to capture screen")
                return@withContext false
            }

            // Validate crop area is within bounds
            if (cropY + cropHeight > fullBitmap.height) {
                Log.e("CaptureImage", "Crop area exceeds bitmap bounds")
                fullBitmap.recycle()
                return@withContext false
            }

            // Crop only the transparent square area from the captured bitmap
            val croppedBitmap = Bitmap.createBitmap(
                fullBitmap,
                cropX,
                cropY,
                cropWidth,
                cropHeight
            )

            // Save to MediaStore
            val contentValues = ContentValues().apply {
                put(
                    MediaStore.Images.Media.DISPLAY_NAME,
                    "capture_${System.currentTimeMillis()}.jpg"
                )
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            }

            val uri = context.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )

            if (uri != null) {
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)
                }
                Log.d("CaptureImage", "Image saved successfully to: $uri")

                // Clean up
                fullBitmap.recycle()
                croppedBitmap.recycle()

                return@withContext true
            } else {
                Log.e("CaptureImage", "Failed to create MediaStore entry")
                fullBitmap.recycle()
                return@withContext false
            }

        } catch (e: Exception) {
            Log.e("CaptureImage", "Error capturing/saving image", e)
            return@withContext false
        }
    }
}

/**
 * Capture screen using PixelCopy API (Android 8+)
 * This properly handles hardware bitmaps
 */
@RequiresApi(Build.VERSION_CODES.O)
private suspend fun captureScreenWithPixelCopy(
    context: Context,
    view: View,
    width: Int,
    height: Int
): Bitmap? = suspendCoroutine { continuation ->
    try {
        val bitmap = createBitmap(width, height)
        val locationOfView = IntArray(2)
        view.getLocationInWindow(locationOfView)

        val window = (context as? Activity)?.window
        if (window == null) {
            Log.e("CaptureImage", "Context is not an Activity")
            continuation.resume(null)
            return@suspendCoroutine
        }

        PixelCopy.request(
            window,
            android.graphics.Rect(
                locationOfView[0],
                locationOfView[1],
                locationOfView[0] + width,
                locationOfView[1] + height
            ),
            bitmap,
            { copyResult ->
                if (copyResult == PixelCopy.SUCCESS) {
                    continuation.resume(bitmap)
                } else {
                    Log.e("CaptureImage", "PixelCopy failed with result: $copyResult")
                    continuation.resume(null)
                }
            },
            Handler(Looper.getMainLooper())
        )
    } catch (e: Exception) {
        Log.e("CaptureImage", "Error in PixelCopy", e)
        continuation.resume(null)
    }
}

/**
 * Legacy capture method for Android < 8
 * Disables hardware acceleration temporarily to avoid hardware bitmap issues
 */
private suspend fun captureScreenLegacy(
    view: View,
    width: Int,
    height: Int
): Bitmap? = withContext(Dispatchers.Main) {
    try {
        // Temporarily disable hardware acceleration
        val originalLayerType = view.layerType
        view.setLayerType(View.LAYER_TYPE_SOFTWARE, null)

        val bitmap = createBitmap(width, height)
        val canvas = Canvas(bitmap)
        view.draw(canvas)

        // Restore original layer type
        view.setLayerType(originalLayerType, null)

        bitmap
    } catch (e: Exception) {
        Log.e("CaptureImage", "Error in legacy capture", e)
        null
    }
}

@SuppressLint("ConfigurationScreenWidthHeight")
@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)
@Composable
fun Option2Screen() {
    val newsFeedItems = remember { NewsFeedData.getSampleData() }
    val listState = rememberLazyListState()
    val context = LocalContext.current
    val view = LocalView.current
    val scope = rememberCoroutineScope()

    var itemFound by remember { mutableStateOf(ItemSelected(null, IntOffset.Zero)) }

    var showImage by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    val config = LocalConfiguration.current
    val screenHeightPx = with(density) { config.screenHeightDp.dp.toPx() }
    val screenWidthPx = with(density) { config.screenWidthDp.dp.toPx() }

    val insets = WindowInsets
    val statusBarHeight = insets.statusBars.getTop(LocalDensity.current)
    val navBarHeight = insets.navigationBars.getBottom(LocalDensity.current)

    var isZoom by remember { mutableStateOf(false) }
    var currentCroppingImageUrl by remember { mutableStateOf<String?>(null) }
    var viewWidth by remember { mutableIntStateOf(0) }
    var viewHeight by remember { mutableIntStateOf(0) }
    var resetZoomCallback by remember { mutableStateOf<(() -> Unit)?>(null) }
    var isLongClick by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { size ->
                viewWidth = size.width
                viewHeight = size.height
            }
    ) {
        Scaffold(
            modifier = Modifier.zIndex(0f)
        ) { paddingValues ->
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color(0xFFF0F2F5)) // Facebook background color
                ,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Add post creation card
                item {
                    CreatePostCard()
                }

                // News feed items
                items(newsFeedItems.size) { index ->
                    val item = newsFeedItems[index]
                    NewsFeedCard(
                        item = item,
                        index = index,
                        isVisible = itemFound.image != item.postImage,
                        onClick = {
                            itemFound = it
                            coroutineScope.launch {
                                val layoutInfo = listState.layoutInfo
                                val viewportCenter =
                                    (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
                                Log.d(
                                    "======>>>>>>>>> ",
                                    "NewsFeedListScreen: view port center  $viewportCenter    ${(layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset)}"
                                )
                                val itemCenter = it.offset.y + it.itemHeight / 2
                                val diff = itemCenter - viewportCenter
                                listState.animateScrollBy(
                                    diff.toFloat(),
                                    animationSpec = tween(
                                        durationMillis = 500,
                                        delayMillis = 0,
                                        easing = EaseInOut
                                    )
                                )
                            }
                        },
                        onLongClick = {
                            isLongClick = true && isZoom
                        },
                        onZoomChange = { zooming ->
                            isZoom = zooming
                            currentCroppingImageUrl = if (zooming) {
                                "pdf_${index}" // Use a unique identifier for PDF
                            } else {
                                null
                            }
                        },
                        onResetZoom = { resetFn ->
                            resetZoomCallback = resetFn
                        }
                    )
                }

                // Bottom spacing
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            if (isZoom) {
                IconButton({}) {
                    Icon(
                        Icons.Default.Clear,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier
                            .statusBarsPadding()
                            .padding(30.dp)
                    )
                }
            }
        }


        if (!itemFound.image.isNullOrEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = Color.Black.copy(alpha = 0.6f))
                    .clickable {},
            ) {

            }
        }


        if (!itemFound.image.isNullOrEmpty()) {
            var moved by remember { mutableStateOf(false) }
            val offset by animateIntOffsetAsState(
                targetValue = if (moved) {
                    IntOffset(
                        0,
                        ((screenHeightPx + navBarHeight + statusBarHeight - itemFound.itemHeight) / 2).toInt()
                    )
                } else {
                    itemFound.offset
                },
                label = "offset",

                animationSpec = tween(durationMillis = 500, easing = EaseInOut)
            )

            LaunchedEffect(Unit) {
                moved = true
            }


            AsyncImage(
                itemFound.image,
                contentDescription = "Page ${0 + 1}",
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .offset { offset }
                    .fillMaxWidth()
                    .zoomable(zoomRange = 1f..3f)
                    .clickable {
                        itemFound = ItemSelected()
                    }
            )

            IconButton(
                {
                    itemFound = ItemSelected()

                }, modifier = Modifier
                    .safeGesturesPadding()
                    .padding(8.dp)
                    .background(color = Color.Gray.copy(alpha = 0.5f), shape = CircleShape)
                    .zIndex(100f)
            ) {

                Icon(
                    Icons.Default.Clear,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.padding(4.dp)
                )
            }
        }

        // Crop overlay - shows when PDF is zooming
        if (isZoom && currentCroppingImageUrl != null && isLongClick) {
            CropOverlay(
                context = context,
                imageUrl = currentCroppingImageUrl!!,
                onCancel = {
                    // Reset zoom state before closing overlay
                    resetZoomCallback?.invoke()
                    isZoom = false
                    currentCroppingImageUrl = null
                    isLongClick = false
                },
                onCrop = {
                    scope.launch {
                        // Get root view for capturing
                        val rootView = view.rootView
                        val success = captureAndSaveCropArea(
                            context = context,
                            rootView = rootView,
                            viewWidth = viewWidth,
                            viewHeight = viewHeight
                        )
                        withContext(Dispatchers.Main) {
                            if (success) {
                                Toast.makeText(
                                    context,
                                    "Screen captured and saved successfully!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Toast.makeText(
                                    context,
                                    "Failed to capture screen",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun NewsFeedCard(
    item: NewsFeedItem,
    index: Int,
    onClick: (ItemSelected) -> Unit,
    isVisible: Boolean = true,
    onLongClick: () -> Unit = {},
    onZoomChange: (Boolean) -> Unit = {},
    onResetZoom: (() -> Unit) -> Unit = {}
) {
    val zoomState = rememberMutableZoomState()
    val isZooming = zoomState.value.scale > 1f

    // Notify parent about zoom state
    onZoomChange(isZooming)

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
        rawResId = if (index % 2 == 0) R.raw.test_1 else R.raw.test_2
    )


    // Reactions and Stats
    ReactionsAndStats(item)

    Divider(modifier = Modifier.padding(horizontal = 12.dp))

    // Action Buttons
    ActionButtons(item)


}

@Composable
fun CropOverlay(
    context: Context,
    imageUrl: String,
    modifier: Modifier = Modifier,
    onCancel: () -> Unit = {},
    onCrop: () -> Unit = {}
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
                    detectDragGestures(
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
                    )
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

            if (isInitialized && cropRect != Rect.Zero) {
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
                onClick = onCrop,
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

const val LONG_PRESS_TIME = 1500L

