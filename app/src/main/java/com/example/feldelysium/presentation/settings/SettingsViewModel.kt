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

/**
 * Activity-scoped source of [AppSettings].
 *
 * Obtain with `viewModel(activity)` from any screen, the same way as
 * [com.example.feldelysium.presentation.shared.SceneViewModel]. The flow stays
 * hot for the activity, so a change on Settings is visible on Playback without
 * passing arguments through navigation.
 */
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
        }
    }

    private fun update(transform: (AppSettings) -> AppSettings) {
        viewModelScope.launch { repository.update(transform) }
    }
}
