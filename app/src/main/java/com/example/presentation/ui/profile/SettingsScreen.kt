package com.example.presentation.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.repository.AuthRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    authRepository: AuthRepository,
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    val currentUser by authRepository.getCurrentUser().collectAsState(initial = null)
    val scope = rememberCoroutineScope()

    val initials = currentUser?.let {
        val first = it.firstName.take(1).uppercase()
        val last = it.lastName.take(1).uppercase()
        if (first.isNotBlank() || last.isNotBlank()) "$first$last" else "U"
    } ?: "U"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        containerColor = Color(0xFFF3F4F6) // Light gray background like the image
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // Account section
            item {
                SettingsSection(
                    title = "Account",
                    items = listOf(
                        SettingsItemData(Icons.Outlined.Person, "Account"),
                        SettingsItemData(Icons.Outlined.Shield, "Security and permissions"),
                        SettingsItemData(Icons.Outlined.Share, "Share profile")
                    )
                )
            }

            // Visibility section
            item {
                SettingsSection(
                    title = "Visibility",
                    items = listOf(
                        SettingsItemData(Icons.Outlined.Lock, "Private account"),
                        SettingsItemData(Icons.Outlined.Block, "Blocked accounts")
                    )
                )
            }

            // Content & display section
            item {
                SettingsSection(
                    title = "Content & display",
                    items = listOf(
                        SettingsItemData(Icons.Outlined.History, "Activity centre"),
                        SettingsItemData(Icons.Outlined.PlayArrow, "Playback"),
                        SettingsItemData(Icons.Outlined.Brightness2, "Display"),
                        SettingsItemData(Icons.Outlined.SortByAlpha, "Language")
                    )
                )
            }

            // Support & about section
            item {
                SettingsSection(
                    title = "Support & about",
                    items = listOf(
                        SettingsItemData(Icons.Outlined.Headset, "Help Centre"),
                        SettingsItemData(Icons.Outlined.Lock, "Privacy Centre"),
                        SettingsItemData(Icons.Outlined.Info, "Terms and policies")
                    )
                )
            }

            // Login section
            item {
                Text(
                    text = "Login",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column {
                        // Switch account item
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Outlined.SwapHoriz, contentDescription = "Switch account", tint = Color.Gray)
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "Switch account",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(initials, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray)
                        }
                        
                        Divider(color = Color(0xFFF3F4F6), thickness = 1.dp)
                        
                        // Log out item
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    scope.launch {
                                        authRepository.logout()
                                        onLogout()
                                    }
                                }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Outlined.Logout, contentDescription = "Log out", tint = Color.Gray)
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "Log out",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray)
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

data class SettingsItemData(val icon: ImageVector, val title: String)

@Composable
fun SettingsSection(title: String, items: List<SettingsItemData>) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column {
                items.forEachIndexed { index, item ->
                    SettingsItemRow(item = item)
                    if (index < items.size - 1) {
                        Divider(color = Color(0xFFF3F4F6), thickness = 1.dp)
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsItemRow(item: SettingsItemData) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(item.icon, contentDescription = item.title, tint = Color.Gray)
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = item.title,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold, // To match the darker look in the screenshot
            modifier = Modifier.weight(1f)
        )
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray)
    }
}
