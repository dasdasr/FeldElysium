
package com.example.feldelysium.data.feldpack

import android.content.Context
import android.net.Uri
import com.example.feldelysium.domain.model.Speaker
import kotlinx.serialization.json.Json
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class SpeakerPackExporter(private val context: Context) {
    private val json = Json {
        prettyPrint = true
        encodeDefaults = true
    }

    fun export(speakers: List<Speaker>, destination: File): File {
        require(speakers.map { it.id }.distinct().size == speakers.size) {
            "Speaker IDs must be unique"
        }

        destination.parentFile?.mkdirs()

        val iconBytes = linkedMapOf<String, ByteArray>()

        val speakerDtos = speakers.map { speaker ->
            val iconPath = speaker.customIconUri
                ?.let { uri ->
                    readIcon(uri)?.let { bytes ->
                        val fileName = safeFileName(speaker.id) + ".webp"
                        val path = SpeakerPackDto.ICONS_DIRECTORY + fileName
                        iconBytes[path] = bytes
                        path
                    }
                }

            speaker.toDto(iconPath)
        }

        val manifest = SpeakerPackDto(speakers = speakerDtos)
        val manifestBytes = json.encodeToString(manifest).toByteArray(Charsets.UTF_8)

        ZipOutputStream(destination.outputStream().buffered()).use { zip ->
            zip.putNextEntry(ZipEntry(SpeakerPackDto.MANIFEST_FILE))
            zip.write(manifestBytes)
            zip.closeEntry()

            iconBytes.forEach { (path, bytes) ->
                zip.putNextEntry(ZipEntry(path))
                zip.write(bytes)
                zip.closeEntry()
            }
        }

        return destination
    }

    private fun readIcon(rawUri: String): ByteArray? = runCatching {
        context.contentResolver
            .openInputStream(Uri.parse(rawUri))
            ?.use { it.readBytes() }
    }.getOrNull()

    private fun safeFileName(value: String): String = value.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        .ifBlank { "speaker" }
}
