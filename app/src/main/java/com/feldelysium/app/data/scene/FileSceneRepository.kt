package com.feldelysium.app.data.scene

import android.content.Context
import androidx.core.content.edit
import com.feldelysium.app.domain.model.DialogueScene
import com.feldelysium.app.domain.model.SceneInfo
import com.feldelysium.app.domain.repository.SceneRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileFilter
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

class FileSceneRepository(context: Context, private val io: CoroutineDispatcher = Dispatchers.IO) : SceneRepository {

    private val dir = File(context.filesDir, "scenes").apply { mkdirs() }
    private val prefs = context.getSharedPreferences("scene_store", Context.MODE_PRIVATE)
    private val mutex = Mutex()
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private var currentId: String?
        get() = prefs.getString("current", null)
        set(value) = prefs.edit { putString("current", value) }

    private fun fileOf(id: String) = File(dir, "$id.json")

    private fun read(file: File): SceneFile? =
        if (!file.exists()) null else runCatching { json.decodeFromString<SceneFile>(file.readText()) }.getOrNull()

    override suspend fun loadCurrent(): DialogueScene? = currentId?.let { open(it) }

    override suspend fun open(id: String): DialogueScene? = withContext(io) {
        read(fileOf(id))?.toScene()?.also { currentId = id }
    }

    override suspend fun save(scene: DialogueScene) = withContext(io) {
        mutex.withLock {
            val tmp = File(dir, "${scene.id}.tmp")
            tmp.writeText(json.encodeToString(scene.toFile()))
            val target = fileOf(scene.id)
            if (!tmp.renameTo(target)) {
                tmp.copyTo(target, overwrite = true)
                tmp.delete()
            }
            currentId = scene.id
        }
    }

    override suspend fun delete(id: String) = withContext(io) {
        mutex.withLock {
            fileOf(id).delete()
            if (currentId == id) currentId = null
        }
    }

    override suspend fun list(): List<SceneInfo> = withContext(io) {
        dir.listFiles(FileFilter { it.extension == "json" }).orEmpty()
            .mapNotNull { f ->
                read(f)?.toScene()?.let { sc ->
                    SceneInfo(
                        id = sc.id,
                        title = sc.title,
                        lines = sc.entries.size,
                        lengths = sc.entries.map { it.text.length },
                        modified = f.lastModified(),
                        preview = sc.entries.firstOrNull()?.text.orEmpty().take(140),
                        barcode = sc.entries.map { it.speaker.colorArgb },
                    )
                }
            }
            .sortedByDescending { it.modified }
    }

    override suspend fun export(scene: DialogueScene, output: OutputStream) = withContext(io) {
        output.write(json.encodeToString(scene.toFile()).toByteArray())
    }

    override suspend fun import(input: InputStream): DialogueScene = withContext(io) {
        val file = json.decodeFromString<SceneFile>(input.bufferedReader().readText())
        require(file.version <= SceneFile.VERSION) { "Scene file is from a newer version" }
        file.toScene().copy(id = UUID.randomUUID().toString())
    }
}
