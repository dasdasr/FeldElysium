package com.feldelysium.designsystem.components.container

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.feldelysium.designsystem.foundation.decal.DecalContext
import com.feldelysium.designsystem.foundation.decal.DecalSpec
import com.feldelysium.designsystem.foundation.decal.LocalFeldDecalContext
import com.feldelysium.designsystem.foundation.decal.rememberDecalLayer
import com.feldelysium.designsystem.foundation.material.FeldPaper
import com.feldelysium.designsystem.foundation.material.PaperSpec
import com.feldelysium.designsystem.foundation.material.paperMaterial
import com.feldelysium.designsystem.foundation.seed.rememberSeed
import com.feldelysium.designsystem.foundation.shape.RoughShape
import com.feldelysium.designsystem.foundation.shape.RoughStyle
import com.feldelysium.designsystem.theme.FeldTheme

@Composable
public fun FeldCard(
    modifier: Modifier = Modifier,
    seed: Int = rememberSeed(),
    spec: PaperSpec = FeldPaper.Panel,
    rough: RoughStyle = RoughStyle(),
    ambient: Float? = null,
    decalSpec: DecalSpec = DecalSpec.Panel,
    decalContext: DecalContext = LocalFeldDecalContext.current,
    decalInset: Dp = 0.dp,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    revealOnEnter: Boolean = false,
    textured: Boolean = true,
    showDecals: Boolean = textured,
    revealMillis: Int = 300,
    pressScale: Float = 0.012f,
    pressMillis: Int = 90,
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

    val enter = remember { Animatable(if (revealOnEnter) 0f else 1f) }
    LaunchedEffect(revealOnEnter) {
        if (revealOnEnter) enter.animateTo(1f, tween(revealMillis, easing = FastOutSlowInEasing))
    }

    val shape = remember(seed, rough) { RoughShape(seed = seed, style = rough) }
    val decals = if (showDecals) rememberDecalLayer(ambient, seed, decalSpec, decalContext, decalInset) else null
    val decalKey = remember(showDecals, ambient, decalSpec, decalContext, decalInset) {
        listOf(showDecals, ambient, decalSpec, decalContext, decalInset)
    }

    Column(
        modifier = modifier
            .graphicsLayer {
                val e = enter.value
                val s = (0.96f + 0.04f * e) * (1f - pressScale * press)
                scaleX = s
                scaleY = s
                alpha = e
            }
            .paperMaterial(
                shape = shape,
                spec = spec,
                press = { press },
                decals = decals,
                decalKey = decalKey,
                textured = textured,
            )
            .then(
                if (onClick != null) {
                    Modifier.clickable(interactionSource = interaction, indication = null, onClick = onClick)
                } else {
                    Modifier
                },
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
    FeldTheme {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SampleCard()

            SampleCard(
                spec = FeldPaper.Panel.copy(wear = 0.2f),
            )

            SampleCard(
                spec = FeldPaper.Panel.copy(
                    wear = 0.6f,
                    chipSize = 6.dp,
                    inkContrast = 3.2f,
                ),
            )
        }
    }
}

@Composable
private fun SampleCard(seed: Int = rememberSeed(), spec: PaperSpec = FeldPaper.Panel) {
    FeldCard(
        modifier = Modifier.fillMaxWidth(),
        seed = seed,
        spec = spec,
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
