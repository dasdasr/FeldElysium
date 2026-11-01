package com.feldelysium.designsystem.components.input

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import com.feldelysium.designsystem.foundation.decal.FeldDecalType
import com.feldelysium.designsystem.foundation.haptics.rememberFeldHaptics
import com.feldelysium.designsystem.foundation.material.FeldPaper
import com.feldelysium.designsystem.foundation.material.paperMaterial
import com.feldelysium.designsystem.foundation.material.paperMaterialMorph
import com.feldelysium.designsystem.foundation.seed.rememberSeed
import com.feldelysium.designsystem.foundation.shape.RoughShape
import com.feldelysium.designsystem.foundation.shape.RoughStyle
import com.feldelysium.designsystem.foundation.sound.feldSounds
import com.feldelysium.designsystem.theme.FeldPalette
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch
import kotlin.math.abs

private val OnSpec = FeldPaper.DryButtonRust
private val OffSpec = FeldPaper.DryButtonRust.copy(
    base = Color(0xFF222A29),
    scuff = Color(0xFF141A1A),
    mottleStrength = 0.06f,
    scuffTint = 0.25f,
)
private val RimSpec = FeldPaper.Panel.copy(
    wear = 0.18f,
    ink = Color(0xFF8C8E8A),
    inkWidth = 1.2.dp,
    inkMinAlpha = 0.75f,
)

private val Settle = spring<Float>(dampingRatio = 0.62f, stiffness = 520f)

private fun smooth(x: Float): Float {
    val t = x.coerceIn(0f, 1f)
    return t * t * (3f - 2f * t)
}

private class SideRoundShape(private val l: Float, private val r: Float) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val max = size.height / 2f
        val cl = CornerRadius(l.coerceIn(0f, max))
        val cr = CornerRadius(r.coerceIn(0f, max))
        return Outline.Rounded(RoundRect(0f, 0f, size.width, size.height, cl, cr, cr, cl))
    }
}

