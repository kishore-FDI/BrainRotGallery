package com.example.myapplication.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.SettingsDataStore
import com.example.myapplication.ui.QualityAccordion
import kotlinx.coroutines.launch

@Composable
fun GalleryScreen() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Gallery Tab (To be implemented)")
    }
}

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val settingsStore = remember { SettingsDataStore(context) }
    val scope = rememberCoroutineScope()

    // Flows can return null if not yet set, so handle safely
    val ytQualityRaw by settingsStore.ytQualityFlow.collectAsState(initial = null)
    val ytFallbackRaw by settingsStore.ytFallbackFlow.collectAsState(initial = null)
    val instaQualityRaw by settingsStore.instaQualityFlow.collectAsState(initial = null)
    val instaFallbackRaw by settingsStore.instaFallbackFlow.collectAsState(initial = null)

    // Null-safe defaults
    val ytQuality = ytQualityRaw ?: "BEST"
    val ytFallback = ytFallbackRaw ?: "next-lower"
    val instaQuality = instaQualityRaw ?: "BEST"
    val instaFallback = instaFallbackRaw ?: "next-lower"

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Settings", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        QualityAccordion(
            title = "YouTube",
            currentQuality = ytQuality,
            currentFallback = ytFallback,
            onSettingChange = { quality, fallback ->
                scope.launch {
                    settingsStore.setYoutubeSettings(quality, fallback)
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        QualityAccordion(
            title = "Instagram",
            currentQuality = instaQuality,
            currentFallback = instaFallback,
            onSettingChange = { quality, fallback ->
                scope.launch {
                    settingsStore.setInstagramSettings(quality, fallback)
                }
            }
        )
    }
}
