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

    private fun skill(id: String, name: String, iconResId: Int?, tag: SpeakerType, sample: Boolean = false) = Speaker(
        id = id,
        name = name,
        colorArgb = tag.colorArgb ?: SpeakerColor.DEFAULT.argb,
        iconResId = iconResId,
        type = tag,
        isBundled = true,
        isSample = sample,
    )

    val YOU = Speaker("you", "You", SpeakerColor.PLAIN.argb, null, type = SpeakerType.YOU, isBundled = true)
    val LEGO_HARRY = Speaker(
        "lego_harry",
        "Harry Du Brick",
        SpeakerColor.PLAIN.argb,
        R.drawable.ic_lego_harry,
        type = SpeakerType.NPC,
        isBundled = true,
    )
    val LEGO_KIM = Speaker(
        "lego_kim",
        "Kim Legoragi",
        SpeakerColor.PLAIN.argb,
        R.drawable.ic_lego_kim,
        type = SpeakerType.NPC,
        isBundled = true,
    )

    val WORTH = skill("worth", "Worth", R.drawable.ic_worth, INTELLECT_TAG, sample = true)
    val WORLDWRIGHT = skill("worldwright", "Worldwright", R.drawable.ic_worldwright, INTELLECT_TAG, sample = true)

    val TENDERNESS = skill("tenderness", "Tenderness", R.drawable.ic_tenderness, PSYCHE_TAG, sample = true)
    val THE_UNSAID = skill("the_unsaid", "The Unsaid", R.drawable.ic_the_unsaid, PSYCHE_TAG, sample = true)

    val GROUNDING = skill("grounding", "Grounding", R.drawable.ic_grounding, PHYSIQUE_TAG, sample = true)
    val BREATHWARDEN = skill("breathwarden", "Breathwarden", R.drawable.ic_breathwarden, PHYSIQUE_TAG, sample = true)

    val EVASION = skill("evasion", "Evasion", R.drawable.ic_evasion, MOTORICS_TAG, sample = true)
    val OFF_KILTER = skill("off_kilter", "Off-Kilter", R.drawable.ic_off_kilter, MOTORICS_TAG, sample = true)

    val SKILLS: List<Speaker> = listOf(
        WORTH,
        WORLDWRIGHT,
        TENDERNESS,
        THE_UNSAID,
        GROUNDING,
        BREATHWARDEN,
        EVASION,
        OFF_KILTER,
    )

    val NPCS: List<Speaker> = listOf(
        LEGO_HARRY,
        LEGO_KIM,
    )

    val ALL: List<Speaker> = SKILLS + NPCS + YOU

    private val byNameIndex: Map<String, Speaker> = ALL.associateBy { it.name.uppercase() }

    fun findByName(name: String): Speaker? = byNameIndex[name.trim().uppercase()]

    fun byTag(tagId: String): List<Speaker> = ALL.filter { it.type.id == tagId }
}
