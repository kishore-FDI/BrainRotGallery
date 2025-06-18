package com.example.myapplication

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.LinkEntity
import com.example.myapplication.ui.screens.MainScreen
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.yausername.ffmpeg.FFmpeg
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
//        try {
//            YoutubeDL.getInstance().init(application)
//        } catch (e: YoutubeDLException) {
//            Log.e("YoutubeDL", "Failed to initialize", e)
//        }
//
//        // Initialize FFmpeg (optional, if merging is ever needed)
//        try {
//            FFmpeg.getInstance().init(application)
//        } catch (e: Exception) {
//            Log.e("FFmpeg", "Failed to initialize", e)
//        }

        setContent {
            MyApplicationTheme {
                MainScreen()
            }
        }
    }
}
