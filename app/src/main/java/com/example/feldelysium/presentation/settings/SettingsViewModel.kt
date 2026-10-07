package com.example.feldelysium.presentation.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.feldelysium.data.settings.AppSettings
import com.example.feldelysium.data.settings.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Activity-scoped [viewModel(activity)], so Settings changes are visible on Playback without nav args. */
class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SettingsRepository(application)

    val state: StateFlow<AppSettings> = repository.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = AppSettings(),
    )

    fun dispatch(event: SettingsEvent) {
        when (event) {
            is SettingsEvent.SetLockAspectRatio16x9 -> update {
                it.copy(lockAspectRatio16x9 = event.enabled)
            }
            is SettingsEvent.SetPlaybackMoney -> update {
                it.copy(playbackMoney = event.value)
            }
            is SettingsEvent.SetPlaybackTime -> update {
                it.copy(playbackTime = event.value)
            }
            is SettingsEvent.SetPlaybackDay -> update {
                it.copy(playbackDay = event.value)
            }
            SettingsEvent.ResetPlaybackUiText -> update {
                val defaults = AppSettings()
                it.copy(
                    playbackMoney = defaults.playbackMoney,
                    playbackTime = defaults.playbackTime,
                    playbackDay = defaults.playbackDay,
                )
            }
        }
    }

    private fun update(transform: (AppSettings) -> AppSettings) {
        viewModelScope.launch { repository.update(transform) }
    }
}
