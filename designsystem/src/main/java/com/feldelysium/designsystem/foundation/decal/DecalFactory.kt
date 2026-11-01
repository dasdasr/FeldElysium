package com.feldelysium.designsystem.foundation.decal

import android.util.LruCache
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.sp
import kotlin.random.Random

private data class TextKey(
    val s: String,
    val style: TextStyle,
    val sizeSp: Float,
    val density: Float,
    val ld: LayoutDirection,
)

private val textCache = LruCache<TextKey, TextLayoutResult>(64)

internal fun buildDecal(
    kind: DecalKind,
    rng: Random,
    ctx: DecalContext,
    density: Density,
    ld: LayoutDirection,
    measurer: TextMeasurer,
    cardSize: Size,
): Decal? {
    val d = density.density
    fun r(lo: Float, hi: Float) = lo + rng.nextFloat() * (hi - lo)

    fun layout(s: String, base: TextStyle, sizeSp: Float): TextLayoutResult {
        val key = TextKey(s, base, sizeSp, density.density, ld)
        return textCache.get(key) ?: measurer.measure(
            s,
            base.copy(fontSize = sizeSp.sp, lineHeight = (sizeSp * 1.35f).sp),
            softWrap = false,
            layoutDirection = ld,
            density = density,
        ).also { textCache.put(key, it) }
    }

    return when (kind) {
        DecalKind.Ruler -> RulerDecal(
            r(60f, 120f) * d,
            8f * d,
            r(3f, 4.5f) * d,
            if (rng.nextBoolean()) 5 else 4,
            0.7f * d,
        )

        DecalKind.Perforation -> {
            val vertical = cardSize.height / d >= 140f && rng.nextBoolean()
            val edge = if (vertical) cardSize.height else cardSize.width
            val hole = 2.4f * d
            val len = edge * r(0.45f, 0.85f)
            if (len > edge - 8f * d) return null
            StripDecal(len, hole, hole, hole * 2.6f, null, vertical)
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

        DecalKind.Glyph -> GlyphDecal(GlyphShape.entries.random(rng), r(7f, 11f) * d, 0.7f * d)

        DecalKind.Mark -> {
            val shape = MarkShape.entries.random(rng)
            val big = shape == MarkShape.Circle
            MarkDecal(
                shape,
                (if (big) r(22f, 32f) else r(16f, 34f)) * d,
                (if (big) r(14f, 20f) else r(10f, 20f)) * d,
                r(0.8f, 1.2f) * d,
                FloatArray(8) { (rng.nextFloat() - 0.5f) * 0.18f },
            )
        }

        DecalKind.Tape -> TapeDecal(r(26f, 44f) * d, r(9f, 13f) * d)

        DecalKind.Ring -> RingDecal(r(9f, 16f) * d, 0.8f * d)

        DecalKind.Stitch -> StitchDecal(r(40f, 90f) * d, r(2.5f, 4f) * d, 0.8f * d)

        DecalKind.Staple -> StapleDecal(r(8f, 11f) * d, r(5f, 7f) * d, 1f * d)

        DecalKind.Corner -> CornerFoldDecal(r(10f, 16f) * d)

        DecalKind.Tally -> TallyDecal(rng.nextInt(3, 9), r(7f, 10f) * d, r(2f, 2.6f) * d, 0.7f * d)
    }
}
