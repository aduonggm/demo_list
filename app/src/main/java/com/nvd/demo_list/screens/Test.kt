package com.nvd.demo_list.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

@Composable
fun HorizontalGrid() {
    val days = (1..300).toList()
    val weekChunks: List<List<Int>> = days.chunked(7)
    LazyColumn {
        items(items = weekChunks) { days: List<Int> ->
            Week(days)
        }
    }
}

@Composable
fun Week(days: List<Int>) {
    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier
            .fillMaxSize()
            .zIndex(if (days.contains(17)) 2f else 1f)
    ) {
        days.forEach {
            Day(it)
        }
    }
}

@Composable
fun Day(dayOfWeek: Int) {
    Box(
        modifier = Modifier
            .zIndex(if (dayOfWeek == 17) 2f else 1f)
            .size(48.dp)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {},
                    onDrag = { change, dragAmount ->
                        change.consume()
//                    offsetX += dragAmount.x
//                    offsetY += dragAmount.y
//                    onDrag(offsetX, offsetY)
                    },
                    onDragEnd = {})
            }
            .padding(4.dp)
            .background(Color.LightGray)
    ) {
        Text(
            modifier = Modifier
                .graphicsLayer {

                }
                .drawWithContent {
                    if (dayOfWeek == 17) {
                        drawContent()
                        drawCircle(Color.Green, radius = 150F, center = Offset(50f, 50f))
                    }
                },
            text = dayOfWeek.toString()
        )
    }
}