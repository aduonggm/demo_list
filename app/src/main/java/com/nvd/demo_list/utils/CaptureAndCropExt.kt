package com.nvd.demo_list.utils

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.PixelCopy
import android.view.View
import androidx.annotation.RequiresApi
import androidx.compose.ui.geometry.Rect
import androidx.core.graphics.createBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.birdly.zoombox.ZoomState
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Capture the crop area from screen and save to MediaStore
 * Uses PixelCopy API to handle hardware bitmaps properly
 * Returns the URI of the saved image, or null if failed
 */
suspend fun captureAndSaveCropArea(
    context: Context,
    rootView: View,
    viewWidth: Int,
    viewHeight: Int,
    cropRect: Rect
): Uri? {
    return withContext(Dispatchers.IO) {
        try {
            // Use actual crop rectangle coordinates from the overlay
            val cropX = cropRect.left.toInt()
            val cropY = cropRect.top.toInt()
            val cropWidth = cropRect.width.toInt()
            val cropHeight = cropRect.height.toInt()

            Log.d("CaptureImage", "View size: ${viewWidth}x${viewHeight}")
            Log.d("CaptureImage", "Crop area: x=$cropX, y=$cropY, w=$cropWidth, h=$cropHeight")

            // Capture using PixelCopy API (Android 8+) or fallback
            val fullBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                captureScreenWithPixelCopy(context, rootView, viewWidth, viewHeight)
            } else {
                captureScreenLegacy(rootView, viewWidth, viewHeight)
            }

            if (fullBitmap == null) {
                Log.e("CaptureImage", "Failed to capture screen")
                return@withContext null
            }

            // Validate crop area is within bounds
            if (cropY + cropHeight > fullBitmap.height) {
                Log.e("CaptureImage", "Crop area exceeds bitmap bounds")
                fullBitmap.recycle()
                return@withContext null
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

                return@withContext uri
            } else {
                Log.e("CaptureImage", "Failed to create MediaStore entry")
                fullBitmap.recycle()
                return@withContext null
            }

        } catch (e: Exception) {
            Log.e("CaptureImage", "Error capturing/saving image", e)
            return@withContext null
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

/**
 * Crop area from high-quality source bitmap and save to MediaStore
 * This method crops from the original PDF bitmap to maintain quality
 */
suspend fun cropAndSaveBitmap(
    context: Context,
    sourceFile: File?,
    cropRect: Rect,
    zoomState: ZoomState,
    viewWidth: Int,
    viewHeight: Int
): Boolean {
    return withContext(Dispatchers.IO) {
        try {
            if (sourceFile == null || !sourceFile.exists()) {
                Log.e("CropImage", "Source file is null or doesn't exist")
                return@withContext false
            }

            // Load the high-quality source bitmap from file
            val sourceBitmap = BitmapFactory.decodeFile(sourceFile.absolutePath)
            if (sourceBitmap == null) {
                Log.e("CropImage", "Failed to decode source bitmap")
                return@withContext false
            }

            Log.d("CropImage", "Source bitmap size: ${sourceBitmap.width}x${sourceBitmap.height}")
            Log.d("CropImage", "View size: ${viewWidth}x${viewHeight}")
            Log.d(
                "CropImage",
                "Zoom state: scale=${zoomState.scale}, offset=${zoomState.offset.x},${zoomState.offset.y}"
            )
            Log.d(
                "CropImage",
                "Crop rect on screen: ${cropRect.left},${cropRect.top} - ${cropRect.right},${cropRect.bottom}"
            )

            // Calculate the scale ratio between source bitmap and displayed image
            // With ContentScale.FillWidth, the width fills the screen and height scales proportionally
            val displayScale = viewWidth.toFloat() / sourceBitmap.width.toFloat()

            Log.d("CropImage", "Display scale: $displayScale")

            // Get zoom parameters
            val zoomScale = zoomState.scale
            val zoomOffsetX = zoomState.offset.x
            val zoomOffsetY = zoomState.offset.y

            // Transform screen coordinates to source bitmap coordinates
            // Formula: sourceCoord = (screenCoord - zoomOffset) / zoomScale / displayScale
            val sourceCropX = ((cropRect.left - zoomOffsetX) / zoomScale / displayScale).toInt()
            val sourceCropY = ((cropRect.top - zoomOffsetY) / zoomScale / displayScale).toInt()
            val sourceCropWidth = (cropRect.width / zoomScale / displayScale).toInt()
            val sourceCropHeight = (cropRect.height / zoomScale / displayScale).toInt()

            Log.d(
                "CropImage",
                "Calculated source crop: x=$sourceCropX, y=$sourceCropY, w=$sourceCropWidth, h=$sourceCropHeight"
            )

            // Validate and clamp crop area to bitmap bounds
            val clampedX = sourceCropX.coerceIn(0, sourceBitmap.width - 1)
            val clampedY = sourceCropY.coerceIn(0, sourceBitmap.height - 1)
            val clampedWidth = sourceCropWidth.coerceIn(1, sourceBitmap.width - clampedX)
            val clampedHeight = sourceCropHeight.coerceIn(1, sourceBitmap.height - clampedY)

            if (clampedWidth <= 0 || clampedHeight <= 0) {
                Log.e("CropImage", "Invalid crop dimensions after clamping")
                sourceBitmap.recycle()
                return@withContext false
            }

            Log.d(
                "CropImage",
                "Clamped crop area: x=$clampedX, y=$clampedY, w=$clampedWidth, h=$clampedHeight"
            )

            // Crop from the high-quality source bitmap
            val croppedBitmap = Bitmap.createBitmap(
                sourceBitmap,
                clampedX,
                clampedY,
                clampedWidth,
                clampedHeight
            )

            // Save to MediaStore
            val contentValues = ContentValues().apply {
                put(
                    MediaStore.Images.Media.DISPLAY_NAME,
                    "crop_${System.currentTimeMillis()}.jpg"
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
                Log.d("CropImage", "Image saved successfully to: $uri")

                // Clean up
                sourceBitmap.recycle()
                croppedBitmap.recycle()

                return@withContext true
            } else {
                Log.e("CropImage", "Failed to create MediaStore entry")
                sourceBitmap.recycle()
                croppedBitmap.recycle()
                return@withContext false
            }

        } catch (e: Exception) {
            Log.e("CropImage", "Error cropping image: ${e.message}", e)
            return@withContext false
        }
    }
}