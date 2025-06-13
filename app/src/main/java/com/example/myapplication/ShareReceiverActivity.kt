package com.example.myapplication

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.core.app.NotificationCompat
import androidx.lifecycle.lifecycleScope
// import androidx.work.OneTimeWorkRequestBuilder
// import androidx.work.WorkManager
// import androidx.work.workDataOf
import kotlinx.coroutines.launch

class ShareReceiverActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("ShareReceiver", "Activity launched via intent")

        val action = intent.action
        val type = intent.type

        if (Intent.ACTION_SEND == action) {
            val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
                ?: intent.clipData?.getItemAt(0)?.text?.toString()

            if (!sharedText.isNullOrEmpty()) {
                Log.d("ShareReceiver", "Received shared text: $sharedText")

                lifecycleScope.launch {
                    // enqueueDownload(sharedText) // Optional: enable after testing notifications
                    pushNotification("Shared Link", sharedText)
                }
            } else {
                Log.d("ShareReceiver", "No text found in intent")
            }
        } else {
            Log.d("ShareReceiver", "Intent action not SEND")
        }

        finish() // Close activity immediately
    }

    // Commented out for now to simplify debugging
    /*
    private fun enqueueDownload(link: String) {
        val data = workDataOf("video_url" to link)
        val request = OneTimeWorkRequestBuilder<VideoDownloadWorker>()
            .setInputData(data)
            .build()
        WorkManager.getInstance(applicationContext).enqueue(request)
    }
    */

    private fun pushNotification(title: String, text: String) {
        val channelId = "video_download_channel"
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create channel for Android 8+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Video Downloads",
                NotificationManager.IMPORTANCE_DEFAULT // More visible than LOW
            )
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.stat_sys_download_done) // Use system icon for testing
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        manager.notify(1001, notification)
        Log.d("ShareReceiver", "Notification pushed")
    }
}
