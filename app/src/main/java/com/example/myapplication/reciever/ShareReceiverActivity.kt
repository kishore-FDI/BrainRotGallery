package com.example.myapplication.reciever

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.myapplication.worker.VideoDownloadWorker

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

                enqueueDownload(sharedText) // ✅ Re-enabled download work
            } else {
                Log.d("ShareReceiver", "No text found in intent")
            }
        } else {
            Log.d("ShareReceiver", "Intent action not SEND")
        }

        finish() // Don't show UI
    }

    private fun enqueueDownload(link: String) {
        val data = workDataOf("video_url" to link)
        val request = OneTimeWorkRequestBuilder<VideoDownloadWorker>()
            .setInputData(data)
            .build()

        WorkManager.getInstance(applicationContext).enqueue(request)
    }
}
