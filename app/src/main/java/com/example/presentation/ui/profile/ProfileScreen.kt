package com.example.presentation.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Logout
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
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.domain.model.Video
import com.example.domain.repository.AuthRepository
import com.example.domain.repository.UserRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    authRepository: AuthRepository,
    userRepository: UserRepository,
    channelRepository: com.example.domain.repository.ChannelRepository,
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onSettingsClick: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToLiked: () -> Unit,
    onNavigateToSaved: () -> Unit,
    onNavigateToDownloads: () -> Unit,
    onNavigateToChannel: (String) -> Unit,
    onNavigateToCreateChannel: () -> Unit,
    onVideoClick: (Video) -> Unit
) {
    val currentUser by authRepository.getCurrentUser().collectAsState(initial = null)
    var watchHistory by remember { mutableStateOf<List<Video>>(emptyList()) }
    var userChannel by remember { mutableStateOf<com.example.domain.model.Channel?>(null) }
    var isActingAsChannel by remember { mutableStateOf(false) }
    var showSwitchProfileDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        watchHistory = userRepository.getWatchHistory().take(10)
        userChannel = channelRepository.getUserChannel()
    }

    val initials = currentUser?.let {
        val first = it.firstName.take(1).uppercase()
        val last = it.lastName.take(1).uppercase()
        if (first.isNotBlank() || last.isNotBlank()) "$first$last" else "U"
    } ?: "U"

    val fullName = currentUser?.let {
        if (it.firstName.isNotBlank() || it.lastName.isNotBlank()) "${it.firstName} ${it.lastName}"
        else "Unknown User"
    } ?: "Loading..."

    val email = currentUser?.email ?: "loading..."

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .statusBarsPadding()
                    .padding(horizontal = 4.dp, vertical = 0.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Text(
                    "Profile",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp).weight(1f)
                )
                IconButton(onClick = onSettingsClick) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings")
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // Channel/Profile Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(if (isActingAsChannel) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary)
                            .clickable { showSwitchProfileDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isActingAsChannel && userChannel != null) {
                            if (userChannel!!.profileImageUrl.isNotBlank()) {
                                AsyncImage(
                                    model = userChannel!!.profileImageUrl,
                                    contentDescription = "Channel Profile",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Text(userChannel!!.name.take(1).uppercase(), color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Text(initials, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        if (isActingAsChannel && userChannel != null) {
                            Text(userChannel!!.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text(userChannel!!.handle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("View Channel", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { 
                                onNavigateToChannel(userChannel!!.id)
                            })
                        } else {
                            Text(fullName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text(email, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(4.dp))
                            if (userChannel != null) {
                                Text("View Channel", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { 
                                    onNavigateToChannel(userChannel!!.id)
                                })
                            } else {
                                Text("Create Channel", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { 
                                    onNavigateToCreateChannel()
                                })
                            }
                        }
                    }
                    IconButton(onClick = { showSwitchProfileDialog = true }) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = "Switch Channel")
                    }
                }
                Divider()
            }

            // Watch History Row
            if (watchHistory.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("History", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        TextButton(onClick = onNavigateToHistory) {
                            Text("View All", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(watchHistory) { video ->
                            Column(
                                modifier = Modifier
                                    .width(140.dp)
                                    .clickable { onVideoClick(video) }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(16f/9f)
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
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = video.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = video.creatorName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Divider()
                }
            } else {
                item {
                    ProfileOptionItem(icon = Icons.Outlined.History, title = "History", onClick = onNavigateToHistory)
                }
            }

            // Stats/Lists Options
            item {
                ProfileOptionItem(icon = Icons.Outlined.ThumbUp, title = "Liked Videos", onClick = onNavigateToLiked)
                ProfileOptionItem(icon = Icons.Outlined.BookmarkBorder, title = "Saved Videos", onClick = onNavigateToSaved)
                ProfileOptionItem(icon = Icons.Outlined.Download, title = "Downloads", onClick = onNavigateToDownloads)
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                ProfileOptionItem(icon = Icons.Default.VideoLibrary, title = "Your Videos", onClick = {})
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                ProfileOptionItem(icon = Icons.Outlined.Logout, title = "Logout", tint = MaterialTheme.colorScheme.error) {
                    scope.launch {
                        authRepository.logout()
                        onLogout()
                    }
                }
            }
        }
        
        if (showSwitchProfileDialog) {
            AlertDialog(
                onDismissRequest = { showSwitchProfileDialog = false },
                title = { Text("Switch Profile") },
                text = {
                    Column {
                        // Personal Account
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { 
                                    isActingAsChannel = false
                                    showSwitchProfileDialog = false 
                                }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary), contentAlignment = Alignment.Center) {
                                Text(initials, color = Color.White)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(fullName, fontWeight = if (!isActingAsChannel) FontWeight.Bold else FontWeight.Normal)
                        }
                        Divider()
                        // Channel Profile
                        if (userChannel != null) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { 
                                        isActingAsChannel = true
                                        showSwitchProfileDialog = false 
                                    }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondary), contentAlignment = Alignment.Center) {
                                    if (userChannel!!.profileImageUrl.isNotBlank()) {
                                        AsyncImage(model = userChannel!!.profileImageUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                                    } else {
                                        Text(userChannel!!.name.take(1).uppercase(), color = Color.White)
                                    }
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(userChannel!!.name, fontWeight = if (isActingAsChannel) FontWeight.Bold else FontWeight.Normal)
                            }
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { 
                                        showSwitchProfileDialog = false
                                        onNavigateToCreateChannel()
                                    }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Create")
                                Spacer(modifier = Modifier.width(16.dp))
                                Text("Create Channel")
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showSwitchProfileDialog = false }) {
                        Text("Close")
                    }
                }
            )
        }
    }
}

@Composable
fun ProfileOptionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = title, tint = tint)
        Spacer(modifier = Modifier.width(24.dp))
        Text(title, style = MaterialTheme.typography.bodyLarge, color = if (tint == MaterialTheme.colorScheme.error) tint else Color.Unspecified)
    }
}
