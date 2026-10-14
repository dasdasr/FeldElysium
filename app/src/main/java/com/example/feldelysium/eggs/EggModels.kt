package com.example.feldelysium.eggs

import com.example.feldelysium.domain.model.Speaker
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

enum class EggRarity { Frequent, Rare, Secret, UltraRare }

enum class CheckResult(val label: String) { Success("Success"), Failure("Failure") }

enum class Difficulty(val label: String) {
    None("None"),
    Trivial("Trivial"),
    Easy("Easy"),
    Medium("Medium"),
    Challenging("Challenging"),
    Formidable("Formidable"),
    Legendary("Legendary"),
    Heroic("Heroic"),
    Godly("Godly"),
    Impossible("Impossible"),
}

data class EggReply(
    val skill: Speaker,
    val text: String,
    val difficulty: Difficulty? = null,
    val result: CheckResult? = null,
    val scope: Enum<*>? = null,
) {
    val header: String
        get() = if (difficulty == null && result == null) {
            skill.name.uppercase()
        } else {
            buildString {
                append('[')
                if (difficulty != null) append(difficulty.label).append(": ")
                append(skill.name)
                if (result != null) append(" — ").append(result.label)
                append(']')
            }
        }
}

fun say(skill: Speaker, text: String, scope: Enum<*>? = null) = EggReply(skill = skill, text = text, scope = scope)

fun skillCheck(
    skill: Speaker,
    text: String,
    difficulty: Difficulty = Difficulty.None,
    result: CheckResult = CheckResult.Success,
    scope: Enum<*>? = null,
) = EggReply(skill = skill, text = text, difficulty = difficulty, result = result, scope = scope)

data class AppEvent(val key: EggEvent, val value: String?, val atMillis: Long)

class EggContext(
    val event: AppEvent,
    private val history: List<AppEvent>,
    private val sessionCounts: Map<EggEvent, Int>,
    val hour: Int,
    val minute: Int,
    val epochDay: Long,
) {
    fun count(key: EggEvent, within: Duration, value: String? = null): Int {
        val from = event.atMillis - within.inWholeMilliseconds
        return history.count {
            it.key == key && it.atMillis >= from && (value == null || it.value.equals(value, true))
        }
    }

    fun sessionCount(key: EggEvent): Int = sessionCounts[key] ?: 0

    fun endsWith(keys: List<EggEvent>, within: Duration): Boolean {
        if (keys.isEmpty() || history.size < keys.size) return false
        val tail = history.takeLast(keys.size)
        return tail.map { it.key } == keys &&
            event.atMillis - tail.first().atMillis <= within.inWholeMilliseconds
    }
}

typealias EggTrigger = (EggContext) -> Boolean

fun onEvent(key: EggEvent, value: Enum<*>? = null): EggTrigger = { ctx ->
    ctx.event.key == key && (value == null || ctx.event.value.equals(value.name, true))
}

// fun onEvent(key: EggEvent, value: String): EggTrigger = { ctx ->
// ctx.event.key == key && ctx.event.value .equals(value , true)
// }

fun burst(key: EggEvent, count: Int, within: Duration, value: Enum<*>? = null): EggTrigger = { ctx ->
    onEvent(key, value)(ctx) && ctx.count(key, within, value?.name) >= count
}

fun inOrder(vararg keys: EggEvent, within: Duration = 20.seconds): EggTrigger = { ctx ->
    ctx.endsWith(keys.toList(), within)
}

fun timeBetween(fromHour: Int, toHour: Int, key: EggEvent = EggEvent.AppOpen): EggTrigger = { ctx ->
    ctx.event.key == key && if (fromHour <= toHour) {
        ctx.hour in fromHour until toHour
    } else {
        ctx.hour >= fromHour || ctx.hour < toHour
    }
}

fun whenValue(key: EggEvent, predicate: (String) -> Boolean): EggTrigger = { ctx ->
    ctx.event.key == key && ctx.event.value?.let(predicate) == true
}

fun allOf(vararg triggers: EggTrigger): EggTrigger = { ctx -> triggers.all { it(ctx) } }

fun anyOf(vararg triggers: EggTrigger): EggTrigger = { ctx -> triggers.any { it(ctx) } }

private fun EggRarity.defaultCooldown(): Duration = when (this) {
    EggRarity.Frequent -> Duration.ZERO

    // 20.seconds
    EggRarity.Rare -> Duration.ZERO

    // 1.minutes
    EggRarity.Secret -> Duration.ZERO

    // 1.minutes
    EggRarity.UltraRare -> Duration.ZERO // 1.minutes
}

data class Egg(
    val id: String,
    val rarity: EggRarity,
    val replies: List<EggReply>,
    val trigger: EggTrigger,
    val cooldown: Duration = rarity.defaultCooldown(),
    val chance: Float = if (rarity == EggRarity.UltraRare) 0.05f else 1f,
    val maxPerSession: Int? = if (rarity == EggRarity.Frequent) 6 else null,
    val perDay: Int? = null,
    val priority: Int = 0,
)

data class EggConfig(
    val globalMinGap: Duration = 4.seconds,
    val maxPerWindow: Int = 3,
    val window: Duration = 60.seconds,
    val rollGap: Duration = 10.seconds,
    val toastDurationMillis: Long = 4200L,
)
