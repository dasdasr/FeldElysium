package com.example.feldelysium.designsystem.components.input

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.feldelysium.designsystem.foundation.decal.FeldDecalType
import com.example.feldelysium.designsystem.foundation.material.accentBrush
import com.example.feldelysium.designsystem.foundation.material.wornBrush
import com.example.feldelysium.designsystem.foundation.seed.rememberSeed
import com.example.feldelysium.designsystem.foundation.utils.FeldPalette
import com.example.feldelysium.theme.DiscoDialogueTheme

enum class FeldChipShape { Chamfer, Pill, Rectangle }

object FeldChipColors {
    val Teal = Color(0xFF4FB3A6)
    val Orange = FeldPalette.Orange
    val Cream = FeldPalette.Cream
}

private val BaseDark = Color(0xFF111A1A)
private val BaseDisabled = Color(0xFF131817)
private val InkDark = Color(0xFF0B1011)
private val DisabledGray = Color(0xFF6E706D)

private fun roundedPolygon(pts: List<Offset>, r: Float): Path = Path().apply {
    val n = pts.size
    for (i in 0 until n) {
        val prev = pts[(i - 1 + n) % n]
        val cur = pts[i]
        val next = pts[(i + 1) % n]
        val d1 = prev - cur
        val d2 = next - cur
        val l1 = d1.getDistance()
        val l2 = d2.getDistance()
        val rr = minOf(r, l1 / 2f, l2 / 2f)
        val a = cur + d1 / l1 * rr
        val b = cur + d2 / l2 * rr
        if (i == 0) moveTo(a.x, a.y) else lineTo(a.x, a.y)
        quadraticBezierTo(cur.x, cur.y, b.x, b.y)
    }
    close()
}

private fun chipPath(
    w: Float, h: Float, shape: FeldChipShape, tip: Float, round: Float, inset: Float,
): Path {
    val l = inset
    val t = inset
    val r = w - inset
    val b = h - inset
    return when (shape) {
        FeldChipShape.Chamfer -> {
            val k = tip.coerceAtMost((r - l) / 3f)
            roundedPolygon(
                listOf(
                    Offset(l + k, t), Offset(r - k, t), Offset(r, h / 2f),
                    Offset(r - k, b), Offset(l + k, b), Offset(l, h / 2f),
                ),
                round,
            )
        }
        FeldChipShape.Pill -> Path().apply {
            addRoundRect(RoundRect(Rect(l, t, r, b), CornerRadius((b - t) / 2f)))
        }
        FeldChipShape.Rectangle -> Path().apply {
            addRoundRect(RoundRect(Rect(l, t, r, b), CornerRadius(round)))
        }
    }
}

@Composable
fun FeldChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    accent: Color = FeldChipColors.Cream,
    shape: FeldChipShape = FeldChipShape.Chamfer,
    outline: Boolean = true,
    leading: Boolean = false,
    leadingContent: (@Composable () -> Unit)? = null,
    height: Dp = 40.dp,
    tip: Dp = 12.dp,
    rounding: Dp = 4.dp,
    seed: Int = rememberSeed(),
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()

    val fillState = animateFloatAsState(
        targetValue = if (enabled && (selected || pressed)) 1f else 0f,
        animationSpec = tween(120),
        label = "chipFill",
    )
    val scale by animateFloatAsState(if (pressed && enabled) 0.97f else 1f, tween(90), label = "chipScale")
    val fill = fillState.value

    val textColor = when {
        !enabled -> DisabledGray
        else -> lerp(FeldPalette.Cream, InkDark, fill)
    }
    val diamondColor = if (enabled) accent else DisabledGray
    val sidePad = (if (shape == FeldChipShape.Chamfer) tip else 0.dp) +
            (if (shape == FeldChipShape.Pill) height / 3 else 12.dp)

    Row(
        modifier = modifier
            .height(height)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .semantics { this.selected = selected }
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(1.dp)
            .drawWithCache {
                val w = size.width
                val h = size.height
                val body = chipPath(w, h, shape, tip.toPx(), rounding.toPx(), 0f)
                val rim = chipPath(w, h, shape, tip.toPx(), rounding.toPx(), 0.8.dp.toPx())

                val base = accentBrush(
                    if (enabled) BaseDark else BaseDisabled, seed, density,
                    fade = 0.5f, glow = 0.35f, grain = 0.05f,
                )
                val filled = accentBrush(accent, seed + 1, density, fade = 0.9f, glow = 0.9f, grain = 0.09f)
                val rimColor = if (enabled) lerp(FeldPalette.Cream, accent, 0f) else FeldPalette.Line
                val rimBrush = wornBrush(rimColor, seed + 2, density, wear = 0.45f, scale = 14f)
                val stroke = Stroke(1.dp.toPx(), join = StrokeJoin.Round)

                onDrawWithContent {
                    clipPath(body) {
                        drawRect(base)
                        val t = fillState.value
                        if (t > 0.001f) drawRect(filled, alpha = t)
                    }
                    drawContent()
                    if (outline) {
                        drawPath(rim, rimBrush, alpha = (if (enabled) 0.85f else 0.45f) * (1f - 0.55f * fillState.value), style = stroke)
                    }
                }
            }
            .padding(horizontal = sidePad),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (leadingContent != null) {
            leadingContent()
        } else if (leading) {
            Canvas(Modifier.size(height * 0.34f)) {
                val s = size.minDimension
                val d = Path().apply {
                    moveTo(s / 2f, 0f); lineTo(s, s / 2f); lineTo(s / 2f, s); lineTo(0f, s / 2f); close()
                }
                drawPath(d, diamondColor, alpha = 1f - fillState.value)
                drawPath(d, InkDark, alpha = fillState.value)
            }
        }
        Text(
            text = text.uppercase(),
            style = FeldDecalType.Label.copy(fontSize = (height.value * 0.33f).sp, letterSpacing = 1.4.sp),
            color = textColor,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0B1011, widthDp = 360)
@Composable
private fun FeldChipPreview() {
    DiscoDialogueTheme {
        androidx.compose.foundation.layout.Column(
            Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            FeldChip("Default", selected = false, onClick = {})
            FeldChip("Pressed", selected = true, onClick = {})
            FeldChip("Disabled", selected = false, onClick = {}, enabled = false)
            FeldChip("Pill", selected = true, onClick = {}, shape = FeldChipShape.Pill)
            FeldChip("Rect", selected = false, onClick = {}, shape = FeldChipShape.Rectangle, outline = false)
        }
    }
}
