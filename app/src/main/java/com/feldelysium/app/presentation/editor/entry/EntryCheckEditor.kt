package com.feldelysium.app.presentation.editor.entry

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.feldelysium.app.R
import com.feldelysium.designsystem.components.input.FeldChip
import com.feldelysium.designsystem.components.input.FeldChipColors
import com.feldelysium.designsystem.components.input.FeldChipShape
import com.feldelysium.app.domain.model.CheckDifficulty
import com.feldelysium.app.domain.model.CheckResult
import com.feldelysium.app.domain.model.DamageEffect
import com.feldelysium.app.domain.model.DamageType
import com.feldelysium.app.domain.model.SkillCheck
import com.feldelysium.app.domain.model.Speaker
import com.feldelysium.app.presentation.playback.components.CheckAssets

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EntryCheckEditor(
    speaker: Speaker,
    check: SkillCheck?,
    onChange: (SkillCheck?) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!speaker.type.allowsChecks) return

    Column(modifier) {
        FieldLabel(stringResource(R.string.check_label), optional = true)

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CheckChip(stringResource(R.string.check_none), check == null) {
                onChange(null)
            }
            CheckChip(
                stringResource(R.string.check_passive),
                check != null && !check.active,
            ) {
                onChange((check ?: SkillCheck()).copy(active = false))
            }
            CheckChip(
                stringResource(R.string.check_active),
                check != null && check.active,
            ) {
                onChange((check ?: SkillCheck()).copy(active = true))
            }
        }

        if (check == null) return@Column

        Spacer(Modifier.height(14.dp))
        FieldLabel(stringResource(R.string.check_difficulty))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CheckDifficulty.entries.forEach { difficulty ->
                CheckChip(
                    text = stringResource(difficulty.stringRes),
                    selected = check.difficulty == difficulty,
                    shape = FeldChipShape.Pill,
                    outline = false,
                ) {
                    onChange(check.copy(difficulty = difficulty))
                }
            }
        }

        Spacer(Modifier.height(14.dp))
        FieldLabel(stringResource(R.string.check_result))

        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CheckResult.entries.forEach { result ->
                CheckChip(
                    text = stringResource(result.stringRes),
                    selected = check.result == result,
                ) {
                    onChange(check.copy(result = result, die1 = null, die2 = null))
                }
            }
        }

        if (check.active) {
            val (d1, d2) = check.dice

            Spacer(Modifier.height(14.dp))
            FieldLabel(stringResource(R.string.check_left_die), optional = true)

            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (n in 1..6) {
                    CheckChip(
                        text = "$n",
                        selected = d1 == n,
                        height = 32.dp,
                        width = 32.dp,
                        shape = FeldChipShape.Rectangle,
                        sidePad = 0.dp,
                    ) {
                        onChange(check.copy(die1 = n, die2 = d2))
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
            FieldLabel(stringResource(R.string.check_right_die), optional = true)

            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (n in 1..6) {
                    CheckChip(
                        text = "$n",
                        selected = d2 == n,
                        height = 32.dp,
                        width = 32.dp,
                        shape = FeldChipShape.Rectangle,
                        sidePad = 0.dp,
                    ) {
                        onChange(check.copy(die1 = d1, die2 = n))
                    }
                }
            }
        }
    }
}

private val CheckDifficulty.stringRes: Int
    get() = when (this) {
        CheckDifficulty.Trivial -> R.string.check_difficulty_trivial
        CheckDifficulty.Easy -> R.string.check_difficulty_easy
        CheckDifficulty.Medium -> R.string.check_difficulty_medium
        CheckDifficulty.Challenging -> R.string.check_difficulty_challenging
        CheckDifficulty.Formidable -> R.string.check_difficulty_formidable
        CheckDifficulty.Legendary -> R.string.check_difficulty_legendary
        CheckDifficulty.Heroic -> R.string.check_difficulty_heroic
        CheckDifficulty.Godly -> R.string.check_difficulty_godly
        CheckDifficulty.Impossible -> R.string.check_difficulty_impossible
    }

private val CheckResult.stringRes: Int
    get() = when (this) {
        CheckResult.Success -> R.string.check_result_success
        CheckResult.Failure -> R.string.check_result_failure
    }

private val DamageType.stringRes: Int
    get() = when (this) {
        DamageType.Morale -> R.string.damage_type_morale
        DamageType.Health -> R.string.damage_type_health
    }

@Composable
private fun CheckChip(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    height: Dp = 30.dp,
    width: Dp = Dp.Unspecified,
    shape: FeldChipShape = FeldChipShape.Chamfer,
    outline: Boolean = true,
    sidePad: Dp = Dp.Unspecified,
    accent: Color = FeldChipColors.Orange,
    onClick: () -> Unit,
) {
    FeldChip(
        text = text,
        selected = selected,
        onClick = onClick,
        modifier = if (width != Dp.Unspecified) modifier.width(width) else modifier,
        height = height,
        accent = accent,
        shape = shape,
        outline = outline,
        contentHorizontalPadding = sidePad,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EntryDamageEditor(damage: DamageEffect?, onChange: (DamageEffect) -> Unit, modifier: Modifier = Modifier) {
    val d = damage ?: return

    Column(modifier) {
        FieldLabel(stringResource(R.string.damage_type))

        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DamageType.entries.forEach { type ->
                CheckChip(
                    text = stringResource(type.stringRes),
                    selected = d.type == type,
                    accent = CheckAssets.damageColor(type),
                ) {
                    onChange(d.copy(type = type))
                }
            }
        }

        Spacer(Modifier.height(14.dp))
        FieldLabel(stringResource(R.string.damage_amount))

        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            for (n in 1..5) {
                CheckChip(
                    text = "$n",
                    selected = d.amount == n,
                    height = 32.dp,
                    width = 32.dp,
                    shape = FeldChipShape.Rectangle,
                    sidePad = 0.dp,
                    accent = CheckAssets.damageColor(d.type),
                ) {
                    onChange(d.copy(amount = n))
                }
            }
        }
    }
}
