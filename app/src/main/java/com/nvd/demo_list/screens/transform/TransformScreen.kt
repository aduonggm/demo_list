package com.nvd.demo_list.screens.transform

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateRotation
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import com.nvd.demo_list.R
import com.nvd.demo_list.screens.drawCropFrame
import com.nvd.demo_list.screens.drawResizeHandles
import com.nvd.demo_list.screens.getHandleAtPosition
import com.nvd.demo_list.screens.updateCropRect
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch


suspend fun PointerInputScope.handleTransformGestures(
    controllers: List<TransformController>,
    lazyState: LazyListState,
    scope: CoroutineScope
) {
    var controller: TransformController? = null
    awaitEachGesture {
        awaitFirstDown()
        Log.d("===>>>>>> ", "handleTransformGestures: on down start ")

        var touchCount = 1
        do {
            val event = awaitPointerEvent()
            touchCount = event.changes.count { it.pressed }
            val touches = event.changes
            val pos1 = touches[0].position
            if (controller == null) {
                val found = controllers.filter { it.bounds?.contains(pos1) == true }
                    .sortedBy { !it.isZooming }

                Log.d("========>>>>>> ", "handleTransformGestures: zooming sort  ${found.map { !it.isZooming }}")
                controller = found.firstOrNull()
            }

            if (touchCount >= 2 || (touchCount == 1 && controller != null && controller!!.isZooming)) {
                // Khi có 2 ngón → xử lý zoom
                val zoom = event.calculateZoom()
                val pan = event.calculatePan()

                controller?.onTransform(zoom, pan, 0f)

                // Consume để không scroll
                event.changes.forEach { it.consume() }
            }


        } while (event.changes.any { it.pressed })


        controller?.touchUp()
        controller = null
    }
}

@Composable
fun ParentScreen() {
    val controllers = remember { (1..10).map { TransformController() } }
    val state = rememberTransformableState { zoomChange, panChange, _ ->

    }


    val coroutineScope = rememberCoroutineScope()
    val lazyState = rememberLazyListState()
    Column(Modifier.fillMaxSize()) {
        LazyColumn(
            userScrollEnabled = !controllers.map { it.isZooming }.toSet().contains(true),
            state = lazyState,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .pointerInput(Unit) {
                    handleTransformGestures(controllers, lazyState, coroutineScope)
                }
                .drawCrop()


        ) {

            items(10) { index ->
                TransformableBox(
                    controller = controllers[index],
                    modifier = Modifier
                        .fillMaxWidth()

                        .weight(1f)
                        .background(Color.LightGray)
                ) { modifier ->
                    Image(
                        painter = painterResource(id = R.drawable.test),
                        contentDescription = null,
                        modifier = modifier.fillMaxSize()
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Button(onClick = { }) {
                Text("Reset")
            }
            Button(onClick = { }) {
                Text("Zoom +")
            }
            Button(onClick = { }) {
                Text("Zoom -")
            }
        }
    }
}

@Composable
fun Modifier.drawCrop(): Modifier {
    var cropRect by remember { mutableStateOf(Rect.Zero) }
    var isDragging by remember { mutableStateOf(false) }
    var dragHandle by remember { mutableIntStateOf(-1) }
    var isInitialized by remember { mutableStateOf(false) }


    val e = Modifier
        .drawWithContent {
            drawContent() // vẽ nội dung bên dưới (ví dụ hình ảnh)

            val screenWidth = size.width
            val screenHeight = size.height

            // Khởi tạo cropRect lần đầu
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

            if (/*isVisible &&*/ cropRect != Rect.Zero) {
                // 🔳 Vẽ vùng mờ bên ngoài cropRect
                drawRect(
                    Color.Black.copy(alpha = 0.7f),
                    size = Size(screenWidth, cropRect.top)
                ) // top
                drawRect(
                    Color.Black.copy(alpha = 0.7f),
                    topLeft = Offset(0f, cropRect.bottom),
                    size = Size(screenWidth, screenHeight - cropRect.bottom)
                ) // bottom
                drawRect(
                    Color.Black.copy(alpha = 0.7f),
                    topLeft = Offset(0f, cropRect.top),
                    size = Size(cropRect.left, cropRect.height)
                ) // left
                drawRect(
                    Color.Black.copy(alpha = 0.7f),
                    topLeft = Offset(cropRect.right, cropRect.top),
                    size = Size(screenWidth - cropRect.right, cropRect.height)
                ) // right

                // 🟦 Vẽ khung crop
                drawCropFrame(cropRect)

                // 🔘 Vẽ các handle resize
                drawResizeHandles(cropRect)
            }
        }
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
                            0f, 0f, size.width.toFloat(),
                            size.height.toFloat()
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

    return then(e)
}
