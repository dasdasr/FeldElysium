package com.example.feldelysium.presentation.editor

import com.example.feldelysium.domain.model.DialogueEntry
import com.example.feldelysium.domain.model.Speaker

/**
 * All state required to render the Editor screen.
 * Derived from [SceneState] + local editor UI concerns.
 */
data class EditorUiState(
    // Synced from SceneState
    val entries: List<DialogueEntry> = emptyList(),
    val allSpeakers: List<Speaker> = emptyList(),
    val sceneName: String = "Untitled Scene",

    // Local UI state
    val editingEntry: DialogueEntry? = null,
    val pendingSpeaker: Speaker? = null,  // selected for new entry, awaiting text input

    // Dialog visibility
    val isSpeakerPickerOpen: Boolean = false,
    val isEntryEditOpen: Boolean = false,
    val isImportDialogOpen: Boolean = false,
    val isCustomSpeakerDialogOpen: Boolean = false,

    // Import state
    val importText: String = "",
    val importError: String? = null
)

sealed class EditorEvent {
    // Entry management
    data object RequestAddEntry : EditorEvent()
    data class SelectSpeakerForNewEntry(val speaker: Speaker) : EditorEvent()
    data class ConfirmNewEntry(val text: String) : EditorEvent()
    data class RequestEditEntry(val entry: DialogueEntry) : EditorEvent()
    data class ConfirmEditEntry(val updated: DialogueEntry) : EditorEvent()
    data class DeleteEntry(val entryId: String) : EditorEvent()
    data class ReorderEntries(val entries: List<DialogueEntry>) : EditorEvent()
    data object DismissEntryEdit : EditorEvent()

    // Custom speaker management
    data object RequestCustomSpeaker : EditorEvent()
    data class ConfirmCustomSpeaker(val speaker: Speaker) : EditorEvent()
    data object DismissCustomSpeaker : EditorEvent()

    // Bulk import
    data object OpenImportDialog : EditorEvent()
    data object CloseImportDialog : EditorEvent()
    data class ImportTextChanged(val text: String) : EditorEvent()
    data object ConfirmImport : EditorEvent()

    // Speaker picker
    data object OpenSpeakerPicker : EditorEvent()
    data object CloseSpeakerPicker : EditorEvent()

    // Navigation / scene
    data object NavigateToPlayback : EditorEvent()
    data class RenameScene(val name: String) : EditorEvent()
    data object ClearScene : EditorEvent()
}

sealed class EditorEffect {
    data object NavigateToPlayback : EditorEffect()
    data class ShowSnackbar(val message: String) : EditorEffect()
    data class ShowImportWarning(val skipped: List<String>) : EditorEffect()
    data object LaunchGalleryPicker : EditorEffect() // for custom speaker icon
}