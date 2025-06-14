// QualityPreference.kt
package com.example.myapplication.ui.settings

enum class FallbackStrategy {
    NEXT_HIGHER, NEXT_LOWER
}

data class QualitySetting(
    val useBest: Boolean = true,
    val selectedQuality: String = "720p",
    val fallback: FallbackStrategy = FallbackStrategy.NEXT_LOWER
)
