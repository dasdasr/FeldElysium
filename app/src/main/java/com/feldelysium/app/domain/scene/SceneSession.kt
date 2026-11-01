package com.feldelysium.app.domain.scene

import com.feldelysium.app.domain.model.DialogueEntry
import com.feldelysium.app.domain.model.DialogueScene
import com.feldelysium.app.domain.model.SceneInfo
import com.feldelysium.app.domain.model.normalized
import com.feldelysium.app.domain.repository.SceneRepository
import com.feldelysium.app.domain.repository.SpeakerRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.InputStream
import java.io.OutputStream
import kotlin.time.Duration.Companion.milliseconds

interface SceneSource {
    val scene: StateFlow<DialogueScene>
}

@OptIn(FlowPreview::class)
class SceneSession(
    private val repository: SceneRepository,
    private val speakers: SpeakerRepository,
    private val reconciler: SceneSpeakerReconciler,
    private val scope: CoroutineScope,
) : SceneSource {

    private val _scene = MutableStateFlow(DialogueScene())
    override val scene: StateFlow<DialogueScene> = _scene.asStateFlow()

    private val _isLoaded = MutableStateFlow(false)
    val isLoaded: StateFlow<Boolean> = _isLoaded.asStateFlow()

    private val switchLock = Mutex()
    private var started = false

    fun start() {
        check(!started) { "SceneSession already started" }
        started = true
        scope.launch {
            repository.loadCurrent()?.let { _scene.value = reconciler.reconcile(it) }
            _isLoaded.value = true
            launch { autosave() }
            launch { keepSpeakersFresh() }
        }
    }

    fun addEntry(entry: DialogueEntry) = addEntries(listOf(entry))

    fun addEntries(entries: List<DialogueEntry>) = edit { scene ->
        scene.copy(entries = scene.entries + entries.map { it.normalized() })
    }

    fun updateEntry(entry: DialogueEntry) = edit { scene ->
        scene.copy(entries = scene.entries.map { if (it.id == entry.id) entry.normalized() else it })
    }

    fun deleteEntry(id: String) = edit { scene ->
        scene.copy(entries = scene.entries.filterNot { it.id == id })
    }

    fun reorder(entries: List<DialogueEntry>) = edit { it.copy(entries = entries) }
    fun clear() = edit { it.copy(entries = emptyList()) }
    fun rename(title: String) = edit { it.copy(title = title) }

    suspend fun listScenes(): List<SceneInfo> = repository.list()

    suspend fun newScene() = switchTo { blankScene() }

    suspend fun openScene(id: String) {
        if (id != _scene.value.id) switchTo { repository.open(id)?.let { reconciler.reconcile(it) } }
    }

    suspend fun deleteScene(id: String) = switchLock.withLock {
        repository.delete(id)
        if (id == _scene.value.id) _scene.value = blankScene()
    }

    suspend fun clearScene(id: String) {
        if (id == _scene.value.id) {
            clear()
            return
        }
        switchLock.withLock {
            repository.open(id)?.let { repository.save(it.copy(entries = emptyList())) }
            repository.save(_scene.value)
        }
    }

    suspend fun renameSceneById(id: String, title: String) {
        if (id == _scene.value.id) {
            rename(title)
            return
        }
        switchLock.withLock {
            repository.open(id)?.let { repository.save(it.copy(title = title)) }
            repository.save(_scene.value)
        }
    }

    suspend fun importScene(input: InputStream) = switchLock.withLock {
        repository.save(_scene.value)
        val imported = reconciler.reconcile(repository.import(input))
        repository.save(imported)
        _scene.value = imported
    }

    suspend fun exportScene(output: OutputStream) = repository.export(_scene.value, output)

    suspend fun save() {
        if (_isLoaded.value) repository.save(_scene.value)
    }

    /** Fire-and-forget for onStop. */
    fun flush() {
        scope.launch { save() }
    }

    private fun edit(block: (DialogueScene) -> DialogueScene) = _scene.update(block)

    private suspend fun switchTo(next: suspend () -> DialogueScene?) = switchLock.withLock {
        repository.save(_scene.value)
        next()?.let { _scene.value = it }
    }

    private suspend fun autosave() {
        _scene.drop(1).debounce(500.milliseconds).collect { repository.save(it) }
    }

    private suspend fun keepSpeakersFresh() {
        speakers.speakers.collect { list ->
            val byId = list.associateBy { it.id }
            _scene.update { scene ->
                val synced = scene.entries.map { e ->
                    val fresh = byId[e.speaker.id]
                    if (fresh != null && fresh != e.speaker) e.copy(speaker = fresh).normalized() else e
                }
                if (synced == scene.entries) scene else scene.copy(entries = synced)
            }
        }
    }

    private fun blankScene() = DialogueScene(title = "Untitled Scene")
}
