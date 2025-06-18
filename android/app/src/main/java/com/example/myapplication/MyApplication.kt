package com.example.myapplication

import android.app.Application
import android.util.Log
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLException
import com.yausername.ffmpeg.FFmpeg

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        try {
            YoutubeDL.getInstance().init(this)
            Log.d("YoutubeDL", "Initialized successfully")
        } catch (e: YoutubeDLException) {
            Log.e("YoutubeDL", "YoutubeDL init failed", e)
        }

        try {
            FFmpeg.getInstance().init(this)
            Log.d("FFmpeg", "Initialized successfully")
        } catch (e: Exception) {
            Log.e("FFmpeg", "FFmpeg init failed", e)
        }
    }
}
