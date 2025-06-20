package com.example.myapplication.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.myapplication.R
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.LinkEntity
import com.example.myapplication.data.SettingsDataStore
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLException
import com.yausername.youtubedl_android.YoutubeDLRequest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.io.File
import okhttp3
import okio
import org.json

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

    private fun isYouTubeUrl(url: String): Boolean {
        return url.contains("youtube.com") || url.contains("youtu.be")
    }

    private fun showNotAvailableNotification(url: String) {
        val channelId = "video_download_channel"
        val notifId = url.hashCode()

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
            .setContentTitle("Download Not Supported")
            .setContentText("Currently not available for this site.")
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)

        manager.notify(notifId, builder.build())
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
            val downloadsDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

            if (isYouTubeUrl(videoUrl)) {
                val settings = SettingsDataStore(context)
                val (qualityPref, fallbackPref) = runBlocking {
                    settings.ytQualityFlow.first() to settings.ytFallbackFlow.first()
                }

                val formatOption = buildFormatOption(qualityPref, fallbackPref)

                val request = YoutubeDLRequest(videoUrl).apply {
                    addOption("-f", formatOption)
                    addOption("-o", "${downloadsDir.absolutePath}/%(title)s.%(ext)s")
                }

                val response = YoutubeDL.getInstance().execute(request) { progress, _, _ ->
                    val percent = (progress * 100).toInt().coerceIn(0, 100)
                    builder.setProgress(100, percent, false)
                    manager.notify(notifId, builder.build())
                }

                Log.d("YouTubeDL", "Download complete: ${response.out}")
                val outputPath = response.out.trim()
                val downloadedFile = File(outputPath)

                val mimeType = when (downloadedFile.extension.lowercase()) {
                    "mp4" -> "video/mp4"
                    "webm" -> "video/webm"
                    "mkv" -> "video/x-matroska"
                    else -> "video/*"
                }

                if (downloadedFile.exists()) {
                    MediaScannerConnection.scanFile(
                        context,
                        arrayOf(downloadedFile.absolutePath),
                        arrayOf(mimeType),
                        null
                    )
                } else {
                    Log.w("YouTubeDL", "Downloaded file not found: $outputPath")
                }

                builder.setProgress(0, 0, false)
                    .setContentTitle("Download Complete")
                    .setContentText("Saved to Downloads")
                    .setAutoCancel(true)
                manager.notify(notifId, builder.build())

                runBlocking {
                    val db = AppDatabase.getInstance(context)
                    db.linkDao().insert(LinkEntity(url = videoUrl))
                }

                Result.success()
            } else {
                // --- Non-YouTube: Use API ---
                try {
                    val client = okhttp3.OkHttpClient()
                    val requestBody = okhttp3.MediaType.parse("application/json")?.let {
                        okhttp3.RequestBody.create(it, "{\"url\":\"$videoUrl\"}")
                    } ?: return Result.failure()
                    val request = okhttp3.Request.Builder()
                        .url("https://brain-rot-gallery.vercel.app/")
                        .post(requestBody)
                        .build()
                    val response = client.newCall(request).execute()
                    if (!response.isSuccessful) throw Exception("API call failed: ${response.code()}")
                    val body = response.body()?.string() ?: throw Exception("Empty API response")
                    val directUrl = org.json.JSONObject(body).optString("response")
                    if (!directUrl.startsWith("http")) throw Exception("API did not return a direct video URL: $directUrl")

                    // Download the video file
                    val fileName = "video_${System.currentTimeMillis()}.mp4" // fallback name
                    val file = File(downloadsDir, fileName)
                    val videoReq = okhttp3.Request.Builder().url(directUrl).build()
                    val videoResp = client.newCall(videoReq).execute()
                    if (!videoResp.isSuccessful) throw Exception("Video download failed: ${videoResp.code()}")
                    val sink = okio.Okio.sink(file)
                    val bufferedSink = okio.Okio.buffer(sink)
                    videoResp.body()?.source()?.let { bufferedSink.writeAll(it) }
                    bufferedSink.close()

                    // Scan file
                    MediaScannerConnection.scanFile(
                        context,
                        arrayOf(file.absolutePath),
                        arrayOf("video/mp4"),
                        null
                    )

                    builder.setProgress(0, 0, false)
                        .setContentTitle("Download Complete")
                        .setContentText("Saved to Downloads")
                        .setAutoCancel(true)
                    manager.notify(notifId, builder.build())

                    runBlocking {
                        val db = AppDatabase.getInstance(context)
                        db.linkDao().insert(LinkEntity(url = videoUrl))
                    }

                    Result.success()
                } catch (e: Exception) {
                    Log.e("API", "Error: ${e.message}")
                    showNotAvailableNotification(videoUrl)
                    Result.failure()
                }
            }
        } catch (e: YoutubeDLException) {
            Log.e("YouTubeDL", "Error: ${e.message}")
            builder.setContentTitle("Download Failed")
                .setContentText(e.localizedMessage ?: "Unknown error")
                .setAutoCancel(true)
            manager.notify(notifId, builder.build())
            Result.retry()
        }
    }

    private fun buildFormatOption(qualityPref: String?, fallbackPref: String?): String {
        val qualities = listOf("1080", "720", "480", "360")
        val fallbackSorted = when (fallbackPref ?: "next-lower") {
            "next-higher" -> qualities.sortedBy { it.toInt() }
            else -> qualities.sortedByDescending { it.toInt() }
        }

        return if (qualityPref == "BEST") {
            "bestvideo[ext=mp4]+bestaudio[ext=m4a]/mp4"
        } else {
            val base = qualityPref?.filter { it.isDigit() }?.toIntOrNull() ?: 720
            val fallbackHeights = fallbackSorted.filter {
                if ((fallbackPref ?: "next-lower") == "next-higher") it.toInt() >= base
                else it.toInt() <= base
            }

            fallbackHeights.joinToString("/") {
                "bestvideo[height<=${it}][ext=mp4]+bestaudio[ext=m4a]/mp4"
            }
        }
    }
}
