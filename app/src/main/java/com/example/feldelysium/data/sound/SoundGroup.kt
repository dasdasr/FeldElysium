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
        "intellect_appear.wav"
    ),
    PSYCHE(
        "psyche",
        "psyche_appear.wav"
    ),
    PHYSIQUE(
        "physique",
        "physique_appear.wav"
    ),
    MOTORICS(
        "motorics",
        "motorics_appear.wav"
    ),
    NPC(
        "npc",
        "npc_appear.wav"
    ),
    DEFAULT(
        "default",
        "default_appear.wav"
    );

    companion object {
        fun fromKey(key: String?): SoundGroup =
            key?.let { k -> entries.firstOrNull { it.key == k } } ?: DEFAULT
    }
}