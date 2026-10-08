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
                    val speakers = sceneState.allSpeakers
                    fun Speaker.fresh(): Speaker = speakers.firstOrNull { it.id == id } ?: this

                    editor.copy(
                        entries = sceneState.scene.entries,
                        allSpeakers = speakers,
                        sceneName = sceneState.scene.title,
                        editingEntry = editor.editingEntry?.let { it.copy(speaker = it.speaker.fresh()) },
                        pendingSpeaker = editor.pendingSpeaker?.fresh(),
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun dispatch(event: EditorEvent) = when (event) {

        EditorEvent.RequestAddEntry -> _state.update {
            it.copy(isSpeakerPickerOpen = true)
        }

        EditorEvent.RequestChangeSpeaker -> _state.update {
            it.copy(isSpeakerPickerOpen = true, isChangingSpeaker = true)
        }

        is EditorEvent.SelectSpeakerForNewEntry -> _state.update {
            if (it.isChangingSpeaker) {
                val editing = it.editingEntry
                if (editing != null) {
                    it.copy(
                        editingEntry = editing.copy(speaker = event.speaker),
                        isSpeakerPickerOpen = false,
                        isChangingSpeaker = false,
                    )
                } else {
                    it.copy(
                        pendingSpeaker = event.speaker,
                        isSpeakerPickerOpen = false,
                        isChangingSpeaker = false,
                    )
                }
            } else {
                it.copy(
                    pendingSpeaker = event.speaker,
                    isSpeakerPickerOpen = false,
                    isEntryEditOpen = true
                )
            }
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
            it.copy(editingSpeaker = null, isCustomSpeakerDialogOpen = true)
        }

        is EditorEvent.ConfirmCustomSpeaker -> {
            sceneViewModel.dispatch(SceneAction.AddCustomSpeaker(event.speaker))
            _state.update { it.copy(isCustomSpeakerDialogOpen = false, editingSpeaker = null) }
        }

        is EditorEvent.RequestEditSpeaker -> _state.update {
            it.copy(editingSpeaker = event.speaker, isCustomSpeakerDialogOpen = true)
        }

        is EditorEvent.ConfirmEditSpeaker -> {
            sceneViewModel.dispatch(SceneAction.UpdateCustomSpeaker(event.speaker))
            _state.update { it.copy(isCustomSpeakerDialogOpen = false, editingSpeaker = null) }
        }

        EditorEvent.DismissCustomSpeaker -> _state.update {
            it.copy(isCustomSpeakerDialogOpen = false, editingSpeaker = null)
        }

        EditorEvent.OpenImportDialog -> _state.update {
            it.copy(isImportDialogOpen = true, importText = "", importPreview = null)
        }

        EditorEvent.CloseImportDialog -> _state.update {
            it.copy(isImportDialogOpen = false)
        }

        is EditorEvent.ImportTextChanged -> _state.update {
            it.copy(importText = event.text, importPreview = previewFor(event.text))
        }

        EditorEvent.ConfirmImport -> handleImport()

        EditorEvent.OpenSpeakerPicker -> _state.update { it.copy(isSpeakerPickerOpen = true) }
        EditorEvent.CloseSpeakerPicker -> _state.update {
            it.copy(isSpeakerPickerOpen = false, isChangingSpeaker = false)
        }

        EditorEvent.NavigateToPlayback -> viewModelScope.launch {
            _effects.send(EditorEffect.NavigateToPlayback)
        }

        is EditorEvent.RenameScene ->
            sceneViewModel.dispatch(SceneAction.RenameScene(event.name))

        EditorEvent.ClearScene ->
            sceneViewModel.dispatch(SceneAction.ClearScene)
    }

    private fun previewFor(text: String): ImportPreview? {
        if (text.isBlank()) return null
        val result = parseUseCase.execute(text, sceneViewModel.state.value.customSpeakers)
        return ImportPreview(
            lineCount = result.entries.size,
            newSpeakerCount = result.newSpeakers.size,
        )
    }

    private fun handleImport() {
        val text = _state.value.importText
        if (text.isBlank()) {
            notify("Nothing to import.")
            return
        }

        val result = parseUseCase.execute(text, sceneViewModel.state.value.customSpeakers)
        if (result.entries.isEmpty()) {
            notify("No lines recognized.")
            return
        }

        result.newSpeakers.forEach { speaker ->
            sceneViewModel.dispatch(SceneAction.AddCustomSpeaker(speaker))
        }
        sceneViewModel.dispatch(SceneAction.BulkAddEntries(result.entries))
        _state.update { it.copy(isImportDialogOpen = false, importText = "", importPreview = null) }
    }

    private fun notify(message: String) {
        viewModelScope.launch { _effects.send(EditorEffect.ShowSnackbar(message)) }
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
