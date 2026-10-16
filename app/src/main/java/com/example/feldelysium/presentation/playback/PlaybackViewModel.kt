package com.example.feldelysium.presentation.playback

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.feldelysium.FeldApp
import com.example.feldelysium.domain.model.DamageEffect
import com.example.feldelysium.domain.playback.PlaybackAudio
import com.example.feldelysium.domain.playback.PlaybackEngine
import com.example.feldelysium.domain.playback.PlaybackStep
import com.example.feldelysium.presentation.playback.components.CheckMotion
import com.example.feldelysium.presentation.playback.components.PanelMotion
import com.example.feldelysium.presentation.shared.SceneViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PlaybackViewModel(
    private val sceneViewModel: SceneViewModel,
    private val audio: PlaybackAudio,
) : ViewModel() {

    private lateinit var engine: PlaybackEngine
    private var checkJob: Job? = null
    private var damageJob: Job? = null

    private val _state = MutableStateFlow(PlaybackUiState())
    val state: StateFlow<PlaybackUiState> = _state.asStateFlow()

    private val _effects = Channel<PlaybackEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        viewModelScope.launch {
            val sceneState = sceneViewModel.state.first()
            engine = PlaybackEngine(sceneState.scene.entries)
            _state.update { it.copy(allEntries = sceneState.scene.entries) }
            audio.preload()
            open()
        }
    }

    fun dispatch(event: PlaybackEvent) = when (event) {
        PlaybackEvent.Tap -> onTap()
        PlaybackEvent.Reset -> reset()
        PlaybackEvent.ScreenStopped -> audio.stop()
        PlaybackEvent.NavigateBack -> viewModelScope.launch {
            _effects.send(PlaybackEffect.NavigateBack)
        }

        is PlaybackEvent.ScrollChanged -> {
            if (!_state.value.isPanelOpen) Unit
            else if (event.active) audio.startScrollLoop() else audio.stopScrollLoop()
        }
    }

    private fun onTap() {
        if (!::engine.isInitialized) return
        val s = _state.value
        when {
            s.isCheckRolling -> Unit
            !s.isPanelOpen -> reset()
            s.isFinished -> close()
            else -> advance()
        }
    }

    private fun open() {
        _state.update { it.copy(isPanelOpen = true) }
    }

    private fun close() {
        cancelTimers()
        audio.panelOut()
        _state.update {
            it.copy(isPanelOpen = false, isCheckRolling = false, checkOverlay = null, damageOverlay = null)
        }
    }

    private fun reset() {
        if (!::engine.isInitialized) return
        cancelTimers()
        audio.stop()
        engine.reset()
        _state.update { PlaybackUiState(allEntries = it.allEntries) }
        open()
    }

    private fun advance() {
        val step = engine.advance() ?: return
        val entry = step.entry
        val allowed = entry.speaker.type.allowsChecks
        val check = entry.check?.takeIf { it.active && allowed }
        val damage = entry.damage?.takeIf { allowed }

        audio.click()

        if (check == null) {
            reveal(step, overlay = null)
            if (damage != null) showDamage(entry.id, damage, delayMs = 0L)
            return
        }

        _state.update { it.copy(isCheckRolling = true, checkOverlay = null) }
        checkJob = viewModelScope.launch {
            audio.diceRoll()

            delay(CheckMotion.TAPE_ROLL_START_DELAY_MS.toLong())

            delay(
                (CheckMotion.TAPE_ROLL_MS - CheckMotion.CHECK_RESULT_EARLY_MS)
                    .coerceAtLeast(0L).toLong()
            )

            audio.checkResult(check.result)

            delay(CheckMotion.CHECK_RESULT_EARLY_MS)

            reveal(step, overlay = CheckOverlay(entryId = entry.id, check = check))

            if (damage != null) {
                showDamage(entry.id, damage, CheckMotion.CHECK_OVERLAY_TOTAL_MS)
            }
        }
    }

    private fun showDamage(entryId: String, damage: DamageEffect, delayMs: Long) {
        damageJob?.cancel()
        damageJob = viewModelScope.launch {
            delay(delayMs)
            audio.damage(damage.type)
            _state.update { it.copy(damageOverlay = DamageOverlay(entryId, damage)) }
        }
    }

    private fun reveal(step: PlaybackStep, overlay: CheckOverlay?) {
        _state.update { current ->
            current.copy(
                revealedEntries = current.revealedEntries + step.entry,
                isFinished = step.isLast,
                isCheckRolling = false,
                checkOverlay = overlay,
            )
        }
        audio.lineRevealed(step.entry)
    }

    private fun cancelTimers() {
        checkJob?.cancel(); checkJob = null
        damageJob?.cancel(); damageJob = null
    }

    override fun onCleared() {
        audio.stop()
        super.onCleared()
    }

    companion object {
        private const val CLICK_TO_ROLL_MS = 90L

        fun factory(sceneViewModel: SceneViewModel) = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as FeldApp
                PlaybackViewModel(sceneViewModel, app.container.playbackAudio())
            }
        }
    }
}
