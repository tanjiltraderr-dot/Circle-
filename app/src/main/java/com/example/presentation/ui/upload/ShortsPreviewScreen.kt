package com.example.presentation.ui.upload

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView

// Tracks user edits for final "hard-baking" upload process
data class VideoEditState(
    val textOverlays: List<String> = emptyList(),
    val selectedFilter: String? = null,
    val stickers: List<String> = emptyList()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShortsPreviewScreen(
    uri: Uri,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(uri))
            repeatMode = Player.REPEAT_MODE_ONE
            prepare()
            playWhenReady = true
        }
    }

    DisposableEffect(Unit) {
        onDispose { exoPlayer.release() }
    }

    var editState by remember { mutableStateOf(VideoEditState()) }
    var showTextInput by remember { mutableStateOf(false) }
    var currentTextInput by remember { mutableStateOf("") }
    var showStickers by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
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

        // Simulated Filter Overlay tracked in state
        if (editState.selectedFilter != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x33FF0000))
            )
        }

        // Render Text Overlays
        editState.textOverlays.forEachIndexed { index, text ->
            Text(
                text = text,
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = (index * 40).dp)
                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            )
        }

        // Render Stickers
        editState.stickers.forEachIndexed { index, sticker ->
            Text(
                text = sticker,
                fontSize = 64.sp,
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(x = (-50 + index * 50).dp, y = (-100).dp)
            )
        }

        // Right Sidebar (Editing Tools)
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            IconButton(
                onClick = { 
                    editState = editState.copy(
                        selectedFilter = if (editState.selectedFilter == null) "Vintage" else null
                    ) 
                },
                modifier = Modifier.clip(CircleShape).background(Color.Black.copy(alpha = 0.4f))
            ) {
                Icon(Icons.Default.ColorLens, "Filters", tint = Color.White)
            }
            
            IconButton(
                onClick = { showStickers = true },
                modifier = Modifier.clip(CircleShape).background(Color.Black.copy(alpha = 0.4f))
            ) {
                Icon(Icons.Default.EmojiEmotions, "Stickers", tint = Color.White)
            }
            
            IconButton(
                onClick = { showTextInput = true },
                modifier = Modifier.clip(CircleShape).background(Color.Black.copy(alpha = 0.4f))
            ) {
                Icon(Icons.Default.TextFields, "Text", tint = Color.White)
            }
        }

        // Bottom Controls
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray.copy(alpha = 0.7f))
            ) {
                Text("Back", color = Color.White)
            }
            
            Button(
                onClick = {
                    // Send to next phase where state is baked via backend/transformer pipeline
                    onNext()
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Upload", color = MaterialTheme.colorScheme.onPrimary)
            }
        }

        // Text Input Overlay
        if (showTextInput) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    OutlinedTextField(
                        value = currentTextInput,
                        onValueChange = { currentTextInput = it },
                        textStyle = LocalTextStyle.current.copy(color = Color.White, fontSize = 24.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        placeholder = { Text("Type something...", color = Color.Gray) }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = {
                        if (currentTextInput.isNotBlank()) {
                            editState = editState.copy(textOverlays = editState.textOverlays + currentTextInput)
                            currentTextInput = ""
                        }
                        showTextInput = false
                    }) {
                        Text("Done")
                    }
                }
            }
        }

        // Stickers Bottom Sheet
        if (showStickers) {
            ModalBottomSheet(onDismissRequest = { showStickers = false }) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Stickers", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        listOf("🔥", "😂", "✨", "❤️").forEach { emoji ->
                            Text(
                                text = emoji,
                                fontSize = 48.sp,
                                modifier = Modifier.clickable {
                                    editState = editState.copy(stickers = editState.stickers + emoji)
                                    showStickers = false
                                }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}
