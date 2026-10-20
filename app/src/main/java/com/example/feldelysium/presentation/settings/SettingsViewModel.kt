package com.example.feldelysium.presentation.settings

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.feldelysium.data.settings.AppSettings
import com.example.feldelysium.data.settings.SettingsRepository
import com.example.feldelysium.di.AppContainer
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repository: SettingsRepository,
    private val resolver: ContentResolver,
) : ViewModel() {

    val state: StateFlow<AppSettings> =
        repository.settings.stateIn(viewModelScope, SharingStarted.Eagerly, AppSettings())

    fun dispatch(event: SettingsEvent) {
        if (event is SettingsEvent.SetBackgroundMedia) swapReadPermission(event.uri)
        viewModelScope.launch { repository.update { it.apply(event) } }
    }

    private fun swapReadPermission(newUri: String) {
        val uri = runCatching { Uri.parse(newUri) }.getOrNull() ?: return
        val previous = state.value.backgroundMediaUri
        if (previous.isNotBlank() && previous != newUri) {
            runCatching {
                resolver.releasePersistableUriPermission(Uri.parse(previous), Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        }
        runCatching { resolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
    }

    companion object {
        fun factory(c: AppContainer) = viewModelFactory {
            initializer { SettingsViewModel(c.settingsRepository, c.contentResolver) }
        }
    }
}
