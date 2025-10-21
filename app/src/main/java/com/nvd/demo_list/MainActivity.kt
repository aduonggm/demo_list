package com.nvd.demo_list

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.nvd.demo_list.navigation.AppNavigation
import com.nvd.demo_list.ui.theme.Demo_listTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        R.drawable.ic_launcher_background
        setContent {
            Demo_listTheme {
                AppNavigation()
            }
        }
    }
}