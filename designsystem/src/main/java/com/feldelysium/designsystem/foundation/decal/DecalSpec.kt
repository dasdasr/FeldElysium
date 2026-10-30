package com.feldelysium.designsystem.foundation.decal

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.feldelysium.designsystem.theme.FeldPalette

public enum class DecalKind {
    Ruler,
    Perforation,
    Text,
    DataBlock,
    Counter,
    Glyph,
    Mark,
    Tape,
    Ring,
    Stitch,
    Staple,
    Corner,
    Tally,
}

private val DefaultAlphaCap = mapOf(
    DecalKind.Ruler to 0.07f, DecalKind.Perforation to 0.10f,
    DecalKind.Text to 0.14f, DecalKind.DataBlock to 0.12f, DecalKind.Counter to 0.17f,
    DecalKind.Glyph to 0.10f, DecalKind.Mark to 0.15f,
    DecalKind.Tape to 0.09f, DecalKind.Ring to 0.08f, DecalKind.Stitch to 0.12f,
    DecalKind.Staple to 0.14f, DecalKind.Corner to 0.10f, DecalKind.Tally to 0.14f,
)

private val DarkTones = listOf(
    FeldPalette.Muted,
    FeldPalette.Muted,
    FeldPalette.Muted,
    FeldPalette.Muted,
    FeldPalette.Muted,
    FeldPalette.Cream,
    FeldPalette.Orange,
    FeldPalette.Line,
)

@Immutable
public data class DecalSpec(
    val weights: Map<DecalKind, Float>,
    val maxDecals: Int = 4,
    val alphaCap: Map<DecalKind, Float> = DefaultAlphaCap,
    val tones: List<Color> = DarkTones,
    val gap: Dp = 8.dp,
    val edgeMarginDp: ClosedFloatingPointRange<Float> = 2f..7f,
    val allowCenter: Boolean = true,
    val leakChance: Float = 0.05f,
    val leakColor: Color = FeldPalette.Orange,
) {
    public companion object {
        public val Entry: DecalSpec = DecalSpec(
            weights = mapOf(
                DecalKind.Ruler to 0.6f,
                DecalKind.Text to 3f,
                DecalKind.Glyph to 1.5f,
                DecalKind.Mark to 1f,
                DecalKind.Perforation to 0.5f,
                DecalKind.Stitch to 0.8f,
                DecalKind.Tally to 0.8f,
                DecalKind.Corner to 0.5f,
                DecalKind.Staple to 0.5f,
            ),
            maxDecals = 4,
            allowCenter = false,
            leakChance = 0.05f,
        )
        public val Dialog: DecalSpec = DecalSpec(
            weights = mapOf(
                DecalKind.Ruler to 0.8f,
                DecalKind.Perforation to 0.8f,
                DecalKind.Text to 3f,
                DecalKind.DataBlock to 2f,
                DecalKind.Counter to 1.5f,
                DecalKind.Glyph to 2f,
                DecalKind.Mark to 1.5f,
                DecalKind.Tape to 1f,
                DecalKind.Ring to 0.8f,
                DecalKind.Stitch to 1f,
                DecalKind.Staple to 0.8f,
                DecalKind.Corner to 0.8f,
                DecalKind.Tally to 1f,
            ),
            maxDecals = 4,
        )
        public val Panel: DecalSpec = Dialog.copy(maxDecals = 6)
    }
}
