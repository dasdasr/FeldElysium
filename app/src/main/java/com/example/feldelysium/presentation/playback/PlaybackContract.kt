package com.example.feldelysium.presentation.playback

import com.example.feldelysium.domain.model.DialogueEntry

data class PlaybackUiState(
    val allEntries: List<DialogueEntry> = emptyList(),
    val revealedEntries: List<DialogueEntry> = emptyList(),
    val isFinished: Boolean = false
)

sealed class PlaybackEvent {
    data object Tap : PlaybackEvent()  // advance to next entry
    data object Reset : PlaybackEvent()  // restart from beginning
    data object NavigateBack : PlaybackEvent()
}

sealed class PlaybackEffect {
    data object NavigateBack : PlaybackEffect()
}