package com.example.feldelysium.data.settings

enum class BackgroundKind { MEDIA, COLOR }

data class AppSettings(
    val lockAspectRatio16x9: Boolean = true,
    val playbackMoney: String = "40.95",
    val playbackTime: String = "16:36",
    val playbackDay: String = "Day 3",
    val backgroundKind: BackgroundKind = BackgroundKind.COLOR,
    val backgroundColorArgb: Long = DEFAULT_BACKGROUND_COLOR,
    val backgroundMediaUri: String = "",
    val backgroundMediaIsVideo: Boolean = false,
    val backgroundVideoSound: Boolean = false,
    val backgroundVideoFill: Boolean = true,
) {
    companion object {
        const val DEFAULT_BACKGROUND_COLOR = 0xFF000000L
    }
}
