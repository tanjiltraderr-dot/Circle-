package com.example.presentation.ui.upload

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.example.domain.repository.UploadRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadDetailsScreen(
    uri: Uri,
    uploadRepository: UploadRepository,
    onUploadComplete: () -> Unit,
    onBack: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isUploading by remember { mutableStateOf(false) }
    
    // Privacy Toggles
    var preventDownloads by remember { mutableStateOf(false) }
    var allowComments by remember { mutableStateOf(true) }
    var showLikeCount by remember { mutableStateOf(true) }

    // Navigation state
    var showDescriptionEditor by remember { mutableStateOf(false) }
    var showThumbnailPicker by remember { mutableStateOf(false) }

    // Thumbnail state
    var customThumbnailUri by remember { mutableStateOf<Uri?>(null) }
    var thumbnailTimestamp by remember { mutableLongStateOf(0L) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(uri))
            prepare()
            playWhenReady = false
        }
    }

    DisposableEffect(Unit) {
        onDispose { exoPlayer.release() }
    }

    // Hashtag parser
    val hashtagRegex = Regex("#\\\\w+")
    val activeHashtags = hashtagRegex.findAll(title).map { it.value }.toList()

    if (showDescriptionEditor) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Add Description") },
                    navigationIcon = {
                        IconButton(onClick = { showDescriptionEditor = false }) {
                            Icon(Icons.Default.ArrowBack, "Back")
                        }
                    },
                    actions = {
                        TextButton(onClick = { showDescriptionEditor = false }) {
                            Text("Done", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                )
            }
        ) { padding ->
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                placeholder = { Text("Write your description here...") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                )
            )
        }
        return
    }

    if (showThumbnailPicker) {
        val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { selectedUri ->
            if (selectedUri != null) {
                customThumbnailUri = selectedUri
                showThumbnailPicker = false
            }
        }
        
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Choose Thumbnail") },
                    navigationIcon = {
                        IconButton(onClick = { showThumbnailPicker = false }) {
                            Icon(Icons.Default.ArrowBack, "Back")
                        }
                    },
                    actions = {
                        TextButton(onClick = { 
                            thumbnailTimestamp = exoPlayer.currentPosition
                            customThumbnailUri = null
                            showThumbnailPicker = false 
                        }) {
                            Text("Select Frame", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AndroidView(
                    factory = {
                        PlayerView(context).apply {
                            player = exoPlayer
                            useController = true
                            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { galleryLauncher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Image, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Upload from Gallery")
                }
            }
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Shorts Post") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                // Thumbnail preview
                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .height(140.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.DarkGray)
                        .clickable { showThumbnailPicker = true },
                    contentAlignment = Alignment.Center
                ) {
                    if (customThumbnailUri != null) {
                        AsyncImage(
                            model = customThumbnailUri,
                            contentDescription = "Thumbnail",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        AndroidView(
                            factory = {
                                PlayerView(context).apply {
                                    player = exoPlayer
                                    useController = false
                                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.3f))
                    )
                    Text("Edit\nThumbnail", color = Color.White, style = MaterialTheme.typography.labelSmall)
                }

                // Title Input
                Column(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        placeholder = { Text("Add a title with #hashtags") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        maxLines = 5
                    )
                }
            }
            
            if (activeHashtags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(activeHashtags) { tag ->
                        AssistChip(
                            onClick = { },
                            label = { Text(tag, color = MaterialTheme.colorScheme.primary) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Divider()
            Spacer(modifier = Modifier.height(8.dp))

            // Description Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDescriptionEditor = true }
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Description", fontWeight = FontWeight.Bold)
                    if (description.isNotBlank()) {
                        Text(
                            description,
                            maxLines = 1,
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodySmall
                        )
                    } else {
                        Text("Add more details", color = Color.Gray)
                    }
                }
                Icon(Icons.Default.ChevronRight, contentDescription = "Edit Description")
            }

            Divider()
            Spacer(modifier = Modifier.height(16.dp))
            Text("Advanced Settings", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))

            // Privacy Toggles
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Allow Comments")
                Switch(checked = allowComments, onCheckedChange = { allowComments = it })
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Show Like Count")
                Switch(checked = showLikeCount, onCheckedChange = { showLikeCount = it })
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Prevent Downloads")
                    Text("Protect video from being downloaded", color = Color.Gray, style = MaterialTheme.typography.labelSmall)
                }
                Switch(checked = preventDownloads, onCheckedChange = { preventDownloads = it })
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    isUploading = true
                    scope.launch {
                        val fileName = "upload_${System.currentTimeMillis()}.mp4"
                        val mimeType = context.contentResolver.getType(uri) ?: "video/mp4"
                        
                        // Proceed to upload via the existing hook
                        val result = uploadRepository.uploadFile(uri, fileName, mimeType)
                        isUploading = false
                        
                        if (result.isSuccess) {
                            Toast.makeText(context, "Upload successful!", Toast.LENGTH_SHORT).show()
                            onUploadComplete()
                        } else {
                            Toast.makeText(context, "Upload failed: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !isUploading && title.isNotBlank()
            ) {
                if (isUploading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Uploading...")
                } else {
                    Text("Post Shorts")
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
