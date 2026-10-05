package com.example.feldelysium.presentation.playback

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.feldelysium.data.sound.SoundGroup
import com.example.feldelysium.data.sound.SoundManager
import com.example.feldelysium.domain.engine.PlaybackEngine
import com.example.feldelysium.presentation.shared.SceneViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * [AndroidViewModel] because [SoundManager] requires Application context.
 *
 * Sound is played synchronously inside [advance] on the main dispatcher —
 * SoundPool.play() is main-thread safe and fast enough to not block frames.
 *
 * Scroll-to-bottom is handled in the Composable via LaunchedEffect on
 * revealedEntries.size — this avoids needing a Channel for it.
 */
class PlaybackViewModel(
    application: Application,
    private val sceneViewModel: SceneViewModel
) : AndroidViewModel(application) {

    private val soundManager = SoundManager(application)
    private lateinit var engine: PlaybackEngine

    private val _state = MutableStateFlow(PlaybackUiState())
    val state: StateFlow<PlaybackUiState> = _state.asStateFlow()

    private val _effects = Channel<PlaybackEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        viewModelScope.launch {
            // Take the current scene snapshot — playback freezes the scene as-is
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
        val step = engine.advance() ?: return

        // Update state first so Compose starts rendering the new entry
        _state.update { current ->
            current.copy(
                revealedEntries = current.revealedEntries + step.entry,
                isFinished = step.isLast
            )
        }

        // Play sound — synchronous, fast, safe on main thread
        val group = SoundGroup.fromKey(step.entry.speaker.soundKey)
        soundManager.play(group)
    }

    private fun reset() {
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