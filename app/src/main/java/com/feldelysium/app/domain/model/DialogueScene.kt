package com.feldelysium.app.domain.model

import androidx.compose.runtime.Immutable
import java.util.UUID

@Immutable
data class DialogueScene(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "Feld Scene",
    val entries: List<DialogueEntry> = emptyList(),
)
