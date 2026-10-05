package com.example.feldelysium.domain.model

import java.util.UUID

/**
 * The full ordered sequence of dialogue entries that make up one scene.
 */
data class DialogueScene(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "Untitled Scene",
    val entries: List<DialogueEntry> = emptyList()
)