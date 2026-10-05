package com.example.feldelysium.domain.registry

import com.example.feldelysium.R
import com.example.feldelysium.domain.model.AttributeGroup
import com.example.feldelysium.domain.model.Speaker
import com.example.feldelysium.domain.model.SpeakerType
import kotlin.collections.filter

/**
 * Compile-time catalog of all built-in speakers.
 *
 * Design rationale:
 *   - Named `val` properties give IDE navigation and compile-time references.
 *   - Pre-built Map index makes name lookup O(1) — critical for bulk import parsing.
 *   - This is NOT a DI singleton. It is a Kotlin object: a compile-time constant catalog.
 *   - Color values are ARGB Longs (approximated from the DE UI palette).
 */
object PredefinedSpeakers {

    // ── Builder helpers ───────────────────────────────────────────────────────

    private fun skill(
        id: String,
        name: String,
        colorArgb: Long,
        iconResId: Int?,
        group: AttributeGroup  //TODO() delete nullable
    ) = Speaker(
        id = id,
        name = name,
        colorArgb = colorArgb,
        iconResId = iconResId,
        type = SpeakerType.SKILL,
        attributeGroup = group
    )

    private fun npc(
        id: String, name: String, colorArgb: Long, iconResId: Int?
    ) = Speaker(
        id = id, name = name, colorArgb = colorArgb, iconResId = iconResId, type = SpeakerType.NPC
    )

    // ── INTELLECT skills ──────────────────────────────────────────────────────

    val LOGIC =
        skill("logic", "Logic", 0xFF_6B_9E_C8L, R.drawable.ic_logic, AttributeGroup.INTELLECT)
    val ENCYCLOPEDIA =
        skill("encyclopedia", "Encyclopedia", 0xFF_5A_8F_B8L, null, AttributeGroup.INTELLECT)
    val RHETORIC = skill(
        "rhetoric", "Rhetoric", 0xFF_7A_AE_D8L, R.drawable.ic_rhetoric, AttributeGroup.INTELLECT
    )
    val DRAMA =
        skill("drama", "Drama", 0xFF_89_4A_9EL, R.drawable.ic_drama, AttributeGroup.INTELLECT)
    val CONCEPTUALIZATION = skill(
        "conceptualization", "Conceptualization", 0xFF_6E_5E_9EL, null, AttributeGroup.INTELLECT
    )
    val VISUAL_CALCULUS =
        skill("visual_calculus", "Visual Calculus", 0xFF_5E_6E_AEL, null, AttributeGroup.INTELLECT)

    // ── PSYCHE skills ─────────────────────────────────────────────────────────

    val VOLITION = skill("volition", "Volition", 0xFF_B5_A6_42L, null, AttributeGroup.PSYCHE)
    val INLAND_EMPIRE = skill(
        "inland_empire",
        "Inland Empire",
        0xFF_C8_6E_48L,
        R.drawable.ic_inland_empire,
        AttributeGroup.PSYCHE
    )
    val EMPATHY = skill("empathy", "Empathy", 0xFF_C8_6E_88L, null, AttributeGroup.PSYCHE)
    val AUTHORITY = skill(
        "authority", "Authority", 0xFF_A8_5E_38L, R.drawable.ic_authority, AttributeGroup.PSYCHE
    )
    val ESPRIT_DE_CORPS =
        skill("esprit_de_corps", "Esprit De Corps", 0xFF_98_8E_48L, null, AttributeGroup.PSYCHE)
    val SUGGESTION = skill("suggestion", "Suggestion", 0xFF_B8_6E_98L, null, AttributeGroup.PSYCHE)

    // ── PHYSIQUE skills ───────────────────────────────────────────────────────

    val ENDURANCE =
        skill("endurance", "Endurance", 0xFF_8E_4E_2EL, null, AttributeGroup.PHYSIQUE)
    val PAIN_THRESHOLD =
        skill("pain_threshold", "Pain Threshold", 0xFF_7E_3E_1EL, null, AttributeGroup.PHYSIQUE)
    val PHYSICAL_INSTRUMENT =
        skill("physical_instrument", "Physical Instrument", 0xFF_9E_5E_3EL, R.drawable.ic_physical_instrument, AttributeGroup.PHYSIQUE)
    val ELECTROCHEMISTRY =
        skill("electrochemistry", "Electrochemistry", 0xFF_CE_7E_1EL, R.drawable.ic_electrochemistry, AttributeGroup.PHYSIQUE)
    val SHIVERS =
        skill("shivers", "Shivers", 0xFF_6E_8E_AEL, null, AttributeGroup.PHYSIQUE)
    val HALF_LIGHT =
        skill("half_light", "Half Light", 0xFF_AE_4E_2EL, null, AttributeGroup.PHYSIQUE)

