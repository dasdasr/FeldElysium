package com.feldelysium.designsystem.components.input

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.feldelysium.designsystem.components.container.FeldDivider
import com.feldelysium.designsystem.foundation.haptics.rememberFeldHaptics
import com.feldelysium.designsystem.foundation.material.FeldPaper
import com.feldelysium.designsystem.foundation.material.agsl.SEGMENT_PAINT_AGSL
import com.feldelysium.designsystem.foundation.material.paperMaterial
import com.feldelysium.designsystem.foundation.seed.rememberSeed
import com.feldelysium.designsystem.foundation.shape.RoughShape
import com.feldelysium.designsystem.foundation.shape.RoughStyle
import com.feldelysium.designsystem.foundation.sound.feldSounds
import com.feldelysium.designsystem.theme.FeldTheme
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

private val TrackRough = RoughStyle(
    roughness = 0.6.dp,
    step = 36.dp,
    corner = 7.dp,
    cornerVariation = 0.15f,
    tornChance = 0f,
    jerkChance = 0.05f,
)

private val TrackPaper = FeldPaper.Panel.copy(
    ink = Color(0xFFCBBFA5),
    inkWidth = 1.3.dp,
    inkMinAlpha = 0.5f,
    inkContrast = 1.6f,
)

private val PaintBase = Color(0xFF58AA9B)
private val PaintDeep = Color(0xFF3E8A7E)
private val PaintLight = Color(0xFF86CDBC)
private val PaintHole = Color(0xFF0B1211)

private val OnText = Color(0xFF0E1816)
private val OffText = Color(0xFFD8CCB0)
private val DividerColor = Color(0xFFB9AD92)
private val Snap = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)

@Composable
public fun FeldSegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    seed: Int = rememberSeed(),
) {
    val haptics = rememberFeldHaptics()
    val count = options.size.coerceAtLeast(1)
    val selected = selectedIndex.coerceIn(0, count - 1)
    val trackShape = remember(seed) { RoughShape(seed, TrackRough) }

    val position by animateFloatAsState(
        targetValue = selected.toFloat(),
        animationSpec = tween(durationMillis = 260, easing = Snap),
        label = "segmentPosition",
    )

    var pressed by remember { mutableIntStateOf(-1) }
    val press by animateFloatAsState(
        targetValue = if (pressed == selected) 1f else 0f,
        animationSpec = tween(70),
        label = "segmentPress",
    )

    val sounds = feldSounds

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .paperMaterial(trackShape, TrackPaper)
            .padding(3.dp),
    ) {
        val segmentWidth = maxWidth / count

        for (i in 1 until count) {
            FeldDivider(
                vertical = true,
                seed = seed + i,
                color = DividerColor,
                modifier = Modifier
                    .offset { IntOffset((segmentWidth * i).roundToPx(), 0) }
                    .padding(vertical = 8.dp)
                    .graphicsLayer {
                        val t = position - (i - 1)
                        alpha = if (t in 0f..1f) 1f - sin(PI.toFloat() * t) else 1f
                    },
            )
        }

        Box(
            Modifier
                .offset { IntOffset((segmentWidth * position).roundToPx(), 0) }
                .width(segmentWidth)
                .fillMaxHeight()
                .graphicsLayer {
                    val s = 1f - 0.025f * press
                    scaleX = s
                    scaleY = s
                }
                .paintedPill(seed) { (selected - position).coerceIn(-1f, 1f) },
        )

        Row(Modifier.fillMaxSize()) {
            options.forEachIndexed { i, label ->
                val isSelected = i == selected
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .semantics(mergeDescendants = true) {
                            role = Role.Tab
                            this.selected = isSelected
                            onClick {
                                onSelect(i)
                                true
                            }
                        }
                        .pointerInput(i, selected) {
                            detectTapGestures(
                                onPress = {
                                    pressed = i
                                    tryAwaitRelease()
                                    pressed = -1
                                },
                                onTap = {
                                    if (i != selected) {
                                        haptics.tick()
                                        sounds.tab(seed + i)
                                    }
                                    onSelect(i)
                                },
                            )
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = label.uppercase(),
                        style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 1.6.sp),
                        color = lerp(OffText, OnText, (1f - abs(position - i)).coerceIn(0f, 1f)),
                    )
                }
            }
        }
    }
}

private fun Modifier.paintedPill(seed: Int, smear: () -> Float): Modifier = drawWithCache {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val shader = RuntimeShader(SEGMENT_PAINT_AGSL).apply {
            setFloatUniform("size", size.width, size.height)
            setFloatUniform("seed", seed.mod(97).toFloat())
            setFloatUniform("u", density)
            setColorUniform("baseColor", PaintBase.toArgb())
            setColorUniform("deepColor", PaintDeep.toArgb())
            setColorUniform("lightColor", PaintLight.toArgb())
            setColorUniform("holeColor", PaintHole.toArgb())
        }
        val brush = ShaderBrush(shader)
        onDrawBehind {
            shader.setFloatUniform("smear", smear())
            drawRect(brush)
        }
    } else {
        onDrawBehind {
            val i = 1.5.dp.toPx()
            drawRoundRect(
                PaintBase,
                Offset(i, i),
                Size(size.width - 2 * i, size.height - 2 * i),
                CornerRadius(3.dp.toPx()),
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0B1011)
@Composable
private fun FeldSegmentedControlPreview() {
    FeldTheme {
        Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            var a by remember { mutableIntStateOf(0) }
            var b by remember { mutableIntStateOf(1) }
            FeldSegmentedControl(listOf("Tab 1", "Tab 2", "Tab 3"), a, { a = it })
            FeldSegmentedControl(listOf("Media", "Color"), b, { b = it })
        }
    }
}
