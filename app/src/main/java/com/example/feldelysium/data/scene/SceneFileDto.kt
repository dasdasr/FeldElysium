package com.example.feldelysium.data.scene

import kotlinx.serialization.Serializable

@Serializable
internal data class SceneFile(
    val version: Int = VERSION,
    val id: String,
    val title: String,
    val entries: List<EntryData>,
    val speakers: List<SpeakerSnapshot> = emptyList(),
) {
    companion object { const val VERSION = 1 }
}

@Serializable
internal data class EntryData(
    val id: String,
    val speakerId: String,
    val text: String,
    val check: CheckData? = null,
    val damage: DamageData? = null,
)

@Serializable
internal data class CheckData(
    val difficulty: String,
    val result: String,
    val active: Boolean,
    val die1: Int? = null,
    val die2: Int? = null,
)

@Serializable
internal data class DamageData(val type: String, val amount: Int)

@Serializable
internal data class SpeakerSnapshot(
    val id: String,
    val name: String,
    val color: Long,
    val tagId: String? = null,
    val tagName: String? = null,
    val allowsChecks: Boolean = false,
)
