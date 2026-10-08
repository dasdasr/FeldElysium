package com.example.feldelysium.presentation.shared

import androidx.lifecycle.ViewModel
import com.example.feldelysium.domain.model.SpeakerType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Activity-scoped ViewModel — shared between EditorViewModel and PlaybackViewModel.
 *
 * This is the single mutable source of truth for the session.
 * All other ViewModels read from [state] and dispatch [SceneAction]s.
 *
 * Scoping: obtain via `viewModel(activity)` in both Editor and Playback screens.
 * The NavGraph approach also works if you prefer keeping it graph-scoped.
 */
class SceneViewModel : ViewModel() {

    private val _state = MutableStateFlow(SceneState())
    val state: StateFlow<SceneState> = _state.asStateFlow()

    fun dispatch(action: SceneAction) = _state.update { reduce(it, action) }

    private fun reduce(current: SceneState, action: SceneAction): SceneState = when (action) {

        is SceneAction.AddEntry -> current.copy(
            scene = current.scene.copy(entries = current.scene.entries + action.entry)
        )

        is SceneAction.DeleteEntry -> current.copy(
            scene = current.scene.copy(
                entries = current.scene.entries.filter { it.id != action.entryId }
            )
        )

        is SceneAction.UpdateEntry -> current.copy(
            scene = current.scene.copy(
                entries = current.scene.entries.map {
                    if (it.id == action.entry.id) action.entry else it
                }
            )
        )

        is SceneAction.ReorderEntries -> current.copy(
            scene = current.scene.copy(entries = action.entries)
        )

        is SceneAction.BulkAddEntries -> current.copy(
            scene = current.scene.copy(
                entries = current.scene.entries + action.entries
            )
        )

        is SceneAction.AddCustomSpeaker -> {
            val custom = action.speaker.copy(type = SpeakerType.CUSTOM)
            current.copy(customSpeakers = current.customSpeakers + custom)
        }

        is SceneAction.UpdateCustomSpeaker -> {
            val updated = action.speaker.copy(type = SpeakerType.CUSTOM)
            if (current.customSpeakers.none { it.id == updated.id }) current
            else current.copy(
                customSpeakers = current.customSpeakers.map {
                    if (it.id == updated.id) updated else it
                },
                scene = current.scene.copy(
                    entries = current.scene.entries.map { entry ->
                        if (entry.speaker.id == updated.id) entry.copy(speaker = updated) else entry
                    }
                )
            )
        }

        is SceneAction.RemoveCustomSpeaker -> current.copy(
            customSpeakers = current.customSpeakers.filter { it.id != action.speakerId }
        )

        is SceneAction.RenameScene -> current.copy(
            scene = current.scene.copy(title = action.title)
        )

        SceneAction.ClearScene -> current.copy(
            scene = current.scene.copy(entries = emptyList())
        )
    }
}
