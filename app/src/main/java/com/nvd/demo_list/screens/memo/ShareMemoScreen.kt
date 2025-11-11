package com.nvd.demo_list.screens.memo

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.core.net.toUri

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareMemoScreen(
    viewModel: MemoViewModel,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    var selectedMemoIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    val allSelected = selectedMemoIds.size == viewModel.memos.size && viewModel.memos.isNotEmpty()
    
    // Calculate total images count for selected memos (recomputed when selection changes)
    val totalImagesCount = remember(selectedMemoIds) {
        viewModel.memos
            .filter { selectedMemoIds.contains(it.id) }
            .sumOf { it.imageUris.size }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Share",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            if (allSelected) {
                                selectedMemoIds = emptySet()
                            } else {
                                selectedMemoIds = viewModel.memos.map { it.id }.toSet()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = if (allSelected) MaterialTheme.colorScheme.primary else Color.Gray
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (allSelected) "Deselect All" else "Select All",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (allSelected) MaterialTheme.colorScheme.primary else Color.Gray
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                Button(
                    onClick = {
                        if (selectedMemoIds.isNotEmpty()) {
                            val selectedMemos = viewModel.memos.filter { selectedMemoIds.contains(it.id) }
                            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            val shareText = selectedMemos.joinToString("\n") { memo ->
                                memo.dueDate?.let { date ->
                                    "${memo.content} - ${dateFormat.format(date)}"
                                } ?: memo.content
                            }
                            
                            // Collect all image URIs from selected memos
                            val imageUris = selectedMemos.flatMap { it.imageUris }
                                .mapNotNull { uriString ->
                                    try {
                                        uriString.toUri()
                                    } catch (e: Exception) {
                                        null
                                    }
                                }
                            
                            if (imageUris.isNotEmpty()) {
                                // Share with images and text
                                val shareIntent = Intent().apply {
                                    action = Intent.ACTION_SEND_MULTIPLE
                                    type = "image/*"
                                    putExtra(
                                        Intent.EXTRA_STREAM,
                                        ArrayList(imageUris)
                                    )
                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share memos"))
                            } else {
                                // Share only text
                                val shareIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share memos"))
                            }
                        } else {
                            Toast.makeText(
                                context,
                                "Please select at least one memo",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = selectedMemoIds.isNotEmpty(),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2196F3),
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (totalImagesCount > 0) {
                            "Share Selected (${selectedMemoIds.size} memos, $totalImagesCount images)"
                        } else {
                            "Share Selected (${selectedMemoIds.size})"
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
        ) {
            if (viewModel.memos.isEmpty()) {
                Text(
                    text = "No memos to share",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(32.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = Color(0xFFE3E3E3)),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(
                        start = 0.dp,
                        end = 0.dp,
                        top = 8.dp,
                        bottom = 80.dp
                    ),
                ) {
                    items(viewModel.memos, key = { it.id }) { memo ->
                        ShareMemoItem(
                            memo = memo,
                            isSelected = selectedMemoIds.contains(memo.id),
                            onSelectionChanged = { isSelected ->
                                selectedMemoIds = if (isSelected) {
                                    selectedMemoIds + memo.id
                                } else {
                                    selectedMemoIds - memo.id
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ShareMemoItem(
    memo: Memo,
    isSelected: Boolean,
    onSelectionChanged: (Boolean) -> Unit
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = Color.White)
            .padding(vertical = 8.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
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
                            color = Color.Red,
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

        Spacer(modifier = Modifier.width(12.dp))

        Checkbox(
            checked = isSelected,
            onCheckedChange = onSelectionChanged
        )
    }
}

