package com.example.feldelysium.presentation.editor.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.feldelysium.designsystem.components.input.FeldChip
import com.example.feldelysium.designsystem.components.input.FeldChipColors
import com.example.feldelysium.designsystem.components.input.FeldChipShape
import com.example.feldelysium.domain.model.CheckDifficulty
import com.example.feldelysium.domain.model.CheckResult
import com.example.feldelysium.domain.model.DamageEffect
import com.example.feldelysium.domain.model.DamageType
import com.example.feldelysium.domain.model.SkillCheck
import com.example.feldelysium.domain.model.Speaker
import com.example.feldelysium.presentation.playback.components.CheckAssets

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
        FieldLabel("Check", optional = true)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CheckChip("None", check == null) { onChange(null) }
            CheckChip("Passive", check != null && !check.active) {
                onChange((check ?: SkillCheck()).copy(active = false))
            }
            CheckChip("Active", check != null && check.active) {
                onChange((check ?: SkillCheck()).copy(active = true))
            }
        }

        if (check == null) return@Column

        Spacer(Modifier.height(14.dp))
        FieldLabel("Difficulty")
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CheckDifficulty.entries.forEach { d ->
                CheckChip(
                    text = d.name,
                    selected = check.difficulty == d,
                    shape = FeldChipShape.Pill,
                    outline = false,
                ) {
                    onChange(check.copy(difficulty = d))
                }
            }
        }

        Spacer(Modifier.height(14.dp))
        FieldLabel("Result")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CheckResult.entries.forEach { r ->
                CheckChip(r.name, check.result == r) {
                    onChange(check.copy(result = r, die1 = null, die2 = null))
                }
            }
        }

        if (check.active) {
            val (d1, d2) = check.dice
            Spacer(Modifier.height(14.dp))
            FieldLabel("Left die", optional = true)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (n in 1..6) {
                    CheckChip(
                        "$n",
                        d1 == n,
                        height = 32.dp,
                        width = 32.dp,
                        shape = FeldChipShape.Rectangle,
                        sidePad = 0.dp,
                    ) { onChange(check.copy(die1 = n, die2 = d2)) }
                }
            }
            Spacer(Modifier.height(10.dp))
            FieldLabel("Right die", optional = true)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (n in 1..6) {
                    CheckChip(
                        "$n",
                        d2 == n,
                        height = 32.dp,
                        width = 32.dp,
                        shape = FeldChipShape.Rectangle,
                        sidePad = 0.dp,
                    ) { onChange(check.copy(die1 = d1, die2 = n)) }
                }
            }
        }
    }
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
        FieldLabel("Type")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DamageType.entries.forEach { t ->
                CheckChip(t.name, d.type == t, accent = CheckAssets.DamageColor(t)) {
                    onChange(d.copy(type = t))
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        FieldLabel("Amount")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            for (n in 1..5) {
                CheckChip(
                    "$n",
                    d.amount == n,
                    height = 32.dp,
                    width = 32.dp,
                    shape = FeldChipShape.Rectangle,
                    sidePad = 0.dp,
                    accent = CheckAssets.DamageColor(d.type),
                ) { onChange(d.copy(amount = n)) }
            }
        }
    }
}
