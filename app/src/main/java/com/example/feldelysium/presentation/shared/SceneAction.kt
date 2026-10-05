package com.example.feldelysium.presentation.shared

import com.example.feldelysium.domain.model.DialogueEntry
import com.example.feldelysium.domain.model.Speaker

/**
 * All possible mutations to [SceneState].
 *
 * Dispatched by EditorViewModel and PlaybackViewModel.
 * Processed exclusively inside [SceneViewModel.reduce].
 */
sealed class SceneAction {
    // Dialogue entries
    data class AddEntry(val entry: DialogueEntry) : SceneAction()
    data class DeleteEntry(val entryId: String) : SceneAction()
    data class UpdateEntry(val entry: DialogueEntry) : SceneAction()
    data class ReorderEntries(val entries: List<DialogueEntry>) : SceneAction()
    data class BulkAddEntries(val entries: List<DialogueEntry>) : SceneAction()

    // Speakers
    data class AddCustomSpeaker(val speaker: Speaker) : SceneAction()
    data class RemoveCustomSpeaker(val speakerId: String) : SceneAction()

    // Scene-level
    data class RenameScene(val title: String) : SceneAction()
    data object ClearScene : SceneAction()
}