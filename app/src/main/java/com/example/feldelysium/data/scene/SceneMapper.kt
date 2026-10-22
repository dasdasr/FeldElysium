package com.example.feldelysium.data.scene

import com.example.feldelysium.domain.model.CheckDifficulty
import com.example.feldelysium.domain.model.CheckResult
import com.example.feldelysium.domain.model.DamageEffect
import com.example.feldelysium.domain.model.DamageType
import com.example.feldelysium.domain.model.DialogueEntry
import com.example.feldelysium.domain.model.DialogueScene
import com.example.feldelysium.domain.model.ReplyChoices
import com.example.feldelysium.domain.model.SkillCheck
import com.example.feldelysium.domain.model.Speaker
import com.example.feldelysium.domain.model.SpeakerType
import com.example.feldelysium.domain.registry.PredefinedSpeakers

internal fun DialogueScene.toFile() = SceneFile(
    id = id,
    title = title,
    entries = entries.map { e ->
        EntryData(
            id = e.id,
            speakerId = e.speaker.id,
            text = e.text,
            check = e.check?.let { c -> CheckData(c.difficulty.name, c.result.name, c.active, c.die1, c.die2) },
            damage = e.damage?.let { d -> DamageData(d.type.name, d.amount) },
            replies = e.replies?.let { RepliesData(it.options, it.chosenIndex) },
        )
    },
    speakers = entries.map { it.speaker }
        .filterNot { it.isBundled }
        .distinctBy { it.id }
        .map { s ->
            SpeakerSnapshot(
                id = s.id,
                name = s.name,
                color = s.colorArgb,
                tagId = s.type.id.takeUnless { s.type.isCustom },
                tagName = s.type.name,
                allowsChecks = s.type.allowsChecks,
            )
        },
)

internal fun SceneFile.toScene(): DialogueScene {
    val snapshots = speakers.associate { it.id to it.toSpeaker() }
    return DialogueScene(
        id = id,
        title = title,
        entries = entries.mapNotNull { e ->
            val speaker = snapshots[e.speakerId]
                ?: PredefinedSpeakers.ALL.firstOrNull { it.id == e.speakerId }
                ?: return@mapNotNull null
            DialogueEntry(
                id = e.id,
                speaker = speaker,
                text = e.text,
                check = e.check?.toDomain(),
                damage = e.damage?.toDomain(),
                replies = e.replies?.let { ReplyChoices(it.options, it.chosenIndex).normalized() },
            )
        },
    )
}

private fun SpeakerSnapshot.toSpeaker() = Speaker(
    id = id,
    name = name,
    colorArgb = color,
    type = tagId?.let { SpeakerType(it, tagName ?: it, allowsChecks = allowsChecks) } ?: SpeakerType.CUSTOM,
)

private fun CheckData.toDomain() = SkillCheck(
    difficulty = enumOr(difficulty, CheckDifficulty.Medium),
    result = enumOr(result, CheckResult.Success),
    active = active,
    die1 = die1,
    die2 = die2,
)

private fun DamageData.toDomain() = DamageEffect(enumOr(type, DamageType.Morale), amount.coerceIn(1, 5))

private inline fun <reified E : Enum<E>> enumOr(name: String, default: E): E =
    enumValues<E>().firstOrNull { it.name == name } ?: default
