package com.example.feldelysium.designsystem.foundation.decal

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.sp
import kotlin.random.Random

internal fun buildDecal(
    kind: DecalKind,
    rng: Random,
    ctx: DecalContext,
    spec: DecalSpec,
    density: Density,
    ld: LayoutDirection,
    measurer: TextMeasurer,
    cardSize: Size,
): Decal? {
    val d = density.density
    fun r(lo: Float, hi: Float) = lo + rng.nextFloat() * (hi - lo)

    fun layout(s: String, base: TextStyle, sizeSp: Float): TextLayoutResult = measurer.measure(
        s,
        base.copy(fontSize = sizeSp.sp, lineHeight = (sizeSp * 1.35f).sp),
        softWrap = false,
        layoutDirection = ld,
        density = density,
    )

    return when (kind) {
        DecalKind.Ruler -> RulerDecal(
            r(60f, 120f) * d,
            8f * d,
            r(3f, 4.5f) * d,
            if (rng.nextBoolean()) 5 else 4,
            0.7f * d,
        )

        DecalKind.Perforation, DecalKind.FilmStrip -> {
            val vertical = cardSize.height / d >= 140f && rng.nextBoolean()
            val edge = if (vertical) cardSize.height else cardSize.width
            val hole = 2.4f * d
            val text = if (kind == DecalKind.FilmStrip) {
                layout(DecalText.strip(rng, ctx), FeldDecalType.Base, r(5.5f, 6.5f))
            } else {
                null
            }
            val thick = if (text != null) hole * 2 + text.size.height + 3f * d else hole
            var len = edge * r(0.45f, 0.85f)
            if (text != null) len = maxOf(len, text.size.width + 14f * d)
            if (len > edge - 8f * d) return null
            StripDecal(len, thick, hole, hole * 2.6f, text, vertical)
        }

        DecalKind.Text -> {
            val label = rng.nextFloat() < 0.4f
            TextDecal(
                if (label) {
                    layout(DecalText.text(rng, ctx), FeldDecalType.Label, r(7f, 8.5f))
                } else {
                    layout(DecalText.text(rng, ctx), FeldDecalType.Base, r(6f, 8f))
                },
            )
        }

        DecalKind.DataBlock -> TextDecal(
            layout(
                DecalText.dataBlock(rng, ctx),
                FeldDecalType.Base,
                r(5.8f, 6.8f),
            ),
        )

        DecalKind.Counter -> TextDecal(
            layout(
                DecalText.counter(rng, ctx),
                FeldDecalType.Label,
                r(8f, 11f),
            ),
        )

        DecalKind.Glyph -> GlyphDecal(GlyphShape.values().random(rng), r(7f, 11f) * d, 0.7f * d)

        DecalKind.Mark -> {
            val shape = MarkShape.values().random(rng)
            val big = shape == MarkShape.Circle
            MarkDecal(
                shape,
                (if (big) r(22f, 32f) else r(16f, 34f)) * d,
                (if (big) r(14f, 20f) else r(10f, 20f)) * d,
                r(0.8f, 1.2f) * d,
                FloatArray(8) { (rng.nextFloat() - 0.5f) * 0.18f },
            )
        }

        DecalKind.Sprite -> spec.atlas?.takeIf { it.sprites.isNotEmpty() }?.let { atlas ->
            val src = atlas.sprites.random(rng)
            val k = r(0.5f, 1f) * d / 3f
            SpriteDecal(atlas.bitmap, src, Size(src.width * k, src.height * k))
        }
    }
}
