package com.nvd.demo_list.navigation

import android.graphics.Rect
import android.util.Log
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitTouchSlopOrCancellation
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.foundation.gestures.horizontalDrag
import androidx.compose.foundation.gestures.verticalDrag
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.nvd.demo_list.screens.DetailsScreen
import com.nvd.demo_list.screens.MainScreen
import com.nvd.demo_list.screens.NewsFeedListScreen
import com.nvd.demo_list.screens.Option2Screen
import kotlin.math.roundToInt

sealed class Screen(val route: String) {
    object NewsFeedList : Screen("news_feed_list")
    object Option2 : Screen("option_2")
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    SharedTransitionLayout {
        NavHost(
            navController = navController,
            startDestination = "splash"
        ) {
            composable(Screen.NewsFeedList.route) {
                NewsFeedListScreen(
                    navController = navController,
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedContentScope = this@composable,
                    openDetail = {
                        navController.navigate("details/$it")
                    }
                )
            }

            composable("splash") {
                MainScreen {
                    navController.navigate(it)
                }
            }


            composable(Screen.Option2.route) {
                Option2Screen()
//                TestScreen()
            }



            composable(
                "details/{item}",
                arguments = listOf(navArgument("item") { type = NavType.IntType })
            ) { backStackEntry ->
                val index = backStackEntry.arguments?.getInt("item")
                Log.d("======>>>>>>> ", "AppNavigation: index found is  $index")
                DetailsScreen(
                    index ?: 0,
                    this@SharedTransitionLayout,
                    this@composable
                ) {
                    navController.navigateUp()
                }
            }
        }
    }
}

@Composable
fun TestScreen() {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Layer 1 - Zoom
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, _, zoom, _ ->
                        scale *= zoom
                        println("Layer1 zoom: $scale")
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Layer1",
                modifier = Modifier
                    .width(200.dp)
                    .height(150.dp)
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale
                    )
                    .background(color = Color.Blue)
            )
        }

        // Layer 2 - Chỉ consume drag, pass zoom through
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Layer2",
                modifier = Modifier
                    .offset { IntOffset(offset.x.roundToInt(), offset.y.roundToInt()) }
                    .fillMaxSize()
                    .background(color = Color(0x5C00E6FD))
                    .pointerInput(Unit) {
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)

                            do {
                                val event = awaitPointerEvent()
                                val pointerCount = event.changes.count { it.pressed }

                                // Single touch -> consume cho drag
                                if (pointerCount == 1) {
                                    event.changes.firstOrNull()?.let { change ->
                                        if (change.positionChanged()) {
                                            offset += change.positionChange()
                                            change.consume()
                                            println("Layer2 drag: $offset")
                                        }
                                    }
                                }
                                // Multi-touch -> KHÔNG consume, để pass xuống Layer1

                            } while (event.changes.any { it.pressed })
                        }
                    }
            )
        }
    }
}

// OK
//
//@Composable
//fun TestScreen() {
//    var scale by remember { mutableStateOf(1f) }
//    var offset by remember { mutableStateOf(Offset.Zero) }
//
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            // ĐẶT ZOOM Ở BOX CHA - quan trọng!
//            .pointerInput(Unit) {
//                detectTransformGestures { _, _, zoom, _ ->
//                    scale *= zoom
//                    println("Layer1 zoom: $scale")
//                }
//            }
//    ) {
//        // Layer 1 - Zoom
//        Box(
//            modifier = Modifier.fillMaxSize(),
//            contentAlignment = Alignment.Center
//        ) {
//            Text(
//                "Layer1",
//                modifier = Modifier
//                    .width(200.dp)
//                    .height(150.dp)
//                    .graphicsLayer(
//                        scaleX = scale,
//                        scaleY = scale
//                    )
//                    .background(color = Color.Blue)
//            )
//        }
//
//        // Layer 2 - Drag only
//        Box(
//            modifier = Modifier.fillMaxSize(),
//            contentAlignment = Alignment.Center
//        ) {
//            Text(
//                "Layer2",
//                modifier = Modifier
//                    .offset { IntOffset(offset.x.roundToInt(), offset.y.roundToInt()) }
//                    .fillMaxSize()
//                    .background(color = Color(0x5C00E6FD))
//                    .pointerInput(Unit) {
//                        awaitEachGesture {
//                            awaitFirstDown(requireUnconsumed = false)
//
//                            do {
//                                val event = awaitPointerEvent()
//
//                                // Chỉ consume single touch
//                                if (event.changes.size == 1) {
//                                    event.changes.forEach { change ->
//                                        val dragAmount = change.position - change.previousPosition
//                                        offset += dragAmount
//                                        change.consume()
//                                        println("Layer2 drag: $offset")
//                                    }
//                                }
//                                // Multi-touch không consume - pass through
//                            } while (event.changes.any { it.pressed })
//                        }
//                    }
//            )
//        }
//    }
//}