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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.nvd.demo_list.R
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
                controller = controllers.firstOrNull { it.bounds?.contains(pos1) == true }
            }

            if (touchCount >= 2 || (touchCount == 1 && controller!= null && controller!!.isZooming)) {
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
