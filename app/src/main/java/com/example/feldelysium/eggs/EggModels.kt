package com.example.feldelysium.eggs

import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

enum class EggRarity { Frequent, Rare, Secret, UltraRare }

enum class CheckResult(val label: String) { Success("Success"), Failure("Failure") }

object Difficulty {
    const val Trivial = "Trivial"
    const val Easy = "Easy"
    const val Medium = "Medium"
    const val Challenging = "Challenging"
    const val Formidable = "Formidable"
    const val Legendary = "Legendary"
    const val Heroic = "Heroic"
    const val Godly = "Godly"
    const val Impossible = "Impossible"
}

data class EggReply(
    val skill: String,
    val text: String,
    val difficulty: String? = null,
    val result: CheckResult? = null,
    val scope: String? = null,
) {
    val header: String
        get() = if (difficulty == null && result == null) skill.uppercase() else buildString {
            append('[')
            if (difficulty != null) append(difficulty).append(": ")
            append(skill)
            if (result != null) append(" — ").append(result.label)
            append(']')
        }
}

fun say(skill: String, text: String, scope: String? = null) =
    EggReply(skill = skill, text = text, scope = scope)

fun skillCheck(
    skill: String,
    text: String,
    difficulty: String? = null,
    result: CheckResult = CheckResult.Success,
    scope: String? = null,
) = EggReply(skill = skill, text = text, difficulty = difficulty, result = result, scope = scope)

data class AppEvent(val key: String, val value: String?, val atMillis: Long)

class EggContext(
    val event: AppEvent,
    private val history: List<AppEvent>,
    private val sessionCounts: Map<String, Int>,
    val hour: Int,
    val minute: Int,
    val epochDay: Long,
) {
    fun count(key: String, within: Duration, value: String? = null): Int {
        val from = event.atMillis - within.inWholeMilliseconds
        return history.count {
            it.key == key && it.atMillis >= from && (value == null || it.value.equals(value, true))
        }
    }

    fun sessionCount(key: String): Int = sessionCounts[key] ?: 0

    fun endsWith(keys: List<String>, within: Duration): Boolean {
        if (keys.isEmpty() || history.size < keys.size) return false
        val tail = history.takeLast(keys.size)
        return tail.map { it.key } == keys &&
                event.atMillis - tail.first().atMillis <= within.inWholeMilliseconds
    }
}

typealias EggTrigger = (EggContext) -> Boolean

fun onEvent(key: String, value: String? = null): EggTrigger = { ctx ->
    ctx.event.key == key && (value == null || ctx.event.value.equals(value, true))
}

fun burst(key: String, count: Int, within: Duration, value: String? = null): EggTrigger = { ctx ->
    onEvent(key, value)(ctx) && ctx.count(key, within, value) >= count
}

fun inOrder(vararg keys: String, within: Duration = 20.seconds): EggTrigger = { ctx ->
    ctx.endsWith(keys.toList(), within)
}

fun timeBetween(fromHour: Int, toHour: Int, key: String = "app.open"): EggTrigger = { ctx ->
    ctx.event.key == key && if (fromHour <= toHour) ctx.hour in fromHour until toHour
    else ctx.hour >= fromHour || ctx.hour < toHour
}

fun whenValue(key: String, predicate: (String) -> Boolean): EggTrigger = { ctx ->
    ctx.event.key == key && ctx.event.value?.let(predicate) == true
}

fun allOf(vararg triggers: EggTrigger): EggTrigger = { ctx -> triggers.all { it(ctx) } }

fun anyOf(vararg triggers: EggTrigger): EggTrigger = { ctx -> triggers.any { it(ctx) } }

private fun EggRarity.defaultCooldown(): Duration = when (this) {
    EggRarity.Frequent -> Duration.ZERO //20.seconds
    EggRarity.Rare -> Duration.ZERO //1.minutes
    EggRarity.Secret -> Duration.ZERO //1.minutes
    EggRarity.UltraRare -> Duration.ZERO //1.minutes
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
    val toastDurationMillis: Long = 5500L,
)
