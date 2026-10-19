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
            is SettingsEvent.SetLockAspectRatio16x9,
            is SettingsEvent.SetPlaybackMoney,
            is SettingsEvent.SetPlaybackTime,
            is SettingsEvent.SetPlaybackDay,
            SettingsEvent.ResetPlaybackUiText,
            -> updatePlaybackSettings(event)

            is SettingsEvent.SetBackgroundKind,
            is SettingsEvent.SetBackgroundColor,
            is SettingsEvent.SetBackgroundMedia,
            is SettingsEvent.SetBackgroundVideoSound,
            is SettingsEvent.SetBackgroundVideoFill,
            SettingsEvent.ResetBackground,
            -> updateBackgroundSettings(event)

            is SettingsEvent.SetSamplesMode -> update {
                it.copy(samplesMode = event.mode)
            }

            is SettingsEvent.SetBarIconState -> update {
                it.copy(barIcons = it.barIcons + (event.slot to event.state))
            }

            is SettingsEvent.SetInventoryIcon -> update {
                it.copy(inventoryIcons = it.inventoryIcons + (event.slot to event.icon))
            }

            SettingsEvent.ResetBarIcons -> update {
                val defaults = AppSettings()
                it.copy(
                    barIcons = defaults.barIcons,
                    inventoryIcons = defaults.inventoryIcons,
                )
            }
        }
    }

    private fun updatePlaybackSettings(event: SettingsEvent) {
        update {
            when (event) {
                is SettingsEvent.SetLockAspectRatio16x9 ->
                    it.copy(lockAspectRatio16x9 = event.enabled)

                is SettingsEvent.SetPlaybackMoney ->
                    it.copy(playbackMoney = event.value)

                is SettingsEvent.SetPlaybackTime ->
                    it.copy(playbackTime = event.value)

                is SettingsEvent.SetPlaybackDay ->
                    it.copy(playbackDay = event.value)

                SettingsEvent.ResetPlaybackUiText -> {
                    val defaults = AppSettings()
                    it.copy(
                        playbackMoney = defaults.playbackMoney,
                        playbackTime = defaults.playbackTime,
                        playbackDay = defaults.playbackDay,
                    )
                }

                else -> it
            }
        }
    }

    private fun updateBackgroundSettings(event: SettingsEvent) {
        when (event) {
            is SettingsEvent.SetBackgroundMedia -> persistRead(event.uri)
            else -> Unit
        }

        update {
            when (event) {
                is SettingsEvent.SetBackgroundKind ->
                    it.copy(backgroundKind = event.kind)

                is SettingsEvent.SetBackgroundColor ->
                    it.copy(
                        backgroundColorArgb = event.argb,
                        backgroundKind = BackgroundKind.COLOR,
                    )

                is SettingsEvent.SetBackgroundMedia ->
                    it.copy(
                        backgroundKind = BackgroundKind.MEDIA,
                        backgroundMediaUri = event.uri,
                        backgroundMediaIsVideo = event.isVideo,
                    )

                is SettingsEvent.SetBackgroundVideoSound ->
                    it.copy(backgroundVideoSound = event.enabled)

                is SettingsEvent.SetBackgroundVideoFill ->
                    it.copy(backgroundVideoFill = event.enabled)

                SettingsEvent.ResetBackground -> {
                    val defaults = AppSettings()
                    it.copy(
                        backgroundKind = defaults.backgroundKind,
                        backgroundColorArgb = defaults.backgroundColorArgb,
                        backgroundMediaUri = defaults.backgroundMediaUri,
                        backgroundMediaIsVideo = defaults.backgroundMediaIsVideo,
                        backgroundVideoSound = defaults.backgroundVideoSound,
                        backgroundVideoFill = defaults.backgroundVideoFill,
                    )
                }

                else -> it
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
