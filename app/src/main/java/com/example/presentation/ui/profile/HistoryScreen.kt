package com.example.presentation.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.domain.model.Video
import com.example.domain.repository.UserRepository
import com.example.presentation.components.shimmerEffect
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    userRepository: UserRepository,
    onBack: () -> Unit,
    onVideoClick: (Video) -> Unit
) {
    var history by remember { mutableStateOf<List<Video>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        // Assume UserRepository limits to 5 months if implemented in backend, or we could filter here
        val videos = userRepository.getWatchHistory()
        
        // Filter out videos older than 5 months
        val fiveMonthsAgo = Calendar.getInstance().apply { add(Calendar.MONTH, -5) }.timeInMillis
        history = videos.filter { it.uploadDate > fiveMonthsAgo } // assuming uploadDate represents interaction or we just use it for sorting
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Watch History", fontWeight = FontWeight.Bold) },
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
        } else if (history.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No watch history.", color = Color.Gray)
            }
        } else {
            // Group by date
            // Note: Currently Video model doesn't store 'watchedAt'. 
            // We'll simulate 'Today', 'Yesterday' grouping based on `uploadDate` as a fallback.
            // A real app would use the actual `watchedAt` timestamp from WatchHistoryDto.
            
            val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            val todayDate = sdf.format(Date())
            val yesterdayDate = sdf.format(Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }.time)
            
            val groupedHistory = history.groupBy { video ->
                val dateStr = sdf.format(Date(video.uploadDate))
                when (dateStr) {
                    todayDate -> "Today"
                    yesterdayDate -> "Yesterday"
                    else -> dateStr
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                groupedHistory.forEach { (dateGroup, videosInGroup) ->
                    item(span = { GridItemSpan(3) }) {
                        Text(
                            text = dateGroup,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                        )
                    }

                    // Render videos. Longs take full width, shorts take 1 cell
                    val longs = videosInGroup.filter { !it.isShort }
                    val shorts = videosInGroup.filter { it.isShort }

                    // Long videos
                    items(longs, span = { GridItemSpan(3) }) { video ->
                        LongVideoItem(video, onClick = { onVideoClick(video) })
                    }

                    // Shorts
                    items(shorts, span = { GridItemSpan(1) }) { video ->
                        ShortVideoItem(video, onClick = { onVideoClick(video) })
                    }
                }
            }
        }
    }
}

@Composable
fun LongVideoItem(video: Video, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .width(160.dp)
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.DarkGray)
        ) {
            AsyncImage(
                model = video.thumbnailUrl.ifEmpty { video.url },
                contentDescription = video.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = video.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = video.creatorName,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun ShortVideoItem(video: Video, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .aspectRatio(9f / 16f)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.DarkGray)
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = video.thumbnailUrl.ifEmpty { video.url },
            contentDescription = video.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.5f))
                .padding(4.dp)
        ) {
            Text(
                text = video.title,
                color = Color.White,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
