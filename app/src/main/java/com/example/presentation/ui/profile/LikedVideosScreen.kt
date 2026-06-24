package com.example.presentation.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.clip
import com.example.domain.model.Video
import com.example.domain.repository.UserRepository
import com.example.presentation.components.shimmerEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LikedVideosScreen(
    userRepository: UserRepository,
    onBack: () -> Unit,
    onVideoClick: (Video) -> Unit
) {
    var videos by remember { mutableStateOf<List<Video>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        videos = userRepository.getLikedVideos()
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Liked Videos", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                repeat(4) {
                    Box(modifier = Modifier.fillMaxWidth().height(100.dp).padding(vertical = 8.dp).clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp)).shimmerEffect())
                }
            }
        } else if (videos.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No liked videos.", color = Color.Gray)
            }
        } else {
            val longs = videos.filter { !it.isShort }
            val shorts = videos.filter { it.isShort }

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                if (longs.isNotEmpty()) {
                    item(span = { GridItemSpan(3) }) {
                        Text("Videos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                    }
                    items(longs, span = { GridItemSpan(3) }) { video ->
                        LongVideoItem(video, onClick = { onVideoClick(video) })
                    }
                }

                if (shorts.isNotEmpty()) {
                    item(span = { GridItemSpan(3) }) {
                        Text("Shorts", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
                    }
                    items(shorts, span = { GridItemSpan(1) }) { video ->
                        ShortVideoItem(video, onClick = { onVideoClick(video) })
                    }
                }
            }
        }
    }
}
