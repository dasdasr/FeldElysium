package com.example.feldelysium.presentation.playback

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.feldelysium.data.sound.SoundGroup
import com.example.feldelysium.data.sound.SoundManager
import com.example.feldelysium.domain.model.CheckResult
import com.example.feldelysium.domain.playback.PlaybackEngine
import com.example.feldelysium.domain.playback.PlaybackStep
import com.example.feldelysium.presentation.playback.components.CheckMotion
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

class PlaybackViewModel(application: Application, private val sceneViewModel: SceneViewModel) :
    AndroidViewModel(application) {

    private val soundManager = SoundManager(application)
    private lateinit var engine: PlaybackEngine
    private var checkJob: Job? = null

    private val _state = MutableStateFlow(PlaybackUiState())
    val state: StateFlow<PlaybackUiState> = _state.asStateFlow()

    private val _effects = Channel<PlaybackEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        viewModelScope.launch {
            val sceneState = sceneViewModel.state.first()
            engine = PlaybackEngine(sceneState.scene.entries)
            _state.update { it.copy(allEntries = sceneState.scene.entries) }
        }
    }

    fun dispatch(event: PlaybackEvent) = when (event) {
        PlaybackEvent.Tap -> advance()

        PlaybackEvent.Reset -> reset()

        PlaybackEvent.NavigateBack -> viewModelScope.launch {
            _effects.send(PlaybackEffect.NavigateBack)
        }
    }

    private fun advance() {
        if (!::engine.isInitialized) return
        if (_state.value.isCheckRolling) return
        val step = engine.advance() ?: return

        val check = step.entry.check?.takeIf { it.active && step.entry.speaker.type.allowsChecks }
        if (check == null) {
            reveal(step, overlay = null)
            return
        }

        playCheckSound(check.result)
        _state.update { it.copy(isCheckRolling = true, checkOverlay = null) }
        checkJob = viewModelScope.launch {
            delay(CheckMotion.TAPE_ROLL_TOTAL_MS)
            reveal(step, overlay = CheckOverlay(entryId = step.entry.id, check = check))
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
        soundManager.play(SoundGroup.fromKey(step.entry.speaker.soundKey))
    }

    // TODO(sounds): hook up a dedicated active-check sound, e.g. soundManager.playCheck(result).
    @Suppress("UNUSED_PARAMETER")
    private fun playCheckSound(result: CheckResult) = Unit

    private fun reset() {
        checkJob?.cancel()
        checkJob = null
        if (::engine.isInitialized) engine.reset()
        _state.update { PlaybackUiState(allEntries = it.allEntries) }
    }

    override fun onCleared() {
        soundManager.release()
        super.onCleared()
    }

    companion object {
        fun factory(sceneViewModel: SceneViewModel) = viewModelFactory {
            initializer {
                val app =
                    this[androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]!!
                PlaybackViewModel(app, sceneViewModel)
            }
        }
    }
}
