package com.nvd.demo_list.navigation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.nvd.demo_list.screens.ImageDetailScreen
import com.nvd.demo_list.screens.NewsFeedListScreen

sealed class Screen(val route: String) {
    object NewsFeedList : Screen("news_feed_list")
    object ImageDetail : Screen("image_detail/{itemId}/{imageUrl}") {
        fun createRoute(itemId: String, imageUrl: String) = "image_detail/$itemId/$imageUrl"
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    
    SharedTransitionLayout {
        NavHost(
            navController = navController,
            startDestination = Screen.NewsFeedList.route
        ) {
            composable(Screen.NewsFeedList.route) {
                NewsFeedListScreen(
                    navController = navController,
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedContentScope = this@composable
                )
            }
            
            composable(Screen.ImageDetail.route) { backStackEntry ->
                val itemId = backStackEntry.arguments?.getString("itemId") ?: ""
                val imageUrl = backStackEntry.arguments?.getString("imageUrl") ?: ""
                
                ImageDetailScreen(
                    itemId = itemId,
                    imageUrl = imageUrl,
                    navController = navController,
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedContentScope = this@composable
                )
            }
        }
    }
}

