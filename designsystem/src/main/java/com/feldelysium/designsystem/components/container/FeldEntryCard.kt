package com.feldelysium.designsystem.components.container

import android.os.SystemClock
import android.util.LruCache
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.feldelysium.designsystem.components.content.FeldIcon
import com.feldelysium.designsystem.components.content.FeldIconId
import com.feldelysium.designsystem.foundation.decal.DecalSpec
import com.feldelysium.designsystem.foundation.decal.LocalFeldDecalContext
import com.feldelysium.designsystem.foundation.material.FeldMarker
import com.feldelysium.designsystem.foundation.material.FeldPaper
import com.feldelysium.designsystem.foundation.material.wornBrush
import com.feldelysium.designsystem.foundation.seed.rememberSeed
import com.feldelysium.designsystem.foundation.shape.RoughShape
import com.feldelysium.designsystem.foundation.shape.RoughStyle
import com.feldelysium.designsystem.theme.FeldTheme
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.exp
import kotlin.random.Random

private const val SWAY_STIFFNESS = 140f
private const val SWAY_DAMPING = 9f
private const val SWAY_GAIN = 0.20f
private const val SWAY_MAX_ANGLE = 6f
private const val SWAY_VELOCITY_TTL_NANOS = 80_000_000L
private const val SWIPE_DELETE_THRESHOLD = 0.65f
private const val SWIPE_FILL_FULL = 0.3f

private val EntryRough = RoughStyle(
    roughness = 0.6.dp,
    step = 44.dp,
    corner = 3.dp,
    inkLevels = 5,
    cornerVariation = 0.05f,
    cornerJitter = 0.1f,
    tornChance = 0f,
    jerkChance = 0.04f,
)

public fun entryRough(seed: Int): RoughStyle {
    val r = Random(seed * 31 + 7)
    return EntryRough.copy(
        corner = (2f + r.nextFloat() * 4f).dp,
        cornerVariation = 0.35f,
        cornerJitter = 0.3f,
        tornChance = 0.04f,
    )
}

private class SwayState {
    var angle by mutableFloatStateOf(0f)
    var velocityY = 0f
    var velocityStamp = 0L
    var lastY = Float.NaN
    var lastStamp = 0L
    var dir = 1f
    var width = 1f
    var grabX = 0f
    var grabY = 0f
}

private val diamondBrushCache = LruCache<Triple<Color, Int, Float>, Brush>(128)

private fun cachedDiamondBrush(color: Color, seed: Int, density: Float): Brush {
    val key = Triple(color, seed, density)
    return diamondBrushCache.get(key)
        ?: wornBrush(color, seed + 1, density, wear = 0.4f, scale = 10f).also { diamondBrushCache.put(key, it) }
}

