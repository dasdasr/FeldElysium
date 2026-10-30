package com.feldelysium.designsystem.components.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.feldelysium.designsystem.foundation.material.DistressSpec
import com.feldelysium.designsystem.foundation.material.FeldTape
import com.feldelysium.designsystem.foundation.material.distressMaterial
import com.feldelysium.designsystem.foundation.material.stickerTapeMaterial
import com.feldelysium.designsystem.foundation.seed.rememberSeed
import com.feldelysium.designsystem.theme.FeldTheme

private val DistressPad = 3.dp

@Composable
public fun FeldDistressedTitle(
    text: String,
    modifier: Modifier = Modifier,
    seed: Int = rememberSeed(),
    spec: DistressSpec = FeldDistressedTitles.Small,
    textStyle: TextStyle = MaterialTheme.typography.titleLarge,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    roughness: Float = 1f,
    roughnessSize: Float = 1f,
    strength: Float = 1f,
) {
    val tuned = remember(spec, roughness, roughnessSize, strength) {
        spec.scaled(roughness, roughnessSize, spec.strength * strength)
    }
    val bakeKey = remember(text, textStyle, maxLines, overflow, textAlign) {
        listOf(text, textStyle, maxLines, overflow, textAlign)
    }
    Text(
        text = text.uppercase(),
        style = textStyle.copy(color = spec.color),
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = overflow,
        modifier = modifier
            .distressMaterial(tuned, seed, bakeKey = bakeKey)
            .padding(DistressPad),
    )
}

public object FeldDistressedTitles {

    public val Small: DistressSpec = DistressSpec(
        color = Color(0xFF1B1612),
        edgeSoftness = 0.5.dp, edgeRough = 0.3.dp, edgeSize = 1.6.dp,
        edgeErode = 0.5f, bleed = 0.12f,
        patchy = 0.18f, patchSize = 30.dp,
        dropout = 0.75f, dropoutSize = 6.dp,
        scratch = 0.7f, scratchLength = 40.dp, scratchWidth = 0.45.dp, scratchAngle = -14f,
        speck = 0.6f, speckSize = 2.4.dp,
        grain = 0.35f, grainSize = 0.6.dp,
        splatter = 0.25f, splatterRadius = 1.6.dp,
    )

    public val Large: DistressSpec = DistressSpec(
        color = Color(0xFFE6DCC4),
        edgeSoftness = 0.9.dp, edgeRough = 0.5.dp, edgeSize = 2.6.dp,
        edgeErode = 0.6f, bleed = 0.25f,
        patchy = 0.25f, patchSize = 12.dp,
        dropout = 0.9f, dropoutSize = 12.dp,
        scratch = 0.9f, scratchLength = 110.dp, scratchWidth = 0.6.dp, scratchAngle = -9f,
        speck = 0.7f, speckSize = 4.dp,
        grain = 0.4f, grainSize = 0.9.dp,
        splatter = 0.3f, splatterRadius = 3.dp,
    )
}

@Preview(widthDp = 500, heightDp = 500, showBackground = true, backgroundColor = 0xFF0B1011)
@Composable
private fun FeldDistressedTitlePreview() {
    FeldTheme {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            val tape = FeldTape.Cream
            val largeStyle = MaterialTheme.typography.titleLarge.copy(
                fontSize = 52.sp,
                lineHeight = 54.sp,
                letterSpacing = 1.sp,
            )
            Box(
                Modifier
                    .stickerTapeMaterial(tape, seed = 3)
                    .padding(horizontal = tape.edgeInset + 16.dp, vertical = 12.dp),
            ) { FeldDistressedTitle("Choose speaker", seed = 5) }

            Box(
                Modifier
                    .stickerTapeMaterial(tape, seed = 9)
                    .padding(horizontal = tape.edgeInset + 16.dp, vertical = 12.dp),
            ) { FeldDistressedTitle("Entry edit", seed = 12) }

            FeldDistressedTitle(
                "The final interview",
                seed = 2,
                spec = FeldDistressedTitles.Large,
                textStyle = largeStyle,
            )
            FeldDistressedTitle(
                "App settings",
                seed = 7,
                spec = FeldDistressedTitles.Large,
                textStyle = largeStyle,
            )
        }
    }
}
