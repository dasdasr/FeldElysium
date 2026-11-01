package com.feldelysium.app.presentation.editor

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.saveable.Saver
import com.feldelysium.app.R
import com.feldelysium.app.domain.model.DamageEffect
import com.feldelysium.app.domain.model.DialogueEntry
import com.feldelysium.app.domain.model.ReplyChoices
import com.feldelysium.app.domain.model.SceneInfo
import com.feldelysium.app.domain.model.SkillCheck
import com.feldelysium.app.domain.model.Speaker
import com.feldelysium.app.domain.model.SpeakerType
import com.feldelysium.app.domain.registry.PredefinedSpeakers
import com.feldelysium.app.presentation.shared.FeldError
import com.feldelysium.app.presentation.shared.toastText
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

@Immutable
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

data class ImportPreview(val lineCount: Int, val newSpeakerCount: Int)

sealed interface EditorEvent {
    data class AddEntry(
        val speakerId: String,
        val text: String,
        val check: SkillCheck?,
        val damage: DamageEffect?,
        val replies: ReplyChoices?,
    ) : EditorEvent

    data class UpdateEntry(val entry: DialogueEntry) : EditorEvent
    data class DeleteEntry(val id: String) : EditorEvent
    data class ReorderEntries(val entries: List<DialogueEntry>) : EditorEvent
    data object ClearScene : EditorEvent
    data class SaveSpeaker(val speaker: Speaker) : EditorEvent
    data class DeleteSpeaker(val id: String) : EditorEvent
    data class SaveTag(val tag: SpeakerType) : EditorEvent
    data class DeleteTag(val id: String) : EditorEvent
    data class ImportLines(val text: String) : EditorEvent
    data object RefreshScenes : EditorEvent
    data object NewScene : EditorEvent
    data class OpenScene(val id: String) : EditorEvent
    data class DeleteScene(val id: String) : EditorEvent
    data class ClearSceneById(val id: String) : EditorEvent
    data class RenameSceneById(val id: String, val title: String) : EditorEvent
    data class ExportScene(val output: OutputStream) : EditorEvent
    data class ImportScene(val input: InputStream) : EditorEvent
}

fun ImportPreview.caption(context: Context): String {
    if (lineCount == 0) return context.getString(R.string.import_preview_no_lines)

    val res = context.resources
    val lines = res.getQuantityString(R.plurals.import_preview_lines, lineCount, lineCount)
    val speakers = if (newSpeakerCount == 0) {
        null
    } else {
        res.getQuantityString(R.plurals.import_preview_new_speakers, newSpeakerCount, newSpeakerCount)
    }

    return if (speakers == null) lines else "$lines · $speakers"
}

@Serializable
sealed interface EditorDialog {
    @Serializable
    data object None : EditorDialog

    @Serializable
    data class NewEntry(val speakerId: String? = null) : EditorDialog

    @Serializable
    data class EditEntry(val entryId: String, val speakerId: String, val pickingSpeaker: Boolean = false) :
        EditorDialog

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

sealed interface EditorMessage {
    data class Text(@StringRes val resId: Int) : EditorMessage
    data class Error(val error: FeldError) : EditorMessage
}

fun EditorMessage.resolve(context: Context): String = when (this) {
    is EditorMessage.Text -> context.getString(resId)
    is EditorMessage.Error -> error.toastText(context)
}
