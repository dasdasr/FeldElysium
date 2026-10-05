package com.example.feldelysium.domain.model

/**
 * The five core attribute groups from Disco Elysium.
 * Each group maps to a distinct appearance sound.
 */
enum class AttributeGroup(
    val displayName: String,
    val soundKey: String         // matches SoundGroup.key in the data layer
) {
    INTELLECT("Intellect", "intellect"),
    PSYCHE("Psyche", "psyche"),
    PHYSIQUE("Physique", "physique"),
    MOTORICS("Motorics", "motorics"),
}