package com.example.feldelysium.domain.model

/**
 * A speaker is the entity that owns a dialogue entry.
 *
 * Color is stored as a raw ARGB Long so this class has zero
 * Compose or Android dependencies and can be unit-tested with plain JUnit.
 *
 * Icon resolution priority at render time:
 *   customIconUri != null  →  load via Coil (gallery image)
 *   iconResId != null      →  load from R.drawable
 *   else                   →  render initials fallback
 */
data class Speaker(
    val id: String,
    val name: String,
    val colorArgb: Long,             // e.g. 0xFF_B5_A6_42L
    val iconResId: Int? = null,      // R.drawable.ic_xxx — predefined speakers only
    val customIconUri: String? = null, // content:// URI — custom speakers only
    val type: SpeakerType,
    val attributeGroup: AttributeGroup? = null  // non-null only for SKILL type
) {
    /**
     * Resolved sound key forwarded to SoundManager.
     * Custom speakers use the default sound.
     */
    val soundKey: String? get() = when (type) {
        SpeakerType.SKILL   -> attributeGroup?.soundKey
        SpeakerType.NPC     -> "npc"
        SpeakerType.CUSTOM  -> "default"
    }
}