package com.example.feldelysium.data.speaker

import android.content.Context
import android.net.Uri
import java.io.File
import java.util.UUID

class SpeakerIconStore(context: Context) {
    private val appContext = context.applicationContext
    val dir: File = File(appContext.filesDir, "speaker_icons").apply { mkdirs() }

    fun fileOf(name: String): File = File(dir, File(name).name)

    fun uriOf(name: String): String = Uri.fromFile(fileOf(name)).toString()

    fun nameOf(uri: String): String? {
        val path = Uri.parse(uri).path ?: return null
        val file = File(path)
        return file.name.takeIf { file.parentFile?.canonicalPath == dir.canonicalPath }
    }

    fun copyIn(source: Uri): String? = runCatching {
        val out = File(dir, "${UUID.randomUUID()}.img")
        appContext.contentResolver.openInputStream(source)?.use { input ->
            out.outputStream().use { input.copyTo(it) }
        } ?: return null
        Uri.fromFile(out).toString()
    }.getOrNull()

    fun adopt(file: File): String {
        val name = "${UUID.randomUUID()}.img"
        file.copyTo(File(dir, name), overwrite = true)
        return name
    }

    fun prune(referenced: Set<String>, graceMs: Long = 10 * 60 * 1000L) {
        val now = System.currentTimeMillis()
        dir.listFiles()?.forEach { f ->
            if (f.isFile && f.name !in referenced && now - f.lastModified() > graceMs) f.delete()
        }
    }
}