@Composable
public fun FeldEntryCard(
    speakerName: String,
    speakerColor: Color,
    text: String,
    modifier: Modifier = Modifier,
    decalSpec: DecalSpec = DecalSpec.Entry,
    number: Int? = null,
    ambient: Float? = null,
    textured: Boolean = true,
    showDecals: Boolean = textured,
    worn: Boolean = false,
    seed: Int = rememberSeed(),
    isDragging: Boolean = false,
    selected: Boolean = false,
    dragModifier: Modifier = Modifier,
    fontFamily: FontFamily? = null,
    onClick: (() -> Unit)? = null,
    onSwipeDelete: (() -> Unit)? = null,
    onSwipeArmed: () -> Unit = {},
) {
    val density = LocalDensity.current.density
    val longPressMs = LocalViewConfiguration.current.longPressTimeoutMillis.toInt()
    val scope = rememberCoroutineScope()
    val swipe = remember { Animatable(0f) }

    val lift by animateFloatAsState(
        targetValue = if (isDragging) 1f else 0f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
        label = "lift",
    )

    var holding by remember { mutableStateOf(false) }
    val charging = holding && !isDragging
    val hold by animateFloatAsState(
        targetValue = if (charging) 1f else 0f,
        animationSpec = if (charging) {
            tween((longPressMs - 120).coerceAtLeast(100), delayMillis = 120, easing = LinearEasing)
        } else {
            tween(140)
        },
        label = "hold",
    )
    val pulse = if (isDragging) {
        rememberInfiniteTransition(label = "beat").animateFloat(
            0f,
            1f,
            infiniteRepeatable(tween(1150, easing = LinearEasing), RepeatMode.Restart),
            label = "pulse",
        )
    } else {
        null
    }
    val diamondBrush: Brush = remember(speakerColor, seed, density) {
        cachedDiamondBrush(speakerColor, seed, density)
    }
    val barBrush: Brush = remember(speakerColor, seed, density, worn) {
        if (worn) {
            wornBrush(speakerColor, seed, density, wear = 0.6f, scale = 7f)
        } else {
            SolidColor(speakerColor)
        }
    }
    val flatBrush = remember(speakerColor) { SolidColor(speakerColor) }

    val sway = remember { SwayState() }
    LaunchedEffect(isDragging) {
        if (!isDragging) {
            sway.lastY = Float.NaN
        } else {
            val raw = ((sway.grabX - sway.width / 2f) / (sway.width / 2f)).coerceIn(-1f, 1f)
            sway.dir = raw * 0.75f + if (raw >= 0f) 0.25f else -0.25f
        }
        var angularVelocity = 0f
        var previous = SystemClock.elapsedRealtimeNanos()
        while (isDragging || abs(sway.angle) > 0.02f || abs(angularVelocity) > 0.05f) {
            withFrameNanos { }
            val now = SystemClock.elapsedRealtimeNanos()
            val dt = ((now - previous) / 1e9f).coerceAtMost(0.032f)
            previous = now

            val fresh = isDragging && now - sway.velocityStamp < SWAY_VELOCITY_TTL_NANOS
            val velocity = if (fresh) sway.velocityY else 0f
            val torque = -SWAY_STIFFNESS * sway.angle -
                SWAY_DAMPING * angularVelocity +
                SWAY_GAIN * velocity * sway.dir

            angularVelocity += torque * dt
            sway.angle = (sway.angle + angularVelocity * dt)
                .coerceIn(-SWAY_MAX_ANGLE, SWAY_MAX_ANGLE)
        }
        sway.angle = 0f
    }
    val rough = remember(seed) { entryRough(seed) }
    val cardShape = remember(seed, rough) { RoughShape(seed = seed, style = rough) }

    val baseCtx = LocalFeldDecalContext.current
    val decalCtx = remember(baseCtx, number, speakerName) {
        baseCtx.copy(lineNumber = number, speakerName = speakerName)
    }

    val swiping by remember { derivedStateOf { swipe.value > 0.5f } }

    FeldCard(
        modifier = modifier
            .onSizeChanged { sway.width = it.width.toFloat().coerceAtLeast(1f) }
            .then(
                if (isDragging) {
                    Modifier.onGloballyPositioned { c ->
                        val y = c.positionInRoot().y
                        val now = SystemClock.elapsedRealtimeNanos()
                        if (!sway.lastY.isNaN() && now > sway.lastStamp) {
                            val instant = (y - sway.lastY) / ((now - sway.lastStamp) / 1e9f)
                            sway.velocityY = sway.velocityY * 0.6f + instant * 0.4f
                            sway.velocityStamp = now
                        }
                        sway.lastY = y
                        sway.lastStamp = now
                    }
                } else {
                    Modifier
                },
            )
            .graphicsLayer {
                val s = 1f + 0.035f * lift
                scaleX = s
                scaleY = s
                rotationZ = sway.angle
                if (size.width > 0f && size.height > 0f) {
                    transformOrigin = TransformOrigin(
                        (sway.grabX / size.width).coerceIn(0f, 1f),
                        (sway.grabY / size.height).coerceIn(0f, 1f),
                    )
                }
            }
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    sway.grabX = down.position.x
                    sway.grabY = down.position.y
                    holding = true
                    waitForUpOrCancellation()
                    holding = false
                }
            }
            .pointerInput(onSwipeDelete != null, isDragging) {
                if (onSwipeDelete == null || isDragging) return@pointerInput
                var armed = false
                detectHorizontalDragGestures(
                    onDragStart = { armed = false },
                    onDragEnd = {
                        val width = sway.width
                        if (swipe.value / width >= SWIPE_DELETE_THRESHOLD) {
                            scope.launch {
                                swipe.animateTo(width, tween(120))
                                onSwipeDelete()
                            }
                        } else {
                            scope.launch { swipe.animateTo(0f, tween(200)) }
                        }
                    },
                    onDragCancel = { scope.launch { swipe.animateTo(0f, tween(200)) } },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        val next = (swipe.value + dragAmount).coerceIn(0f, sway.width)
                        val nowArmed = next / sway.width >= SWIPE_DELETE_THRESHOLD
                        if (nowArmed && !armed) onSwipeArmed()
                        armed = nowArmed
                        scope.launch { swipe.snapTo(next) }
                    },
                )
            }
            .then(dragModifier),
        ambient = ambient,
        textured = textured,
        showDecals = showDecals,
        decalSpec = decalSpec,
        decalContext = decalCtx,
        decalInset = 7.dp,
        spec = FeldPaper.Entry,
        rough = rough,
        contentPadding = PaddingValues(0.dp),
        onClick = onClick,
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .then(if (textured) Modifier.clip(cardShape) else Modifier),
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
            ) {
                Box(
                    Modifier
                        .width(7.dp)
                        .fillMaxHeight()
                        .drawWithCache {
                            val path = tabPath(this, seed, size.width, size.height)
                            onDrawBehind { drawPath(path, barBrush) }
                        },
                )

                Row(
                    Modifier
                        .weight(1f)
                        .padding(start = 10.dp, end = 8.dp, top = 8.dp, bottom = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .drawBehind { drawDiamondBeat(diamondBrush, hold, pulse?.value) },
                        contentAlignment = Alignment.Center,
                    ) {
                        FeldHandDiamond(
                            speakerColor,
                            seed,
                            modifier = Modifier.graphicsLayer {
                                val s = beatScale(pulse?.value)
                                scaleX = s
                                scaleY = s
                            },
                            size = 22.dp,
                            brush = diamondBrush,
                        )
                    }
                    Spacer(Modifier.width(14.dp))

                    Column(Modifier.weight(1f)) {
                        Text(
                            text = speakerName.uppercase(),
                            style = MaterialTheme.typography.labelMedium,
                            color = speakerColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )

                        Text(
                            text = text,
                            style = MaterialTheme.typography.bodyMedium.let { s ->
                                if (fontFamily != null) s.copy(fontFamily = fontFamily) else s
                            },
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(top = 3.dp),
                        )
                    }
                }
            }

            if (number != null) {
                StampNumber(
                    number = number,
                    seed = seed,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 10.dp, bottom = 4.dp),
                )
            }

            if (onSwipeDelete != null && swiping) {
                Box(
                    Modifier
                        .matchParentSize()
                        .drawBehind {
                            val p = (swipe.value / size.width.coerceAtLeast(1f)).coerceIn(0f, 1f)
                            val tabW = 7.dp.toPx()
                            val barW = tabW + (size.width - tabW) * p
                            val a = (p / SWIPE_FILL_FULL).coerceIn(0f, 1f)
                            if (worn) {
                                drawTab(flatBrush, seed, barW, a)
                                drawTab(barBrush, seed, barW)
                                drawTab(flatBrush, seed, barW, a)
                            } else {
                                drawTab(barBrush, seed, barW)
                            }
                        },
                    contentAlignment = Alignment.CenterStart,
                ) {
                    FeldIcon(
                        id = FeldIconId.Delete,
                        seed = seed,
                        markerFilmSpec = FeldMarker.Ink,
                        size = 24.dp,
                        modifier = Modifier
                            .padding(start = 21.dp)
                            .graphicsLayer {
                                val p = (swipe.value / sway.width).coerceIn(0f, 1f)
                                val barW = 7.dp.toPx() + (sway.width - 7.dp.toPx()) * p
                                alpha = ((barW - 50.dp.toPx()) / 20.dp.toPx()).coerceIn(0f, 1f)
                            },
                    )
                }
            }
            if (selected) {
                Box(
                    Modifier
                        .matchParentSize()
                        .background(Color.Black.copy(alpha = 0.4f)),
                )
            }
        }
    }
}

