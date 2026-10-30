package com.feldelysium.designsystem.foundation.material

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import android.util.LruCache
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
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.feldelysium.designsystem.foundation.material.agsl.MARKER_FILM_AGSL
import kotlinx.coroutines.delay
import androidx.compose.ui.graphics.RenderEffect as ComposeRenderEffect

@Immutable
public data class MarkerFilmSpec(
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

public object FeldMarker {
    public val Chalk: MarkerFilmSpec = MarkerFilmSpec(color = Color(0xFFEDE9DD))

    public val Ink: MarkerFilmSpec = MarkerFilmSpec(
        color = Color(0xFF14110F),
        coverage = 0.96f,
        unevenness = 0.10f,
        streak = 0.08f,
        skip = 0.25f,
        grain = 0.06f,
        smudge = 0.10f,
        speck = 0.3f,
    )

    public val Dry: MarkerFilmSpec = Chalk.copy(
        coverage = 0.72f,
        unevenness = 0.38f,
        erode = 0.8f,
        streak = 0.30f,
        skip = 0.9f,
        skipCoverage = 0.6f,
        pinhole = 1f,
    )
}

private object MarkerShader {
    val shader: RuntimeShader? by lazy(LazyThreadSafetyMode.NONE) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            RuntimeShader(MARKER_FILM_AGSL)
        } else {
            null
        }
    }
}

private data class BakeKey(
    val spec: MarkerFilmSpec,
    val seed: Int,
    val w: Int,
    val h: Int,
    val density: Float,
    val key: Any?,
)

private object BakeCache {
    private val lru = object : LruCache<BakeKey, ImageBitmap>(12 * 1024 * 1024) {
        override fun sizeOf(key: BakeKey, value: ImageBitmap) = value.width * value.height * 4
    }

    fun get(k: BakeKey): ImageBitmap? = lru.get(k)
    fun put(k: BakeKey, v: ImageBitmap) {
        lru.put(k, v)
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
private fun createMarkerEffect(spec: MarkerFilmSpec, seed: Int, density: Density): ComposeRenderEffect {
    val s = MarkerShader.shader ?: error("RuntimeShader unavailable on API 33+")
    with(density) {
        s.setFloatUniform("seed", seed.mod(97).toFloat())
        s.setFloatUniform("coverage", spec.coverage)
        s.setFloatUniform("unevenness", spec.unevenness)
        s.setFloatUniform("blotchScale", spec.blotchSize.toPx())
        s.setFloatUniform("poolEdge", spec.poolEdge)
        s.setFloatUniform("edgeSoft", spec.edgeSoftness.toPx())
        s.setFloatUniform("erode", spec.erode)
        s.setFloatUniform("erodeScale", spec.erodeSize.toPx())
        s.setFloatUniform("wobble", spec.wobble.toPx())
        s.setFloatUniform("streak", spec.streak)
        s.setFloatUniform("streakLen", spec.streakLength.toPx())
        s.setFloatUniform("streakWid", spec.streakWidth.toPx())
        s.setFloatUniform("streakAngle", Math.toRadians(spec.streakAngle.toDouble()).toFloat())
        s.setFloatUniform("skip", spec.skip)
        s.setFloatUniform("skipCoverage", spec.skipCoverage)
        s.setFloatUniform("skipScale", spec.skipSize.toPx())
        s.setFloatUniform("pinhole", spec.pinhole)
        s.setFloatUniform("grain", spec.grain)
        s.setFloatUniform("grainScale", spec.grainSize.toPx())
        s.setFloatUniform("smudge", spec.smudge)
        s.setFloatUniform("smudgeScale", spec.smudgeSize.toPx())
        s.setFloatUniform("smudgeRadius", spec.smudgeRadius.toPx())
        s.setFloatUniform("speck", spec.speck)
    }
    val has = spec.color != Color.Unspecified
    s.setFloatUniform("useInk", if (has) 1f else 0f)
    s.setColorUniform("inkColor", (if (has) spec.color else Color.White).toArgb())
    return RenderEffect.createRuntimeShaderEffect(s, "content").asComposeRenderEffect()
}

@Composable
public fun Modifier.markerFilmMaterial(
    spec: MarkerFilmSpec = FeldMarker.Chalk,
    seed: Int = 0,
    bakeKey: Any? = null,
    bake: Boolean = true,
): Modifier = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    markerFilmApi33(
        spec,
        seed,
        bakeKey,
        bake,
    )
} else {
    this
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
private fun Modifier.markerFilmApi33(spec: MarkerFilmSpec, seed: Int, bakeKey: Any?, bake: Boolean): Modifier {
    val density = LocalDensity.current
    val effect = remember(spec, seed, density.density) { createMarkerEffect(spec, seed, density) }
    val layer = rememberGraphicsLayer()
    var px by remember { mutableStateOf(IntSize.Zero) }

    val key = remember(spec, seed, px, density.density, bakeKey) {
        if (px == IntSize.Zero) {
            null
        } else {
            BakeKey(
                spec,
                seed,
                px.width,
                px.height,
                density.density,
                bakeKey,
            )
        }
    }
    var baked by remember(key) { mutableStateOf(key?.let { BakeCache.get(it) }) }

    LaunchedEffect(key, bake) {
        if (!bake || key == null || baked != null) return@LaunchedEffect
        delay(220)
        withFrameNanos { }
        withFrameNanos { }
        val bmp = runCatching { layer.toImageBitmap() }.getOrNull() ?: return@LaunchedEffect
        BakeCache.put(key, bmp)
        baked = bmp
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
