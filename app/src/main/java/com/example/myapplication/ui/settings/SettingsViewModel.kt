// SettingsViewModel.kt
package com.example.myapplication.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.myapplication.ui.settings.QualitySetting

class SettingsViewModel : ViewModel() {
    var youtubeSettings by mutableStateOf(QualitySetting())
    var instaSettings by mutableStateOf(QualitySetting())
}
