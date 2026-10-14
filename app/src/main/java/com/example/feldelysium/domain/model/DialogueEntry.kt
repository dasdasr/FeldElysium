package com.example.feldelysium.domain.model

import java.util.UUID

data class DialogueEntry(
    val id: String = UUID.randomUUID().toString(),
    val speaker: Speaker,
    val text: String,
    val metadata: Map<String, String> = emptyMap(),
)
