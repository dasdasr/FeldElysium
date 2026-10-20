package com.example.feldelysium.domain

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
