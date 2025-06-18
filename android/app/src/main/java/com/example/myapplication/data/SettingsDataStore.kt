package com.example.myapplication.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val DATASTORE_NAME = "user_settings"

private val Context.dataStore by preferencesDataStore(DATASTORE_NAME)

object PreferenceKeys {
    val ytQuality = stringPreferencesKey("yt_quality")
    val ytFallback = stringPreferencesKey("yt_fallback")

    val instaQuality = stringPreferencesKey("insta_quality")
    val instaFallback = stringPreferencesKey("insta_fallback")
}

class SettingsDataStore(private val context: Context) {

    val ytQualityFlow: Flow<String?> = context.dataStore.data
        .map { it[PreferenceKeys.ytQuality] }

    val ytFallbackFlow: Flow<String?> = context.dataStore.data
        .map { it[PreferenceKeys.ytFallback] }

    val instaQualityFlow: Flow<String?> = context.dataStore.data
        .map { it[PreferenceKeys.instaQuality] }

    val instaFallbackFlow: Flow<String?> = context.dataStore.data
        .map { it[PreferenceKeys.instaFallback] }

    suspend fun setYoutubeSettings(quality: String, fallback: String) {
        context.dataStore.edit {
            it[PreferenceKeys.ytQuality] = quality
            it[PreferenceKeys.ytFallback] = fallback
        }
    }

    suspend fun setInstagramSettings(quality: String, fallback: String) {
        context.dataStore.edit {
            it[PreferenceKeys.instaQuality] = quality
            it[PreferenceKeys.instaFallback] = fallback
        }
    }
}
