package com.example.feldelysium.data.speaker

import com.example.feldelysium.domain.model.SpeakerDocument
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

internal object SpeakerPackIO {
    private const val MAX_ENTRIES = 2000
    private const val MAX_JSON = 4L shl 20
    private const val MAX_ICON = 8L shl 20
    private const val MAX_TOTAL = 96L shl 20
    private val IconEntry = Regex("^icons/([A-Za-z0-9][A-Za-z0-9._-]{0,79})$")

    fun write(doc: SpeakerDocument, iconFiles: Map<String, File>, out: OutputStream) {
        val zip = ZipOutputStream(out)
        zip.putNextEntry(ZipEntry("pack.json"))
        zip.write(SpeakerJson.encode(doc).toByteArray())
        zip.closeEntry()
        iconFiles.forEach { (name, file) ->
            if (file.exists()) {
                zip.putNextEntry(ZipEntry("icons/$name"))
                file.inputStream().use { it.copyTo(zip) }
                zip.closeEntry()
            }
        }
        zip.finish()
        zip.flush()
    }

    fun read(input: InputStream, tempDir: File): SpeakerDocument {
        File(tempDir, "icons").mkdirs()
        val zip = ZipInputStream(input)
        var json: String? = null
        var entries = 0
        var total = 0L

        while (true) {
            val entry = zip.nextEntry ?: break
            if (++entries > MAX_ENTRIES) throw PackFormatException("Too many files in pack")
            if (entry.isDirectory) continue

            if (entry.name == "pack.json") {
                val bytes = readCapped(zip, MAX_JSON)
                total += bytes.size
                json = bytes.decodeToString()
            } else {
                val name = IconEntry.matchEntire(entry.name)?.groupValues?.get(1) ?: continue
                val target = File(tempDir, "icons/$name")
                target.outputStream().use { out ->
                    var left = MAX_ICON
                    val buf = ByteArray(8192)
                    while (true) {
                        val n = zip.read(buf)
                        if (n < 0) break
                        left -= n
                        if (left < 0) throw PackFormatException("Image is too large")
                        out.write(buf, 0, n)
                    }
                }
                total += target.length()
            }
            if (total > MAX_TOTAL) throw PackFormatException("Pack is too large")
        }
        return SpeakerJson.decode(
            json ?: throw PackFormatException("pack.json is missing"),
            strict = true,
        )
    }

    private fun readCapped(input: InputStream, cap: Long): ByteArray {
        val out = java.io.ByteArrayOutputStream()
        val buf = ByteArray(8192)
        while (true) {
            val n = input.read(buf)
            if (n < 0) break
            if (out.size() + n > cap) throw PackFormatException("pack.json is too large")
            out.write(buf, 0, n)
        }
        return out.toByteArray()
    }
}
