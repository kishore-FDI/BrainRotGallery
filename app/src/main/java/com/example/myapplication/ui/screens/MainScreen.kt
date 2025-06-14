package com.example.myapplication.ui.screens

import GalleryScreen
import android.Manifest
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.*
import com.example.myapplication.DownloadsScreen
import com.example.myapplication.screen.SettingsScreen
import com.yausername.ffmpeg.FFmpeg
import com.yausername.youtubedl_android.YoutubeDL

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val items = listOf("gallery", "downloads", "settings")

    val context = LocalContext.current

    // Init YouTubeDL + FFmpeg
    LaunchedEffect(Unit) {
        try {
            YoutubeDL.getInstance().init(context)
            FFmpeg.getInstance().init(context)
        } catch (e: Exception) {
            Log.e("MainScreen", "Init failed", e)
        }
    }

    // Permission request
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {}

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEach { screen ->
                    NavigationBarItem(
                        selected = navController.currentBackStackEntry?.destination?.route == screen,
                        onClick = { navController.navigate(screen) },
                        icon = { Icon(Icons.Filled.Home, contentDescription = screen) },
                        label = { Text(screen.replaceFirstChar { it.uppercase() }) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "downloads",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("gallery") { GalleryScreen() }
            composable("downloads") { DownloadsScreen() }
            composable("settings") { SettingsScreen() }
        }
    }
}
