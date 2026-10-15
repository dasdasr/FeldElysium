package com.example.feldelysium.presentation.editor

import com.example.feldelysium.domain.model.DamageEffect
import com.example.feldelysium.domain.model.DialogueEntry
import com.example.feldelysium.domain.model.SceneInfo
import com.example.feldelysium.domain.model.SkillCheck
import com.example.feldelysium.domain.model.Speaker
import com.example.feldelysium.domain.model.SpeakerType
import com.example.feldelysium.domain.registry.PredefinedSpeakers
import java.io.InputStream
import java.io.OutputStream

data class EditorUiState(
    val entries: List<DialogueEntry> = emptyList(),
    val allSpeakers: List<Speaker> = PredefinedSpeakers.ALL,
    val sceneName: String = "Untitled Scene",

    val tags: List<SpeakerType> = PredefinedSpeakers.TAGS,

    val editingEntry: DialogueEntry? = null,
    val pendingSpeaker: Speaker? = null,

    val isSpeakerPickerOpen: Boolean = false,
    val isEntryEditOpen: Boolean = false,
    val isImportDialogOpen: Boolean = false,
    val isCustomSpeakerDialogOpen: Boolean = false,

    val importText: String = "",
    val importPreview: ImportPreview? = null,

    val editingSpeaker: Speaker? = null,
    val isChangingSpeaker: Boolean = false,

    val sceneId: String = "",
    val isSceneManagerOpen: Boolean = false,
    val scenes: List<SceneInfo> = emptyList(),
)

data class ImportPreview(val lineCount: Int, val newSpeakerCount: Int) {
    val caption: String
        get() {
            if (lineCount == 0) return "No lines"
            val lines = if (lineCount == 1) "1 line" else "$lineCount lines"
            if (newSpeakerCount == 0) return lines
            val speakers =
                if (newSpeakerCount == 1) "1 new speaker" else "$newSpeakerCount new speakers"
            return "$lines · $speakers"
        }
}

sealed class EditorEvent {
    data object RequestAddEntry : EditorEvent()
    data class SelectSpeakerForNewEntry(val speaker: Speaker) : EditorEvent()
    data class ConfirmNewEntry(val text: String, val check: SkillCheck? = null, val damage: DamageEffect? = null) :
        EditorEvent()

    data class RequestEditEntry(val entry: DialogueEntry) : EditorEvent()
    data class ConfirmEditEntry(val updated: DialogueEntry) : EditorEvent()
    data class DeleteEntry(val entryId: String) : EditorEvent()
    data class ReorderEntries(val entries: List<DialogueEntry>) : EditorEvent()
    data object DismissEntryEdit : EditorEvent()

    data object RequestCustomSpeaker : EditorEvent()
    data class ConfirmCustomSpeaker(val speaker: Speaker) : EditorEvent()
    data class RequestEditSpeaker(val speaker: Speaker) : EditorEvent()
    data class ConfirmEditSpeaker(val speaker: Speaker) : EditorEvent()
    data object DismissCustomSpeaker : EditorEvent()

    data class SaveTag(val tag: SpeakerType) : EditorEvent()
    data class DeleteTag(val tag: SpeakerType) : EditorEvent()
    data class DeleteSpeaker(val id: String) : EditorEvent()

    data object OpenImportDialog : EditorEvent()
    data object CloseImportDialog : EditorEvent()
    data class ImportTextChanged(val text: String) : EditorEvent()
    data object ConfirmImport : EditorEvent()

    data object OpenSpeakerPicker : EditorEvent()
    data object CloseSpeakerPicker : EditorEvent()
    data object RequestChangeSpeaker : EditorEvent()
    data object AddEntryWithRandomSpeaker : EditorEvent()

    data object NavigateToPlayback : EditorEvent()
    data class RenameScene(val name: String) : EditorEvent()
    data object ClearScene : EditorEvent()

    data object SaveScene : EditorEvent()
    data object OpenSceneManager : EditorEvent()
    data object CloseSceneManager : EditorEvent()
    data object NewScene : EditorEvent()
    data class OpenScene(val id: String) : EditorEvent()
    data class DeleteScene(val id: String) : EditorEvent()
    data class ExportScene(val output: OutputStream?) : EditorEvent()
    data class ImportScene(val input: InputStream?) : EditorEvent()
}

sealed class EditorEffect {
    data object NavigateToPlayback : EditorEffect()
    data class ShowSnackbar(val message: String) : EditorEffect()
    data object LaunchGalleryPicker : EditorEffect()
}
