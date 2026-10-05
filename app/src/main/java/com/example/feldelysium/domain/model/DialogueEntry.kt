package com.example.feldelysium.domain.model

import java.util.UUID

/**
 * A single line of dialogue.
 *
 * Immutable value object. Mutations produce new instances via [copy].
 */
data class DialogueEntry(
    val id: String = UUID.randomUUID().toString(),
    val speaker: Speaker,
    val text: String,
    val metadata: Map<String, String> = emptyMap()
)