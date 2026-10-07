package com.example.feldelysium.presentation.settings

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.feldelysium.data.settings.AppSettings
import com.example.feldelysium.data.settings.BackgroundKind
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
            is SettingsEvent.SetBackgroundKind -> update {
                it.copy(backgroundKind = event.kind)
            }
            is SettingsEvent.SetBackgroundColor -> update {
                it.copy(backgroundColorArgb = event.argb, backgroundKind = BackgroundKind.COLOR)
            }
            is SettingsEvent.SetBackgroundMedia -> {
                persistRead(event.uri)
                update {
                    it.copy(
                        backgroundKind = BackgroundKind.MEDIA,
                        backgroundMediaUri = event.uri,
                        backgroundMediaIsVideo = event.isVideo,
                    )
                }
            }
            is SettingsEvent.SetBackgroundVideoSound -> update {
                it.copy(backgroundVideoSound = event.enabled)
            }
            is SettingsEvent.SetBackgroundVideoFill -> update {
                it.copy(backgroundVideoFill = event.enabled)
            }
        }
    }

    private fun persistRead(uriString: String) {
        val uri = runCatching { Uri.parse(uriString) }.getOrNull() ?: return
        val resolver = getApplication<Application>().contentResolver
        val previous = state.value.backgroundMediaUri
        if (previous.isNotBlank() && previous != uriString) {
            runCatching {
                resolver.releasePersistableUriPermission(
                    Uri.parse(previous),
                    Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
            }
        }
        runCatching {
            resolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    private fun update(transform: (AppSettings) -> AppSettings) {
        viewModelScope.launch { repository.update(transform) }
    }
}
