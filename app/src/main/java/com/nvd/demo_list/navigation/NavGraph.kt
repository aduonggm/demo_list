package com.nvd.demo_list.navigation

import android.util.Log
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.nvd.demo_list.screens.DetailsScreen
import com.nvd.demo_list.screens.NewsFeedListScreen

sealed class Screen(val route: String) {
    object NewsFeedList : Screen("news_feed_list")
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
                    animatedContentScope = this@composable,
                    openDetail = {
                        navController.navigate("details/$it")
                    }
                )
            }

            composable(
                "details/{item}",
                arguments = listOf(navArgument("item") { type = NavType.IntType })
            ) { backStackEntry ->
                val index = backStackEntry.arguments?.getInt("item")
                Log.d("======>>>>>>> ", "AppNavigation: index found is  $index")
                DetailsScreen(
                    index ?: 0 ,
                    this@SharedTransitionLayout,
                    this@composable
                ) {
                    navController.navigateUp()
                }
            }
        }
    }
}

