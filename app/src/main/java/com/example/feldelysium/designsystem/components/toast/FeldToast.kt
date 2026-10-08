package com.example.feldelysium.designsystem.components.toast

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.feldelysium.designsystem.components.FeldDiamond
import com.example.feldelysium.designsystem.components.card.FeldCard
import com.example.feldelysium.designsystem.components.title.FeldTitleTag
import com.example.feldelysium.designsystem.material.DryPaperSpec
import com.example.feldelysium.designsystem.material.dryPaperMaterial
import com.example.feldelysium.designsystem.shape.RoughStyle
import kotlin.math.*
import kotlin.random.Random

private val Cream = Color(0xFFDCCFB5)
private val PadL = 40.dp
private val PadT = 26.dp
private val PadB = 24.dp

private class ToastLayout(seed: Int) {
    private val r = Random(seed * 17 + 3)
    private fun f(a: Float, b: Float) = a + r.nextFloat() * (b - a)

    val leftW = f(14f, 20f).dp
    val leftTop = f(0f, 6f).dp
    val leftBottom = f(0f, 8f).dp
    val leftStart = f(3f, 7f).dp

    val rightW = f(4f, 7f).dp
    val rightH = f(28f, 48f).dp
    val rightTop = f(10f, 34f).dp
    val rightShift = f(-2f, 2f).dp

    class Seg(val a0: Float, val a1: Float, val jitter: FloatArray, val width: Float)

    val segs: List<Seg> = buildList {
        var a = 262f - f(0f, 10f)
        val end = 80f + 360f
        a += 0f
        var cur = 262f
        while (cur < 440f) {
            val len = f(22f, 55f)
            val s = Seg(cur, min(cur + len, 440f), FloatArray(24) { f(-1f, 1f) }, f(1.6f, 3.2f))
            add(s)
            cur += len + f(7f, 22f)
        }
    }
}

@Composable
fun FeldToast(
    speakerName: String,
    speakerColor: Color,
    text: String,
    modifier: Modifier = Modifier,
    seed: Int = 0,
    onDismiss: (() -> Unit)? = null,
) {
    val lay = remember(seed) { ToastLayout(seed) }

    val rough = remember {
        RoughStyle(roughness = 1.8.dp, jerkChance = 0.22f, tornChance = 0.3f, slide = 0.2f)
    }
    val stripe = remember(speakerColor) {
        DryPaperSpec(
            base = speakerColor,
            crack = lerp(speakerColor, Color.Black, 0.55f),
            tearDepth = 2.dp, tearCoverage = 0.4f, crackCoverage = 0.35f,
        )
    }

    val inf = rememberInfiniteTransition(label = "toast")
    val beat = inf.animateFloat(
        0f, 1f,
        infiniteRepeatable(keyframes {
            durationMillis = 2200
            0f at 0
            1f at 110 using FastOutSlowInEasing
            0.15f at 240
            0.7f at 340 using FastOutSlowInEasing
            0f at 520
            0f at 2200
        }),
        label = "beat",
    )
    val bob = inf.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "bob",
    )

    val burst = remember { Animatable(0f) }
    LaunchedEffect(seed) { burst.animateTo(1f, tween(420, easing = FastOutSlowInEasing)) }

    Box(
        modifier.pointerInput(onDismiss) {
            var dy = 0f
            detectVerticalDragGestures(
                onDragEnd = { if (dy > 36.dp.toPx()) onDismiss?.invoke(); dy = 0f },
                onDragCancel = { dy = 0f },
            ) { _, d -> dy += d }
        }
    ) {
        Canvas(Modifier.matchParentSize()) {
            val top = PadT.toPx()
            val cardH = size.height - top - PadB.toPx()
            val left = PadL.toPx()

            drawTornArc(lay, left, top, cardH)

            val c = Offset(left + 46.dp.toPx(), top)
            val R = 13.dp.toPx() * (1f + 0.2f * beat.value)
            drawCircle(Color(0xFF0C1212), R)
            drawCircle(speakerColor, R - 2.dp.toPx(), style = Stroke(4.5.dp.toPx()))
            drawCircle(Color.White, 5.dp.toPx() * (1f + 0.25f * beat.value), c)

            val p = burst.value
            val alpha = min(1f, p * 4f)
            listOf(-62f to 10.dp, -34f to 13.dp).forEach { (deg, len) ->
                val rad = Math.toRadians(deg.toDouble())
                val dir = Offset(cos(rad).toFloat(), sin(rad).toFloat())
                val s = R + 7.dp.toPx() + p * 9.dp.toPx()
                drawLine(
                    Cream.copy(alpha = alpha), c + dir * s, c + dir * (s + len.toPx()),
                    strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round,
                )
            }

            val tx = left + (size.width - left) / 2f
            val ty = top + cardH + 11.dp.toPx() + bob.value * 3.dp.toPx()
            val w = 8.dp.toPx()
            drawPath(
                Path().apply {
                    moveTo(tx - w, ty); lineTo(tx + w, ty); lineTo(tx, ty + w * 1.1f); close()
                },
                Cream.copy(alpha = 0.55f + 0.4f * bob.value),
            )
        }

        Box(Modifier.padding(start = PadL, top = PadT, bottom = PadB)) {
            FeldCard(
                seed = seed,
                rough = rough,
                contentPadding = PaddingValues(0.dp),
            ) {
                Box(Modifier.fillMaxWidth()) {
                    Row(Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)) {
                        Box(
                            Modifier
                                .padding(
                                    start = lay.leftStart,
                                    top = lay.leftTop,
                                    bottom = lay.leftBottom
                                )
                                .width(lay.leftW)
                                .fillMaxHeight()
                                .dryPaperMaterial(stripe, seed)
                        )
                        Row(
                            Modifier
                                .weight(1f)
                                .padding(start = 14.dp, top = 12.dp, bottom = 14.dp, end = 18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            FeldDiamond(speakerColor, size = 28.dp)
                            Column(Modifier.weight(1f)) {
                                FeldTitleTag(speakerName, seed = seed, distressed = true)
                                Text(
                                    text,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(top = 8.dp),
                                )
                            }
                        }
                    }
                    Box(
                        Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = lay.rightTop)
                            .offset(x = lay.rightShift)
                            .width(lay.rightW)
                            .height(lay.rightH)
                            .dryPaperMaterial(stripe, seed + 7)
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawTornArc(lay: ToastLayout, left: Float, top: Float, cardH: Float) {
    val cx = left + 34.dp.toPx()
    val cy = top + cardH / 2f
    val rx = 64.dp.toPx()
    val ry = cardH / 2f + 18.dp.toPx()

    for (seg in lay.segs) {
        val n = seg.jitter.size / 2
        var prev: Offset? = null
        for (i in 0..n) {
            val t = i / n.toFloat()
            val ang = Math.toRadians((seg.a0 + (seg.a1 - seg.a0) * t).toDouble())
            val j = if (i < n) seg.jitter[i * 2] * 1.6.dp.toPx() else 0f
            val pt = Offset(
                cx + (rx + j) * cos(ang).toFloat(),
                cy + (ry + j) * sin(ang).toFloat(),
            )
            prev?.let {
                val taper = (sin(PI * t).toFloat()).pow(0.6f).coerceAtLeast(0.25f)
                val w = seg.width.dp.toPx() * taper * (0.8f + 0.4f * abs(seg.jitter[i * 2 - 1]))
                drawLine(Cream, it, pt, strokeWidth = w, cap = StrokeCap.Round)
            }
            prev = pt
        }
    }
}
