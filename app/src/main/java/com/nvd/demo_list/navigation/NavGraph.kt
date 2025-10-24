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
import com.nvd.demo_list.screens.ImageListCropScreen
import com.nvd.demo_list.screens.MainScreen
import com.nvd.demo_list.screens.NewsFeedListScreen
import com.nvd.demo_list.screens.Option2ImageScreen
import com.nvd.demo_list.screens.Option2PdfScreen
import com.nvd.demo_list.screens.Option2Screen
import com.nvd.demo_list.screens.toturial.GesturePropagationExample
import com.nvd.demo_list.screens.toturial.Tutorial5_6Screen2
import com.nvd.demo_list.screens.transform.ParentScreen

sealed class Screen(val route: String) {
    object NewsFeedList : Screen("news_feed_list")
    object Option2 : Screen("option_2")
    object Option2Image : Screen("option_2_image")
    object ImageCropListScreen : Screen("image_crop_list_screen")
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

            composable("splash"){
//                Tutorial5_6Screen2()
                ParentScreen()
//                MainScreen {
//                    navController.navigate(it)
//                }
            }


            composable(Screen.Option2.route) {
                //Option2Screen(navController = navController)
                Option2PdfScreen(navController = navController)
            }

            composable(Screen.Option2Image.route) {
                Option2ImageScreen(
                    navController = navController
                )
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

            composable(Screen.ImageCropListScreen.route) {
                ImageListCropScreen(
                    onBackClick = {
                        navController.navigateUp()
                    }
                )
            }
        }
    }
}

