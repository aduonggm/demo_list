package com.nvd.demo_list.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nvd.demo_list.navigation.Screen

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,

    onNavigateScreen: (String) -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(
            16.dp,
            alignment = Alignment.CenterVertically
        )
    ) {

        Button({
            onNavigateScreen(Screen.NewsFeedList.route)
        }) {
            Text("Click to zoom image")
        }

//        Button({
//            onNavigateScreen(Screen.Option2.route)
//        }) {
//            Text("Option 2 PDF")
//        }

        Button({
            onNavigateScreen(Screen.Option2Image.route)
        }) {
            Text("Pinch to zoom image")
        }

        Button({
            onNavigateScreen(Screen.MemoScreen.route)
        }) {
            Text("Memo")
        }
    }
}