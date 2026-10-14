package com.example.feldelysium.designsystem.components.button

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.feldelysium.designsystem.foundation.material.DryPaperSpec
import com.example.feldelysium.designsystem.foundation.material.dryPaperMaterial
import com.example.feldelysium.designsystem.foundation.seed.rememberSeed
import com.example.feldelysium.theme.DiscoDialogueTheme

object FeldButtonDefaults {
    val ContentPadding = PaddingValues(horizontal = 40.dp, vertical = 18.dp)
    val MinHeight = 56.dp
    val InkDark = Color(0xFF1B1612)

    val Cream = DryPaperSpec(base = Color(0xFFDCCFB5), crack = Color(0xFF5E4F38))
    val Orange = DryPaperSpec(base = Color(0xFFDD6224), crack = Color(0xFF6B2A0C))

    val OrangeFresh = DryPaperSpec(
        base = Color(0xFFDB6A2C), crack = Color(0xFF7A3414),
        tearDepth = 1.1.dp, tearScale = 16.dp, tearCoverage = 0.22f, bow = 0.6.dp,
        crackScale = 22.dp, crackDepth = 7.dp, crackWidth = 0.8.dp,
        crackStrength = 0.55f, crackCoverage = 0.10f,
        grainScale = 0.8.dp, grainStrength = 0.075f, fiberStrength = 0.02f,
        mottleScale = 20.dp, mottleStrength = 0.20f, pressShade = 0.10f,
    )
}

@Composable
fun FeldButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    seed: Int = rememberSeed(),
    spec: DryPaperSpec = FeldButtonDefaults.Cream,
    contentColor: Color = FeldButtonDefaults.InkDark,
    textStyle: TextStyle = MaterialTheme.typography.labelLarge.copy(
        letterSpacing = 1.6.sp,
    ),
    icon: (@Composable () -> Unit)? = null,
    iconSpacing: Dp = 10.dp,
    minWidth: Dp = Dp.Unspecified,
    minHeight: Dp = FeldButtonDefaults.MinHeight,
    contentPadding: PaddingValues = FeldButtonDefaults.ContentPadding,
    contentAlignment: Alignment = Alignment.Center,
    enabled: Boolean = true,
    pressScale: Float = 0.015f,
    pressShift: Dp = 1.5.dp,
    pressMillis: Int = 0,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val press by animateFloatAsState(
        targetValue = if (pressed) 1f else 0f,
        animationSpec = tween(pressMillis),
        label = "press",
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                val s = 1f - pressScale * press
                scaleX = s
                scaleY = s
                translationY = pressShift.toPx() * press
                alpha = if (enabled) 1f else 0.5f
            }
            .dryPaperMaterial(spec = spec, seed = seed, press = { press })
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            )
            .defaultMinSize(minWidth = minWidth, minHeight = minHeight)
            .padding(contentPadding),
        contentAlignment = contentAlignment,
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(iconSpacing),
            ) {
                icon?.invoke()
                Text(text.uppercase(), style = textStyle, color = contentColor)
            }
        }
    }
}

@Preview(widthDp = 400, heightDp = 260, showBackground = true, backgroundColor = 0xFF0B1011)
@Composable
private fun FeldButtonPreview() {
    DiscoDialogueTheme {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                FeldButton("Cancel", {}, Modifier.weight(1f), seed = 21)
                FeldButton(
                    "Save",
                    {},
                    Modifier.weight(1f),
                    seed = 8,
                    spec = FeldButtonDefaults.Orange,
                    icon = { Icon(Icons.Default.Check, null, Modifier.size(20.dp)) },
                )
            }
            FeldButton(
                "Delete line",
                {},
                Modifier.fillMaxWidth(),
                seed = 3,
                spec = FeldButtonDefaults.Orange,
                minHeight = 72.dp,
                contentAlignment = Alignment.CenterStart,
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF111819)
@Composable
private fun FeldDialogActionsPreview() {
    DiscoDialogueTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            FeldDialogActions(
                confirmLabel = "Save entry",
                onConfirm = {},
                onCancel = {},
                seed = 1,
            )

            FeldDialogActions(
                confirmLabel = "Create speaker",
                onConfirm = {},
                cancelLabel = "Back",
                onCancel = {},
                seed = 2,
            )

            FeldDialogActions(
                confirmLabel = "Import",
                onConfirm = {},
                onCancel = {},
                confirmEnabled = false,
                seed = 3,
            )

            FeldDialogActions(
                confirmLabel = "Delete",
                onConfirm = {},
                cancelLabel = "Keep",
                onCancel = {},
                seed = 4,
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF111819)
@Composable
private fun FeldTextButtonPreview() {
    DiscoDialogueTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FeldTextButton(text = "Cancel", onClick = {})
            FeldTextButton(text = "Discard changes", onClick = {})
            FeldTextButton(text = "Back to editing", onClick = {})
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF111819)
@Composable
private fun FeldLabeledBlockPreview() {
    DiscoDialogueTheme {
        Column(Modifier.padding(horizontal = 16.dp)) {
            FeldLabeledBlock(
                label = "IDENTIFICATION",
                seed = 7,
            ) {
                Text(
                    text = "Give this voice a name.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                FeldTextButton(
                    text = "Choose voice",
                    onClick = {},
                )
            }

            FeldLabeledBlock(
                label = "PERSONALITY",
                seed = 12,
            ) {
                Text(
                    text = "A little theatrical. Difficult to ignore.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}
