package com.feldelysium.designsystem.foundation.decal

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Immutable
data class DecalContext(
    val sceneName: String = "",
    val lineNumber: Int? = null,
    val lineCount: Int? = null,
    val segmentFrom: Int? = null,
    val segmentTo: Int? = null,
    val speakerName: String? = null,
    val appVersion: String? = null,
    val date: String = retroDate(),
)

fun retroDate(): String = SimpleDateFormat("MM/dd/yy", Locale.US).format(Date())

val LocalFeldDecalContext = compositionLocalOf { DecalContext() }
