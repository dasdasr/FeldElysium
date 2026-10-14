package com.example.feldelysium.designsystem.components.input

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
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

private val ChipMinHeight = 32.dp

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

private fun chipPath(w: Float, h: Float, shape: FeldChipShape, tip: Float, round: Float, inset: Float): Path {
    val l = inset
    val t = inset
    val r = w - inset
    val b = h - inset
    return when (shape) {
        FeldChipShape.Chamfer -> {
            val k = tip.coerceAtMost((r - l) / 3f)
            roundedPolygon(
                listOf(
                    Offset(l + k, t),
                    Offset(r - k, t),
                    Offset(r, h / 2f),
                    Offset(r - k, b),
                    Offset(l + k, b),
                    Offset(l, h / 2f),
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

enum class FeldChipFill { Diamond, Chamfer }

private fun smooth(x: Float): Float {
    val t = x.coerceIn(0f, 1f);
    return t * t * (3f - 2f * t)
}

private fun revealPath(
    fillShape: FeldChipFill,
    body: Path,
    o: Offset,
    w: Float,
    h: Float,
    t: Float,
    round: Float,
): Path = when (fillShape) {
    FeldChipFill.Diamond -> {
        val maxR = maxOf(o.x, w - o.x) + maxOf(o.y, h - o.y)
        val r = maxR * 1.02f * t
        roundedPolygon(
            listOf(
                Offset(o.x, o.y - r),
                Offset(o.x + r, o.y),
                Offset(o.x, o.y + r),
                Offset(o.x - r, o.y),
            ),
            r * 0.10f,
        )
    }

    FeldChipFill.Chamfer -> {
        val k = 1.35f * t
        val m = Matrix().apply {
            translate(o.x, o.y);
            scale(k, k);
            translate(-o.x, -o.y)
        }
        Path().apply {
            addPath(body);
            transform(m)
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
    fillShape: FeldChipFill = FeldChipFill.Diamond,
    fillMillis: Int = 320,
    outline: Boolean = true,
    leading: Boolean = false,
    textSize: Float = 1f,
    leadingContent: (@Composable () -> Unit)? = null,
    contentHorizontalPadding: Dp = Dp.Unspecified,
    tip: Dp = 10.dp,
    rounding: Dp = 3.dp,
    seed: Int = rememberSeed(),
) {
    val interaction = remember { MutableInteractionSource() }
    val density = LocalDensity.current
    var chipHeight by remember { mutableStateOf(ChipMinHeight) }
    val pressed by interaction.collectIsPressedAsState()
    var origin by remember { mutableStateOf<Offset?>(null) }
    LaunchedEffect(interaction) {
        interaction.interactions.collect {
            if (it is PressInteraction.Press) origin = it.pressPosition
        }
    }

    val fillState = animateFloatAsState(
        targetValue = if (enabled && (selected || pressed)) 1f else 0f,
        animationSpec = tween(fillMillis, easing = FastOutSlowInEasing),
        label = "chipFill",
    )
    val scale by animateFloatAsState(
        if (pressed && enabled) 0.97f else 1f,
        tween(90),
        label = "chipScale",
    )
    val fill = fillState.value
    val flip = smooth((fill - 0.30f) / 0.35f)

    val textColor = if (!enabled) DisabledGray else lerp(FeldPalette.Cream, InkDark, flip)
    val diamondColor = if (enabled) accent else DisabledGray
    val sidePad = if (contentHorizontalPadding != Dp.Unspecified) {
        contentHorizontalPadding
    } else {
        when (shape) {
        FeldChipShape.Chamfer -> tip + 6.dp
        FeldChipShape.Pill -> chipHeight / 4
        FeldChipShape.Rectangle -> 8.dp
    }
    }

    Row(
        modifier = modifier
            .defaultMinSize(minHeight = ChipMinHeight)
            .onSizeChanged { chipHeight = with(density) { it.height.toDp() } }
            .graphicsLayer {
                scaleX = scale;
                scaleY = scale
            }
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
                    if (enabled) BaseDark else BaseDisabled,
                    seed,
                    density.density,
                    fade = 0.5f,
                    glow = 0.35f,
                    grain = 0.05f,
                )
                val filled =
                    accentBrush(accent, seed + 1, density.density, fade = 0.9f, glow = 0.9f, grain = 0.09f)
                val rimBrush = wornBrush(
                    if (enabled) FeldPalette.Cream else FeldPalette.Line,
                    seed + 2,
                    density.density,
                    wear = 0.45f,
                    scale = 14f,
                )
                val stroke = Stroke(1.dp.toPx(), join = StrokeJoin.Round)
                val defaultOrigin =
                    if (leading || leadingContent != null) {
                        Offset(
                        sidePad.toPx() + h * 0.17f,
                        h / 2f,
                    )
                    } else {
                        Offset(w / 2f, h / 2f)
                    }

                onDrawWithContent {
                    val t = fillState.value
                    clipPath(body) {
                        drawRect(base)
                        if (t > 0.999f) {
                            drawRect(filled)
                        } else if (t > 0.001f) {
                            val o = origin ?: defaultOrigin
                            clipPath(
                                revealPath(
                                    fillShape,
                                    body,
                                    o,
                                    w,
                                    h,
                                    t,
                                    rounding.toPx(),
                                ),
                            ) { drawRect(filled) }
                        }
                    }
                    drawContent()
                    if (outline) {
                        drawPath(
                            rim,
                            rimBrush,
                            alpha = (if (enabled) 0.85f else 0.45f) * (1f - 0.55f * t),
                            style = stroke,
                        )
                    }
                }
            }
            .padding(horizontal = sidePad),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
    ) {
        if (leadingContent != null) {
            leadingContent()
        } else if (leading) {
            Canvas(Modifier.size(chipHeight * 0.34f)) {
                val s = size.minDimension
                val d = Path().apply {
                    moveTo(s / 2f, 0f);
                    lineTo(s, s / 2f);
                    lineTo(s / 2f, s);
                    lineTo(
                        0f,
                        s / 2f,
                    );
                    close()
                }
                drawPath(d, diamondColor, alpha = 1f - flip)
                drawPath(d, InkDark, alpha = flip)
            }
        }
        Text(
            text = text.uppercase(),
            style = FeldDecalType.Label.copy(
                fontSize = (chipHeight.value * 0.33f * textSize).sp,
                lineHeight = (chipHeight.value * 0.4f * textSize).sp,
                letterSpacing = 1.4.sp,
            ),
            color = textColor,
            modifier = Modifier.align(Alignment.CenterVertically),
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
            FeldChip(
                "Rect",
                selected = false,
                onClick = {},
                shape = FeldChipShape.Rectangle,
                outline = false,
            )
        }
    }
}
