package com.example.presentation.ui.channel

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
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
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.domain.model.Channel
import com.example.domain.repository.ChannelRepository
import com.example.domain.repository.VideoRepository
import com.example.domain.model.Video
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.filled.Sort
import java.text.SimpleDateFormat
import java.util.Locale

import com.example.presentation.ui.home.LongVideoCard
import com.example.presentation.ui.home.ShortVideoCard
import com.example.presentation.components.shimmerEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChannelProfileScreen(
    channelId: String,
    channelRepository: ChannelRepository,
    videoRepository: VideoRepository,
    onBack: () -> Unit,
    onVideoClick: (Video) -> Unit,
    onEditVideoClick: (String) -> Unit = {}
) {
    var channel by remember { mutableStateOf<Channel?>(null) }
    var currentChannel by remember { mutableStateOf<Channel?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isDescriptionExpanded by remember { mutableStateOf(false) }
    
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Home", "Videos", "Shorts")
    
    var homeVideos by remember { mutableStateOf<List<Video>>(emptyList()) }
    var regularVideos by remember { mutableStateOf<List<Video>>(emptyList()) }
    var shortVideos by remember { mutableStateOf<List<Video>>(emptyList()) }
    
    var videoSortBy by remember { mutableStateOf("Latest") }
    var shortsSortBy by remember { mutableStateOf("Latest") }
    var showVideoSortMenu by remember { mutableStateOf(false) }
    var showShortsSortMenu by remember { mutableStateOf(false) }
    val sortOptions = listOf("Latest", "Oldest", "Popular")

    val context = LocalContext.current

    LaunchedEffect(channelId) {
        channel = channelRepository.getChannel(channelId)
        currentChannel = channelRepository.getUserChannel()
        isLoading = false
    }

    LaunchedEffect(channelId) {
        videoRepository.getChannelHomeFeed(channelId).collect { homeVideos = it }
    }
    
    LaunchedEffect(channelId, videoSortBy) {
        videoRepository.getChannelVideos(channelId, isShort = false, sortBy = videoSortBy).collect { regularVideos = it }
    }

    LaunchedEffect(channelId, shortsSortBy) {
        videoRepository.getChannelVideos(channelId, isShort = true, sortBy = shortsSortBy).collect { shortVideos = it }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        if (isLoading) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.fillMaxWidth().height(180.dp).shimmerEffect())
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-40).dp)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    Box(modifier = Modifier.size(80.dp).clip(CircleShape).shimmerEffect())
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.padding(bottom = 8.dp)) {
                        Box(modifier = Modifier.width(150.dp).height(24.dp).clip(androidx.compose.foundation.shape.RoundedCornerShape(4.dp)).shimmerEffect())
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(modifier = Modifier.width(100.dp).height(16.dp).clip(androidx.compose.foundation.shape.RoundedCornerShape(4.dp)).shimmerEffect())
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
                repeat(3) {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp).padding(16.dp).clip(androidx.compose.foundation.shape.RoundedCornerShape(16.dp)).shimmerEffect())
                }
            }
        } else if (channel == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Channel not found")
            }
        } else {
            val ch = channel!!
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    // Top Layout: Full-width Channel Cover/Banner image.
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .background(Color.DarkGray)
                    ) {
                        if (ch.bannerImageUrl.isNotBlank()) {
                            AsyncImage(
                                model = ch.bannerImageUrl,
                                contentDescription = "Banner",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    // Left-Aligned Profile Avatar overlapping banner
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset(y = (-40).dp)
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(4.dp) // Border effect
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                if (ch.profileImageUrl.isNotBlank()) {
                                    AsyncImage(
                                        model = ch.profileImageUrl,
                                        contentDescription = "Profile",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Text(
                                        ch.name.take(1).uppercase(),
                                        color = Color.White,
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.padding(bottom = 8.dp)) {
                            Text(
                                text = ch.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = ch.handle,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.clickable {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Channel Handle", ch.handle)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Handle copied to clipboard", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }

                    // View Description Layout
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset(y = (-20).dp) // adjust for the avatar overlap offset
                            .padding(horizontal = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isDescriptionExpanded = !isDescriptionExpanded }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Info, contentDescription = "Info", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("View Description", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }

                        if (isDescriptionExpanded) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = ch.description.ifEmpty { "No description provided." },
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Divider()
                                    Spacer(modifier = Modifier.height(16.dp))
                                    
                                    // Removed fake views count
                                    
                                    val dateStr = try {
                                        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                                        val date = inputFormat.parse(ch.createdAt)
                                        val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                                        if (date != null) outputFormat.format(date) else ch.createdAt
                                    } catch (e: Exception) {
                                        ch.createdAt
                                    }
                                    Text("Created: $dateStr", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    val shareLink = "https://circle.com/channel/${ch.id}"
                                    Text(
                                        text = "Shareable Link: $shareLink",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.clickable {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            val clip = ClipData.newPlainText("Channel Link", shareLink)
                                            clipboard.setPrimaryClip(clip)
                                            Toast.makeText(context, "Link copied to clipboard", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                
                item {
                    TabRow(
                        selectedTabIndex = selectedTabIndex,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTabIndex == index,
                                onClick = { selectedTabIndex = index },
                                text = { Text(title, fontWeight = FontWeight.Bold) }
                            )
                        }
                    }
                }
                
                when (selectedTabIndex) {
                    0 -> { // Home
                        if (homeVideos.isEmpty()) {
                            item { Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { Text("No content yet.", color = Color.Gray) } }
                        } else {
                            items(homeVideos) { video ->
                                if (video.isShort) {
                                    Box(
                                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        ShortVideoCard(
                                            video = video,
                                            modifier = Modifier.size(112.dp, 176.dp),
                                            currentChannelId = currentChannel?.id,
                                            onEditClick = { onEditVideoClick(it.id) },
                                            onClick = { onVideoClick(video) }
                                        )
                                    }
                                } else {
                                    LongVideoCard(
                                        video = video,
                                        currentChannelId = currentChannel?.id,
                                        onEditClick = { onEditVideoClick(it.id) },
                                        onClick = { onVideoClick(video) }
                                    )
                                }
                            }
                        }
                    }
                    1 -> { // Videos
                        item {
                            FilterDropdown(
                                selectedSort = videoSortBy,
                                onSortSelected = { videoSortBy = it },
                                sortOptions = sortOptions,
                                isExpanded = showVideoSortMenu,
                                onExpandedChange = { showVideoSortMenu = it }
                            )
                        }
                        if (regularVideos.isEmpty()) {
                            item { Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { Text("No videos yet.", color = Color.Gray) } }
                        } else {
                            items(regularVideos) { video ->
                                LongVideoCard(
                                    video = video,
                                    currentChannelId = currentChannel?.id,
                                    onEditClick = { onEditVideoClick(it.id) },
                                    onClick = { onVideoClick(video) }
                                )
                            }
                        }
                    }
                    2 -> { // Shorts
                        item {
                            FilterDropdown(
                                selectedSort = shortsSortBy,
                                onSortSelected = { shortsSortBy = it },
                                sortOptions = sortOptions,
                                isExpanded = showShortsSortMenu,
                                onExpandedChange = { showShortsSortMenu = it }
                            )
                        }
                        if (shortVideos.isEmpty()) {
                            item { Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { Text("No shorts yet.", color = Color.Gray) } }
                        } else {
                            val chunks = shortVideos.chunked(4)
                            items(chunks) { rowShorts ->
                                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
                                    for (short in rowShorts) {
                                        Box(modifier = Modifier.weight(1f).padding(4.dp)) {
                                            ShortVideoCard(
                                                video = short,
                                                modifier = Modifier.fillMaxWidth().aspectRatio(9f/16f),
                                                currentChannelId = currentChannel?.id,
                                                onEditClick = { onEditVideoClick(it.id) },
                                                onClick = { onVideoClick(short) }
                                            )
                                        }
                                    }
                                    for (i in 0 until (4 - rowShorts.size)) {
                                        Spacer(modifier = Modifier.weight(1f).padding(4.dp))
                                    }
                                }
                            }
                        }
                    }
                }
                
                item { Spacer(modifier = Modifier.height(100.dp)) }
            }
        }
    }
}

@Composable
fun FilterDropdown(
    selectedSort: String,
    onSortSelected: (String) -> Unit,
    sortOptions: List<String>,
    isExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        Box {
            OutlinedButton(onClick = { onExpandedChange(true) }) {
                Icon(Icons.Default.Sort, contentDescription = "Sort")
                Spacer(Modifier.width(8.dp))
                Text(selectedSort)
            }
            DropdownMenu(
                expanded = isExpanded,
                onDismissRequest = { onExpandedChange(false) }
            ) {
                sortOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onSortSelected(option)
                            onExpandedChange(false)
                        }
                    )
                }
            }
        }
    }
}
