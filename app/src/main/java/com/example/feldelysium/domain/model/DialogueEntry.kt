package com.example.feldelysium.domain.model

import java.util.UUID

data class DialogueEntry(
    val id: String = UUID.randomUUID().toString(),
    val speaker: Speaker,
    val text: String,
    val check: SkillCheck? = null,
    val damage: DamageEffect? = null,
    val replies: ReplyChoices? = null,
    val metadata: Map<String, String> = emptyMap(),
)

data class ReplyChoices(val options: List<String>, val chosenIndex: Int = 0) {
    val chosen: String get() = options.getOrElse(chosenIndex) { options.first() }

    fun normalized(): ReplyChoices? {
        val kept = options.mapIndexedNotNull { i, o ->
            o.trim().take(MAX_LENGTH).takeIf { it.isNotEmpty() }?.let { i to it }
        }
        if (kept.size < 2) return null
        val idx = kept.indexOfFirst { it.first == chosenIndex }.coerceAtLeast(0)
        return ReplyChoices(kept.map { it.second }, idx)
    }

    companion object {
        const val MAX_LENGTH = 60
        const val MAX_OPTIONS = 5
    }
}
