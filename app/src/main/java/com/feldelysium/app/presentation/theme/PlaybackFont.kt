package com.feldelysium.app.presentation.theme

import android.content.Context
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.feldelysium.app.domain.model.settings.AppSettings
import com.feldelysium.designsystem.theme.Alegreya
import java.io.File

const val FONT_CUSTOM = "custom"

fun Context.fontDir(): File = File(filesDir, "fonts")

@Composable
fun rememberCustomFontFamily(fileName: String): FontFamily? {
    val context = LocalContext.current
    return remember(fileName) {
        fileName.takeIf { it.isNotBlank() }
            ?.let { File(context.fontDir(), it) }
            ?.takeIf { it.exists() }
            ?.let { FontFamily(Font(it)) }
    }
}

@Composable
fun rememberPlaybackFontFamily(settings: AppSettings): FontFamily {
    val context = LocalContext.current
    return remember(settings.fontChoice, settings.customFontFile) {
        when (settings.fontChoice) {
            "" -> null
            FONT_CUSTOM -> File(context.fontDir(), settings.customFontFile)
            else -> File(settings.fontChoice)
        }?.takeIf { it.isFile }
            ?.let { FontFamily(Font(it)) }
            ?: Alegreya
    }
}

@Composable
fun ProvidePlaybackFont(settings: AppSettings, content: @Composable () -> Unit) {
    val family = rememberPlaybackFontFamily(settings)
    val base = MaterialTheme.typography
    val typography = remember(base, family) {
        base.copy(
            bodyMedium = base.bodyMedium.copy(fontFamily = family),
            bodySmall = base.bodySmall.copy(fontFamily = family),
            labelSmall = base.labelSmall.copy(fontFamily = family),
        )
    }
    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme,
        shapes = MaterialTheme.shapes,
        typography = typography,
        content = content,
    )
}
