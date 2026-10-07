package com.example.feldelysium.designsystem.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.feldelysium.designsystem.material.FeldPaper
import com.example.feldelysium.designsystem.material.PaperSpec
import com.example.feldelysium.designsystem.material.paperMaterial
import com.example.feldelysium.designsystem.shape.RoughShape
import com.example.feldelysium.theme.DialogueText
import com.example.feldelysium.theme.DiscoDialogueTheme

@Composable
fun FeldCard(
    modifier: Modifier = Modifier,
    seed: Int = 0,
    spec: PaperSpec = FeldPaper.Panel,
    roughness: Dp = 2.5.dp,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    revealOnEnter: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = remember(seed, roughness) { RoughShape(seed = seed, roughness = roughness) }

    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val press by animateFloatAsState(if (pressed) 1f else 0f, tween(90), label = "press")

    // 1 = paint is washed out, 0 = settled. Wear goes spec.wear + 0.65 -> spec.wear
    val reveal = remember { Animatable(if (revealOnEnter) 1f else 0f) }
    LaunchedEffect(revealOnEnter) {
        if (revealOnEnter) reveal.animateTo(0f, tween(700, easing = FastOutSlowInEasing))
    }

    Column(
        modifier = modifier
            .graphicsLayer {
                val s = 1f - 0.012f * press
                scaleX = s
                scaleY = s
            }
            .paperMaterial(
                shape = shape,
                spec = spec,
                wear = { spec.wear + reveal.value * 0.65f },
                press = { press },
            )
            .then(
                if (onClick != null) {
                    Modifier.clickable(interaction, indication = null, onClick = onClick)
                } else Modifier
            )
            .padding(contentPadding),
        content = content,
    )
}

@Preview(widthDp = 360, heightDp = 300, showBackground = true, backgroundColor = 0xFF0B1011)
@Composable
private fun FeldCardPreview() {
    DiscoDialogueTheme {
        Column(Modifier.padding(16.dp)) {
            FeldCard(Modifier.fillMaxWidth(), seed = 7, revealOnEnter = true, onClick = {}) {
                Text(
                    "LOGIC",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF4FB3A6),
                )
                Text(
                    "Let's be precise. What do you actually remember about that night?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DialogueText,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
        }
    }
}
