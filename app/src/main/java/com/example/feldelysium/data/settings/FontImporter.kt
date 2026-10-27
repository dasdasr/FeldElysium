package com.example.feldelysium.data.settings

import android.content.ContentResolver
import android.graphics.Typeface
import android.net.Uri
import android.provider.OpenableColumns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class FontImporter(
    private val resolver: ContentResolver,
    private val fontDir: File,
) {
    data class Imported(val fileName: String, val displayName: String)

    suspend fun import(uri: Uri): Imported? = withContext(Dispatchers.IO) {
        fontDir.mkdirs()
        val target = File(fontDir, "font_${System.currentTimeMillis()}")
        val valid = runCatching {
            val input = resolver.openInputStream(uri) ?: error("no stream")
            input.use { src -> target.outputStream().use { src.copyTo(it) } }
            check(target.length() in 1..MAX_FONT_BYTES)
            check(Typeface.createFromFile(target) !== Typeface.DEFAULT)
        }.isSuccess

        if (!valid) {
            target.delete()
            return@withContext null
        }
        Imported(fileName = target.name, displayName = displayName(uri))
    }

    suspend fun delete(fileName: String) {
        if (fileName.isBlank()) return
        withContext(Dispatchers.IO) { File(fontDir, fileName).delete() }
    }

    private fun displayName(uri: Uri): String =
        resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            ?.use { if (it.moveToFirst()) it.getString(0) else null }
            ?.substringBeforeLast('.')
            ?: "Custom font"

    private companion object {
        const val MAX_FONT_BYTES = 10L * 1024 * 1024
    }
}
