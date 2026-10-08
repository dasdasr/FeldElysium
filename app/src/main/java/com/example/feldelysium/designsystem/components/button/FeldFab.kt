package com.example.feldelysium.designsystem.components.button

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.feldelysium.designsystem.components.FeldIcon
import com.example.feldelysium.designsystem.components.FeldIconId
import com.example.feldelysium.designsystem.material.FeldPaper
import com.example.feldelysium.designsystem.material.PaperSpec
import com.example.feldelysium.designsystem.material.paperMaterial
import com.example.feldelysium.designsystem.shape.RoughShape
import com.example.feldelysium.designsystem.shape.RoughStyle
import com.example.feldelysium.theme.DiscoDialogueTheme

@Composable
fun FeldFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    seed: Int = 0,
    size: Dp = 56.dp,
    cornerFraction: Float = 0.45f,
    contentDescription: String? = null,
    spec: PaperSpec = FeldPaper.DryButtonRust,
    content: @Composable () -> Unit = {
        FeldIcon(
            id = FeldIconId.Add,
            tint = MaterialTheme.colorScheme.surface,
            size = 36.dp
        )
    }
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val press by animateFloatAsState(if (pressed) 1f else 0f, tween(60), label = "press")

    val shape = remember(seed, size, cornerFraction) {
        RoughShape(
            seed = seed,
            style = RoughStyle(
                roughness = 1.dp,
                corner = size * cornerFraction,
                cornerVariation = 0.1f,
                cornerJitter = 0.3f,
                step = 28.dp,
                tornChance = 0f,
                jerkChance = 0.1f,
            ),
        )
    }

    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                val s = 1f - 0.04f * press
                scaleX = s
                scaleY = s
            }
            .paperMaterial(shape = shape, spec = spec, press = { press })
            .semantics { contentDescription?.let { this.contentDescription = it } }
            .clickable(
                interactionSource = interaction,
                indication = null,
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@Preview(widthDp = 220, heightDp = 120, showBackground = true, backgroundColor = 0xFF0B1011)
@Composable
private fun FeldFabPreview() {
    DiscoDialogueTheme {
        Box(Modifier.padding(24.dp)) {
            FeldFab(onClick = {}, seed = 3, contentDescription = "Add entry")
        }
    }
}
