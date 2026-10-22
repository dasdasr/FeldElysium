package com.example.feldelysium.presentation.settings

import android.content.ContentResolver
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.feldelysium.data.settings.AppSettings
import com.example.feldelysium.data.settings.SettingsRepository
import com.example.feldelysium.data.settings.TapMode
import com.example.feldelysium.di.AppContainer
import com.example.feldelysium.theme.FONT_CUSTOM
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class SettingsViewModel(
    private val repository: SettingsRepository,
    private val resolver: ContentResolver,
    private val fontDir: File
) :
    ViewModel() {

    val state: StateFlow<AppSettings> =
        repository.settings.stateIn(viewModelScope, SharingStarted.Eagerly, AppSettings())

    fun dispatch(event: SettingsEvent) {
        if (event is SettingsEvent.SetBackgroundMedia) {
            swapReadPermission(event.uri)
        }

        viewModelScope.launch {
            repository.update { settings ->
                val updated = settings.apply(event)

                if (
                    event is SettingsEvent.SetAutoPlay &&
                    !event.enabled &&
                    updated.tapMode == TapMode.NOWHERE
                ) {
                    updated.copy(tapMode = TapMode.WHOLE_SCREEN)
                } else {
                    updated
                }
            }
        }
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

    suspend fun importFont(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        fontDir.mkdirs()
        val target = File(fontDir, "font_${System.currentTimeMillis()}")
        val valid = runCatching {
            val input = resolver.openInputStream(uri) ?: error("no stream")
            input.use { src -> target.outputStream().use { src.copyTo(it) } }
            check(target.length() in 1..MAX_FONT_BYTES)
            check(Typeface.createFromFile(target) !== Typeface.DEFAULT)
        }.isSuccess
        if (!valid) {
            target.delete()
            return@withContext false
        }
        val previous = state.value.customFontFile
        repository.update {
            it.copy(customFontFile = target.name, customFontName = displayName(uri), fontChoice = FONT_CUSTOM)
        }
        File(fontDir, previous).takeIf { previous.isNotBlank() }?.delete()
        true
    }

    fun selectFont(choice: String) {
        viewModelScope.launch { repository.update { it.copy(fontChoice = choice) } }
    }

    private fun displayName(uri: Uri): String =
        resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            ?.use { if (it.moveToFirst()) it.getString(0) else null }
            ?.substringBeforeLast('.')
            ?: "Custom font"

    companion object {
        private const val MAX_FONT_BYTES = 10L * 1024 * 1024

        fun factory(c: AppContainer, fontDir: File) = viewModelFactory {
            initializer { SettingsViewModel(c.settingsRepository, c.contentResolver, fontDir) }
        }
    }
}
