package com.example.feldelysium.presentation.playback

import com.example.feldelysium.domain.model.DamageEffect
import com.example.feldelysium.domain.model.DialogueEntry
import com.example.feldelysium.domain.model.SkillCheck

data class PlaybackUiState(
    val allEntries: List<DialogueEntry> = emptyList(),
    val revealedEntries: List<DialogueEntry> = emptyList(),
    val isFinished: Boolean = false,
    val isCheckRolling: Boolean = false,
    val isPanelOpen: Boolean = false,
    val checkOverlay: CheckOverlay? = null,
    val damageOverlay: DamageOverlay? = null,
    val prompt: Prompt = Prompt.None,
)

sealed class PlaybackEvent {
    data object Tap : PlaybackEvent()
    data object Reset : PlaybackEvent()
    data object ScreenStopped : PlaybackEvent()
    data class ScrollChanged(val active: Boolean) : PlaybackEvent()
    data object ContinueClicked : PlaybackEvent()
    data object ChoiceClicked : PlaybackEvent()
}

data class CheckOverlay(val entryId: String, val check: SkillCheck)
data class DamageOverlay(val entryId: String, val damage: DamageEffect)

sealed interface Prompt {
    data object None : Prompt
    data object Pending : Prompt
    data class Continue(val nextSpeakerColor: Long) : Prompt
    data class Choices(val options: List<String>, val chosenIndex: Int, val selected: Boolean = false) : Prompt
}
