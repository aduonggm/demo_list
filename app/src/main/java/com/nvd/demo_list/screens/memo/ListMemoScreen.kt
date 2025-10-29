package com.nvd.demo_list.screens.memo

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListMemoScreen() {
    val context = LocalContext.current
    val viewModel = remember { MemoViewModel(context) }
    var memoText by remember { mutableStateOf("") }
    var editingText by remember { mutableStateOf("") }
    var isAddMemoFocused by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf<Long?>(null) }

    val datePickerState = rememberDatePickerState()

    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current

    val lazyListState = rememberLazyListState()
    val hapticFeedback = LocalHapticFeedback.current

    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        // Update the list
        viewModel.reorderMemo(from.index, to.index)
    }

    // Auto-focus text field when date picker closes and input is focused
    LaunchedEffect(showDatePicker) {
        if (!showDatePicker && isAddMemoFocused) {
            delay(200)
            focusRequester.requestFocus()
        }
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
            // Add memo input
            AddMemoInput(
                value = memoText,
                onValueChange = { memoText = it },
                onAddMemo = {
                    if (viewModel.isEditing && viewModel.editingMemo != null) {
                        // Update existing memo
                        val editingMemo = viewModel.editingMemo
                        val dueDate = if (selectedDate != null) Date(selectedDate!!) else null
                        editingMemo?.let { viewModel.updateMemoWithDate(it, memoText, dueDate) }
                        viewModel.cancelEditing()
                    } else {
                        // Add new memo
                        val dueDate = if (selectedDate != null) Date(selectedDate!!) else null
                        viewModel.addMemo(memoText, dueDate)
                    }
                    // Reset everything after saving
                    memoText = ""
                    selectedDate = null
                    isAddMemoFocused = false
                    focusManager.clearFocus()
                },
                onFocusChange = { isAddMemoFocused = it },
                onDatePickerClick = { showDatePicker = true },
                focusRequester = focusRequester,
                selectedDate = selectedDate,
                onRemoveDate = { selectedDate = null }
            )

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
                                            memoText = memo.content
                                            selectedDate = memo.dueDate?.time
                                            isAddMemoFocused = true
                                            focusRequester.requestFocus()
                                        }
                                    )
                                }

                            }

                        }
                    }
                }

                // Backdrop - only covers memo list when input is focused
                if (isAddMemoFocused) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(Color.Black.copy(alpha = 0.5f))
                            .clickable {
                                isAddMemoFocused = false
                                focusManager.clearFocus()
                                // Reset input, selected date, and editing state when clearing focus
                                memoText = ""
                                selectedDate = null
                                if (viewModel.isEditing) {
                                    viewModel.cancelEditing()
                                }
                            }
                    )
                }
            }
        }

        // DatePicker Dialog
        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            selectedDate = datePickerState.selectedDateMillis
                            showDatePicker = false
                            keyboard?.show()
                        }
                    ) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showDatePicker = false
                            keyboard?.show()
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            ) {
                DatePicker(
                    state = datePickerState,
                    colors = DatePickerDefaults.colors(
                        containerColor = Color.White
                    )
                )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddMemoInput(
    value: String,
    onValueChange: (String) -> Unit,
    onAddMemo: () -> Unit,
    onFocusChange: (Boolean) -> Unit,
    onDatePickerClick: () -> Unit,
    focusRequester: FocusRequester,
    selectedDate: Long?,
    onRemoveDate: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused = interactionSource.collectIsFocusedAsState().value

    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    // Update focus state when it changes
    LaunchedEffect(isFocused) {
        onFocusChange(isFocused)
    }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Color.White)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Add",
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                interactionSource = interactionSource,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                placeholder = {
                    Text(
                        "Add new memo...",
                        color = Color.LightGray
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    disabledContainerColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (value.isNotBlank()) {
                            onAddMemo()
                        }
                    }
                )
            )
        }

        // Date picker button and selected date - only show when focused
        if (isFocused) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Icon(
                    Icons.Default.DateRange,
                    contentDescription = "Add",
                    modifier = Modifier.size(20.dp),
                    tint = Color(0xFF2991FF)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Date selection button - shows selected date or "Chọn ngày"
                OutlinedButton(
                    onClick = onDatePickerClick,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(0.5.dp, Color.LightGray),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF4CAF50)
                    ),
                    contentPadding = PaddingValues(vertical = 0.dp, horizontal = 8.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text(
                        text = if (selectedDate != null)
                            dateFormatter.format(Date(selectedDate))
                        else
                            "Select date",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }

                // Remove button - only show when date is selected
                if (selectedDate != null) {
                    TextButton(
                        onClick = onRemoveDate,
                        contentPadding = PaddingValues(vertical = 0.dp, horizontal = 8.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text(
                            "Delete",
                            color = Color(0xFFFF9628)
                        )
                    }
                }
            }
        }
    }
}