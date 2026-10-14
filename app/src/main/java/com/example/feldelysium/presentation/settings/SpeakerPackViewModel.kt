package com.example.feldelysium.presentation.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.feldelysium.data.speaker.PackFormatException
import com.example.feldelysium.data.speaker.SpeakerStorage
import com.example.feldelysium.domain.model.ImportPolicy
import com.example.feldelysium.domain.model.ImportReport
import com.example.feldelysium.domain.model.SpeakerType
import com.example.feldelysium.domain.registry.PredefinedSpeakers
import com.example.feldelysium.domain.usecase.ImportSpeakerPackUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.InputStream
import java.io.OutputStream

class SpeakerPackViewModel(app: Application) : AndroidViewModel(app) {

    data class UiState(val pending: ImportSpeakerPackUseCase.Prepared? = null, val message: String? = null)

    private val repo = SpeakerStorage.repository(app)
    private val useCase = ImportSpeakerPackUseCase(repo)

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

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

    fun export(output: OutputStream?, ids: Set<String>? = null) {
        if (output == null) return
        viewModelScope.launch {
            val ok = runCatching { output.use { repo.exportPack(ids, it) } }.isSuccess
            say(if (ok) "Pack exported" else "Export failed")
        }
    }

    fun prepareImport(input: InputStream?) {
        if (input == null) return
        viewModelScope.launch {
            try {
                val prepared = input.use { useCase.prepare(it) }
                if (prepared.needsDecision) {
                    _state.update { it.copy(pending = prepared) }
                } else {
                    say(report(useCase.apply(prepared)))
                }
            } catch (e: PackFormatException) {
                say(e.message ?: "Not a speaker pack")
            } catch (_: Exception) {
                say("Cannot read this file")
            }
        }
    }

    fun confirmImport(policy: ImportPolicy) {
        val prepared = _state.value.pending ?: return
        _state.update { it.copy(pending = null) }
        viewModelScope.launch { say(report(useCase.apply(prepared, policy))) }
    }

    fun cancelImport() {
        _state.value.pending?.let(useCase::cancel)
        _state.update { it.copy(pending = null) }
    }

    fun consumeMessage() = _state.update { it.copy(message = null) }

    override fun onCleared() {
        _state.value.pending?.let(useCase::cancel)
    }

    private fun say(text: String) = _state.update { it.copy(message = text) }

    private fun report(r: ImportReport) = buildString {
        append("Added ${r.added}")
        if (r.replaced > 0) append(", replaced ${r.replaced}")
        if (r.skipped > 0) append(", skipped ${r.skipped}")
    }
}
