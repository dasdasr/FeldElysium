package com.feldelysium.app.data.speaker

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import java.io.File
import java.security.MessageDigest
import java.util.UUID

class SpeakerIconStore(context: Context) {
    private val appContext = context.applicationContext
    val dir: File = File(appContext.filesDir, "speaker_icons").apply { mkdirs() }

    fun fileOf(name: String): File = File(dir, File(name).name)

    fun uriOf(name: String): String = Uri.fromFile(fileOf(name)).toString()

    fun nameOf(uri: String): String? {
        val path = uri.toUri().path ?: return null
        val file = File(path)
        return file.name.takeIf { file.parentFile?.canonicalPath == dir.canonicalPath }
    }

    fun copyIn(source: Uri): String? = runCatching {
        val tmp = File(dir, "${UUID.randomUUID()}.tmp")
        val digest = MessageDigest.getInstance("SHA-256")

        appContext.contentResolver.openInputStream(source)?.use { input ->
            tmp.outputStream().use { output ->
                val buf = ByteArray(8192)
                while (true) {
                    val n = input.read(buf)
                    if (n < 0) break
                    digest.update(buf, 0, n)
                    output.write(buf, 0, n)
                }
            }
        } ?: run {
            tmp.delete()
            return null
        }

        val hash = digest.digest().joinToString("") { "%02x".format(it) }
        val target = File(dir, "$hash.img")

        if (target.exists()) {
            tmp.delete()
            target.setLastModified(System.currentTimeMillis())
        } else if (!tmp.renameTo(target)) {
            tmp.delete()
            return null
        }

        Uri.fromFile(target).toString()
    }.getOrNull()

    fun allUris(): List<String> = dir.listFiles()
        ?.filter { it.isFile }
        ?.sortedByDescending { it.lastModified() }
        ?.map { Uri.fromFile(it).toString() }
        .orEmpty()

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
