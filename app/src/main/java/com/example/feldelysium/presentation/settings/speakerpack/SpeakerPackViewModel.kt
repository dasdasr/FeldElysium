package com.example.feldelysium.presentation.settings.speakerpack

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.feldelysium.data.speaker.PackFormatException
import com.example.feldelysium.di.AppContainer
import com.example.feldelysium.domain.model.ImportPolicy
import com.example.feldelysium.domain.model.SpeakerType
import com.example.feldelysium.domain.registry.PredefinedSpeakers
import com.example.feldelysium.domain.repository.SettingsRepository
import com.example.feldelysium.domain.repository.SpeakerRepository
import com.example.feldelysium.domain.usecase.ImportSpeakerPackUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SpeakerPackViewModel(
    private val settingsRepository: SettingsRepository,
    private val repo: SpeakerRepository,
    private val resolver: ContentResolver,
) : ViewModel() {

    private val useCase = ImportSpeakerPackUseCase(repo)

    private val _pending = MutableStateFlow<ImportSpeakerPackUseCase.Prepared?>(null)
    val pending: StateFlow<ImportSpeakerPackUseCase.Prepared?> = _pending.asStateFlow()

    private val _messages = Channel<PackMessage>(Channel.BUFFERED)
    val messages: Flow<PackMessage> = _messages.receiveAsFlow()

    val speakers = repo.speakers.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        PredefinedSpeakers.ALL,
    )
    val tags = repo.tags.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        PredefinedSpeakers.TAGS,
    )

    fun saveTag(tag: SpeakerType) {
        viewModelScope.launch { repo.saveTag(tag) }
    }

    fun deleteTag(tag: SpeakerType) {
        viewModelScope.launch { repo.deleteTag(tag.id) }
    }

    fun export(target: Uri, ids: Set<String>? = null) {
        viewModelScope.launch {
            val ok = runCatching {
                withContext(Dispatchers.IO) {
                    val out = resolver.openOutputStream(target) ?: error("no stream")
                    out.use { repo.exportPack(ids, it) }
                }
            }.isSuccess
            _messages.send(if (ok) PackMessage.Exported else PackMessage.ExportFailed)
        }
    }

    fun prepareImport(source: Uri) {
        viewModelScope.launch {
            try {
                val prepared = withContext(Dispatchers.IO) {
                    val input = resolver.openInputStream(source) ?: error("no stream")
                    input.use { useCase.prepare(it) }
                }
                if (prepared.needsDecision) _pending.value = prepared else applyImport(prepared)
            } catch (e: CancellationException) {
                throw e
            } catch (e: PackFormatException) {
                _messages.send(PackMessage.NotAPack(e.message))
            } catch (_: Exception) {
                _messages.send(PackMessage.CannotRead)
            }
        }
    }

    fun confirmImport(policy: ImportPolicy) {
        val prepared = _pending.value ?: return
        _pending.value = null
        viewModelScope.launch { applyImport(prepared, policy) }
    }

    fun cancelImport() {
        _pending.value?.let(useCase::cancel)
        _pending.value = null
    }

    override fun onCleared() {
        _pending.value?.let(useCase::cancel)
    }

    private suspend fun applyImport(prepared: ImportSpeakerPackUseCase.Prepared, policy: ImportPolicy? = null) {
        val result = if (policy != null) useCase.apply(prepared, policy) else useCase.apply(prepared)

        if (result.added + result.replaced > 0) {
            settingsRepository.update { it.copy(packImported = true) }
        }
        _messages.send(PackMessage.Imported(result.added, result.replaced, result.skipped))
    }

    companion object {
        fun factory(container: AppContainer) = viewModelFactory {
            initializer {
                SpeakerPackViewModel(
                    settingsRepository = container.settingsRepository,
                    repo = container.speakerRepository,
                    resolver = container.contentResolver,
                )
            }
        }
    }
}
