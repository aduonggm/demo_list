package com.nvd.demo_list.screens

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.Spring.DampingRatioLowBouncy
import androidx.compose.animation.core.Spring.DampingRatioNoBouncy
import androidx.compose.animation.core.Spring.StiffnessLow
import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeGesturesPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.nvd.demo_list.R
import com.nvd.demo_list.models.NewsFeedData
import com.nvd.demo_list.models.NewsFeedItem
import com.nvd.demo_list.models.PostPrivacy
import com.nvd.demo_list.models.ReactionType
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import nl.birdly.zoombox.ZoomState
import nl.birdly.zoombox.gesture.condition.TouchCondition
import nl.birdly.zoombox.gesture.transform.TransformGestureHandler
import nl.birdly.zoombox.rememberMutableZoomState
import nl.birdly.zoombox.zoomable
import kotlin.math.log
import kotlin.math.roundToInt

@SuppressLint("ConfigurationScreenWidthHeight")
@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)
@Composable
fun Option2Screen(

) {
    val newsFeedItems = remember { NewsFeedData.getSampleData() }
    val listState = rememberLazyListState()

    var itemFound by remember { mutableStateOf(ItemSelected(null, IntOffset.Zero)) }

    var showImage by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    val config = LocalConfiguration.current
    val screenHeightPx = with(density) { config.screenHeightDp.dp.toPx() }
    val screenWidthPx = with(density) { config.screenWidthDp.dp.toPx() }

    val insets = WindowInsets
    val statusBarHeight = insets.statusBars.getTop(LocalDensity.current)
    val navBarHeight = insets.navigationBars.getBottom(LocalDensity.current)
    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
        ) { paddingValues ->

            var isZoom by remember { mutableStateOf(false) }
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()

                    .background(Color(0xFFF0F2F5)) // Facebook background color
                ,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Add post creation card
                item {
                    CreatePostCard()
                }

                // News feed items
                items(newsFeedItems.size) { index ->
                    val item = newsFeedItems[index]
                    NewsFeedCard1(
                        item = item,
                        index = index,
                        isVisible = itemFound.image != item.postImage,
                        onClick = {
                            itemFound = it
                            coroutineScope.launch {
                                val layoutInfo = listState.layoutInfo
                                val viewportCenter =
                                    (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
                                Log.d(
                                    "======>>>>>>>>> ",
                                    "NewsFeedListScreen: view port center  $viewportCenter    ${(layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset)}"
                                )
                                val itemCenter = it.offset.y + it.itemHeight / 2
                                val diff = itemCenter - viewportCenter
                                listState.animateScrollBy(
                                    diff.toFloat(),
                                    animationSpec = tween(
                                        durationMillis = 500,
                                        delayMillis = 0,
                                        easing = EaseInOut
                                    )
                                )
                            }
                        },
                    )
                }

                // Bottom spacing
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            if (isZoom) {
                IconButton({}) {
                    Icon(
                        Icons.Default.Clear,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier
                            .statusBarsPadding()
                            .padding(30.dp)
                    )
                }
            }
        }


        AnimatedVisibility(
            !itemFound.image.isNullOrEmpty(),
            enter = fadeIn(animationSpec = tween(durationMillis = 500)),
            exit = fadeOut()
        ) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = Color.Black.copy(alpha = 0.6f))
                    .clickable {},
            ) {

            }
        }


        if (!itemFound.image.isNullOrEmpty()) {
            var moved by remember { mutableStateOf(false) }
            val offset by animateIntOffsetAsState(
                targetValue = if (moved) {
                    IntOffset(
                        0,
                        ((screenHeightPx + navBarHeight + statusBarHeight - itemFound.itemHeight) / 2).toInt()
                    )
                } else {
                    itemFound.offset
                },
                label = "offset",

                animationSpec = tween(durationMillis = 500, easing = EaseInOut)
            )

            LaunchedEffect(Unit) {
                moved = true
            }


            AsyncImage(
                itemFound.image,
                contentDescription = "Page ${0 + 1}",
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .offset { offset }
                    .fillMaxWidth()
                    .zoomable(zoomRange = 1f..3f)
                    .clickable {
                        itemFound = ItemSelected()
                    }
            )

            IconButton(
                {
                    itemFound = ItemSelected()

                }, modifier = Modifier
                    .safeGesturesPadding()
                    .padding(8.dp)
                    .background(color = Color.Gray.copy(alpha = 0.5f), shape = CircleShape)
                    .zIndex(100f)
            ) {

                Icon(
                    Icons.Default.Clear,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.padding(4.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun NewsFeedCard1(
    item: NewsFeedItem,
    index: Int,
    onClick: (ItemSelected) -> Unit,
    isVisible: Boolean = true

) {
    // Post Header
    PostHeader(item)

    // Post Content
    PostContent(item)


    PdfViewer(

        modifier = Modifier
            .fillMaxWidth()
            .zoomable(
                zoomRange = 1f..3f,

                transformGestureHandler = TransformGestureHandler(
                    onCondition = object : TouchCondition {
                        override fun invoke(
                            zoomStateProvider: () -> ZoomState,
                            pointerInputScope: PointerInputScope,
                            pointerEvent: PointerEvent
                        ): Boolean {
                            return pointerEvent.changes.size > 1 || zoomStateProvider().scale > 1f
                        }
                    }
                )
            ),
        rawResId = if (index % 2 == 0) R.raw.test_1 else R.raw.test_2
    )


    // Reactions and Stats
    ReactionsAndStats(item)

    Divider(modifier = Modifier.padding(horizontal = 12.dp))

    // Action Buttons
    ActionButtons(item)


}
