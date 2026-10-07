package com.example.feldelysium.presentation.shared

import com.example.feldelysium.domain.model.DialogueEntry
import com.example.feldelysium.domain.model.Speaker

/** Actions that mutate [SceneState]. */
sealed class SceneAction {
    data class AddEntry(val entry: DialogueEntry) : SceneAction()
    data class DeleteEntry(val entryId: String) : SceneAction()
    data class UpdateEntry(val entry: DialogueEntry) : SceneAction()
    data class ReorderEntries(val entries: List<DialogueEntry>) : SceneAction()
    data class BulkAddEntries(val entries: List<DialogueEntry>) : SceneAction()

    data class AddCustomSpeaker(val speaker: Speaker) : SceneAction()
    data class UpdateCustomSpeaker(val speaker: Speaker) : SceneAction()
    data class RemoveCustomSpeaker(val speakerId: String) : SceneAction()

    data class RenameScene(val title: String) : SceneAction()
    data object ClearScene : SceneAction()
}
