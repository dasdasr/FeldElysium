package com.example.feldelysium.designsystem.foundation.material

import android.graphics.RuntimeShader
import android.os.Build
import android.util.LruCache
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import com.example.feldelysium.designsystem.foundation.decal.DecalLayer
import com.example.feldelysium.designsystem.foundation.decal.DecalPlan
import com.example.feldelysium.designsystem.foundation.decal.drawDecals
import com.example.feldelysium.designsystem.foundation.material.agsl.INK_AGSL
import com.example.feldelysium.designsystem.foundation.material.agsl.PAPER_AGSL
import com.example.feldelysium.designsystem.foundation.shape.InkLayer
import com.example.feldelysium.designsystem.foundation.shape.RoughShape
import com.example.feldelysium.designsystem.foundation.shape.RoughStyle
import com.example.feldelysium.designsystem.foundation.shape.buildInkLayers
import com.example.feldelysium.designsystem.foundation.shape.drawInk
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

fun Modifier.paperMaterial(
    shape: RoughShape,
    spec: PaperSpec,
    wear: Float = spec.wear,
    press: () -> Float = { 0f },
    decals: DecalLayer? = null,
    decalKey: Any? = decals,
    bake: Boolean = true,
): Modifier = when {
    Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU -> paperFlat(shape, spec, decals)
    bake -> paperBaked(shape, spec, wear, press, decals, decalKey)
    else -> paperLive(shape, spec, wear, decals)
}

internal data class PaperBakeKey(
    val seed: Int,
    val rough: RoughStyle,
    val spec: PaperSpec,
    val wear: Float,
    val width: Int,
    val height: Int,
    val density: Float,
    val decals: Any?,
)

internal object PaperBakeCache {
    private val maxBytes = (Runtime.getRuntime().maxMemory() / 16).coerceAtMost(32L * 1024 * 1024).toInt()

    private val lru = object : LruCache<PaperBakeKey, ImageBitmap>(maxBytes) {
        override fun sizeOf(key: PaperBakeKey, value: ImageBitmap): Int =
            (value.width * value.height * 4).coerceAtLeast(1)
    }

    fun get(key: PaperBakeKey): ImageBitmap? = lru.get(key)
    fun put(key: PaperBakeKey, bitmap: ImageBitmap) {
        lru.put(key, bitmap)
    }

    fun trimHalf() = lru.trimToSize(maxBytes / 2)
    fun clear() = lru.evictAll()
}

private class PaperArt(
    val path: Path,
    val placed: DecalPlan,
    val ink: List<InkLayer>,
    val paperBrush: ShaderBrush,
    val inkBrush: ShaderBrush,
)

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
private fun buildArt(
    d: Density,
    size: Size,
    shape: RoughShape,
    spec: PaperSpec,
    wear: Float,
    decals: DecalLayer?,
): PaperArt {
    val path = shape.buildPath(size, d)
    val placed = decals?.plan(size, d) ?: DecalPlan(emptyList(), null)
    val seed = shape.seed.mod(97).toFloat()
    val rim = shape.rimPx(d)

    val ink = with(d) {
        if (spec.inkWidth.value > 0f) shape.buildInkLayers(path, d, spec.inkWidth.toPx()) else emptyList()
    }

    val paper = RuntimeShader(PAPER_AGSL)
    val inkShader = RuntimeShader(INK_AGSL)
    with(d) {
        paper.apply {
            setFloatUniform("size", size.width, size.height)
            setFloatUniform("seed", seed)
            setFloatUniform("rim", rim)
            setFloatUniform("wear", wear)
            setFloatUniform("edgeWear", spec.edgeWear)
            setFloatUniform("band", spec.edgeBand.toPx())
            setFloatUniform("chipScale", spec.chipSize.toPx())
            setFloatUniform("chipCoverage", spec.chipCoverage)
            setFloatUniform("innerWear", spec.innerWear)
            setFloatUniform("maxLoss", spec.maxLoss)
            setFloatUniform("scuffTint", spec.scuffTint)
            setFloatUniform("mottleScale", spec.mottleSize.toPx())
            setFloatUniform("mottleStrength", spec.mottleStrength)
            setFloatUniform("grainScale", spec.grainSize.toPx())
            setFloatUniform("grainStrength", spec.grainStrength)
            setFloatUniform("colorGrain", spec.colorGrain)
            setFloatUniform("toothStrength", spec.toothStrength)
            setFloatUniform("speckleStrength", spec.speckleStrength)
            setFloatUniform("edgeShade", spec.edgeShade)
            setFloatUniform("sinkBand", spec.sinkBand.toPx())
            setFloatUniform("sinkShade", spec.sinkShade)
            setFloatUniform("press", 0f)
            setFloatUniform("pressShade", 0f)
            setFloatUniform("time", 0f)
            setColorUniform("baseColor", spec.base.toArgb())
            setColorUniform("scuffColor", spec.scuff.toArgb())
        }
        inkShader.apply {
            setFloatUniform("seed", seed)
            setFloatUniform("grainScale", spec.inkGrainSize.toPx())
            setFloatUniform("minAlpha", spec.inkMinAlpha)
            setFloatUniform("contrast", spec.inkContrast)
            setColorUniform("inkColor", spec.ink.toArgb())
        }
    }
    return PaperArt(path, placed, ink, ShaderBrush(paper), ShaderBrush(inkShader))
}