@Composable
public fun FeldSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    width: Dp = 76.dp,
    height: Dp = 32.dp,
    seed: Int = rememberSeed(),
) {
    val density = LocalDensity.current
    val haptics = rememberFeldHaptics()
    val scope = rememberCoroutineScope()
    val sounds = feldSounds

    val inset = 2.dp
    val halfInner = (width - inset * 2) / 2
    val travelPx = with(density) { halfInner.toPx() }
    val smallPx = with(density) { 2.dp.toPx() }

    val pos = remember { Animatable(if (checked) 0f else 1f) }
    var dragging by remember { mutableStateOf(false) }
    var pressed by remember { mutableStateOf(false) }
    val currentChecked by rememberUpdatedState(checked)
    val currentOnChange by rememberUpdatedState(onCheckedChange)

    LaunchedEffect(checked) {
        val t = if (checked) 0f else 1f
        if (pos.targetValue != t) {
            pos.animateTo(t, Settle)
        }
    }

    val press by animateFloatAsState(if (pressed) 1f else 0f, tween(90), label = "press")
    val stretch by animateFloatAsState(if (dragging) 1f else 0f, tween(140), label = "stretch")

    val trackShape = remember(seed, height) {
        RoughShape(
            seed,
            RoughStyle(
                roughness = 0.4.dp,
                step = 18.dp,
                corner = height / 2,
                inset = 0.dp,
                cornerVariation = 0f,
                cornerJitter = 0f,
                tornChance = 0f,
                jerkChance = 0.03f,
            ),
        )
    }
    val thumbShape = remember(seed) {
        RoughShape(
            seed + 1,
            RoughStyle(
                roughness = 0.35.dp,
                step = 14.dp,
                corner = 2.dp,
                inset = 0.dp,
                cornerVariation = 0.2f,
                cornerJitter = 0.1f,
                tornChance = 0f,
                jerkChance = 0.04f,
            ),
        )
    }

    fun settle(velocityPerSec: Float) {
        val projected = pos.value + velocityPerSec * 0.12f
        val target = if (projected < 0.5f) 0f else 1f
        scope.launch { pos.animateTo(target, Settle, initialVelocity = velocityPerSec) }
        val newChecked = target == 0f
        if (newChecked != currentChecked) {
            haptics.confirm()
            sounds.toggle(seed)
            currentOnChange(newChecked)
        }
    }

    val gestures = if (!enabled) {
        Modifier
    } else {
        Modifier.pointerInput(travelPx) {
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)
                pressed = true
                val tracker = VelocityTracker()
                tracker.addPosition(down.uptimeMillis, down.position)
                var total = 0f
                var drag = false
                var finished = false

                while (!finished) {
                    val ev = awaitPointerEvent()
                    val c = ev.changes.firstOrNull { it.id == down.id } ?: break
                    if (c.isConsumed && !drag) break
                    tracker.addPosition(c.uptimeMillis, c.position)

                    if (!c.pressed) {
                        if (drag) {
                            dragging = false
                            settle(tracker.calculateVelocity().x / travelPx)
                        } else {
                            haptics.confirm()
                            sounds.toggle(seed + 1)
                            currentOnChange(!currentChecked)
                        }
                        finished = true
                    } else {
                        val dx = c.positionChange().x
                        total += dx
                        if (!drag && abs(total) > viewConfiguration.touchSlop) {
                            drag = true
                            dragging = true
                            scope.launch(start = CoroutineStart.UNDISPATCHED) { pos.stop() }
                        }
                        if (drag) {
                            c.consume()
                            val old = pos.value
                            val new = (old + dx / travelPx).coerceIn(0f, 1f)
                            if ((old < 0.5f) != (new < 0.5f)) {
                                haptics.tick()
                                sounds.toggle(seed)
                            }
                            scope.launch(start = CoroutineStart.UNDISPATCHED) { pos.snapTo(new) }
                        }
                    }
                }
                if (!finished && drag) {
                    dragging = false
                    settle(0f)
                }
                pressed = false
            }
        }
    }

    val labelStyle =
        FeldDecalType.Label.copy(fontSize = (height.value * 0.29f).sp, letterSpacing = 1.2.sp)

    Box(
        modifier = modifier
            .size(width, height)
            .graphicsLayer {
                val s = 1f - 0.03f * press
                scaleX = s
                scaleY = s
                alpha = if (enabled) 1f else 0.45f
            }
            .drawBehind {
                val a = 0.16f * (1f - pos.value)
                if (a > 0.005f) {
                    val r = size.height * 1.4f
                    val c = Offset(
                        inset.toPx() + travelPx / 2f + pos.value * travelPx,
                        size.height / 2f,
                    )
                    drawCircle(
                        Brush.radialGradient(
                            listOf(
                                FeldPalette.Orange.copy(alpha = a),
                                Color.Transparent,
                            ),
                            c,
                            r,
                        ),
                        radius = r,
                        center = c,
                    )
                }
            }
            .paperMaterial(shape = trackShape, spec = RimSpec)
            .semantics(mergeDescendants = true) {
                role = Role.Switch
                toggleableState = ToggleableState(checked)
                onClick {
                    sounds.toggle(seed)
                    onCheckedChange(!checked)
                    true
                }
            }
            .then(gestures),
    ) {
        Box(
            Modifier
                .matchParentSize()
                .padding(inset),
        ) {
            Box(
                Modifier
                    .fillMaxHeight()
                    .width(halfInner)
                    .graphicsLayer {
                        val p = pos.value
                        translationX = p * travelPx
                        scaleX = 1f + 0.05f * stretch
                        val big = size.height / 2f
                        val t = smooth(p)

                        val l = lerp(big, smallPx, t)
                        val r = lerp(smallPx, big, t)

                        shape = SideRoundShape(l, r)
                        clip = true
                    }
                    .paperMaterialMorph(
                        shape = thumbShape,
                        from = OnSpec,
                        to = OffSpec,
                        progress = { pos.value },
                        wearBump = 0.05f,
                    ),
            )
        }

        Row(
            Modifier
                .matchParentSize()
                .padding(inset),
        ) {
            SwitchLabel("ON", labelStyle, { lerp(0.35f, 1f, 1f - pos.value) }, Modifier.weight(1f))
            SwitchLabel("OFF", labelStyle, { lerp(0.35f, 1f, pos.value) }, Modifier.weight(1f))
        }
    }
}

@Composable
private fun SwitchLabel(text: String, style: TextStyle, alpha: () -> Float, modifier: Modifier) {
    Box(
        modifier
            .fillMaxHeight()
            .graphicsLayer { this.alpha = alpha() },
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = style, color = FeldPalette.Cream)
    }
}
