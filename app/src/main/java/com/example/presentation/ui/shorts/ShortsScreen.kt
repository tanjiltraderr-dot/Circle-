package com.example.presentation.ui.shorts

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.Video
import com.example.domain.repository.VideoRepository
import com.example.presentation.components.VideoPlayer
import com.example.presentation.components.shimmerEffect

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ShortsScreen(videoRepository: VideoRepository, initialVideoId: String? = null) {
    val shorts by videoRepository.getHomeFeed(isShort = true).collectAsState(initial = emptyList())
    
    // We want to set the initial page when shorts load.
    var initialPageSet by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    var initialPage by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(0) }
    
    // Find index when shorts load
    androidx.compose.runtime.LaunchedEffect(shorts, initialVideoId) {
        if (!initialPageSet && shorts.isNotEmpty()) {
            if (initialVideoId != null) {
                val index = shorts.indexOfFirst { it.id == initialVideoId }
                if (index != -1) {
                    initialPage = index
                }
            }
            initialPageSet = true
        }
    }

    if (!initialPageSet) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            // Shimmer loading UI for Shorts
            Box(modifier = Modifier.fillMaxSize().shimmerEffect())
            
            // Top App Bar Skeleton
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.3f)))
                Box(modifier = Modifier.width(120.dp).height(20.dp).clip(androidx.compose.foundation.shape.RoundedCornerShape(4.dp)).background(Color.White.copy(alpha = 0.3f)))
                Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.3f)))
            }
            
            // Right column actions skeleton
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 100.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                repeat(5) {
                    Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.3f)))
                }
            }
            
            // Bottom info skeleton
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth(0.75f)
                    .padding(start = 16.dp, bottom = 100.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.3f)))
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(modifier = Modifier.width(100.dp).height(16.dp).clip(androidx.compose.foundation.shape.RoundedCornerShape(4.dp)).background(Color.White.copy(alpha = 0.3f)))
                }
                Spacer(modifier = Modifier.height(12.dp))
                Box(modifier = Modifier.fillMaxWidth().height(16.dp).clip(androidx.compose.foundation.shape.RoundedCornerShape(4.dp)).background(Color.White.copy(alpha = 0.3f)))
                Spacer(modifier = Modifier.height(8.dp))
                Box(modifier = Modifier.width(150.dp).height(16.dp).clip(androidx.compose.foundation.shape.RoundedCornerShape(4.dp)).background(Color.White.copy(alpha = 0.3f)))
            }
        }
        return
    }

    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { shorts.size })

    Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
        if (shorts.isNotEmpty()) {
            VerticalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val video = shorts[page]
                Box(modifier = Modifier.fillMaxSize()) {
                    VideoPlayer(videoUrl = video.url, modifier = Modifier.fillMaxSize())
                    
                    // Top App Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { /* Back */ }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                        Row {
                            Text("Following", color = Color.White.copy(alpha = 0.7f), fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 16.dp))
                            Text("For You", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        IconButton(onClick = { /* Search */ }) {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White)
                        }
                    }

                    // Right column actions
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 16.dp, bottom = 100.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        ActionItem(icon = Icons.Default.FavoriteBorder, label = "${video.likes}")
                        ActionItem(icon = Icons.Outlined.ChatBubbleOutline, label = "${video.commentsCount}")
                        ActionItem(icon = Icons.Outlined.BookmarkBorder, label = "Save")
                        ActionItem(icon = Icons.Outlined.Share, label = "Share")
                        ActionItem(icon = Icons.Default.MoreVert, label = "")
                    }

                    // Bottom info
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth(0.75f)
                            .padding(start = 16.dp, bottom = 100.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.Gray))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(video.creatorName, color = Color.White, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { /* Follow */ },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                modifier = Modifier.height(28.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                            ) {
                                Text("Follow", color = Color.White, fontSize = 12.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(video.title, color = Color.White, maxLines = 2)
                        Text("#shorts #trending", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
             Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                 Text("No content available", color = Color.White, style = MaterialTheme.typography.bodyLarge)
             }
        }
    }
}

@Composable
fun ActionItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
        if (label.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(label, color = Color.White, fontSize = 12.sp)
        }
    }
}

