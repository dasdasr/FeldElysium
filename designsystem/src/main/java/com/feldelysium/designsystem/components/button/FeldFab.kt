package com.feldelysium.designsystem.components.button

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.feldelysium.designsystem.foundation.haptics.rememberFeldHaptics
import com.feldelysium.designsystem.foundation.material.ChipTexture
import com.feldelysium.designsystem.foundation.material.ChipTextures
import com.feldelysium.designsystem.foundation.material.FeldMarker
import com.feldelysium.designsystem.foundation.material.accentBrush
import com.feldelysium.designsystem.foundation.material.markerFilmMaterial
import com.feldelysium.designsystem.foundation.seed.rememberSeed
import com.feldelysium.designsystem.theme.FeldTheme
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

public enum class FeldFabVariant {
    Film,
    Marker,
    Rec,
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
public fun FeldFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    seed: Int = rememberSeed(),
    size: Dp = 56.dp,
    contentDescription: String? = null,
    onLongClick: (() -> Unit)? = null,
    variant: FeldFabVariant = FeldFabVariant.Film, // TODO(): Add these variants to Settings
    content: (@Composable () -> Unit)? = null,
) {
    val haptics = rememberFeldHaptics()
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val press by animateFloatAsState(
        targetValue = if (pressed) 1f else 0f,
        animationSpec = tween(
            durationMillis = if (pressed) 75 else 150,
            easing = if (pressed) FastOutSlowInEasing else LinearOutSlowInEasing,
        ),
        label = "press",
    )
    val isFilm = variant == FeldFabVariant.Film

    Box(
        modifier = modifier
            .size(
                width = if (isFilm) size + 20.dp else size,
                height = if (isFilm) size - 6.dp else size,
            )
            .graphicsLayer {
                val s = 1f - 0.055f * press
                scaleX = s
                scaleY = s

                rotationZ = when (variant) {
                    FeldFabVariant.Film -> -4f + 3f * press
                    FeldFabVariant.Marker -> -6f * press
                    FeldFabVariant.Rec -> 0f
                }

                translationY = 1.5f * press
            }
            .then(
                if (isFilm) {
                    Modifier.shadow(
                        elevation = 6.dp,
                        shape = RoundedCornerShape(4.dp),
                        clip = false,
                        ambientColor = Color.Black,
                        spotColor = Color.Black,
                    )
                } else {
                    Modifier
                },
            )
            .semantics { contentDescription?.let { this.contentDescription = it } }
            .combinedClickable(
                interactionSource = interaction,
                indication = null,
                role = Role.Button,
                onClick = {
                    haptics.tick()
                    onClick()
                },
                onLongClick = onLongClick?.let { longClick ->
                    {
                        haptics.thud()
                        longClick()
                    }
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        when (variant) {
            FeldFabVariant.Film -> FilmFace(seed, { press }, content)

            FeldFabVariant.Marker -> Box(
                Modifier
                    .fillMaxSize()
                    .markerFilmMaterial(FeldMarker.Chalk, seed)
                    .padding(6.dp),
                contentAlignment = Alignment.Center,
            ) {
                Canvas(Modifier.fillMaxSize()) {
                    val sw = 3.5.dp.toPx()
                    handRing(seed, sw)
                    if (content == null) handPlus(seed, sw)
                }
                content?.invoke()
            }

            FeldFabVariant.Rec -> RecFace(seed, { press }, content)
        }
    }
}

@Composable
private fun FilmFace(seed: Int, press: () -> Float, content: (@Composable () -> Unit)?) {
    val density = LocalDensity.current.density

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Canvas(
            Modifier
                .fillMaxSize()
                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen },
        ) {
            val body = ChipTextures.brush(
                Color(0xFFE45720),
                ChipTexture.Base,
                density,
                size.width.toInt(),
                size.height.toInt(),
                seed,
            )

            drawRoundRect(brush = body, cornerRadius = CornerRadius(4.dp.toPx()))

            drawFilmPerforation(press())
        }
        Box(
            Modifier
                .size(34.dp)
                .markerFilmMaterial(FeldMarker.Ink, seed)
                .padding(3.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (content != null) {
                content()
            } else {
                Canvas(Modifier.fillMaxSize()) { handPlus(seed, 3.5.dp.toPx(), arm = 0.34f) }
            }
        }
    }
}

@Composable
private fun RecFace(seed: Int, press: () -> Float, content: (@Composable () -> Unit)?) {
    val density = LocalDensity.current.density
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .fillMaxSize()
                .markerFilmMaterial(FeldMarker.Chalk, seed)
                .padding(6.dp),
        ) {
            Canvas(Modifier.fillMaxSize()) { handRing(seed, 3.dp.toPx()) }
        }
        if (content != null) {
            content()
        } else {
            val pulse by rememberInfiniteTransition(label = "rec").animateFloat(
                initialValue = 0.45f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    tween(850, easing = FastOutSlowInEasing),
                    RepeatMode.Reverse,
                ),
                label = "pulse",
            )
            val dot = remember(seed, density) { accentBrush(Color(0xFFE45720), seed, density) }
            Canvas(
                Modifier
                    .fillMaxSize()
                    .padding(19.dp),
            ) {
                val r = size.minDimension / 2f * (1f - 0.25f * press())
                drawCircle(brush = dot, radius = r * 1.5f, alpha = 0.16f * pulse)
                drawCircle(brush = dot, radius = r, alpha = pulse)
            }
        }
    }
}

private fun DrawScope.handRing(seed: Int, strokeW: Float) {
    val rnd = seed.mod(97) * 0.37f
    val r = size.minDimension / 2f - strokeW
    val start = -1.2f + rnd % 1f
    val sweep = 2f * PI.toFloat() * 1.12f
    val steps = 90
    val path = Path()
    for (i in 0..steps) {
        val t = i / steps.toFloat()
        val a = start + sweep * t
        val rr = r * (1f - 0.07f * t + 0.025f * sin(3f * a + rnd))
        val x = center.x + rr * cos(a)
        val y = center.y + rr * sin(a)
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    drawPath(
        path,
        Color.White,
        style = Stroke(strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round),
    )
}

private fun DrawScope.handPlus(seed: Int, strokeW: Float, arm: Float = 0.22f) {
    val k = 1f + (seed.mod(7) - 3) * 0.015f
    val h = size.minDimension * arm
    drawLine(
        Color.White,
        Offset(center.x - h, center.y + h * 0.05f),
        Offset(center.x + h * 1.08f * k, center.y - h * 0.06f),
        strokeW,
        StrokeCap.Round,
    )
    drawLine(
        Color.White,
        Offset(center.x + h * 0.04f, center.y - h * 1.05f),
        Offset(center.x - h * 0.05f * k, center.y + h * 1.0f),
        strokeW,
        StrokeCap.Round,
    )
}

@Preview(widthDp = 340, heightDp = 130, showBackground = true, backgroundColor = 0xFF0B1011)
@Composable
private fun FeldFabPreview() {
    FeldTheme {
        Row(
            Modifier.padding(24.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FeldFabVariant.entries.forEachIndexed { i, v ->
                FeldFab(onClick = {}, seed = 3 + i, variant = v, contentDescription = "Add entry")
            }
        }
    }
}
