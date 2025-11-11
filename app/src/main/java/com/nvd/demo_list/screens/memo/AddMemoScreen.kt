package com.nvd.demo_list.screens.memo

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.rounded.AccountBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.nvd.demo_list.utils.getAllCroppedImages
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMemoScreen(
    viewModel: MemoViewModel,
    onBackClick: () -> Unit,
    editingMemo: Memo? = null
) {
    val context = LocalContext.current
    var memoText by remember { mutableStateOf(editingMemo?.content ?: "") }
    var selectedDate by remember { mutableStateOf<Long?>(editingMemo?.dueDate?.time) }
    var selectedImageUris by remember {
        mutableStateOf<List<String>>(
            editingMemo?.imageUris ?: emptyList()
        )
    }
    var showDatePicker by remember { mutableStateOf(false) }
    var showImageBottomSheet by remember { mutableStateOf(false) }
    var showCroppedImagesBottomSheet by remember { mutableStateOf(false) }
    var croppedImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var selectedCroppedImages by remember { mutableStateOf<Set<Uri>>(emptySet()) }

    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDate)
    val bottomSheetState = rememberModalBottomSheetState()
    val croppedImagesBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Load cropped images when bottom sheet is shown
    LaunchedEffect(showCroppedImagesBottomSheet) {
        if (showCroppedImagesBottomSheet) {
            croppedImageUris = getAllCroppedImages(context)
            selectedCroppedImages = emptySet() // Reset selection when opening
            // Force expand to full height
            croppedImagesBottomSheetState.expand()
        }
    }

    // Image picker launcher (multiple images)
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 10)
    ) { uris ->
        val imageManager = com.nvd.demo_list.utils.ImageManager(context)
        val newUris = uris.mapNotNull { uri ->
            // Copy content URI to app storage to ensure persistence
            val persistentUri = imageManager.copyUriToAppStorage(uri)
            persistentUri?.toString()
        }
        selectedImageUris = selectedImageUris + newUris
        showImageBottomSheet = false
    }

    val pickVisualMediaRequest = remember {
        androidx.activity.result.PickVisualMediaRequest.Builder()
            .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly)
            .build()
    }

    var currentImageUri by remember { mutableStateOf<Uri?>(null) }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && currentImageUri != null) {
            selectedImageUris = selectedImageUris + currentImageUri.toString()
            Toast.makeText(context, "Image has been saved!", Toast.LENGTH_SHORT).show()
            showImageBottomSheet = false
        }
    }


    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val imageManager = com.nvd.demo_list.utils.ImageManager(context)
            val imageFile = imageManager.createImageFile()
            currentImageUri = imageManager.getImageUri(imageFile)
            currentImageUri?.let { uri ->
                cameraLauncher.launch(uri)
            }
        } else {
            Toast.makeText(
                context,
                "Camera permission is required to take photos.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun takePicture() {
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) -> {
                val imageManager = com.nvd.demo_list.utils.ImageManager(context)
                val imageFile = imageManager.createImageFile()
                currentImageUri = imageManager.getImageUri(imageFile)
                currentImageUri?.let { uri ->
                    cameraLauncher.launch(uri)
                }
            }

            else -> {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (editingMemo != null) "Edit Memo" else "Add Memo",
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        val scrollState = rememberScrollState()
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
        ) {
            // Scrollable content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Text input
                OutlinedTextField(
                    value = memoText,
                    onValueChange = { memoText = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text("Memo content")
                    },
                    minLines = 1,
                    maxLines = 2,
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color(0xFF4CAF50),
                        unfocusedIndicatorColor = Color.LightGray,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        unfocusedLabelColor = Color.LightGray,
                        focusedLabelColor = Color(0xFF4CAF50)
                    )
                )

                // Date picker button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { showDatePicker = true },
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(0.5.dp, Color.LightGray),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF4CAF50)
                        )
                    ) {
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = "Date",
                            modifier = Modifier.size(18.dp),
                            tint = Color(0xFF2991FF)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (selectedDate != null)
                                dateFormatter.format(Date(selectedDate!!))
                            else
                                "Select date",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                    if (selectedDate != null) {
                        TextButton(
                            onClick = { selectedDate = null }
                        ) {
                            Text(
                                "Remove",
                                color = Color(0xFFFF9628)
                            )
                        }
                    }
                }

                // Image selection section
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Images",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    // Selected images
                    if (selectedImageUris.isNotEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            selectedImageUris.forEachIndexed { index, uri ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(16f / 9f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.LightGray)
                                ) {
                                    AsyncImage(
                                        model = Uri.parse(uri),
                                        contentDescription = "Selected image",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                    IconButton(
                                        onClick = {
                                            selectedImageUris =
                                                selectedImageUris.filterIndexed { i, _ -> i != index }
                                        },
                                        modifier = Modifier.align(Alignment.TopEnd)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Remove",
                                            tint = Color.Red,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Add Image button
                    OutlinedButton(
                        onClick = { showImageBottomSheet = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(0.5.dp, Color.LightGray),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF2991FF)
                        )
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add Image",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Image")
                    }
                }
            }

            // Save button - always visible at bottom
            Button(
                onClick = {
                    if (memoText.isNotBlank()) {
                        val dueDate = if (selectedDate != null) Date(selectedDate!!) else null
                        if (editingMemo != null) {
                            viewModel.updateMemoWithDate(
                                editingMemo,
                                memoText,
                                dueDate,
                                selectedImageUris
                            )
                        } else {
                            viewModel.addMemo(memoText, dueDate, selectedImageUris)
                        }
                        onBackClick()
                    } else {
                        Toast.makeText(context, "Please enter memo content", Toast.LENGTH_SHORT)
                            .show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                )
            ) {
                Text(
                    text = if (editingMemo != null) "Update" else "Save",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
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
                        }
                    ) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDatePicker = false }
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

        // Image Selection Bottom Sheet
        if (showImageBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showImageBottomSheet = false },
                sheetState = bottomSheetState
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 16.dp)
                        .padding(bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Select Image Source",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedButton(
                        onClick = {
                            showImageBottomSheet = false
                            imagePickerLauncher.launch(pickVisualMediaRequest)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(0.5.dp, Color.LightGray),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF2991FF)
                        )
                    ) {
                        Icon(
                            Icons.Rounded.AccountBox,
                            contentDescription = "Gallery",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Gallery")
                    }

                    OutlinedButton(
                        onClick = {
                            showImageBottomSheet = false
                            takePicture()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(0.5.dp, Color.LightGray),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF2991FF)
                        )
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Camera",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Camera")
                    }

                    OutlinedButton(
                        onClick = {
                            showImageBottomSheet = false
                            showCroppedImagesBottomSheet = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(0.5.dp, Color.LightGray),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF2991FF)
                        )
                    ) {
                        Icon(
                            Icons.Default.Menu,
                            contentDescription = "Crop Image",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Crop Image")
                    }
                }
            }
        }

        // Cropped Images Selection Bottom Sheet
        if (showCroppedImagesBottomSheet) {
            val configuration = LocalConfiguration.current
            val halfScreenHeight = (configuration.screenHeightDp / 1.5F).dp
            
            ModalBottomSheet(
                onDismissRequest = { showCroppedImagesBottomSheet = false },
                sheetState = croppedImagesBottomSheetState
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(halfScreenHeight)
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Select Cropped Images",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        if (selectedCroppedImages.isNotEmpty()) {
                            Text(
                                text = "${selectedCroppedImages.size} selected",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF2991FF)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    if (croppedImageUris.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No cropped images available",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentPadding = PaddingValues(bottom = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(croppedImageUris) { uri ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            selectedCroppedImages = if (selectedCroppedImages.contains(uri)) {
                                                selectedCroppedImages - uri
                                            } else {
                                                selectedCroppedImages + uri
                                            }
                                        }
                                        .background(
                                            if (selectedCroppedImages.contains(uri)) {
                                                Color(0xFF2991FF).copy(alpha = 0.3f)
                                            } else {
                                                Color.LightGray
                                            }
                                        )
                                ) {
                                    AsyncImage(
                                        model = uri,
                                        contentDescription = "Cropped image",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                    if (selectedCroppedImages.contains(uri)) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Selected",
                                            tint = Color(0xFF2991FF),
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(4.dp)
                                                .size(24.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Button(
                        onClick = {
                            val newUris = selectedCroppedImages.map { it.toString() }
                            selectedImageUris = selectedImageUris + newUris
                            showCroppedImagesBottomSheet = false
                            selectedCroppedImages = emptySet()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = selectedCroppedImages.isNotEmpty(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50),
                            disabledContainerColor = Color.LightGray
                        )
                    ) {
                        Text(
                            text = if (selectedCroppedImages.isNotEmpty()) {
                                "Add ${selectedCroppedImages.size} image(s)"
                            } else {
                                "Add Images"
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}


