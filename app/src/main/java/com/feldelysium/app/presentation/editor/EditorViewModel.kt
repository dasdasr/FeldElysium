package com.feldelysium.app.presentation.editor

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.feldelysium.app.R
import com.feldelysium.app.di.AppContainer
import com.feldelysium.app.domain.model.DamageEffect
import com.feldelysium.app.domain.model.DialogueEntry
import com.feldelysium.app.domain.model.DialogueScene
import com.feldelysium.app.domain.model.ReplyChoices
import com.feldelysium.app.domain.model.SceneInfo
import com.feldelysium.app.domain.model.SkillCheck
import com.feldelysium.app.domain.model.Speaker
import com.feldelysium.app.domain.model.SpeakerType
import com.feldelysium.app.domain.model.settings.AppSettings
import com.feldelysium.app.domain.model.settings.SamplesMode
import com.feldelysium.app.domain.registry.PredefinedSpeakers
import com.feldelysium.app.domain.repository.SettingsRepository
import com.feldelysium.app.domain.repository.SpeakerRepository
import com.feldelysium.app.domain.scene.SceneSession
import com.feldelysium.app.domain.sound.SoundCue
import com.feldelysium.app.domain.sound.SoundPlayer
import com.feldelysium.app.domain.usecase.ParseBulkImportUseCase
import com.feldelysium.app.domain.usecase.SceneTransferUseCase
import com.feldelysium.app.presentation.shared.FeldError
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

class EditorViewModel(
    private val session: SceneSession,
    private val speakerRepo: SpeakerRepository,
    settingsRepo: SettingsRepository,
    private val parser: ParseBulkImportUseCase,
    private val sounds: SoundPlayer,
    private val sceneTransfer: SceneTransferUseCase = SceneTransferUseCase(session),
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

    private val _messages = Channel<EditorMessage>(Channel.BUFFERED)

    val messages = _messages.receiveAsFlow()

    @Suppress("LongMethod", "CyclomaticComplexMethod")
    fun dispatch(event: EditorEvent): Boolean = when (event) {
        is EditorEvent.AddEntry -> {
            addEntry(event.speakerId, event.text, event.check, event.damage, event.replies)
            true
        }

        is EditorEvent.UpdateEntry -> {
            session.updateEntry(event.entry)
            true
        }

        is EditorEvent.DeleteEntry -> {
            session.deleteEntry(event.id)
            true
        }

        is EditorEvent.ReorderEntries -> {
            session.reorder(event.entries)
            true
        }

        EditorEvent.ClearScene -> {
            session.clear()
            true
        }

        is EditorEvent.SaveSpeaker -> {
            viewModelScope.launch { speakerRepo.save(event.speaker) }
            true
        }

        is EditorEvent.DeleteSpeaker -> {
            viewModelScope.launch { speakerRepo.delete(event.id) }
            true
        }

        is EditorEvent.SaveTag -> {
            viewModelScope.launch { speakerRepo.saveTag(event.tag) }
            true
        }

        is EditorEvent.DeleteTag -> {
            viewModelScope.launch { speakerRepo.deleteTag(event.id) }
            true
        }

        is EditorEvent.ImportLines -> importLines(event.text)

        EditorEvent.RefreshScenes -> {
            viewModelScope.launch { sceneList.value = session.listScenes() }
            true
        }

        EditorEvent.NewScene -> {
            viewModelScope.launch { session.newScene() }
            true
        }

        is EditorEvent.OpenScene -> {
            viewModelScope.launch { session.openScene(event.id) }
            true
        }

        is EditorEvent.DeleteScene -> {
            viewModelScope.launch {
                session.deleteScene(event.id)
                sceneList.value = session.listScenes()
            }
            true
        }

        is EditorEvent.ClearSceneById -> {
            viewModelScope.launch {
                session.clearScene(event.id)
                sceneList.value = session.listScenes()
            }
            true
        }

        is EditorEvent.RenameSceneById -> {
            renameSceneById(event.id, event.title)
            true
        }

        is EditorEvent.ExportScene -> {
            viewModelScope.launch {
                val ok = attempt { sceneTransfer.export(event.output) }
                if (ok) {
                    sounds.play(SoundCue.UiExport)
                    notify(R.string.message_scene_exported)
                } else {
                    fail(FeldError.ExportFailed)
                }
            }
            true
        }

        is EditorEvent.ImportScene -> {
            viewModelScope.launch {
                val ok = attempt { sceneTransfer.import(event.input) }
                if (ok) {
                    sounds.play(SoundCue.UiDone)
                    notify(R.string.message_scene_imported)
                } else {
                    fail(FeldError.ImportFailed)
                }
            }
            true
        }
    }

    fun previewImport(text: String): ImportPreview? {
        if (text.isBlank()) return null
        val result = parser.execute(text, customSpeakers())
        return ImportPreview(result.entries.size, result.newSpeakers.size)
    }

    private fun addEntry(
        speakerId: String,
        text: String,
        check: SkillCheck?,
        damage: DamageEffect?,
        replies: ReplyChoices?,
    ) {
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

    private fun importLines(text: String): Boolean {
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

    private fun renameSceneById(id: String, title: String) {
        if (id == session.scene.value.id) {
            session.rename(title)
            viewModelScope.launch {
                session.save()
                sceneList.value = session.listScenes()
                notify(R.string.message_scene_saved)
            }
            return
        }
        viewModelScope.launch {
            session.renameSceneById(id, title)
            sceneList.value = session.listScenes()
        }
    }

    private fun notify(@StringRes message: Int): Boolean {
        _messages.trySend(EditorMessage.Text(message))
        return false
    }

    private fun fail(e: FeldError): Boolean {
        _messages.trySend(EditorMessage.Error(e))
        return false
    }

    private fun customSpeakers() = state.value.allSpeakers.filterNot { it.isBundled }

    private suspend inline fun attempt(block: () -> Unit): Boolean = try {
        block()
        true
    } catch (e: CancellationException) {
        throw e
    } catch (_: IOException) {
        false
    } catch (_: IllegalArgumentException) {
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
        scenes = scenes.map {
            if (it.id == scene.id) it.copy(title = scene.title, lines = scene.entries.size) else it
        },
    )
}
