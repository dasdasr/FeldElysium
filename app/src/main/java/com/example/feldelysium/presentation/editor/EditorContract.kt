package com.example.feldelysium.presentation.editor

import kotlinx.serialization.json.Json
import androidx.compose.runtime.saveable.Saver
import com.example.feldelysium.domain.model.DialogueEntry
import com.example.feldelysium.domain.model.SceneInfo
import com.example.feldelysium.domain.model.Speaker
import com.example.feldelysium.domain.model.SpeakerType
import com.example.feldelysium.domain.registry.PredefinedSpeakers
import kotlinx.serialization.Serializable

data class EditorUiState(
    val sceneId: String = "",
    val sceneName: String = "Untitled Scene",
    val entries: List<DialogueEntry> = emptyList(),
    val allSpeakers: List<Speaker> = PredefinedSpeakers.ALL,
    val pickerSpeakers: List<Speaker> = PredefinedSpeakers.ALL,
    val tags: List<SpeakerType> = PredefinedSpeakers.TAGS,
    val scenes: List<SceneInfo> = emptyList(),
) {
    fun speaker(id: String) = allSpeakers.firstOrNull { it.id == id }
    fun entry(id: String) = entries.firstOrNull { it.id == id }
}

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

@Serializable
sealed interface EditorDialog {
    @Serializable
    data object None : EditorDialog

    @Serializable
    data class NewEntry(val speakerId: String? = null) : EditorDialog

    @Serializable
    data class EditEntry(
        val entryId: String,
        val speakerId: String,
        val pickingSpeaker: Boolean = false,
    ) : EditorDialog

    @Serializable
    data class EditSpeaker(val speakerId: String?, val back: EditorDialog = None) : EditorDialog

    @Serializable
    data object ImportLines : EditorDialog

    @Serializable
    data object SceneManager : EditorDialog
}

val EditorDialogSaver = Saver<EditorDialog, String>(
    save = { Json.encodeToString(EditorDialog.serializer(), it) },
    restore = { Json.decodeFromString(EditorDialog.serializer(), it) },
)
