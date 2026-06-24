package com.example.presentation.ui.channel

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
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
import coil.compose.AsyncImage
import com.example.domain.repository.ChannelRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateChannelScreen(
    channelRepository: ChannelRepository,
    onBack: () -> Unit,
    onChannelCreated: () -> Unit
) {
    var currentStep by remember { mutableStateOf(1) }
    
    var channelName by remember { mutableStateOf("") }
    var channelHandle by remember { mutableStateOf("") }
    var channelCategory by remember { mutableStateOf("") }
    var profileImageUri by remember { mutableStateOf<Uri?>(null) }
    var bannerImageUri by remember { mutableStateOf<Uri?>(null) }
    
    var isSubmitting by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val profileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) profileImageUri = uri
    }
    
    val bannerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) bannerImageUri = uri
    }

    val categories = listOf(
        "Entertainment", "Gaming", "Education", "Science & Tech", "Comedy", 
        "Film & Animation", "Music", "News & Politics", "How-to & Style", 
        "Autos & Vehicles", "Travel & Events", "Vlogs"
    )
    var expandedCategoryDropdown by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Channel") },
                navigationIcon = {
                    if (currentStep > 1) {
                        IconButton(onClick = { currentStep-- }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    } else {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Close")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Progress Indicator
            LinearProgressIndicator(
                progress = currentStep / 5f,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(32.dp))

            when (currentStep) {
                1 -> {
                    Text("What should we call you?", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("This is your channel's public name.", color = Color.Gray)
                    Spacer(modifier = Modifier.height(24.dp))
                    OutlinedTextField(
                        value = channelName,
                        onValueChange = { channelName = it },
                        label = { Text("Channel Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Button(
                        onClick = {
                            if (channelName.isNotBlank()) {
                                if (channelHandle.isBlank()) {
                                    val randomSuffix = (1000..9999).random()
                                    channelHandle = "@${channelName.replace(" ", "").lowercase()}$randomSuffix"
                                }
                                currentStep = 2
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = channelName.isNotBlank()
                    ) {
                        Text("Next")
                    }
                }
                2 -> {
                    Text("Choose your handle", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Your handle is unique. It's how people can find and tag you.", color = Color.Gray)
                    Spacer(modifier = Modifier.height(24.dp))
                    OutlinedTextField(
                        value = channelHandle,
                        onValueChange = { 
                            channelHandle = if (it.startsWith("@")) it else "@$it"
                        },
                        label = { Text("Channel Handle") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Button(
                        onClick = { if (channelHandle.length > 1) currentStep = 3 },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = channelHandle.length > 1
                    ) {
                        Text("Next")
                    }
                }
                3 -> {
                    Text("Select a Category", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Help viewers find your content.", color = Color.Gray)
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    ExposedDropdownMenuBox(
                        expanded = expandedCategoryDropdown,
                        onExpandedChange = { expandedCategoryDropdown = !expandedCategoryDropdown }
                    ) {
                        OutlinedTextField(
                            value = channelCategory.ifEmpty { "Select Category" },
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategoryDropdown) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedCategoryDropdown,
                            onDismissRequest = { expandedCategoryDropdown = false }
                        ) {
                            categories.forEach { selectionOption ->
                                DropdownMenuItem(
                                    text = { Text(selectionOption) },
                                    onClick = {
                                        channelCategory = selectionOption
                                        expandedCategoryDropdown = false
                                    }
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.weight(1f))
                    Button(
                        onClick = { if (channelCategory.isNotBlank()) currentStep = 4 },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = channelCategory.isNotBlank()
                    ) {
                        Text("Next")
                    }
                }
                4 -> {
                    Text("Personalize your channel", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Banner Image
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { bannerLauncher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            if (bannerImageUri != null) {
                                AsyncImage(
                                    model = bannerImageUri,
                                    contentDescription = "Banner",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.CameraAlt, contentDescription = "Banner")
                                    Text("Upload Banner", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                        
                        // Profile Image
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .clickable { profileLauncher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            if (profileImageUri != null) {
                                AsyncImage(
                                    model = profileImageUri,
                                    contentDescription = "Profile",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.CameraAlt, contentDescription = "Profile", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                                    Text("Pic", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        TextButton(onClick = { currentStep = 5 }) {
                            Text("Skip", color = Color.Gray)
                        }
                        Button(onClick = { currentStep = 5 }) {
                            Text("Next")
                        }
                    }
                }
                5 -> {
                    Text("Review & Create", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Facebook Style Header Preview
                    Card(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Column {
                                // Top: Full-width horizontal Channel Banner image
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(140.dp)
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    if (bannerImageUri != null) {
                                        AsyncImage(
                                            model = bannerImageUri,
                                            contentDescription = "Banner Preview",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                }
                                
                                // Spacer for Avatar Overlap
                                Spacer(modifier = Modifier.height(50.dp))
                                
                                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                                    Text(
                                        text = channelName,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = channelHandle,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    // Category Tag
                                    Surface(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = RoundedCornerShape(16.dp)
                                    ) {
                                        Text(
                                            text = channelCategory,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                            }
                            
                            // Avatar Overlap
                            Box(
                                modifier = Modifier
                                    .padding(start = 16.dp)
                                    .offset(y = 100.dp)
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surface)
                                    .padding(4.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.secondaryContainer)
                            ) {
                                if (profileImageUri != null) {
                                    AsyncImage(
                                        model = profileImageUri,
                                        contentDescription = "Profile Preview",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))
                    Button(
                        onClick = {
                            isSubmitting = true
                            scope.launch {
                                val result = channelRepository.createChannel(
                                    name = channelName,
                                    handle = channelHandle,
                                    category = channelCategory,
                                    profileImageUri = profileImageUri,
                                    bannerImageUri = bannerImageUri
                                )
                                isSubmitting = false
                                if (result.isSuccess) {
                                    Toast.makeText(context, "Channel created!", Toast.LENGTH_SHORT).show()
                                    onChannelCreated()
                                } else {
                                    Toast.makeText(context, "Failed: ${result.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isSubmitting
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                        } else {
                            Text("Create Channel")
                        }
                    }
                }
            }
        }
    }
}
