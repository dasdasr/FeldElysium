package com.example.feldelysium.designsystem.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.feldelysium.designsystem.material.DryPaperSpec
import com.example.feldelysium.designsystem.material.FeldPaper
import com.example.feldelysium.designsystem.material.dryPaperMaterial
import com.example.feldelysium.designsystem.material.paperMaterial
import com.example.feldelysium.designsystem.shape.RoughShape
import com.example.feldelysium.designsystem.shape.RoughStyle
import com.example.feldelysium.theme.DiscoDialogueTheme
import androidx.compose.foundation.interaction.MutableInteractionSource

private val SwitchRough = RoughStyle(
    roughness = 0.8.dp,
    step = 24.dp,
    corner = 2.dp,
    cornerVariation = 0.3f,
    tornChance = 0.1f,
    jerkChance = 0.1f,
)
private val ThumbOn = DryPaperSpec(base = Color(0xFFDCCFB5), crack = Color(0xFF5E4F38), crackDepth = 6.dp)
private val ThumbOff = DryPaperSpec(base = Color(0xFF8C8E8A), crack = Color(0xFF2A2C2B), crackDepth = 6.dp)

@Composable
fun FeldSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    seed: Int = 0,
    enabled: Boolean = true,
) {
    val trackW = 54.dp
    val trackH = 28.dp
    val thumb = 20.dp
    val pad = 4.dp

    val thumbX by animateDpAsState(
        targetValue = if (checked) trackW - thumb - pad else pad,
        animationSpec = tween(120),
        label = "thumbX",
    )
    val shape = remember(seed) { RoughShape(seed, SwitchRough) }
    val spec = if (checked) FeldPaper.DryButtonRust else FeldPaper.Field

    Box(
        modifier = modifier
            .size(trackW, trackH)
            .graphicsLayer { alpha = if (enabled) 1f else 0.5f }
            .paperMaterial(shape = shape, spec = spec)
            .then(
                if (onCheckedChange != null) Modifier.toggleable(
                    value = checked,
                    enabled = enabled,
                    role = Role.Switch,
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onValueChange = onCheckedChange,
                ) else Modifier
            ),
    ) {
        Box(
            Modifier
                .align(Alignment.CenterStart)
                .offset { IntOffset(thumbX.roundToPx(), 0) }
                .size(thumb)
                .dryPaperMaterial(if (checked) ThumbOn else ThumbOff, seed + 3)
        )
    }
}

@Preview(widthDp = 240, heightDp = 120, showBackground = true, backgroundColor = 0xFF0B1011)
@Composable
private fun FeldSwitchPreview() {
    DiscoDialogueTheme {
        var a by remember { mutableStateOf(true) }
        var b by remember { mutableStateOf(false) }
        Row(Modifier.padding(24.dp), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            FeldSwitch(a, { a = it }, seed = 1)
            FeldSwitch(b, { b = it }, seed = 2)
            FeldSwitch(true, null, seed = 3, enabled = false)
        }
    }
}
