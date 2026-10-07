package com.example.feldelysium.domain.model

data class Speaker(
    val id: String,
    val name: String,
    val colorArgb: Long,
    val iconResId: Int? = null,
    val customIconUri: String? = null,
    val type: SpeakerType,
    val attributeGroup: AttributeGroup? = null // Only set for SKILL speakers.
) {
    val soundKey: String?
        get() = when (type) {
            SpeakerType.SKILL -> attributeGroup?.soundKey
            SpeakerType.NPC -> "npc"
            SpeakerType.CUSTOM -> "default"
        }
}
