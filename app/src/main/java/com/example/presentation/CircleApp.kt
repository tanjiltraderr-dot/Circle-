package com.example.presentation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.di.AppContainer
import com.example.presentation.ui.upload.ShortsPreviewScreen
import com.example.presentation.ui.upload.UploadDetailsScreen
import com.example.presentation.ui.upload.LongVideoUploadScreen
import com.example.presentation.ui.channel.CreateChannelScreen
import android.net.Uri
import com.example.presentation.components.CircleBottomNavigation
import com.example.presentation.ui.home.HomeScreen
import com.example.presentation.ui.home.VideoDetailsScreen
import com.example.presentation.ui.onboarding.OnboardingScreen
import com.example.presentation.ui.auth.ForgotPasswordScreen
import com.example.presentation.ui.auth.LoginScreen
import com.example.presentation.ui.auth.SignupScreen
import com.example.presentation.ui.shorts.ShortsScreen
import com.example.presentation.ui.upload.UploadScreen
import com.example.presentation.ui.following.FollowingScreen
import com.example.presentation.ui.notifications.NotificationsScreen
import com.example.presentation.ui.profile.ProfileScreen
import com.example.presentation.ui.profile.SettingsScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment

import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

import com.example.presentation.ui.channel.ChannelProfileScreen
import androidx.navigation.navDeepLink

