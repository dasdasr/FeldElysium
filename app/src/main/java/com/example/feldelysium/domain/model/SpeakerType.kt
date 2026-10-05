package com.example.feldelysium.domain.model

enum class SpeakerType {
    SKILL,   // An internal voice / skill — belongs to an AttributeGroup
    NPC,     // A named character in the world
    CUSTOM   // Created by the user during the current session
}