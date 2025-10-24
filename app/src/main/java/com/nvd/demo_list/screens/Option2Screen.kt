package com.nvd.demo_list.screens

import android.annotation.SuppressLint
import android.util.Log
import android.view.MotionEvent
import android.widget.Toast
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.nvd.demo_list.R
import com.nvd.demo_list.models.NewsFeedData
import com.nvd.demo_list.models.NewsFeedItem
import com.nvd.demo_list.navigation.Screen
import com.nvd.demo_list.ui.component.CropOverlay
import com.nvd.demo_list.ui.utils.captureAndSaveCropArea
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nl.birdly.zoombox.ZoomState
import nl.birdly.zoombox.gesture.condition.TouchCondition
import nl.birdly.zoombox.gesture.transform.TransformGestureHandler
import nl.birdly.zoombox.rememberMutableZoomState
import nl.birdly.zoombox.zoomable
import java.io.File


@SuppressLint("ConfigurationScreenWidthHeight")
@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)
@Composable
fun Option2Screen(
    navController: androidx.navigation.NavController? = null
) {
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
    var overlayVisible by remember { mutableStateOf(true) }

    // Mutable list to store URIs of cropped images
    val croppedImageUris = remember { mutableListOf<android.net.Uri>() }

    // Store source file and zoom state for high-quality cropping
    // Lưu file theo từng item để tránh bị ghi đè
    var sourceFilesMap by remember { mutableStateOf<Map<String, File>>(emptyMap()) }
    var currentZoomState by remember { mutableStateOf<ZoomState?>(null) }
    var pdfViewerWidth by remember { mutableIntStateOf(0) }
    var pdfViewerHeight by remember { mutableIntStateOf(0) }
    var pdfViewerOffsetY by remember { mutableIntStateOf(0) }

    // State for zoomed PDF overlay
    var zoomedPdfIndex by remember { mutableIntStateOf(-1) }
    var zoomedPdfFile by remember { mutableStateOf<File?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { size ->
                viewWidth = size.width
                viewHeight = size.height
            }
    ) {
        Scaffold(
            modifier = Modifier.zIndex(0f),
            floatingActionButton = {
                if (!isZoom) {
                    FloatingActionButton(
                        onClick = {
                            // Store URIs in companion object and navigate
                            ImageListCropScreenData.croppedImageUris = croppedImageUris.toList()
                            navController?.navigate(Screen.ImageCropListScreen.route)
                        },
                        modifier = Modifier.align(alignment = Alignment.BottomEnd)
                    ) {
                        Text(
                            text = "List Crop"
                        )
                    }
                }
            }
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
                            // Update zoomed PDF state
                            if (zooming) {
                                zoomedPdfIndex = index
                                zoomedPdfFile = sourceFilesMap["pdf_${index}"]
                            } else {
                                zoomedPdfIndex = -1
                                zoomedPdfFile = null
                            }
                        },
                        onResetZoom = { resetFn ->
                            resetZoomCallback = resetFn
                        },
                        onSourceFileReady = { file ->
                            // Lưu file theo index để mỗi item có file riêng
                            file?.let {
                                sourceFilesMap = sourceFilesMap + ("pdf_${index}" to it)
                            }
                        },
                        onZoomStateUpdate = { zoomState ->
                            currentZoomState = zoomState
                        },
                        onPdfViewerSizeChanged = { width, height, offsetY ->
                            pdfViewerWidth = width
                            pdfViewerHeight = height
                            pdfViewerOffsetY = offsetY
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
                isVisible = overlayVisible,
                onCancel = {
                    // Reset zoom state before closing overlay
                    resetZoomCallback?.invoke()
                    isZoom = false
                    currentCroppingImageUrl = null
                    isLongClick = false
                    overlayVisible = true
                },
                onCrop = { cropRect ->
                    scope.launch {
                        // Hide overlay handles before cropping
                        withContext(Dispatchers.Main) {
                            overlayVisible = false
                        }

                        // Wait a bit for UI to update
                        kotlinx.coroutines.delay(100)

                        // Capture and crop from screen instead of source bitmap
                        val croppedUri = captureAndSaveCropArea(
                            context = context,
                            rootView = view,
                            viewWidth = viewWidth,
                            viewHeight = viewHeight,
                            cropRect = cropRect
                        )

                        withContext(Dispatchers.Main) {
                            // Show overlay again
                            overlayVisible = true

                            if (croppedUri != null) {
                                // Add URI to the list
                                croppedImageUris.add(croppedUri)
                                Log.d("CropImage", "Total cropped images: ${croppedImageUris.size}")

                                Toast.makeText(
                                    context,
                                    "Image cropped and saved successfully!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                // Reset zoom state and close overlay after successful crop
//                                resetZoomCallback?.invoke()
//                                isZoom = false
//                                currentCroppingImageUrl = null
//                                isLongClick = false
                            } else {
                                Toast.makeText(
                                    context,
                                    "Failed to crop image",
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

    Divider(modifier = Modifier.padding(horizontal = 12.dp))

    // Action Buttons
    ActionButtons(item)
}

const val LONG_PRESS_TIME = 500L

