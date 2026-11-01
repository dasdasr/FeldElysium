package com.feldelysium.designsystem.components.input

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.ColorProducer
import androidx.compose.ui.graphics.Matrix
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
import com.feldelysium.designsystem.foundation.decal.FeldDecalType
import com.feldelysium.designsystem.foundation.material.ChipTexture
import com.feldelysium.designsystem.foundation.material.ChipTextures
import com.feldelysium.designsystem.foundation.seed.rememberSeed
import com.feldelysium.designsystem.theme.FeldPalette
import com.feldelysium.designsystem.theme.FeldTheme
import kotlin.math.roundToInt

public enum class FeldChipShape { Chamfer, Pill, Rectangle }

public enum class FeldChipFill { Diamond, Chamfer }

public object FeldAccentColors {
    public val Teal: Color = FeldPalette.Teal
    public val Orange: Color = FeldPalette.Orange
    public val Cream: Color = FeldPalette.Cream
}

private val BaseDark = Color(0xFF111A1A)
private val BaseDisabled = Color(0xFF131817)
private val InkDark = Color(0xFF0B1011)
private val DisabledGray = Color(0xFF6E706D)

private val ChipDefaultHeight = 30.dp

private class PressOrigin {
    var x = Float.NaN
    var y = Float.NaN
}

private fun Path.roundedPolygon(pts: List<Offset>, r: Float): Path = apply {
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
        quadraticTo(cur.x, cur.y, b.x, b.y)
    }
    close()
}

private object ChipPaths {
    private data class K(
        val w: Int,
        val h: Int,
        val shape: FeldChipShape,
        val tip: Float,
        val round: Float,
        val inset: Float,
    )

    private val map = HashMap<K, Path>()

    fun get(w: Float, h: Float, shape: FeldChipShape, tip: Float, round: Float, inset: Float): Path {
        if (map.size > 64) map.clear()
        return map.getOrPut(K(w.roundToInt(), h.roundToInt(), shape, tip, round, inset)) {
            build(w, h, shape, tip, round, inset)
        }
    }

