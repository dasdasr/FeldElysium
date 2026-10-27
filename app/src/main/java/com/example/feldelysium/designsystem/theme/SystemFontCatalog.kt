@file:Suppress("MatchingDeclarationName")

package com.example.feldelysium.designsystem.theme

import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.fonts.FontStyle
import android.graphics.fonts.SystemFonts
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

data class SystemFontEntry(val name: String, val path: String)

private var cache: List<SystemFontEntry>? = null

fun prettyFontName(file: File): String = file.nameWithoutExtension
    .substringBefore('-')
    .replace('_', ' ')
    .replace(Regex("(?<=[a-z])(?=[A-Z])"), " ")

suspend fun loadSystemFonts(): List<SystemFontEntry> = cache ?: withContext(Dispatchers.IO) {
    val files: List<File> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        SystemFonts.getAvailableFonts()
            .filter { it.style.weight == 400 && it.style.slant == FontStyle.FONT_SLANT_UPRIGHT }
            .mapNotNull { it.file }
    } else {
        File("/system/fonts").listFiles()
            ?.filter { f -> f.nameWithoutExtension.let { !it.contains('-') || it.endsWith("-Regular") } }
            .orEmpty()
    }
    val paint = Paint()
    files
        .filter { it.extension.lowercase() in setOf("ttf", "otf") }
        .mapNotNull { f ->
            val tf = runCatching { Typeface.createFromFile(f) }.getOrNull() ?: return@mapNotNull null
            paint.typeface = tf
            if (paint.hasGlyph("A") && paint.hasGlyph("Я")) SystemFontEntry(prettyFontName(f), f.absolutePath) else null
        }
        .distinctBy { it.name }
        .sortedBy { it.name }
        .also { cache = it }
}
