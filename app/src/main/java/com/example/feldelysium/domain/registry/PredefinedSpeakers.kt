package com.example.feldelysium.domain.registry

import com.example.feldelysium.R
import com.example.feldelysium.domain.model.Speaker
import com.example.feldelysium.domain.model.SpeakerColor
import com.example.feldelysium.domain.model.SpeakerType

object PredefinedSpeakers {

    private fun tag(id: String, name: String, color: SpeakerColor) =
        SpeakerType(id, name, color.argb, id, allowsChecks = true)

    val INTELLECT_TAG = tag("intellect", "Intellect", SpeakerColor.INTELLECT)
    val PSYCHE_TAG = tag("psyche", "Psyche", SpeakerColor.PSYCHE)
    val PHYSIQUE_TAG = tag("physique", "Physique", SpeakerColor.PHYSIQUE)
    val MOTORICS_TAG = tag("motorics", "Motorics", SpeakerColor.MOTORICS)

    val TAGS: List<SpeakerType> =
        listOf(INTELLECT_TAG, PSYCHE_TAG, PHYSIQUE_TAG, MOTORICS_TAG, SpeakerType.NPC, SpeakerType.YOU)
    private val tagIds = TAGS.map { it.id }.toSet()

    fun isBundledTag(id: String) = id in tagIds

    private fun skill(id: String, name: String, iconResId: Int?, tag: SpeakerType) = Speaker(
        id = id,
        name = name,
        colorArgb = tag.colorArgb ?: SpeakerColor.DEFAULT.argb,
        iconResId = iconResId,
        type = tag,
        isBundled = true,
    )

    private fun npc(id: String, name: String, iconResId: Int?) = Speaker(
        id = id,
        name = name,
        colorArgb = SpeakerColor.PLAIN.argb,
        iconResId = iconResId,
        type = SpeakerType.NPC,
        isBundled = true,
    )

    val YOU = Speaker("you", "You", SpeakerColor.PLAIN.argb, null, type = SpeakerType.YOU, isBundled = true)

    val LOGIC = skill("logic", "Logic", R.drawable.ic_logic, INTELLECT_TAG)
    val ENCYCLOPEDIA = skill("encyclopedia", "Encyclopedia", null, INTELLECT_TAG)
    val RHETORIC = skill("rhetoric", "Rhetoric", R.drawable.ic_rhetoric, INTELLECT_TAG)
    val DRAMA = skill("drama", "Drama", R.drawable.ic_drama, INTELLECT_TAG)
    val CONCEPTUALIZATION = skill("conceptualization", "Conceptualization", null, INTELLECT_TAG)
    val VISUAL_CALCULUS = skill("visual_calculus", "Visual Calculus", null, INTELLECT_TAG)

    val VOLITION = skill("volition", "Volition", null, PSYCHE_TAG)
    val INLAND_EMPIRE =
        skill("inland_empire", "Inland Empire", R.drawable.ic_inland_empire, PSYCHE_TAG)
    val EMPATHY = skill("empathy", "Empathy", null, PSYCHE_TAG)
    val AUTHORITY = skill("authority", "Authority", R.drawable.ic_authority, PSYCHE_TAG)
    val ESPRIT_DE_CORPS = skill("esprit_de_corps", "Esprit De Corps", null, PSYCHE_TAG)
    val SUGGESTION = skill("suggestion", "Suggestion", null, PSYCHE_TAG)

    val ENDURANCE = skill("endurance", "Endurance", null, PHYSIQUE_TAG)
    val PAIN_THRESHOLD = skill("pain_threshold", "Pain Threshold", null, PHYSIQUE_TAG)
    val PHYSICAL_INSTRUMENT = skill(
        "physical_instrument",
        "Physical Instrument",
        R.drawable.ic_physical_instrument,
        PHYSIQUE_TAG,
    )
    val ELECTROCHEMISTRY =
        skill("electrochemistry", "Electrochemistry", R.drawable.ic_electrochemistry, PHYSIQUE_TAG)
    val SHIVERS = skill("shivers", "Shivers", null, PHYSIQUE_TAG)
    val HALF_LIGHT = skill("half_light", "Half Light", null, PHYSIQUE_TAG)

    val HAND_EYE_COORD = skill("hand_eye_coord", "Hand/Eye Coordination", null, MOTORICS_TAG)
    val PERCEPTION = skill("perception", "Perception", null, MOTORICS_TAG)
    val REACTION_SPEED = skill("reaction_speed", "Reaction Speed", null, MOTORICS_TAG)
    val SAVOIR_FAIRE = skill("savoir_faire", "Savoir Faire", null, MOTORICS_TAG)
    val INTERFACING = skill("interfacing", "Interfacing", null, MOTORICS_TAG)
    val COMPOSURE = skill("composure", "Composure", null, MOTORICS_TAG)

    val KIM_KITSURAGI = npc("kim_kitsuragi", "Kim Kitsuragi", null)
    val CUNO = npc("cuno", "Cuno", null)
    val JOYCE_MESSIER = npc("joyce_messier", "Joyce Messier", null)
    val KLAASJE = npc("klaasje", "Klaasje", null)
    val EVRART_CLAIRE = npc("evrart_claire", "Evrart Claire", null)
    val HARRY_DU_BOIS = npc("harry_du_bois", "Harry Du Bois", R.drawable.ic_harry)
    val STEBAN = npc("steban", "Steban", R.drawable.ic_steban)
    val MEASUREHEAD = npc("measurehead", "Measurehead", R.drawable.ic_measurehead)

    val SKILLS: List<Speaker> = listOf(
        LOGIC, ENCYCLOPEDIA, RHETORIC, DRAMA, CONCEPTUALIZATION, VISUAL_CALCULUS,
        VOLITION, INLAND_EMPIRE, EMPATHY, AUTHORITY, ESPRIT_DE_CORPS, SUGGESTION,
        ENDURANCE, PAIN_THRESHOLD, PHYSICAL_INSTRUMENT, ELECTROCHEMISTRY, SHIVERS, HALF_LIGHT,
        HAND_EYE_COORD, PERCEPTION, REACTION_SPEED, SAVOIR_FAIRE, INTERFACING, COMPOSURE,
    )

    val NPCS: List<Speaker> = listOf(
        KIM_KITSURAGI,
        CUNO,
        JOYCE_MESSIER,
        KLAASJE,
        EVRART_CLAIRE,
        HARRY_DU_BOIS,
        MEASUREHEAD,
        STEBAN,
    )

    val ALL: List<Speaker> = SKILLS + NPCS + YOU

    private val byNameIndex: Map<String, Speaker> = ALL.associateBy { it.name.uppercase() }

    fun findByName(name: String): Speaker? = byNameIndex[name.trim().uppercase()]

    fun byTag(tagId: String): List<Speaker> = ALL.filter { it.type.id == tagId }
}
