package com.example.feldelysium.presentation.editor


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.feldelysium.domain.model.DialogueEntry
import com.example.feldelysium.domain.model.Speaker
import com.example.feldelysium.domain.model.SpeakerType
import com.example.feldelysium.domain.usecase.ParseBulkImportUseCase
import com.example.feldelysium.presentation.shared.SceneAction
import com.example.feldelysium.presentation.shared.SceneViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class EditorViewModel(
    private val sceneViewModel: SceneViewModel,
    private val parseUseCase: ParseBulkImportUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(EditorUiState())
    val state: StateFlow<EditorUiState> = _state.asStateFlow()

    private val _effects = Channel<EditorEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        sceneViewModel.state
            .onEach { sceneState ->
                _state.update { editor ->
                    editor.copy(
                        entries = sceneState.scene.entries,
                        allSpeakers = sceneState.allSpeakers,
                        sceneName = sceneState.scene.title
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun dispatch(event: EditorEvent) = when (event) {

        EditorEvent.RequestAddEntry -> _state.update {
            it.copy(isSpeakerPickerOpen = true)
        }

        is EditorEvent.SelectSpeakerForNewEntry -> _state.update {
            it.copy(
                pendingSpeaker = event.speaker,
                isSpeakerPickerOpen = false,
                isEntryEditOpen = true
            )
        }

        is EditorEvent.ConfirmNewEntry -> {
            val speaker = _state.value.pendingSpeaker ?: Speaker(
                id = "-1",
                name = "NO PENDING SPEAKER",
                colorArgb = 0xFF_7E_3A_1AL,
                type = SpeakerType.CUSTOM,
            )
            val entry = DialogueEntry(
                id = UUID.randomUUID().toString(),
                speaker = speaker,
                text = event.text.trim()
            )
            sceneViewModel.dispatch(SceneAction.AddEntry(entry))
            _state.update { it.copy(pendingSpeaker = null, isEntryEditOpen = false) }
        }

        is EditorEvent.RequestEditEntry -> _state.update {
            it.copy(editingEntry = event.entry, isEntryEditOpen = true)
        }

        is EditorEvent.ConfirmEditEntry -> {
            sceneViewModel.dispatch(SceneAction.UpdateEntry(event.updated))
            _state.update { it.copy(editingEntry = null, isEntryEditOpen = false) }
        }

        is EditorEvent.DeleteEntry ->
            sceneViewModel.dispatch(SceneAction.DeleteEntry(event.entryId))

        is EditorEvent.ReorderEntries ->
            sceneViewModel.dispatch(SceneAction.ReorderEntries(event.entries))

        EditorEvent.DismissEntryEdit -> _state.update {
            it.copy(editingEntry = null, pendingSpeaker = null, isEntryEditOpen = false)
        }

        EditorEvent.RequestCustomSpeaker -> _state.update {
            it.copy(isCustomSpeakerDialogOpen = true)
        }

        is EditorEvent.ConfirmCustomSpeaker -> {
            sceneViewModel.dispatch(SceneAction.AddCustomSpeaker(event.speaker))
            _state.update { it.copy(isCustomSpeakerDialogOpen = false) }
        }

        EditorEvent.DismissCustomSpeaker -> _state.update {
            it.copy(isCustomSpeakerDialogOpen = false)
        }

        EditorEvent.OpenImportDialog -> _state.update {
            it.copy(isImportDialogOpen = true, importText = "", importError = null)
        }

        EditorEvent.CloseImportDialog -> _state.update {
            it.copy(isImportDialogOpen = false)
        }

        is EditorEvent.ImportTextChanged -> _state.update {
            it.copy(importText = event.text, importError = null)
        }

        EditorEvent.ConfirmImport -> handleImport()

        EditorEvent.OpenSpeakerPicker -> _state.update { it.copy(isSpeakerPickerOpen = true) }
        EditorEvent.CloseSpeakerPicker -> _state.update { it.copy(isSpeakerPickerOpen = false) }

        EditorEvent.NavigateToPlayback -> viewModelScope.launch {
            _effects.send(EditorEffect.NavigateToPlayback)
        }

        is EditorEvent.RenameScene ->
            sceneViewModel.dispatch(SceneAction.RenameScene(event.name))

        EditorEvent.ClearScene ->
            sceneViewModel.dispatch(SceneAction.ClearScene)
    }

    private fun handleImport() {
        val text = _state.value.importText
        if (text.isBlank()) {
            _state.update { it.copy(importError = "Nothing to import.") }
            return
        }

        val customSpeakers = sceneViewModel.state.value.customSpeakers
        val result = parseUseCase.execute(text, customSpeakers)

        if (result.entries.isEmpty()) {
            val detail = if (result.unrecognizedSpeakers.isNotEmpty())
                "Unrecognized speakers: ${result.unrecognizedSpeakers.joinToString()}"
            else "No dialogue blocks found."
            _state.update { it.copy(importError = detail) }
            return
        }

        sceneViewModel.dispatch(SceneAction.BulkAddEntries(result.entries))
        _state.update { it.copy(isImportDialogOpen = false, importText = "") }

        if (result.unrecognizedSpeakers.isNotEmpty()) {
            viewModelScope.launch {
                _effects.send(EditorEffect.ShowImportWarning(result.unrecognizedSpeakers))
            }
        }
    }

    companion object {
        fun factory(sceneViewModel: SceneViewModel) = viewModelFactory {
            initializer {
                EditorViewModel(
                    sceneViewModel = sceneViewModel,
                    parseUseCase = ParseBulkImportUseCase()
                )
            }
        }
    }
}
