package com.example.feldelysium.domain.model

import java.util.UUID

data class DialogueScene(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "Feld Scene",
    val entries: List<DialogueEntry> = emptyList()
)
