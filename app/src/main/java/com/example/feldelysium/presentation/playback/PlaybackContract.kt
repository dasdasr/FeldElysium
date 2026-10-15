package com.example.feldelysium.presentation.playback

import com.example.feldelysium.domain.model.DialogueEntry
import com.example.feldelysium.domain.model.SkillCheck

data class PlaybackUiState(
    val allEntries: List<DialogueEntry> = emptyList(),
    val revealedEntries: List<DialogueEntry> = emptyList(),
    val isFinished: Boolean = false,
    val isCheckRolling: Boolean = false,
    val checkOverlay: CheckOverlay? = null,
)

sealed class PlaybackEvent {
    data object Tap : PlaybackEvent()
    data object Reset : PlaybackEvent()
    data object NavigateBack : PlaybackEvent()
}

sealed class PlaybackEffect {
    data object NavigateBack : PlaybackEffect()
}

data class CheckOverlay(val entryId: String, val check: SkillCheck)