@Composable
private fun StampNumber(number: Int, seed: Int, color: Color, modifier: Modifier = Modifier) {
    val look = remember(seed) { StampLook(seed) }
    if (!look.visible) return

    Text(
        text = look.format(number),
        style = MaterialTheme.typography.labelSmall.copy(
            letterSpacing = look.tracking.sp,
        ),
        color = color,
        modifier = modifier
            .graphicsLayer {
                alpha = look.alpha
                rotationZ = look.tilt
                translationY = look.dy.dp.toPx()
            }
            .padding(2.dp),
    )
}

private class StampLook(seed: Int) {
    private val r = Random(seed * 7919 + 13)
    val tilt = (r.nextFloat() - 0.5f) * 7f
    val alpha = 0.12f + r.nextFloat() * 0.12f
    val dy = (r.nextFloat() - 0.5f) * 2f
    val tracking = 0.6f + r.nextFloat() * 1.2f
    private val kind = r.nextInt(4)
    val visible = r.nextFloat() > 0.15f

    fun format(n: Int): String {
        val s = n.toString().padStart(2, '0')
        return when (kind) {
            0 -> s
            1 -> "№$s"
            2 -> "#$s"
            else -> "$s."
        }
    }
}

@Composable
public fun FeldHandDiamond(
    color: Color,
    seed: Int,
    modifier: Modifier = Modifier,
    size: Dp = 22.dp,
    brush: Brush? = null,
) {
    val density = LocalDensity.current.density
    val b = brush ?: remember(color, seed, density) { cachedDiamondBrush(color, seed, density) }
    val j = remember(seed) {
        val r = Random(seed * 131 + 5)
        FloatArray(8) { (r.nextFloat() - 0.5f) * 0.07f }
    }
    Canvas(modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val p = Path().apply {
            moveTo(w * (0.5f + j[0]), h * (0.02f + abs(j[1])))
            lineTo(w * (0.98f - abs(j[2])), h * (0.5f + j[3]))
            lineTo(w * (0.5f + j[4]), h * (0.98f - abs(j[5])))
            lineTo(w * (0.02f + abs(j[6])), h * (0.5f + j[7]))
            close()
        }
        drawPath(p, b)
    }
}

