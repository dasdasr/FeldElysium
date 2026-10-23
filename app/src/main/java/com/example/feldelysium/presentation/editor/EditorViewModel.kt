package com.example.feldelysium.presentation.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.feldelysium.data.settings.AppSettings
import com.example.feldelysium.data.settings.SamplesMode
import com.example.feldelysium.data.settings.SettingsRepository
import com.example.feldelysium.di.AppContainer
import com.example.feldelysium.domain.model.DamageEffect
import com.example.feldelysium.domain.model.DialogueEntry
import com.example.feldelysium.domain.model.DialogueScene
import com.example.feldelysium.domain.model.ReplyChoices
import com.example.feldelysium.domain.model.SceneInfo
import com.example.feldelysium.domain.model.SkillCheck
import com.example.feldelysium.domain.model.Speaker
import com.example.feldelysium.domain.model.SpeakerType
import com.example.feldelysium.domain.registry.PredefinedSpeakers
import com.example.feldelysium.domain.repository.SpeakerRepository
import com.example.feldelysium.domain.sound.SoundCue
import com.example.feldelysium.domain.sound.SoundPlayer
import com.example.feldelysium.domain.usecase.ParseBulkImportUseCase
import com.example.feldelysium.presentation.shared.FeldError
import com.example.feldelysium.presentation.shared.SceneSession
import com.example.feldelysium.presentation.shared.toastText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.InputStream
import java.io.OutputStream
import kotlin.coroutines.cancellation.CancellationException

class EditorViewModel(
    private val session: SceneSession,
    private val speakerRepo: SpeakerRepository,
    settingsRepo: SettingsRepository,
    private val parser: ParseBulkImportUseCase,
    private val sounds: SoundPlayer,
) : ViewModel() {

    private val sceneList = MutableStateFlow<List<SceneInfo>>(emptyList())

    val state: StateFlow<EditorUiState> = combine(
        session.scene,
        speakerRepo.speakers,
        speakerRepo.tags,
        settingsRepo.settings,
        sceneList,
    ) { scene, speakers, tags, prefs, scenes ->
        editorUiState(scene, speakers, tags, prefs, scenes)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        editorUiState(session.scene.value, PredefinedSpeakers.ALL, PredefinedSpeakers.TAGS, AppSettings(), emptyList()),
    )

    private val _messages = Channel<String>(Channel.BUFFERED)

    val messages = _messages.receiveAsFlow()

    fun addEntry(speakerId: String, text: String, check: SkillCheck?, damage: DamageEffect?, replies: ReplyChoices?) {
        val speaker = state.value.speaker(speakerId) ?: return
        val allowed = speaker.type.allowsChecks
        session.addEntry(
            DialogueEntry(
                speaker = speaker,
                text = text.trim(),
                check = check.takeIf { allowed },
                damage = damage.takeIf { allowed },
                replies = replies.takeIf { speaker.type == SpeakerType.YOU },
            ),
        )
    }

    fun updateEntry(entry: DialogueEntry) = session.updateEntry(entry)
    fun deleteEntry(id: String) = session.deleteEntry(id)
    fun reorderEntries(entries: List<DialogueEntry>) = session.reorder(entries)
    fun renameScene(name: String) = session.rename(name)
    fun clearScene() = session.clear()

    fun saveSpeaker(speaker: Speaker) = viewModelScope.launch { speakerRepo.save(speaker) }
    fun deleteSpeaker(id: String) = viewModelScope.launch { speakerRepo.delete(id) }
    fun saveTag(tag: SpeakerType) = viewModelScope.launch { speakerRepo.saveTag(tag) }
    fun deleteTag(tag: SpeakerType) = viewModelScope.launch { speakerRepo.deleteTag(tag.id) }

    fun previewImport(text: String): ImportPreview? {
        if (text.isBlank()) return null
        val result = parser.execute(text, customSpeakers())
        return ImportPreview(result.entries.size, result.newSpeakers.size)
    }

    fun importLines(text: String): Boolean {
        val result = parser.execute(text, customSpeakers())

        return when {
            text.isBlank() -> fail(FeldError.NoScriptPasted)

            result.entries.isEmpty() -> fail(FeldError.EmptyImport)

            else -> {
                viewModelScope.launch {
                    result.newSpeakers.forEach { speakerRepo.save(it) }
                    session.addEntries(result.entries)
                }
                true
            }
        }
    }

    fun refreshScenes() {
        viewModelScope.launch { sceneList.value = session.listScenes() }
    }

    fun newScene() = viewModelScope.launch { session.newScene() }
    fun openScene(id: String) = viewModelScope.launch { session.openScene(id) }

    fun deleteScene(id: String) {
        viewModelScope.launch {
            session.deleteScene(id)
            sceneList.value = session.listScenes()
        }
    }

    fun clearSceneById(id: String) {
        viewModelScope.launch {
            session.deleteScene(id)
            refreshScenes()
        }
    }

    fun saveScene() {
        viewModelScope.launch {
            session.save()
            notify("Scene saved")
        }
    }

    fun exportScene(output: OutputStream?) {
        output ?: return
        viewModelScope.launch {
            val ok = attempt { output.use { session.exportScene(it) } }
            if (ok) sounds.play(SoundCue.UiExport)
            if (ok) {
                notify("Scene exported")
            } else {
                fail(FeldError.ExportFailed)
            }
        }
    }

    fun importScene(input: InputStream?) {
        input ?: return
        viewModelScope.launch {
            val ok = attempt { input.use { session.importScene(it) } }
            if (ok) sounds.play(SoundCue.UiDone)
            if (ok) {
                notify("Scene imported")
            } else {
                fail(FeldError.ImportFailed)
            }
        }
    }

    private fun notify(message: String): Boolean {
        _messages.trySend(message)
        return false
    }

    private fun fail(e: FeldError): Boolean = notify(e.toastText())

    private fun customSpeakers() = state.value.allSpeakers.filterNot { it.isBundled }

    private suspend inline fun attempt(block: () -> Unit): Boolean = try {
        block()
        true
    } catch (e: CancellationException) {
        throw e
    } catch (_: Exception) {
        false
    }

    companion object {
        fun factory(c: AppContainer) = viewModelFactory {
            initializer {
                EditorViewModel(
                    c.sceneSession,
                    c.speakerRepository,
                    c.settingsRepository,
                    ParseBulkImportUseCase(),
                    c.soundPlayer,
                )
            }
        }
    }
}

private fun editorUiState(
    scene: DialogueScene,
    speakers: List<Speaker>,
    tags: List<SpeakerType>,
    prefs: AppSettings,
    scenes: List<SceneInfo>,
): EditorUiState {
    val hideSamples = when (prefs.samplesMode) {
        SamplesMode.ALWAYS_SHOW -> false
        SamplesMode.ALWAYS_HIDE -> true
        SamplesMode.AUTO -> prefs.packImported
    }
    return EditorUiState(
        sceneId = scene.id,
        sceneName = scene.title,
        entries = scene.entries,
        allSpeakers = speakers,
        pickerSpeakers = if (hideSamples) speakers.filterNot { it.isSample } else speakers,
        tags = tags,
        scenes = scenes,
    )
}