@Composable
fun CircleApp(container: AppContainer) {
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavRoutes = listOf(
        "home", "shorts", "following", "notifications"
    )

    val sessionStatus by container.authRepository.getCurrentUser().collectAsState(initial = null)
    var isInitialized by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // Wait 500ms to allow session to load from storage. Or observe if sessionStatus becomes something.
        kotlinx.coroutines.delay(500)
        isInitialized = true
    }

    if (!isInitialized) {
        Box(modifier = Modifier.fillMaxSize().background(androidx.compose.ui.graphics.Color.Black), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        bottomBar = {
            if (currentRoute in bottomNavRoutes) {
                CircleBottomNavigation(
                    currentRoute = currentRoute ?: "home",
                    onNavigate = { route ->
                        if (route == "upload") {
                            scope.launch {
                                val channel = container.channelRepository.getUserChannel()
                                if (channel != null) {
                                    navController.navigate("upload")
                                } else {
                                    navController.navigate("create_channel")
                                }
                            }
                        } else {
                            navController.navigate(route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
            val startDest = if (sessionStatus != null) "home" else "onboarding"
            NavHost(
                navController = navController,
                startDestination = startDest,
                modifier = Modifier.padding(innerPadding)
            ) {
            composable("onboarding") {
                OnboardingScreen(
                    onGetStarted = { navController.navigate("login") { popUpTo("onboarding") { inclusive = true } } }
                )
            }
            composable("login") {
                LoginScreen(
                    authRepository = container.authRepository,
                    onLoginSuccess = { navController.navigate("home") { popUpTo("login") { inclusive = true } } },
                    onNavigateSignup = { navController.navigate("signup") },
                    onNavigateForgotPassword = { navController.navigate("forgot_password") }
                )
            }
            composable("forgot_password") {
                ForgotPasswordScreen(
                    authRepository = container.authRepository,
                    onNavigateLogin = { navController.navigate("login") { popUpTo("login") { inclusive = true } } }
                )
            }
            composable("signup") {
                SignupScreen(
                    authRepository = container.authRepository,
                    onSignupSuccess = { navController.navigate("home") { popUpTo("signup") { inclusive = true } } },
                    onNavigateLogin = { navController.navigate("login") { popUpTo("signup") { inclusive = true } } }
                )
            }
            composable("home") {
                HomeScreen(
                    videoRepository = container.videoRepository,
                    authRepository = container.authRepository,
                    onVideoClick = { video ->
                        if (video.isShort) {
                            navController.navigate("shorts?videoId=${video.id}")
                        } else {
                            navController.navigate("video/${video.id}")
                        }
                    },
                    onProfileClick = {
                        navController.navigate("profile")
                    },
                    onSearchClick = {
                        navController.navigate("search")
                    }
                )
            }
            composable("search") {
                com.example.presentation.ui.search.SearchScreen(
                    videoRepository = container.videoRepository,
                    onBackClick = { navController.popBackStack() },
                    onVideoClick = { video ->
                        if (video.isShort) {
                            navController.navigate("shorts?videoId=${video.id}")
                        } else {
                            navController.navigate("video/${video.id}")
                        }
                    }
                )
            }
            composable("profile") {
                ProfileScreen(
                    authRepository = container.authRepository,
                    userRepository = container.userRepository,
                    channelRepository = container.channelRepository,
                    onBack = { navController.popBackStack() },
                    onLogout = {
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onSettingsClick = {
                        navController.navigate("settings")
                    },
                    onNavigateToHistory = { navController.navigate("history") },
                    onNavigateToLiked = { navController.navigate("liked") },
                    onNavigateToSaved = { navController.navigate("saved") },
                    onNavigateToDownloads = { navController.navigate("downloads") },
                    onNavigateToChannel = { channelId -> navController.navigate("channel/$channelId") },
                    onNavigateToCreateChannel = { navController.navigate("create_channel") },
                    onVideoClick = { video ->
                        if (video.isShort) {
                            navController.navigate("shorts?videoId=${video.id}")
                        } else {
                            navController.navigate("video/${video.id}")
                        }
                    }
                )
            }
            composable(
                route = "edit_video/{videoId}",
                arguments = listOf(androidx.navigation.navArgument("videoId") { type = androidx.navigation.NavType.StringType })
            ) { backStackEntry ->
                val videoId = backStackEntry.arguments?.getString("videoId") ?: return@composable
                com.example.presentation.ui.channel.EditVideoScreen(
                    videoId = videoId,
                    videoRepository = container.videoRepository,
                    uploadRepository = container.uploadRepository,
                    onBack = { navController.popBackStack() },
                    onSuccess = { navController.popBackStack() }
                )
            }
            composable(
                route = "channel/{channelId}",
                arguments = listOf(androidx.navigation.navArgument("channelId") { type = androidx.navigation.NavType.StringType }),
                deepLinks = listOf(androidx.navigation.navDeepLink { uriPattern = "https://circle.com/channel/{channelId}" })
            ) { backStackEntry ->
                val channelId = backStackEntry.arguments?.getString("channelId") ?: return@composable
                ChannelProfileScreen(
                    channelId = channelId,
                    channelRepository = container.channelRepository,
                    videoRepository = container.videoRepository,
                    onBack = { navController.popBackStack() },
                    onVideoClick = { video ->
                        if (video.isShort) {
                            navController.navigate("shorts?videoId=${video.id}")
                        } else {
                            navController.navigate("video/${video.id}")
                        }
                    },
                    onEditVideoClick = { videoId ->
                        navController.navigate("edit_video/$videoId")
                    }
                )
            }
            composable("history") {
                com.example.presentation.ui.profile.HistoryScreen(
                    userRepository = container.userRepository,
                    onBack = { navController.popBackStack() },
                    onVideoClick = { video ->
                        if (video.isShort) {
                            navController.navigate("shorts?videoId=${video.id}")
                        } else {
                            navController.navigate("video/${video.id}")
                        }
                    }
                )
            }
            composable("liked") {
                com.example.presentation.ui.profile.LikedVideosScreen(
                    userRepository = container.userRepository,
                    onBack = { navController.popBackStack() },
                    onVideoClick = { video ->
                        if (video.isShort) {
                            navController.navigate("shorts?videoId=${video.id}")
                        } else {
                            navController.navigate("video/${video.id}")
                        }
                    }
                )
            }
            composable("saved") {
                com.example.presentation.ui.profile.SavedVideosScreen(
                    userRepository = container.userRepository,
                    onBack = { navController.popBackStack() },
                    onVideoClick = { video ->
                        if (video.isShort) {
                            navController.navigate("shorts?videoId=${video.id}")
                        } else {
                            navController.navigate("video/${video.id}")
                        }
                    }
                )
            }
            composable("downloads") {
                com.example.presentation.ui.profile.DownloadsScreen(
                    downloadRepository = container.downloadRepository,
                    onBack = { navController.popBackStack() },
                    onVideoClick = { video ->
                        if (video.isShort) {
                            navController.navigate("shorts?videoId=${video.id}")
                        } else {
                            navController.navigate("video/${video.id}")
                        }
                    }
                )
            }
            composable("settings") {
                SettingsScreen(
                    authRepository = container.authRepository,
                    onBack = { navController.popBackStack() },
                    onLogout = {
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
            composable("video/{videoId}") { backStackEntry ->
                val videoId = backStackEntry.arguments?.getString("videoId") ?: return@composable
                VideoDetailsScreen(
                    videoId = videoId,
                    videoRepository = container.videoRepository,
                    downloadRepository = container.downloadRepository,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                "shorts?videoId={videoId}",
                arguments = listOf(androidx.navigation.navArgument("videoId") { nullable = true })
            ) { backStackEntry ->
                val videoId = backStackEntry.arguments?.getString("videoId")
                ShortsScreen(videoRepository = container.videoRepository, initialVideoId = videoId)
            }
            composable("upload") {
                UploadScreen(
                    videoRepository = container.videoRepository,
                    uploadRepository = container.uploadRepository,
                    onClose = {
                        navController.navigate("home") {
                            popUpTo("home") { inclusive = false }
                        }
                    },
                    onNavigateToPreview = { uri ->
                        val encodedUri = java.net.URLEncoder.encode(uri.toString(), "UTF-8")
                        navController.navigate("preview?uri=$encodedUri")
                    },
                    onNavigateToLongUpload = { uri ->
                        val encodedUri = java.net.URLEncoder.encode(uri.toString(), "UTF-8")
                        navController.navigate("long_video_upload?uri=$encodedUri")
                    }
                )
            }
            composable("create_channel") {
                CreateChannelScreen(
                    channelRepository = container.channelRepository,
                    onBack = { navController.popBackStack() },
                    onChannelCreated = {
                        navController.navigate("profile") {
                            popUpTo("home")
                        }
                    }
                )
            }
            composable(
                route = "preview?uri={uri}",
                arguments = listOf(navArgument("uri") { type = NavType.StringType })
            ) { backStackEntry ->
                val uriStr = backStackEntry.arguments?.getString("uri") ?: ""
                val decodedUri = Uri.parse(java.net.URLDecoder.decode(uriStr, "UTF-8"))
                ShortsPreviewScreen(
                    uri = decodedUri,
                    onBack = { navController.popBackStack() },
                    onNext = {
                        val encodedUri = java.net.URLEncoder.encode(decodedUri.toString(), "UTF-8")
                        navController.navigate("upload_details?uri=$encodedUri")
                    }
                )
            }
            composable(
                route = "upload_details?uri={uri}",
                arguments = listOf(navArgument("uri") { type = NavType.StringType })
            ) { backStackEntry ->
                val uriStr = backStackEntry.arguments?.getString("uri") ?: ""
                val decodedUri = Uri.parse(java.net.URLDecoder.decode(uriStr, "UTF-8"))
                UploadDetailsScreen(
                    uri = decodedUri,
                    uploadRepository = container.uploadRepository,
                    onUploadComplete = {
                        navController.navigate("home") {
                            popUpTo(0)
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                route = "long_video_upload?uri={uri}",
                arguments = listOf(navArgument("uri") { type = NavType.StringType })
            ) { backStackEntry ->
                val uriStr = backStackEntry.arguments?.getString("uri") ?: ""
                val decodedUri = Uri.parse(java.net.URLDecoder.decode(uriStr, "UTF-8"))
                LongVideoUploadScreen(
                    uri = decodedUri,
                    uploadRepository = container.uploadRepository,
                    onUploadComplete = {
                        navController.navigate("home") {
                            popUpTo(0)
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable("following") {
                FollowingScreen(videoRepository = container.videoRepository)
            }
            composable("notifications") {
                NotificationsScreen()
            }
        }
    }
}
