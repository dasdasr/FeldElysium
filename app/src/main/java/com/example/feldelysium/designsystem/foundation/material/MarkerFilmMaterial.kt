package com.example.feldelysium.designsystem.foundation.material

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.feldelysium.designsystem.foundation.material.agsl.MARKER_FILM_AGSL
import com.example.feldelysium.designsystem.foundation.seed.rememberSeed

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

@Composable
fun Modifier.markerFilmMaterial(
    spec: MarkerFilmSpec = FeldMarker.Chalk,
    seed: Int = rememberSeed(),
    bakeKey: Any? = null,
    bake: Boolean = true,
): Modifier =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) markerFilmApi33(spec, seed, bakeKey, bake)
    else this

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
private fun Modifier.markerFilmApi33(
    spec: MarkerFilmSpec, seed: Int, bakeKey: Any?, bake: Boolean,
): Modifier {
    val density = LocalDensity.current
    val shader = remember { RuntimeShader(MARKER_FILM_AGSL) }
    val effect = remember(spec, seed, density) {
        shader.apply {
            with(density) {
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
            }
            val has = spec.color != Color.Unspecified
            setFloatUniform("useInk", if (has) 1f else 0f)
            setColorUniform("inkColor", (if (has) spec.color else Color.White).toArgb())
        }
        RenderEffect.createRuntimeShaderEffect(shader, "content").asComposeRenderEffect()
    }

    val layer = rememberGraphicsLayer()
    var px by remember { mutableStateOf(IntSize.Zero) }
    var baked by remember(effect, bakeKey) { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(effect, bakeKey, px, bake) {
        baked = null
        if (!bake || px == IntSize.Zero) return@LaunchedEffect
        withFrameNanos { }
        withFrameNanos { }
        baked = try { layer.toImageBitmap() } catch (_: Throwable) { null }
    }

    return this
        .onSizeChanged { px = it }
        .drawWithContent {
            val b = baked
            if (b != null) {
                drawImage(b)
            } else {
                layer.record { this@drawWithContent.drawContent() }
                layer.renderEffect = effect
                drawLayer(layer)
            }
        }
}
