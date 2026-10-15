package com.example.feldelysium.domain.usecase

import com.example.feldelysium.domain.model.DialogueScene
import com.example.feldelysium.domain.model.SceneInfo
import com.example.feldelysium.domain.model.Speaker
import com.example.feldelysium.domain.model.normalized
import com.example.feldelysium.domain.repository.SceneRepository
import com.example.feldelysium.domain.repository.SpeakerRepository
import kotlinx.coroutines.flow.first
import java.io.InputStream
import java.io.OutputStream

class SceneSpeakerReconciler(private val speakerRepository: SpeakerRepository) {
    suspend fun reconcile(scene: DialogueScene): DialogueScene {
        val known = speakerRepository.speakers.first().associateBy { it.id }
        val resolved = mutableMapOf<String, Speaker>()
        scene.entries.map { it.speaker }.distinctBy { it.id }.forEach { stored ->
            val live = known[stored.id]
            if (live != null) {
                resolved[stored.id] = live
            } else {
                speakerRepository.save(stored)
                resolved[stored.id] = stored
            }
        }
        return scene.copy(
            entries = scene.entries.map { e ->
                e.copy(speaker = resolved.getValue(e.speaker.id)).normalized()
            },
        )
    }
}

class LoadCurrentSceneUseCase(
    private val scenes: SceneRepository,
    private val reconciler: SceneSpeakerReconciler,
) {
    suspend operator fun invoke(): DialogueScene? = scenes.loadCurrent()?.let { reconciler.reconcile(it) }
}

class OpenSceneUseCase(
    private val scenes: SceneRepository,
    private val reconciler: SceneSpeakerReconciler,
) {
    suspend operator fun invoke(id: String): DialogueScene? = scenes.open(id)?.let { reconciler.reconcile(it) }
}

class ImportSceneUseCase(
    private val scenes: SceneRepository,
    private val reconciler: SceneSpeakerReconciler,
) {
    suspend operator fun invoke(input: InputStream): DialogueScene {
        val scene = reconciler.reconcile(scenes.import(input))
        scenes.save(scene)
        return scene
    }
}

class SaveSceneUseCase(private val scenes: SceneRepository) {
    suspend operator fun invoke(scene: DialogueScene) = scenes.save(scene)
}

class ListScenesUseCase(private val scenes: SceneRepository) {
    suspend operator fun invoke(): List<SceneInfo> = scenes.list()
}

class DeleteSceneUseCase(private val scenes: SceneRepository) {
    suspend operator fun invoke(id: String) = scenes.delete(id)
}

class ExportSceneUseCase(private val scenes: SceneRepository) {
    suspend operator fun invoke(scene: DialogueScene, output: OutputStream) = scenes.export(scene, output)
}

class SceneUseCases(
    val loadCurrent: LoadCurrentSceneUseCase,
    val open: OpenSceneUseCase,
    val save: SaveSceneUseCase,
    val list: ListScenesUseCase,
    val delete: DeleteSceneUseCase,
    val export: ExportSceneUseCase,
    val import: ImportSceneUseCase,
)
