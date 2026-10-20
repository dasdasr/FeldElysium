package com.example.feldelysium.presentation.playback

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.feldelysium.di.AppContainer
import com.example.feldelysium.domain.model.DamageEffect
import com.example.feldelysium.domain.playback.PlaybackAudio
import com.example.feldelysium.domain.playback.PlaybackEngine
import com.example.feldelysium.domain.playback.PlaybackStep
import com.example.feldelysium.presentation.playback.components.CheckMotion
import com.example.feldelysium.presentation.shared.SceneSource
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class PlaybackViewModel(scene: SceneSource, private val audio: PlaybackAudio) : ViewModel() {
    private val entries = scene.scene.value.entries
    private val engine = PlaybackEngine(entries)
    private var checkJob: Job? = null
    private var damageJob: Job? = null
    private var autoJob: Job? = null

    private val _autoPlayDone = MutableStateFlow(false)
    val autoPlayDone: StateFlow<Boolean> = _autoPlayDone.asStateFlow()

    private val _state = MutableStateFlow(PlaybackUiState(allEntries = entries))
    val state: StateFlow<PlaybackUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            audio.preload()
            open()
        }
    }

    fun dispatch(event: PlaybackEvent) = when (event) {
        PlaybackEvent.Tap -> onTap()

        PlaybackEvent.Reset -> reset()

        PlaybackEvent.ScreenStopped -> audio.stop()

        is PlaybackEvent.ScrollChanged -> {
            if (!_state.value.isPanelOpen) {
                Unit
            } else if (event.active) {
                audio.startScrollLoop()
            } else {
                audio.stopScrollLoop()
            }
        }
    }

    fun startAutoPlay(speed: Float) {
        if (entries.isEmpty()) {
            _autoPlayDone.value = true
            return
        }
        _autoPlayDone.value = false
        reset()
        autoJob = viewModelScope.launch {
            delay(START_HOLD_MS.milliseconds)
            while (!_state.value.isFinished) {
                advance()
                while (_state.value.isCheckRolling) delay(CHECK_POLL_MS.milliseconds)
                val text = _state.value.revealedEntries.lastOrNull()?.text.orEmpty()
                val readMs = ((BASE_READ_MS + text.length * MS_PER_CHAR) / speed).toLong()
                delay(readMs.milliseconds)
            }
            delay(END_HOLD_MS.milliseconds)
            _autoPlayDone.value = true
        }
    }

    private fun onTap() {
        if (autoJob?.isActive == true) return
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

            delay(CheckMotion.TAPE_ROLL_START_DELAY_MS.toLong().milliseconds)

            delay(
                (CheckMotion.TAPE_ROLL_MS - CheckMotion.CHECK_RESULT_EARLY_MS)
                    .coerceAtLeast(0L).milliseconds,
            )

            audio.checkResult(check.result)

            delay(CheckMotion.CHECK_RESULT_EARLY_MS.milliseconds)

            reveal(step, overlay = CheckOverlay(entryId = entry.id, check = check))

            if (damage != null) {
                showDamage(entry.id, damage, CheckMotion.CHECK_OVERLAY_TOTAL_MS)
            }
        }
    }

    private fun showDamage(entryId: String, damage: DamageEffect, delayMs: Long) {
        damageJob?.cancel()
        damageJob = viewModelScope.launch {
            delay(delayMs.milliseconds)
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
        autoJob?.cancel()
        autoJob = null
        checkJob?.cancel()
        checkJob = null
        damageJob?.cancel()
        damageJob = null
    }

    override fun onCleared() {
        audio.stop()
        super.onCleared()
    }

    companion object {
        private const val START_HOLD_MS = 800L
        private const val CHECK_POLL_MS = 50L
        private const val BASE_READ_MS = 2800L
        private const val MS_PER_CHAR = 8L
        private const val END_HOLD_MS = 2000L

        fun factory(c: AppContainer) = viewModelFactory {
            initializer { PlaybackViewModel(c.sceneSession, c.playbackAudio()) }
        }
    }
}
