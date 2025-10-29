package com.nvd.demo_list.screens.memo

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.nvd.demo_list.utils.ImageManager
import java.io.File


@Composable
fun TakePictureScreen() {
    val context = LocalContext.current
    val imageManager = remember { ImageManager(context) }
    var images by remember { mutableStateOf<List<File>>(emptyList()) }
    var currentImageUri by remember { mutableStateOf<Uri?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Load existing images - sorted by capture time (newest first)
    LaunchedEffect(Unit) {
        images = imageManager.getAllImages() // Already sorted by lastModified desc
    }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            images = imageManager.getAllImages()
            Toast.makeText(context, "Image has been saved!", Toast.LENGTH_SHORT).show()
        }
    }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val imageFile = imageManager.createImageFile()
            currentImageUri = imageManager.getImageUri(imageFile)
            currentImageUri?.let { uri ->
                cameraLauncher.launch(uri)
            }
        } else {
            Toast.makeText(context, "Camera permission is required to take photos.", Toast.LENGTH_SHORT).show()
        }
    }

    fun proceedWithTakePicture() {
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) -> {
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

    fun takePicture() {
        // Check if there are 3 images already
        if (images.size >= 3) {
            // Show dialog to confirm deleting oldest image
            showDeleteDialog = true
        } else {
            // Proceed with taking new picture
            proceedWithTakePicture()
        }
    }


    fun deleteOldestImageAndTakePicture() {
        // Images are sorted by lastModified desc (newest first)
        // So oldest is the last item
        val oldestImage = images.lastOrNull()
        if (oldestImage != null && imageManager.deleteImage(oldestImage)) {
            images = imageManager.getAllImages()
            Toast.makeText(context, "Oldest image has been deleted.", Toast.LENGTH_SHORT).show()
            // Proceed with taking new picture
            proceedWithTakePicture()
        }
    }
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { takePicture() },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Take Picture"
                )
            }
        }
    ) { paddingValues ->
        // Confirmation Dialog
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = {
                    Text(text = "Confirm delete photo")
                },
                text = {
                    Text(text = "You already have 3 photos. Do you want to delete the oldest one to take a new photo?")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showDeleteDialog = false
                            deleteOldestImageAndTakePicture()
                        }
                    ) {
                        Text("Delete & retake")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDeleteDialog = false }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            if (images.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Take Picture",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.Black
                        )
                        Text(
                            text = "Take a photo and save it to a memo",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray
                        )
                        Text(
                            text = "No photos yet. Press the camera button to take your first photo!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(images) { imageFile ->
                        ImageCard(
                            imageFile = imageFile,
                            onDelete = {
                                if (imageManager.deleteImage(imageFile)) {
                                    images = imageManager.getAllImages()
                                    Toast.makeText(context, "Photo deleted", Toast.LENGTH_SHORT).show()
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
private fun ImageCard(
    imageFile: File,
    onDelete: () -> Unit
) {
    Box {
        AsyncImage(
            model = imageFile,
            contentDescription = "Captured Image",
            modifier = Modifier
                .fillMaxSize()
                .aspectRatio(16 / 9F)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )

        IconButton(
            onClick = onDelete,
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete",
                tint = Color.Red
            )
        }
    }

}

