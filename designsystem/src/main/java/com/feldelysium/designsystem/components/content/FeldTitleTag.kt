package com.feldelysium.designsystem.components.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.feldelysium.designsystem.foundation.material.DistressSpec
import com.feldelysium.designsystem.foundation.material.FeldTape
import com.feldelysium.designsystem.foundation.material.StickerTapeSpec
import com.feldelysium.designsystem.foundation.material.stickerTapeMaterial
import com.feldelysium.designsystem.foundation.seed.rememberSeed
import com.feldelysium.designsystem.theme.FeldTheme

public val CompactCream: StickerTapeSpec = FeldTape.Cream.copy(
    tearAmp = 4.dp,
    tearScale = 16.dp,
    slantLeft = 9.dp,
    slantRight = 3.dp,
    fray = 1.4.dp,
    rimWidth = 2.dp,
    cornerRx = 30.dp,
    cornerRy = 16.dp,
    layerZone = 38.dp,
    layerAmp = 4.dp,
    layerScale = 11.dp,
)

@Composable
public fun FeldTitleTag(
    text: String,
    modifier: Modifier = Modifier,
    seed: Int = rememberSeed(),
    spec: StickerTapeSpec = CompactCream,
    contentColor: Color = Color(0xFF1B1612),
    textStyle: TextStyle = MaterialTheme.typography.titleMedium.copy(
        letterSpacing = 0.4.sp,
    ),
    rotation: Float = -0.6f,
    distressed: Boolean = false,
    distressedSpec: DistressSpec = FeldDistressedTitles.Small,
) {
    Box(
        modifier = modifier
            .graphicsLayer { rotationZ = rotation }
            .stickerTapeMaterial(spec, seed)
            .padding(
                start = spec.edgeInset,
                end = spec.edgeInset + 3.dp,
                top = 1.dp,
                bottom = 1.dp,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (distressed) {
            FeldDistressedTitle(
                text = text,
                seed = seed,
                spec = distressedSpec.copy(color = contentColor),
                strength = 1.1f,
                textStyle = textStyle,
            )
        } else {
            Text(text.uppercase(), style = textStyle, color = contentColor)
        }
    }
}

@Preview(widthDp = 360, heightDp = 420, showBackground = true, backgroundColor = 0xFF0B1011)
@Composable
private fun FeldTitleTagPreview() {
    FeldTheme {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            FeldTitleTag("Choose speaker", seed = 1)
            FeldTitleTag("Choose speaker", seed = 1, distressed = true)
            FeldTitleTag("Background settings", seed = 2, distressed = true)
            FeldTitleTag("Entry edit", seed = 14, distressed = true)
        }
    }
}
