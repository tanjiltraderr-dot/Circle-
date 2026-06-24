package com.example.presentation.ui.upload

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.domain.repository.UploadRepository
import com.example.domain.repository.VideoRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun UploadScreen(
    videoRepository: VideoRepository,
    uploadRepository: UploadRepository,
    onClose: () -> Unit,
    onNavigateToPreview: (android.net.Uri) -> Unit,
    onNavigateToLongUpload: (android.net.Uri) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    
    var hasPermissions by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasPermissions = permissions[Manifest.permission.CAMERA] == true &&
                         permissions[Manifest.permission.RECORD_AUDIO] == true
    }

    LaunchedEffect(Unit) {
        if (!hasPermissions) {
            permissionLauncher.launch(
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
            )
        }
    }

    var selectedMode by remember { mutableStateOf("Shorts") } // "Shorts" or "Video"
    var durationLimit by remember { mutableIntStateOf(15) } // 15, 30, 60
    var isRecording by remember { mutableStateOf(false) }
    var recordingTimeSeconds by remember { mutableIntStateOf(0) }
    
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val previewView = remember { PreviewView(context) }
    
    val videoCapture = remember {
        val recorder = Recorder.Builder()
            .setQualitySelector(QualitySelector.from(Quality.SD))
            .build()
        VideoCapture.withOutput(recorder)
    }
    
    var activeRecording: Recording? by remember { mutableStateOf(null) }

    LaunchedEffect(hasPermissions) {
        if (hasPermissions) {
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner, cameraSelector, preview, videoCapture
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Auto-stop logic and timer updater
    LaunchedEffect(isRecording, durationLimit) {
        if (isRecording) {
            val startTime = System.currentTimeMillis()
            while (isRecording) {
                val elapsedMillis = System.currentTimeMillis() - startTime
                recordingTimeSeconds = (elapsedMillis / 1000).toInt()
                
                if (recordingTimeSeconds >= durationLimit) {
                    activeRecording?.stop()
                    isRecording = false
                    break
                }
                delay(100)
            }
        } else {
            recordingTimeSeconds = 0
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            try {
                val retriever = android.media.MediaMetadataRetriever()
                retriever.setDataSource(context, uri)
                val time = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION)
                val timeInMillis = time?.toLongOrNull() ?: 0L
                retriever.release()

                if (timeInMillis > 60000) {
                    onNavigateToLongUpload(uri)
                } else {
                    onNavigateToPreview(uri)
                }
            } catch (e: Exception) {
                onNavigateToPreview(uri)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (hasPermissions) {
            AndroidView(
                factory = { previewView },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Text(
                "Camera and Audio permissions are required to use this feature.",
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
        }
        
        // Top Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
            }
            
            if (selectedMode == "Shorts") {
                // Duration Selector
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black.copy(alpha = 0.5f))
                        .padding(4.dp)
                ) {
                    listOf(15, 30, 60).forEach { dur ->
                        val isSelected = durationLimit == dur
                        TextButton(
                            onClick = { 
                                if (!isRecording) durationLimit = dur 
                            },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.defaultMinSize(minWidth = 1.dp, minHeight = 1.dp)
                        ) {
                            Text(
                                "${dur}s",
                                color = if (isSelected) Color.White else Color.Gray,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            } else {
                Spacer(modifier = Modifier.width(48.dp))
            }
            
            // Dummy spacer to balance the close button if we want the duration selector centered
            Spacer(modifier = Modifier.width(48.dp))
        }

        // Recording Progress Bar / Time indicator
        if (isRecording) {
            Text(
                text = String.format("00:%02d", recordingTimeSeconds),
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 100.dp)
                    .background(Color.Red.copy(alpha = 0.7f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }

        // Status indicator removed

        // Bottom Controls
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Mode selector (Shorts / Video)
            if (!isRecording) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(32.dp),
                    modifier = Modifier
                        .padding(bottom = 24.dp)
                        .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                ) {
                    Text(
                        "Shorts",
                        color = if (selectedMode == "Shorts") Color.White else Color.Gray,
                        fontWeight = if (selectedMode == "Shorts") FontWeight.Bold else FontWeight.Normal,
                        fontSize = 16.sp,
                        modifier = Modifier.clickable { selectedMode = "Shorts" }
                    )
                    Text(
                        "Video",
                        color = if (selectedMode == "Video") Color.White else Color.Gray,
                        fontWeight = if (selectedMode == "Video") FontWeight.Bold else FontWeight.Normal,
                        fontSize = 16.sp,
                        modifier = Modifier.clickable { 
                            selectedMode = "Video"
                            galleryLauncher.launch("video/*") 
                        }
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(64.dp))
            }

            // Action Row
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 48.dp),
                contentAlignment = Alignment.Center
            ) {
                // Gallery Picker
                if (!isRecording && selectedMode == "Shorts") {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.DarkGray)
                            .border(1.dp, Color.White, RoundedCornerShape(8.dp))
                            .clickable {
                                galleryLauncher.launch("video/*")
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.PhotoLibrary, contentDescription = "Gallery", tint = Color.White)
                    }
                }

                // Record Button
                val outerSize by animateDpAsState(if (isRecording) 90.dp else 80.dp)
                val innerSize by animateDpAsState(if (isRecording) 30.dp else 70.dp)
                val cornerRadius by animateDpAsState(if (isRecording) 8.dp else 35.dp)

                Box(
                    modifier = Modifier
                        .size(outerSize)
                        .clip(CircleShape)
                        .border(4.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                        .clickable {
                            if (selectedMode == "Video") {
                                galleryLauncher.launch("video/*")
                            } else {
                                if (isRecording) {
                                    activeRecording?.stop()
                                    isRecording = false
                                } else {
                                    val file = File(context.cacheDir, "record_${System.currentTimeMillis()}.mp4")
                                    val outputOptions = FileOutputOptions.Builder(file).build()
                                    
                                    try {
                                        activeRecording = videoCapture.output
                                            .prepareRecording(context, outputOptions)
                                            .apply {
                                                if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                                                    withAudioEnabled()
                                                }
                                            }
                                            .start(ContextCompat.getMainExecutor(context)) { recordEvent ->
                                                when (recordEvent) {
                                                    is VideoRecordEvent.Start -> {
                                                        isRecording = true
                                                    }
                                                    is VideoRecordEvent.Finalize -> {
                                                        isRecording = false
                                                        if (!recordEvent.hasError()) {
                                                            val uri = android.net.Uri.fromFile(file)
                                                            onNavigateToPreview(uri)
                                                        } else {
                                                            Toast.makeText(context, "Recording failed", Toast.LENGTH_SHORT).show()
                                                        }
                                                    }
                                                }
                                            }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedMode == "Video") {
                        Box(
                            modifier = Modifier
                                .size(innerSize)
                                .clip(RoundedCornerShape(cornerRadius))
                                .background(Color.White)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(innerSize)
                                .clip(RoundedCornerShape(cornerRadius))
                                .background(Color.Red)
                        )
                    }
                }
            }
        }
    }
}
