package com.example.feldelysium.designsystem.foundation.material

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.feldelysium.designsystem.foundation.material.agsl.MARKER_FILM_AGSL

@Immutable
data class MarkerFilmSpec(
    val color: Color = Color.Unspecified,

    val coverage: Float = 0.86f,
    val unevenness: Float = 0.25f,
    val blotchSize: Dp = 26.dp,
    val poolEdge: Float = 0.14f,

    val edgeSoftness: Dp = 1.1.dp,
    val erode: Float = 0.55f,
    val erodeSize: Dp = 2.4.dp,
    val wobble: Dp = 0.45.dp,

    val streak: Float = 0.20f,
    val streakLength: Dp = 24.dp,
    val streakWidth: Dp = 0.65.dp,
    val streakAngle: Float = -7f,

    val skip: Float = 0.55f,
    val skipCoverage: Float = 0.35f,
    val skipSize: Dp = 30.dp,
    val pinhole: Float = 0.6f,

    val grain: Float = 0.16f,
    val grainSize: Dp = 0.6.dp,

    val smudge: Float = 0.16f,
    val smudgeSize: Dp = 50.dp,
    val smudgeRadius: Dp = 3.dp,
    val speck: Float = 0.5f,
)

object FeldMarker {
    val Chalk = MarkerFilmSpec(color = Color(0xFFEDE9DD))

    val Ink = MarkerFilmSpec(
        color = Color(0xFF14110F),
        coverage = 0.96f,
        unevenness = 0.10f,
        streak = 0.08f,
        skip = 0.25f,
        grain = 0.06f,
        smudge = 0.10f,
        speck = 0.3f,
    )

    val Dry = Chalk.copy(
        coverage = 0.72f,
        unevenness = 0.38f,
        erode = 0.8f,
        streak = 0.30f,
        skip = 0.9f,
        skipCoverage = 0.6f,
        pinhole = 1f,
    )
}

fun Modifier.markerFilmMaterial(
    spec: MarkerFilmSpec = FeldMarker.Chalk,
    seed: Int = 0,
): Modifier =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) markerFilmShader(spec, seed)
    else this

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
private fun Modifier.markerFilmShader(spec: MarkerFilmSpec, seed: Int): Modifier = composed {
    val shader = remember { RuntimeShader(MARKER_FILM_AGSL) }
    Modifier.graphicsLayer {
        if (size.width <= 0f || size.height <= 0f) return@graphicsLayer
        shader.apply {
            setFloatUniform("seed", seed.mod(97).toFloat())
            setFloatUniform("coverage", spec.coverage)
            setFloatUniform("unevenness", spec.unevenness)
            setFloatUniform("blotchScale", spec.blotchSize.toPx())
            setFloatUniform("poolEdge", spec.poolEdge)
            setFloatUniform("edgeSoft", spec.edgeSoftness.toPx())
            setFloatUniform("erode", spec.erode)
            setFloatUniform("erodeScale", spec.erodeSize.toPx())
            setFloatUniform("wobble", spec.wobble.toPx())
            setFloatUniform("streak", spec.streak)
            setFloatUniform("streakLen", spec.streakLength.toPx())
            setFloatUniform("streakWid", spec.streakWidth.toPx())
            setFloatUniform("streakAngle", Math.toRadians(spec.streakAngle.toDouble()).toFloat())
            setFloatUniform("skip", spec.skip)
            setFloatUniform("skipCoverage", spec.skipCoverage)
            setFloatUniform("skipScale", spec.skipSize.toPx())
            setFloatUniform("pinhole", spec.pinhole)
            setFloatUniform("grain", spec.grain)
            setFloatUniform("grainScale", spec.grainSize.toPx())
            setFloatUniform("smudge", spec.smudge)
            setFloatUniform("smudgeScale", spec.smudgeSize.toPx())
            setFloatUniform("smudgeRadius", spec.smudgeRadius.toPx())
            setFloatUniform("speck", spec.speck)
            setFloatUniform("useInk", if (spec.color == Color.Unspecified) 0f else 1f)
            setColorUniform(
                "inkColor",
                (if (spec.color == Color.Unspecified) Color.White else spec.color).toArgb(),
            )
        }
        renderEffect = RenderEffect.createRuntimeShaderEffect(shader, "content")
            .asComposeRenderEffect()
    }
}
