package com.example.myapplication

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLException
import com.yausername.youtubedl_android.YoutubeDLRequest
import kotlinx.coroutines.runBlocking
import java.io.File

class VideoDownloadWorker(
    private val context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    private fun normalizeUrl(url: String): String {
        return when {
            url.contains("youtu.be/") -> {
                val id = url.substringAfter("youtu.be/").substringBefore("?")
                "https://www.youtube.com/watch?v=$id"
            }
            url.contains("/shorts/") -> {
                val id = url.substringAfter("/shorts/").substringBefore("?")
                "https://www.youtube.com/watch?v=$id"
            }
            else -> url.substringBefore("?")
        }
    }

    override fun doWork(): Result {
        val rawUrl = inputData.getString("video_url") ?: return Result.failure()
        val videoUrl = normalizeUrl(rawUrl)

        val channelId = "video_download_channel"
        val notifId = videoUrl.hashCode()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Video Downloads",
                NotificationManager.IMPORTANCE_LOW
            )
            manager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Downloading...")
            .setContentText(videoUrl)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)

        manager.notify(notifId, builder.build())

        return try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val targetDir = File(downloadsDir, "BrainRotGallery")
            if (!targetDir.exists()) targetDir.mkdirs()

            val request = YoutubeDLRequest(videoUrl)
            request.addOption("-f", "bestvideo[ext=mp4]+bestaudio[ext=m4a]/mp4")
            request.addOption("--merge-output-format", "mp4")
            request.addOption("-o", "${targetDir.absolutePath}/%(title)s.%(ext)s")

            val response = YoutubeDL.getInstance().execute(request) { progress, _, _ ->
                val percent = (progress * 100).toInt().coerceIn(0, 100)
                builder.setProgress(100, percent, false)
                manager.notify(notifId, builder.build())
            }

            Log.d("YouTubeDL", "Download complete: ${response.out}")

            val downloadedFile = File(targetDir, response.out.substringAfterLast("/"))
            val uri = Uri.fromFile(downloadedFile)
            context.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri))

            builder.setProgress(0, 0, false)
                .setContentTitle("Download Complete")
                .setContentText("Saved to BrainRotGallery")
                .setAutoCancel(true)
            manager.notify(notifId, builder.build())

            runBlocking {
                val db = AppDatabase.getInstance(context)
                db.linkDao().insert(LinkEntity(url = videoUrl))
            }

            Result.success()

        } catch (e: YoutubeDLException) {
            Log.e("YouTubeDL", "Error: ${e.message}")
            builder.setContentTitle("Download Failed")
                .setContentText(e.localizedMessage ?: "Unknown error")
                .setAutoCancel(true)
            manager.notify(notifId, builder.build())
            Result.retry()
        }
    }
}