package com.example.presentation.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.Video
import com.example.domain.repository.VideoRepository
import com.example.presentation.components.VideoPlayer
import kotlinx.coroutines.launch

@Composable
fun VideoDetailsScreen(
    videoId: String,
    videoRepository: VideoRepository,
    downloadRepository: com.example.domain.repository.DownloadRepository,
    onBack: () -> Unit
) {
    // In a real app we'd fetch the exact video, for now we find it dynamically in feed
    val feed by videoRepository.getHomeFeed(false).collectAsState(initial = emptyList())
    // Let's also check downloads so we can play local videos
    val downloads by downloadRepository.getAllDownloadedVideos().collectAsState(initial = emptyList())
    
    val video = downloads.find { it.id == videoId } ?: feed.find { it.id == videoId }
    val scope = rememberCoroutineScope()
    var isDownloaded by remember { mutableStateOf(false) }
    var isDownloading by remember { mutableStateOf(false) }

    LaunchedEffect(video) {
        if (video != null) {
            isDownloaded = downloadRepository.isVideoDownloaded(video.id)
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Video Player Header Area
        Box(modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f).background(Color.Black)) {
            if (video != null) {
                // Play local if downloaded, otherwise stream
                val playUrl = video.localVideoPath ?: video.url
                VideoPlayer(videoUrl = playUrl, modifier = Modifier.fillMaxSize())
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.White)
                }
            }
            
            IconButton(
                onClick = onBack, 
                modifier = Modifier.align(Alignment.TopStart).padding(8.dp)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            
            Row(modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)) {
                IconButton(onClick = {}) {
                    Icon(Icons.Default.ScreenRotation, contentDescription = "Rotate", tint = Color.White)
                }
                IconButton(onClick = {}) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
                }
            }
        }

        LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
            if (video != null) {
                item {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(video.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("${video.views} views", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Creator Info
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.Gray))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(video.creatorName, fontWeight = FontWeight.Bold)
                            }
                            Button(onClick = {}) {
                                Text("Follow")
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Buttons Horizontal Scroll
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            ActionButton(icon = Icons.Default.ThumbUp, label = "Like", onClick = {})
                            ActionButton(icon = Icons.Default.ThumbDown, label = "Dislike", onClick = {})
                            ActionButton(icon = Icons.Default.Share, label = "Share", onClick = {})
                            
                            // Only show download for long videos (VideoDetailsScreen is for long videos, but checking just in case)
                            if (!video.isShort) {
                                if (isDownloading) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                } else {
                                    ActionButton(
                                        icon = if (isDownloaded) Icons.Default.DownloadDone else Icons.Default.Download,
                                        label = if (isDownloaded) "Downloaded" else "Download",
                                        onClick = {
                                            if (!isDownloaded) {
                                                isDownloading = true
                                                kotlinx.coroutines.GlobalScope.launch {
                                                    val result = downloadRepository.downloadVideo(video)
                                                    isDownloading = false
                                                    if (result.isSuccess) {
                                                        isDownloaded = true
                                                    }
                                                }
                                            } else {
                                                // Optional: Remove download
                                                kotlinx.coroutines.GlobalScope.launch {
                                                    downloadRepository.deleteDownloadedVideo(video.id)
                                                    isDownloaded = false
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                            
                            ActionButton(icon = Icons.Default.Bookmark, label = "Save", onClick = {})
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Divider()
                        
                        // Comments Preview
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Comments ${video.commentsCount}", fontWeight = FontWeight.Bold)
                                Text("View comments", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Expand Comments")
                        }
                        Divider()
                    }
                }
            }
            
            item {
                Text(
                    "Related Videos",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleMedium
                )
            }
            
            items(feed.size) { index ->
                val recommendedVideo = feed[index]
                if (video == null || recommendedVideo.id != video.id) {
                    LongVideoCard(recommendedVideo)
                }
            }
        }
    }
}

@Composable
fun ActionButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable(onClick = onClick).padding(8.dp)) {
        Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.height(4.dp))
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
    }
}
