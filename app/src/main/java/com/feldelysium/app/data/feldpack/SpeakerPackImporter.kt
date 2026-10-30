
package com.feldelysium.app.data.feldpack

import android.content.Context
import com.feldelysium.app.domain.model.Speaker
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.File
import java.util.zip.ZipFile

class SpeakerPackImporter(private val context: Context) {
    private val json = Json {
        ignoreUnknownKeys = true
    }

    fun import(source: File): List<Speaker> {
        require(source.isFile) {
            "Pack file does not exist"
        }

        ZipFile(source).use { zip ->
            val manifestEntry = zip.getEntry(
                SpeakerPackDto.MANIFEST_FILE,
            ) ?: throw IllegalArgumentException(
                "Missing pack.json",
            )

            require(!manifestEntry.isDirectory) {
                "Invalid pack.json"
            }

            require(manifestEntry.size <= MAX_MANIFEST_BYTES) {
                "Pack manifest is too large"
            }

            val manifestText = zip.getInputStream(manifestEntry)
                .bufferedReader(Charsets.UTF_8)
                .use { it.readText() }

            val pack = try {
                json.decodeFromString<SpeakerPackDto>(manifestText)
            } catch (exception: SerializationException) {
                throw IllegalArgumentException(
                    "Invalid pack manifest",
                    exception,
                )
            }

            require(pack.format == SpeakerPackDto.FORMAT) {
                "Unsupported pack format: ${pack.format}"
            }

            require(pack.version == SpeakerPackDto.CURRENT_VERSION) {
                "Unsupported pack version: ${pack.version}"
            }

            require(
                pack.speakers.map { it.id }.distinct().size ==
                    pack.speakers.size,
            ) {
                "Duplicate speaker IDs in pack"
            }

            return pack.speakers.map { dto ->
                val iconUri = dto.icon?.let { path ->
                    restoreIcon(zip, path, dto.id)
                }

                dto.toSpeaker(iconUri)
            }
        }
    }

    private fun restoreIcon(zip: ZipFile, path: String, speakerId: String): String? {
        // Only accept files inside the icons/ directory
        if (!path.startsWith(SpeakerPackDto.ICONS_DIRECTORY)) {
            return null
        }

        if (path.contains("..") || path.contains('\\')) {
            return null
        }

        val entry = zip.getEntry(path) ?: return null

        if (entry.isDirectory || entry.size > MAX_ICON_BYTES) {
            return null
        }

        val directory = File(context.filesDir, "speaker_icons")
        if (!directory.exists() && !directory.mkdirs()) {
            throw IllegalStateException(
                "Cannot create speaker icon directory",
            )
        }

        val safeId = speakerId.replace(
            Regex("[^a-zA-Z0-9_-]"),
            "_",
        ).ifBlank { "speaker" }

        val extension = path.substringAfterLast(
            '/',
            "",
        ).substringAfterLast('.', "webp")
            .lowercase()
            .takeIf { it in setOf("webp", "png", "jpg", "jpeg") }
            ?: "webp"

        val destination = File(
            directory,
            "${safeId}_${System.nanoTime()}.$extension",
        )

        return try {
            zip.getInputStream(entry).use { input ->
                destination.outputStream().buffered().use { output ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    var total = 0L

                    while (true) {
                        val count = input.read(buffer)
                        if (count == -1) break

                        total += count
                        if (total > MAX_ICON_BYTES) {
                            throw IllegalArgumentException(
                                "Icon is too large",
                            )
                        }

                        output.write(buffer, 0, count)
                    }
                }
            }

            destination.toURI().toString()
        } catch (exception: Exception) {
            destination.delete()
            throw exception
        }
    }

    private companion object {
        const val MAX_MANIFEST_BYTES = 2L * 1024 * 1024
        const val MAX_ICON_BYTES = 10L * 1024 * 1024
    }
}
