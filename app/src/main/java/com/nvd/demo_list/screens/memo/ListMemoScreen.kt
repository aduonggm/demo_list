package com.nvd.demo_list.screens.memo

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListMemoScreen(
    onNavigateToAddMemo: ((String?) -> Unit)? = null
) {
    val context = LocalContext.current
    val viewModel = remember { MemoViewModel(context) }

    val lazyListState = rememberLazyListState()
    val hapticFeedback = LocalHapticFeedback.current

    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        // Update the list
        viewModel.reorderMemo(from.index, to.index)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Add memo button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = Color.White)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        onNavigateToAddMemo?.invoke(null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    )
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Add New Memo",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Memo list
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (viewModel.memos.isEmpty()) {
                    Text(
                        text = "No notes yet. Please add your first note!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray,
                        modifier = Modifier.padding(32.dp)
                    )
                } else {
                    LazyColumn(
                        state = lazyListState,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(color = Color(0xFFE3E3E3)),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            horizontal = 0.dp,
                            vertical = 8.dp
                        ),
                    ) {
                        items(viewModel.memos, key = { it.id }) { memo ->
                            ReorderableItem(reorderableLazyListState, key = memo.id) {isDragging ->
                                val elevation by animateDpAsState(if (isDragging) 4.dp else 0.dp)
                                Surface(shadowElevation = elevation) {
                                    MemoItem(
                                        modifier = Modifier.longPressDraggableHandle(
                                            onDragStarted = {
                                                hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
                                            },
                                            onDragStopped = {
                                                hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureEnd)
                                            }
                                        ),
                                        memo = memo,
                                        viewModel = viewModel,
                                        onEditClick = {
                                            onNavigateToAddMemo?.invoke(memo.id)
                                        }
                                    )
                                }

                            }

                        }
                    }
                }

            }
        }

    }
}

@Composable
private fun MemoItem(
    modifier: Modifier = Modifier,
    memo: Memo,
    viewModel: MemoViewModel,
    onEditClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val dateTimeFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(color = Color.White)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        RadioButton(
            selected = memo.isCompleted,
            onClick = { viewModel.toggleMemoCompletion(memo) },
            colors = RadioButtonDefaults.colors(
                selectedColor = Color(0xFF4CAF50),
                unselectedColor = Color.Gray
            )
        )

        Column(
            modifier = Modifier.weight(1f)
        ) {
            // Memo content
            Text(
                text = memo.content,
                style = MaterialTheme.typography.bodyLarge,
                textDecoration = if (memo.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                color = Color.Gray,
                fontSize = 16.sp
            )

            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
            )

            // Due Date
            memo.dueDate?.let { dueDate ->
                Box(
                    modifier = Modifier
                        .border(
                            width = 0.5.dp,
                            color = Color.LightGray,
                            shape = RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = dateFormat.format(dueDate),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF4CAF50),
                        fontSize = 14.sp
                    )
                }
            }

            // Images
            if (memo.imageUris.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(memo.imageUris.size) { index ->
                        val uri = memo.imageUris[index]
                        AsyncImage(
                            model = android.net.Uri.parse(uri),
                            contentDescription = "Memo image",
                            modifier = Modifier
                                .size(60.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }


        // Action buttons
        IconButton(
            onClick = {
                viewModel.startEditing(memo)
                onEditClick()
            }
        ) {
            Icon(
                Icons.Outlined.Edit,
                contentDescription = "Edit",
                tint = Color.Gray
            )
        }

        Spacer(
            modifier = Modifier
                .width(1.dp)
                .height(24.dp)
                .background(color = Color.LightGray)
        )

        IconButton(
            onClick = {
                viewModel.deleteMemo(memo)
            }
        ) {
            Icon(
                Icons.Outlined.Delete,
                contentDescription = "Delete",
                tint = Color.Gray
            )
        }
    }
}