private fun gauss(t: Float, c: Float, w: Float): Float {
    val x = (t - c) / w
    return exp(-x * x)
}

internal fun beatScale(t: Float?): Float =
    if (t == null) 1f else 1f + 0.18f * gauss(t, 0.07f, 0.05f) + 0.11f * gauss(t, 0.34f, 0.06f)

public fun DrawScope.drawDiamondBeat(brush: Brush, hold: Float, t: Float?) {
    val stroke = 1.5.dp.toPx()
    val r = size.minDimension / 2f + 6.dp.toPx()
    fun diamond(rad: Float) = Path().apply {
        moveTo(center.x, center.y - rad)
        lineTo(center.x + rad, center.y)
        lineTo(center.x, center.y + rad)
        lineTo(center.x - rad, center.y)
        close()
    }
    if (hold > 0.01f) {
        val m = PathMeasure().apply { setPath(diamond(r), false) }
        val seg = Path()
        m.getSegment(0f, m.length * hold, seg, true)
        drawPath(
            seg,
            brush,
            alpha = 0.9f,
            style = Stroke(stroke, cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
    }
    if (t != null) {
        val s = beatScale(t)
        drawPath(
            diamond(r * (1f + (s - 1f) * 0.6f)),
            brush,
            alpha = 0.9f,
            style = Stroke(stroke, join = StrokeJoin.Round),
        )
        fun ring(start: Float, len: Float, reach: Float, a: Float) {
            val p = (t - start) / len
            if (p !in 0f..1f) return
            val e = 1f - (1f - p) * (1f - p) * (1f - p)
            drawPath(
                diamond(r + reach.dp.toPx() * e),
                brush,
                alpha = a * (1f - p) * (1f - p),
                style = Stroke(stroke * (1.2f - 0.6f * p), join = StrokeJoin.Round),
            )
        }
        ring(0.02f, 0.55f, 14f, 0.7f)
        ring(0.30f, 0.55f, 10f, 0.45f)
    }
}

internal fun tabPath(d: Density, seed: Int, barWidth: Float, height: Float): Path = with(d) {
    val r = Random(seed * 17 + 3)
    val wob = 1.5.dp.toPx()
    val base = barWidth - wob
    val top = r.nextFloat() * 4.dp.toPx()
    val bottom = height - r.nextFloat() * 4.dp.toPx()
    val step = 7.dp.toPx()
    Path().apply {
        moveTo(0f, top)
        lineTo(base + r.nextFloat() * wob, top)
        var y = top + step
        while (y < bottom - step * 0.5f) {
            lineTo(base + r.nextFloat() * wob, y)
            y += step * (0.6f + r.nextFloat() * 0.8f)
        }
        lineTo(base + r.nextFloat() * wob, bottom)
        lineTo(0f, bottom)
        close()
    }
}

private fun DrawScope.drawTab(brush: Brush, seed: Int, barWidth: Float = size.width, alpha: Float = 1f) {
    drawPath(tabPath(this, seed, barWidth, size.height), brush, alpha = alpha)
}

@Preview(widthDp = 380, heightDp = 520, showBackground = true, backgroundColor = 0xFF0B1011)
@Composable
private fun FeldEntryCardPreview() {
    FeldTheme {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            FeldEntryCard(
                "Logic",
                Color(0xFF4FB3A6),
                "This is impossible. He is not from Revachol. He is not even from this isola, and nobody here has ever heard of a place like that.",
                number = 1,
                seed = 1,
                onClick = {},
            )
            FeldEntryCard(
                "Empathy",
                Color(0xFFD06A8B),
                "He looks lonely.",
                number = 2,
                seed = 2,
                onClick = {},
            )
            FeldEntryCard(
                "Drama",
                Color(0xFF8F6FD6),
                "Oh, what a magnificent entrance. The orange clothes. The ridiculous hair.",
                number = 3,
                seed = 3,
                isDragging = true,
            )
        }
    }
}