    private fun build(w: Float, h: Float, shape: FeldChipShape, tip: Float, round: Float, inset: Float): Path {
        val l = inset
        val t = inset
        val r = w - inset
        val b = h - inset
        return when (shape) {
            FeldChipShape.Chamfer -> {
                val k = tip.coerceAtMost((r - l) / 3f)
                Path().roundedPolygon(
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
}

private fun smooth(x: Float): Float {
    val t = x.coerceIn(0f, 1f)
    return t * t * (3f - 2f * t)
}

private fun buildReveal(
    out: Path,
    m: Matrix,
    fill: FeldChipFill,
    body: Path,
    o: Offset,
    w: Float,
    h: Float,
    t: Float,
) {
    out.rewind()
    when (fill) {
        FeldChipFill.Diamond -> {
            val r = (maxOf(o.x, w - o.x) + maxOf(o.y, h - o.y)) * 1.02f * t
            out.roundedPolygon(
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
            m.reset()
            m.translate(o.x, o.y)
            m.scale(k, k)
            m.translate(-o.x, -o.y)
            out.addPath(body)
            out.transform(m)
        }
    }
}

@Composable
public fun FeldChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = ChipDefaultHeight,
    enabled: Boolean = true,
    accent: Color = FeldAccentColors.Cream,
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
    val pressed by interaction.collectIsPressedAsState()
    val origin = remember { PressOrigin() }
    LaunchedEffect(interaction) {
        interaction.interactions.collect {
            if (it is PressInteraction.Press) {
                origin.x = it.pressPosition.x
                origin.y = it.pressPosition.y
            }
        }
    }

    val fill = animateFloatAsState(
        targetValue = if (enabled && (selected || pressed)) 1f else 0f,
        animationSpec = tween(fillMillis, easing = FastOutSlowInEasing),
        label = "chipFill",
    )
    val scale = animateFloatAsState(
        targetValue = if (pressed && enabled) 0.97f else 1f,
        animationSpec = tween(90),
        label = "chipScale",
    )

    val sidePad = when {
        contentHorizontalPadding != Dp.Unspecified -> contentHorizontalPadding
        shape == FeldChipShape.Chamfer -> tip + 6.dp
        shape == FeldChipShape.Pill -> height / 4
        else -> 8.dp
    }
    val label = remember(text) { text.uppercase() }
    val style = remember(height, textSize) {
        FeldDecalType.Label.copy(
            fontSize = (height.value * 0.33f * textSize).sp,
            lineHeight = (height.value * 0.4f * textSize).sp,
            letterSpacing = 1.4.sp,
        )
    }
    val textColor = remember(enabled) {
        ColorProducer {
            if (!enabled) {
                DisabledGray
            } else {
                lerp(FeldPalette.Cream, InkDark, smooth((fill.value - 0.30f) / 0.35f))
            }
        }
    }

    Row(
        modifier = modifier
            .defaultMinSize(minHeight = height)
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
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
                val tipPx = tip.toPx()
                val roundPx = rounding.toPx()
                val body = ChipPaths.get(w, h, shape, tipPx, roundPx, 0f)
                val rim = ChipPaths.get(w, h, shape, tipPx, roundPx, 0.8.dp.toPx())
                val wi = w.roundToInt()
                val hi = h.roundToInt()

                val base = ChipTextures.brush(
                    if (enabled) BaseDark else BaseDisabled,
                    ChipTexture.Base,
                    density,
                    wi,
                    hi,
                    seed,
                )
                val filled by lazy(LazyThreadSafetyMode.NONE) {
                    ChipTextures.brush(accent, ChipTexture.Filled, density, wi, hi, seed + 1)
                }
                val rimBrush by lazy(LazyThreadSafetyMode.NONE) {
                    ChipTextures.brush(
                        if (enabled) FeldPalette.Cream else FeldPalette.Line,
                        ChipTexture.Rim,
                        density,
                        wi,
                        hi,
                        seed + 2,
                    )
                }
                val stroke = Stroke(1.dp.toPx(), join = StrokeJoin.Round)
                val reveal = Path()
                val m = Matrix()
                val defaultOrigin = if (leading || leadingContent != null) {
                    Offset(sidePad.toPx() + h * 0.17f, h / 2f)
                } else {
                    Offset(w / 2f, h / 2f)
                }

                onDrawWithContent {
                    val t = fill.value
                    clipPath(body) {
                        if (t < 0.999f) drawRect(base)
                        if (t > 0.999f) {
                            drawRect(filled)
                        } else if (t > 0.001f) {
                            val o = if (origin.x.isNaN()) defaultOrigin else Offset(origin.x, origin.y)
                            buildReveal(reveal, m, fillShape, body, o, w, h, t)
                            clipPath(reveal) { drawRect(filled) }
                        }
                    }
                    drawContent()
                    if (outline) {
                        drawPath(
                            path = rim,
                            brush = rimBrush,
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
            Canvas(Modifier.size(height * 0.34f)) {
                val s = size.minDimension
                val d = Path().apply {
                    moveTo(s / 2f, 0f)
                    lineTo(s, s / 2f)
                    lineTo(s / 2f, s)
                    lineTo(0f, s / 2f)
                    close()
                }
                val flip = smooth((fill.value - 0.30f) / 0.35f)
                drawPath(d, if (enabled) accent else DisabledGray, alpha = 1f - flip)
                drawPath(d, InkDark, alpha = flip)
            }
        }
        BasicText(
            text = label,
            style = style,
            color = textColor,
            modifier = Modifier.align(Alignment.CenterVertically),
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0B1011, widthDp = 360)
@Composable
private fun FeldChipPreview() {
    FeldTheme {
        Column(
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
