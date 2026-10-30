package com.feldelysium.app.presentation.editor.dialogs

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toColorLong
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.feldelysium.designsystem.theme.FeldPalette

private const val MAX_BARS = 40

private class StripBar(val color: Color, val h: Float)

private fun buildStrip(colors: List<Long>, lengths: List<Int>): List<StripBar> {
    if (colors.isEmpty()) return emptyList()
    val useLen = lengths.size == colors.size && lengths.any { it > 0 }
    val maxLen = if (useLen) lengths.max().coerceAtLeast(1) else 1
    val idx = if (colors.size <= MAX_BARS) colors.indices.toList() else List(MAX_BARS) { it * colors.size / MAX_BARS }
    return idx.map { i ->
        val h = if (useLen) 0.4f + 0.6f * (lengths[i].toFloat() / maxLen).coerceIn(0f, 1f) else 0.85f
        StripBar(Color(colors[i]), h)
    }
}

@Composable
fun DialogueStrip(
    colors: List<Long>,
    seed: Int,
    modifier: Modifier = Modifier,
    lengths: List<Int> = emptyList(),
    lineColor: Color = FeldPalette.Muted,
) {
    val bars = remember(colors, lengths) { buildStrip(colors, lengths) }
    val progress = remember(seed) { Animatable(0f) }
    LaunchedEffect(seed) {
        progress.animateTo(1f, tween((bars.size * 25).coerceIn(250, 700), easing = LinearEasing))
    }

    Canvas(modifier) {
        val lineW = 1.dp.toPx()
        val base = size.height - 4.dp.toPx()

        drawLine(
            lineColor.copy(alpha = 0.55f),
            Offset(0f, base + 1.dp.toPx()),
            Offset(size.width, base + 1.dp.toPx()),
            lineW,
        )
        val tickStep = 8.dp.toPx()
        var x = 0f
        var i = 0
        while (x <= size.width) {
            val tall = if (i % 5 == 0) 4.dp.toPx() else 2.dp.toPx()
            drawLine(
                lineColor.copy(alpha = 0.4f),
                Offset(x, base + 1.dp.toPx()),
                Offset(x, base + 1.dp.toPx() + tall),
                lineW,
            )
            x += tickStep
            i++
        }

        val n = bars.size
        if (n == 0) return@Canvas
        val gap = 2.dp.toPx()
        val bw = ((size.width - gap * (n - 1)) / n).coerceAtMost(10.dp.toPx())
        bars.forEachIndexed { k, b ->
            val t = (progress.value * (n + 2) - k).coerceIn(0f, 1f)
            if (t <= 0f) return@forEachIndexed
            val h = base * b.h * t
            drawRect(b.color, Offset(k * (bw + gap), base - h), Size(bw, h))
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF05090A)
@Composable
private fun DialogueStripPreview() {
    DialogueStrip(
        colors = listOf(
            FeldPalette.Orange.toColorLong(),
            FeldPalette.Muted.toColorLong(),
            FeldPalette.Orange.toColorLong(),
            FeldPalette.Muted.toColorLong(),
            FeldPalette.Orange.toColorLong(),
        ),
        seed = 1,
        modifier = Modifier
            .fillMaxWidth()
            .height(14.dp),
    )
}
