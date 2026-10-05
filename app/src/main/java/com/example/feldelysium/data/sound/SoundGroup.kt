package com.example.feldelysium.data.sound

/**
 * Maps speaker sound keys (domain strings) to asset file paths (data concerns).
 *
 * The `key` property matches Speaker.soundKey and AttributeGroup.soundKey,
 * keeping the domain free of any data-layer knowledge.
 */
enum class SoundGroup(val key: String, val assetPath: String) {
    INTELLECT(
        "intellect",
        "sounds/intellect_appear.wav"
    ),
    PSYCHE(
        "psyche",
        "sounds/psyche_appear.wav"
    ),
    PHYSIQUE(
        "physique",
        "sounds/physique_appear.wav"
    ),
    MOTORICS(
        "motorics",
        "sounds/motorics_appear.wav"
    ),
    NPC(
        "npc",
        "sounds/npc_appear.wav"
    ),
    DEFAULT(
        "default",
        "sounds/default_appear.wav"
    );

    companion object {
        fun fromKey(key: String?): SoundGroup =
            key?.let { k -> entries.firstOrNull { it.key == k } } ?: DEFAULT
    }
}