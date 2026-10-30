package com.feldelysium.app.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class SpeakerType(
    val id: String,
    val name: String,
    val colorArgb: Long? = null,
    val soundKey: String? = null,
    val allowsChecks: Boolean = false,
) {
    val isCustom: Boolean get() = id == CUSTOM_ID

    companion object {
        const val MAX_NAME = 20
        private const val CUSTOM_ID = "custom"

        val CUSTOM = SpeakerType(CUSTOM_ID, "Custom")
        val NPC = SpeakerType("npc", "NPC", SpeakerColor.PLAIN.argb, SoundKeys.NPC)

        val YOU = SpeakerType("you", "You", SpeakerColor.PLAIN.argb, SoundKeys.NONE)

        fun normalizeName(raw: String): String = raw.trim().replace(Regex("\\s+"), " ").take(MAX_NAME)

        fun slugOf(name: String): String = name.lowercase().replace(Regex("[^\\p{L}\\p{N}]+"), "-").trim('-')
            .ifEmpty { "t" + name.hashCode().toString(16) }

        fun create(
            rawName: String,
            colorArgb: Long? = null,
            soundKey: String? = null,
            allowsChecks: Boolean = false,
        ): SpeakerType? {
            val name = normalizeName(rawName)
            if (name.isEmpty()) return null
            val id = slugOf(name)
            return when (id) {
                CUSTOM_ID -> CUSTOM
                NPC.id -> NPC
                else -> SpeakerType(id, name, colorArgb, soundKey, allowsChecks)
            }
        }

        fun fromStored(id: String, name: String): SpeakerType = when (id) {
            CUSTOM_ID -> CUSTOM
            NPC.id -> NPC
            else -> SpeakerType(id, name)
        }
    }
}

object SoundKeys {
    const val NONE = "none"
    const val DEFAULT = "default"
    const val NPC = "npc"
    const val INTELLECT = "intellect"
    const val PSYCHE = "psyche"
    const val PHYSIQUE = "physique"
    const val MOTORICS = "motorics"
    val All = listOf(DEFAULT, NPC, INTELLECT, PSYCHE, PHYSIQUE, MOTORICS)
}
