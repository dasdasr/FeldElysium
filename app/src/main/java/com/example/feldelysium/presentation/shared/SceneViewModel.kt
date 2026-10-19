package com.example.feldelysium.presentation.shared

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.feldelysium.domain.model.DialogueScene
import com.example.feldelysium.domain.model.SceneInfo
import com.example.feldelysium.domain.model.SpeakerType
import com.example.feldelysium.domain.model.normalized
import com.example.feldelysium.domain.usecase.SceneUseCases
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.InputStream
import java.io.OutputStream
import kotlin.time.Duration.Companion.milliseconds

/**
 * Activity-scoped ViewModel — shared between EditorViewModel and PlaybackViewModel.
 *
 * This is the single mutable source of truth for the session.
 * All other ViewModels read from [state] and dispatch [SceneAction]s.
 *
 * Scoping: obtain via `viewModel(activity)` in both Editor and Playback screens.
 * The NavGraph approach also works if you prefer keeping it graph-scoped.
 */
@OptIn(FlowPreview::class)
class SceneViewModel(private val useCases: SceneUseCases) : ViewModel() {

    private val _state = MutableStateFlow(SceneState())
    val state: StateFlow<SceneState> = _state.asStateFlow()

    @Volatile private var loaded = false

    init {
        viewModelScope.launch {
            val saved = useCases.loadCurrent()
            _state.update { it.copy(scene = saved ?: it.scene, isLoaded = true) }
            loaded = true
            _state.map { it.scene }
                .distinctUntilChanged()
                .drop(1)
                .debounce(500.milliseconds)
                .collect { useCases.save(it) }
        }
    }

    fun dispatch(action: SceneAction) = _state.update { reduce(it, action) }

    fun saveNow() {
        if (loaded) viewModelScope.launch { useCases.save(_state.value.scene) }
    }

    suspend fun scenes(): List<SceneInfo> = useCases.list()

    fun newScene() = viewModelScope.launch {
        useCases.save(_state.value.scene)
        dispatch(SceneAction.LoadScene(DialogueScene(title = "Untitled Scene")))
    }

    fun openScene(id: String) = viewModelScope.launch {
        if (id == _state.value.scene.id) return@launch
        useCases.save(_state.value.scene)
        useCases.open(id)?.let { dispatch(SceneAction.LoadScene(it)) }
    }

    fun deleteScene(id: String) = viewModelScope.launch {
        useCases.delete(id)
        if (id == _state.value.scene.id) {
            dispatch(SceneAction.LoadScene(DialogueScene(title = "Untitled Scene")))
        }
    }

    suspend fun exportScene(out: OutputStream) = useCases.export(_state.value.scene, out)

    suspend fun importScene(input: InputStream) {
        useCases.save(_state.value.scene)
        dispatch(SceneAction.LoadScene(useCases.import(input)))
    }

    private fun reduce(current: SceneState, action: SceneAction): SceneState = when (action) {
        is SceneAction.AddEntry -> current.copy(
            scene = current.scene.copy(entries = current.scene.entries + action.entry.normalized()),
        )

        is SceneAction.DeleteEntry -> current.copy(
            scene = current.scene.copy(
                entries = current.scene.entries.filter { it.id != action.entryId },
            ),
        )

        is SceneAction.UpdateEntry -> current.copy(
            scene = current.scene.copy(
                entries = current.scene.entries.map {
                    if (it.id == action.entry.id) action.entry.normalized() else it
                },
            ),
        )

        is SceneAction.ReorderEntries -> current.copy(
            scene = current.scene.copy(entries = action.entries),
        )

        is SceneAction.BulkAddEntries -> current.copy(
            scene = current.scene.copy(
                entries = current.scene.entries + action.entries.map { it.normalized() },
            ),
        )

        is SceneAction.AddCustomSpeaker -> {
            val custom = action.speaker.copy(type = SpeakerType.CUSTOM)
            current.copy(customSpeakers = current.customSpeakers + custom)
        }

        is SceneAction.UpdateCustomSpeaker -> {
            val updated = action.speaker.copy(type = SpeakerType.CUSTOM)
            if (current.customSpeakers.none { it.id == updated.id }) {
                current
            } else {
                current.copy(
                    customSpeakers = current.customSpeakers.map {
                        if (it.id == updated.id) updated else it
                    },
                    scene = current.scene.copy(
                        entries = current.scene.entries.map { entry ->
                            if (entry.speaker.id == updated.id) entry.copy(speaker = updated) else entry
                        },
                    ),
                )
            }
        }

        is SceneAction.RemoveCustomSpeaker -> current.copy(
            customSpeakers = current.customSpeakers.filter { it.id != action.speakerId },
        )

        is SceneAction.RenameScene -> current.copy(
            scene = current.scene.copy(title = action.title),
        )

        is SceneAction.LoadScene -> current.copy(scene = action.scene)

        SceneAction.ClearScene -> current.copy(
            scene = current.scene.copy(entries = emptyList()),
        )

        is SceneAction.SyncSpeakers -> {
            val byId = action.speakers.associateBy { it.id }
            val updated = current.scene.entries.map { entry ->
                val fresh = byId[entry.speaker.id]
                val synced = if (fresh != null && fresh != entry.speaker) entry.copy(speaker = fresh) else entry
                synced.normalized()
            }
            if (updated == current.scene.entries) {
                current
            } else {
                current.copy(scene = current.scene.copy(entries = updated))
            }
        }
    }
}
