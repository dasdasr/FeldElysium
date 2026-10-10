package com.example.feldelysium.designsystem.components.card

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.feldelysium.designsystem.foundation.animation.FeldAnimationValues
import com.example.feldelysium.designsystem.foundation.animation.FeldAnimations
import com.example.feldelysium.designsystem.foundation.animation.feldAnimate
import com.example.feldelysium.designsystem.foundation.material.FeldPaper
import com.example.feldelysium.designsystem.foundation.material.PaperSpec
import com.example.feldelysium.designsystem.foundation.material.paperMaterial
import com.example.feldelysium.designsystem.foundation.seed.rememberSeed
import com.example.feldelysium.designsystem.foundation.shape.RoughShape
import com.example.feldelysium.designsystem.foundation.shape.RoughStyle
import com.example.feldelysium.theme.DiscoDialogueTheme

@Composable
fun FeldCard(
    modifier: Modifier = Modifier,
    seed: Int = rememberSeed(),
    spec: PaperSpec = FeldPaper.Panel,
    rough: RoughStyle = RoughStyle(),
    contentPadding: PaddingValues = PaddingValues(16.dp),
    revealOnEnter: Boolean = false,
    revealMillis: Int = 500,
    pressScale: Float = 0.012f,
    pressMillis: Int = 90,
    time: () -> Float = { 0f },
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }

    val pressed by interaction.collectIsPressedAsState()

    val press by animateFloatAsState(
        targetValue = if (pressed) 1f else 0f,
        animationSpec = tween(pressMillis),
        label = "press",
    )

    val reveal = feldAnimate(
        values = FeldAnimationValues(
            "roughness" to rough.roughness.value,
            "irregularity" to rough.irregularity,
            "wear" to spec.wear,
        ),
        animation = FeldAnimations.Reveal.from(
            initial = mapOf(
                "roughness" to { it + 10f },
                "irregularity" to { it * 1.5f },
                "wear" to { it * 1.5f },
            ),
            enabled = revealOnEnter,
            durationMillis = revealMillis,
        ),
    )

    val animatedRough = rough.copy(
        roughness = reveal["roughness"].dp,
        irregularity = reveal["irregularity"],
    )

    val shape = remember(seed, animatedRough) {
        RoughShape(
            seed = seed,
            style = animatedRough,
        )
    }

    Column(
        modifier = modifier
            .graphicsLayer {
                val scale = 1f - pressScale * press
                scaleX = scale
                scaleY = scale
            }
            .paperMaterial(
                shape = shape,
                spec = spec,
                wear = { reveal["wear"] },
                press = { press },
                time = time,
            )
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interaction,
                        indication = null,
                        onClick = onClick,
                    )
                } else {
                    Modifier
                }
            )
            .padding(contentPadding),
        content = content,
    )
}

@Preview(
    widthDp = 360,
    heightDp = 520,
    showBackground = true,
    backgroundColor = 0xFF0B1011,
)
@Composable
private fun FeldCardPreview() {
    DiscoDialogueTheme {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SampleCard()

            SampleCard(
                spec = FeldPaper.Panel.copy(wear = 0.2f),
                rough = RoughStyle(
                    roughness = 0.7.dp,
                    irregularity = 0.4f,
                ),
            )

            SampleCard(
                spec = FeldPaper.Panel.copy(
                    wear = 0.6f,
                    chipSize = 6.dp,
                    inkContrast = 3.2f,
                ),
                rough = RoughStyle(
                    roughness = 2.dp,
                    irregularity = 0.9f,
                    step = 40.dp,
                ),
            )
        }
    }
}

@Composable
private fun SampleCard(
    seed: Int= rememberSeed(),
    spec: PaperSpec = FeldPaper.Panel,
    rough: RoughStyle = RoughStyle(),
) {
    FeldCard(
        modifier = Modifier.fillMaxWidth(),
        seed = seed,
        spec = spec,
        rough = rough,
        revealOnEnter = true,
        onClick = {},
    ) {
        Text(
            text = "LOGIC",
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF4FB3A6),
        )

        Text(
            text = "Let's be precise. What do you actually remember about that night?",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 6.dp),
        )
    }
}
