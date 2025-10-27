package com.nvd.demo_list.screens

import android.annotation.SuppressLint
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.nvd.demo_list.models.NewsFeedData
import com.nvd.demo_list.navigation.Screen
import com.nvd.demo_list.ui.component.CreatePostCard
import com.nvd.demo_list.ui.component.CropOverlay
import com.nvd.demo_list.ui.component.NewsFeedImageCard
import com.nvd.demo_list.utils.captureAndSaveCropArea
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nl.birdly.zoombox.MutableZoomState

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun Option2ImageScreen(
    navController: NavController? = null
) {
    val newsFeedItems = remember { NewsFeedData.getSampleData() }
    val listState = rememberLazyListState()
    val context = LocalContext.current
    val view = LocalView.current
    val scope = rememberCoroutineScope()

    var isZoom by remember { mutableStateOf(false) }
    var currentCroppingImageUrl by remember { mutableStateOf<String?>(null) }
    var viewWidth by remember { mutableIntStateOf(0) }
    var viewHeight by remember { mutableIntStateOf(0) }
    var resetZoomCallback by remember { mutableStateOf<(() -> Unit)?>(null) }
    var isLongClick by remember { mutableStateOf(false) }
    var overlayVisible by remember { mutableStateOf(true) }
    var currentZoomState by remember { mutableStateOf<MutableZoomState?>(null) }

    // Mutable list to store URIs of cropped images
    val croppedImageUris = remember { mutableListOf<Uri>() }

    fun resetZoomStates() {
        resetZoomCallback?.invoke()
        isZoom = false
        currentCroppingImageUrl = null
        isLongClick = false
        overlayVisible = true
    }

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
                items(
                    newsFeedItems.size,
                    key = { index -> newsFeedItems[index].id })
                { index ->
                    val item = newsFeedItems[index]
                    NewsFeedImageCard(
                        item = item,
                        onLongClick = {
                            isLongClick = true && isZoom
                        },
                        onZoomChange = { zooming ->
                            isZoom = zooming
                            currentCroppingImageUrl = if (zooming) {
                                item.postImage
                            } else {
                                null
                            }
                        },
                        onResetZoom = { resetFn ->
                            resetZoomCallback = resetFn
                        },
                        onZoomStateUpdate = {
                            currentZoomState = it
                        }
                    )
                }

                // Bottom spacing
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        // Crop overlay - shows when PDF is zooming
        if (isZoom && currentCroppingImageUrl != null && isLongClick) {
            CropOverlay(
                context = context,
                imageUrl = currentCroppingImageUrl!!,
                isVisible = overlayVisible,
                zoomState = currentZoomState,
                onCancel = {
                    // Reset zoom state before closing overlay
                    resetZoomStates()
                },
                onCrop = { cropRect ->
                    scope.launch {
                        // Hide overlay handles before cropping
                        withContext(Dispatchers.Main) {
                            overlayVisible = false
                        }

                        // Wait a bit for UI to update
                        delay(100)

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
                                // resetZoomStates()
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