    // ── MOTORICS skills ───────────────────────────────────────────────────────

    val HAND_EYE_COORD =
        skill("hand_eye_coord", "Hand/Eye Coordination", 0xFF_6E_9E_6EL, null, AttributeGroup.MOTORICS)
    val PERCEPTION =
        skill("perception", "Perception", 0xFF_7E_AE_7EL, null, AttributeGroup.MOTORICS)
    val REACTION_SPEED =
        skill("reaction_speed", "Reaction Speed", 0xFF_5E_8E_5EL, null, AttributeGroup.MOTORICS)
    val SAVOIR_FAIRE =
        skill("savoir_faire", "Savoir Faire", 0xFF_8E_BE_8EL, null, AttributeGroup.MOTORICS)
    val INTERFACING =
        skill("interfacing", "Interfacing", 0xFF_6E_AE_6EL, null, AttributeGroup.MOTORICS)
    val COMPOSURE =
        skill("composure", "Composure", 0xFF_9E_CE_9EL, null, AttributeGroup.MOTORICS)

    // ── NPCs ──────────────────────────────────────────────────────────────────

    val KIM_KITSURAGI =
        npc("kim_kitsuragi", "Kim Kitsuragi", 0xFF_4A_7E_9EL, null)
    val CUNO =
        npc("cuno", "Cuno", 0xFF_7E_3A_1AL, null)
    val JOYCE_MESSIER =
        npc("joyce_messier", "Joyce Messier", 0xFF_5A_6E_8EL, null)
    val KLAASJE =
        npc("klaasje", "Klaasje", 0xFF_8E_6A_4EL, null)
    val EVRART_CLAIRE =
        npc("evrart_claire", "Evrart Claire", 0xFF_3A_5E_2EL, null)
    val HARRY_DU_BOIS =
        npc("harry_du_bois", "Harry Du Bois", 0xFF_5E_5E_5EL, R.drawable.ic_harry)
    val STEBAN =
        npc("steban", "Steban", 0xFF_5E_5E_5EL, R.drawable.ic_steban)
    val MEASUREHEAD =
        npc("measurehead", "Measurehead", 0xFF_5E_5E_5EL, R.drawable.ic_measurehead)

    // ── Aggregates ────────────────────────────────────────────────────────────

    val SKILLS: List<Speaker> = listOf(
        LOGIC,
        ENCYCLOPEDIA,
        RHETORIC,
        DRAMA,
        CONCEPTUALIZATION,
        VISUAL_CALCULUS,
        VOLITION,
        INLAND_EMPIRE,
        EMPATHY,
        AUTHORITY,
        ESPRIT_DE_CORPS,
        SUGGESTION,
        ENDURANCE,
        PAIN_THRESHOLD,
        PHYSICAL_INSTRUMENT,
        ELECTROCHEMISTRY,
        SHIVERS,
        HALF_LIGHT,
        HAND_EYE_COORD,
        PERCEPTION,
        REACTION_SPEED,
        SAVOIR_FAIRE,
        INTERFACING,
        COMPOSURE
    )

    val NPCS: List<Speaker> = listOf(
        KIM_KITSURAGI,
        CUNO,
        JOYCE_MESSIER,
        KLAASJE,
        EVRART_CLAIRE,
        HARRY_DU_BOIS,
        MEASUREHEAD,
        STEBAN
    )

    val ALL: List<Speaker> = SKILLS + NPCS

    // Pre-built for O(1) bulk-import lookup. Keys are uppercased display names.
    // "ESPRIT DE CORPS" → ESPRIT_DE_CORPS, "KIM KITSURAGI" → KIM_KITSURAGI, etc.
    private val byNameIndex: Map<String, Speaker> = ALL.associateBy { it.name.uppercase() }

    fun findByName(name: String): Speaker? = byNameIndex[name.trim().uppercase()]

    fun skillsByGroup(group: AttributeGroup): List<Speaker> =
        SKILLS.filter { it.attributeGroup == group }
}