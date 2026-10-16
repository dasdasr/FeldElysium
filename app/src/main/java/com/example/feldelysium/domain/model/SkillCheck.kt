package com.example.feldelysium.domain.model

enum class CheckDifficulty {
    Trivial,
    Easy,
    Medium,
    Challenging,
    Formidable,
    Legendary,
    Heroic,
    Godly,
    Impossible,
}

enum class CheckResult { Success, Failure }

enum class DamageType { Morale, Health }

data class DamageEffect(val type: DamageType = DamageType.Morale, val amount: Int = 1)

/**
 * Passive check: only the "[Medium: Success]" label after the name.
 * Active check ([active] = true): tape roll + dice + result flash before the line appears.
 * [die1]/[die2] are optional overrides; null = default values for the result.
 */
data class SkillCheck(
    val difficulty: CheckDifficulty = CheckDifficulty.Medium,
    val result: CheckResult = CheckResult.Success,
    val active: Boolean = false,
    val die1: Int? = null,
    val die2: Int? = null,
) {
    val dice: Pair<Int, Int>
        get() {
            val default = defaultDice(result)
            return (die1?.coerceIn(1, 6) ?: default.first) to (die2?.coerceIn(1, 6) ?: default.second)
        }

    companion object {
        fun defaultDice(result: CheckResult): Pair<Int, Int> = if (result == CheckResult.Failure) 2 to 1 else 5 to 6
    }
}

fun DialogueEntry.normalized(): DialogueEntry =
    if (check != null && !speaker.type.allowsChecks) copy(check = null) else this
