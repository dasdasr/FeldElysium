package com.feldelysium.app.domain.scene

import com.feldelysium.app.domain.model.DialogueScene
import com.feldelysium.app.domain.model.Speaker
import com.feldelysium.app.domain.model.normalized
import com.feldelysium.app.domain.repository.SpeakerRepository
import kotlinx.coroutines.flow.first

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
