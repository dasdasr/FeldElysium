package com.example.feldelysium.presentation.shared

import com.example.feldelysium.domain.model.DialogueScene
import com.example.feldelysium.domain.model.Speaker
import com.example.feldelysium.domain.registry.PredefinedSpeakers

/**
 * Single source of truth for the session's dialogue content.
 *
 * Immutable. All mutations go through [SceneAction] → reducer in [SceneViewModel].
 * [allSpeakers] is a computed property — it is NOT stored; it is derived on each access.
 */
data class SceneState(val scene: DialogueScene = DialogueScene(), val customSpeakers: List<Speaker> = emptyList()) {
    val allSpeakers: List<Speaker>
        get() = PredefinedSpeakers.ALL + customSpeakers

    fun findSpeakerByName(name: String): Speaker? =
        allSpeakers.firstOrNull { it.name.equals(name.trim(), ignoreCase = true) }
}
