package com.example.feldelysium.presentation.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.feldelysium.R
import com.example.feldelysium.data.settings.FontImporter
import com.example.feldelysium.data.settings.MediaUriPermissions
import com.example.feldelysium.designsystem.theme.FONT_CUSTOM
import com.example.feldelysium.di.AppContainer
import com.example.feldelysium.domain.model.settings.AppSettings
import com.example.feldelysium.domain.repository.SettingsRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

class SettingsViewModel(
    private val repository: SettingsRepository,
    private val uriPermissions: MediaUriPermissions,
    private val fonts: FontImporter,
) : ViewModel() {

    val state: StateFlow<AppSettings> =
        repository.settings.stateIn(viewModelScope, SharingStarted.Eagerly, AppSettings())

    private val _effects = Channel<SettingsEffect>(Channel.BUFFERED)
    val effects: Flow<SettingsEffect> = _effects.receiveAsFlow()

    fun dispatch(event: SettingsEvent) {
        if (event is SettingsEvent.SetBackgroundMedia) {
            uriPermissions.swap(previous = state.value.backgroundMediaUri, next = event.uri)
        }
        viewModelScope.launch { repository.update { it.apply(event) } }
    }

    fun importFont(uri: Uri) {
        viewModelScope.launch {
            val font = fonts.import(uri)
            if (font == null) {
                _effects.send(SettingsEffect.ShowMessage(R.string.settings_font_invalid))
                return@launch
            }
            val previous = state.value.customFontFile
            repository.update {
                it.copy(
                    customFontFile = font.fileName,
                    customFontName = font.displayName,
                    fontChoice = FONT_CUSTOM,
                )
            }
            fonts.delete(previous)
        }
    }

    companion object {
        fun factory(container: AppContainer, fontDir: File) = viewModelFactory {
            initializer {
                SettingsViewModel(
                    repository = container.settingsRepository,
                    uriPermissions = MediaUriPermissions(container.contentResolver),
                    fonts = FontImporter(container.contentResolver, fontDir),
                )
            }
        }
    }
}