private fun DrawScope.drawArt(a: PaperArt) {
    clipPath(a.path) {
        drawRect(a.paperBrush)
        drawDecals(a.placed)
    }
    drawInk(a.ink, a.inkBrush)
}

private fun pressFilter(k: Float): ColorFilter? {
    if (k <= 0.001f) return null
    val s = 1f - k
    return ColorFilter.colorMatrix(
        ColorMatrix(
            floatArrayOf(
                s, 0f, 0f, 0f, 0f,
                0f, s, 0f, 0f, 0f,
                0f, 0f, s, 0f, 0f,
                0f, 0f, 0f, 1f, 0f,
            ),
        ),
    )
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
private fun Modifier.paperBaked(
    shape: RoughShape,
    spec: PaperSpec,
    wear: Float,
    press: () -> Float,
    decals: DecalLayer?,
    decalKey: Any?,
): Modifier = composed {
    val layer = rememberGraphicsLayer()
    val scope = rememberCoroutineScope()
    val epoch = remember { IntArray(1) }

    Modifier.drawWithCache {
        val d = Density(density, fontScale)
        val ld = layoutDirection
        val w = size.width.roundToInt()
        val h = size.height.roundToInt()
        val drawSize = size
        val myEpoch = ++epoch[0]
        val key = PaperBakeKey(shape.seed, shape.style, spec, wear, w, h, density, decalKey)

        val bitmap = mutableStateOf(if (w > 0 && h > 0) PaperBakeCache.get(key) else null)
        val art by lazy(LazyThreadSafetyMode.NONE) { buildArt(d, drawSize, shape, spec, wear, decals) }
        var recorded = false
        var requested = false

        onDrawWithContent {
            val ready = bitmap.value
            if (ready != null) {
                drawImage(
                    image = ready,
                    dstSize = IntSize(w, h),
                    colorFilter = pressFilter(spec.pressShade * press()),
                )
            } else if (w > 0 && h > 0) {
                if (!recorded) {
                    layer.record(d, ld, IntSize(w, h)) { drawArt(art) }
                    recorded = true
                }
                drawLayer(layer)
                if (!requested) {
                    requested = true
                    scope.launch {
                        withFrameNanos { }
                        val bmp = layer.toImageBitmap()
                        if (epoch[0] == myEpoch) {
                            PaperBakeCache.put(key, bmp)
                            bitmap.value = bmp
                        }
                    }
                }
            }
            drawContent()
        }
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
private fun Modifier.paperLive(shape: RoughShape, spec: PaperSpec, wear: Float, decals: DecalLayer?): Modifier =
    drawWithCache {
        val art = buildArt(Density(density, fontScale), size, shape, spec, wear, decals)
        onDrawWithContent {
            drawArt(art)
            drawContent()
        }
    }

private fun Modifier.paperFlat(shape: RoughShape, spec: PaperSpec, decals: DecalLayer?): Modifier = drawWithCache {
    val path = shape.buildPath(size, this)
    val placed = decals?.plan(size, this) ?: DecalPlan(emptyList(), null)
    val inkLayers = if (spec.inkWidth.value > 0f) {
        shape.buildInkLayers(path, this, spec.inkWidth.toPx())
    } else {
        emptyList()
    }
    val inkBrush = SolidColor(spec.ink)
    onDrawWithContent {
        clipPath(path) {
            drawRect(spec.base)
            drawDecals(placed)
            this@onDrawWithContent.drawContent()
        }
        drawInk(inkLayers, inkBrush)
    }
}
