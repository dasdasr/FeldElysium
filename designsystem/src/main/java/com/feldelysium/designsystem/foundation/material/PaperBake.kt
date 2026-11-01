package com.feldelysium.designsystem.foundation.material

import android.graphics.RuntimeShader
import android.os.Build
import android.util.LruCache
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.feldelysium.designsystem.foundation.decal.DecalLayer
import com.feldelysium.designsystem.foundation.decal.DecalPlan
import com.feldelysium.designsystem.foundation.decal.drawDecals
import com.feldelysium.designsystem.foundation.material.agsl.INK_AGSL
import com.feldelysium.designsystem.foundation.material.agsl.PAPER_AGSL
import com.feldelysium.designsystem.foundation.shape.InkLayer
import com.feldelysium.designsystem.foundation.shape.RoughCache
import com.feldelysium.designsystem.foundation.shape.RoughShape
import com.feldelysium.designsystem.foundation.shape.RoughStyle
import com.feldelysium.designsystem.foundation.shape.drawInk
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

public fun Modifier.paperSurface(
    seed: Int,
    rough: RoughStyle = RoughStyle(),
    spec: PaperSpec,
    textured: Boolean = true,
): Modifier = composed {
    val shape = remember(seed, rough) { RoughShape(seed, rough) }
    paperMaterial(shape, spec, textured = textured)
}

internal fun Modifier.paperMaterial(
    shape: RoughShape,
    spec: PaperSpec,
    wear: Float = spec.wear,
    press: () -> Float = { 0f },
    decals: DecalLayer? = null,
    decalKey: Any? = decals,
    bake: Boolean = true,
    textured: Boolean = true,
): Modifier = when {
    !textured || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU -> paperFlat(shape, spec, decals)
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
    val geo = RoughCache.get(
        shape = shape,
        size = size,
        d = d,
        inkWidthPx = with(d) { spec.inkWidth.toPx() },
    )
    val path = geo.path
    val ink = geo.ink
    val placed = decals?.plan(size, d) ?: DecalPlan(emptyList(), null)
    val seed = shape.seed.mod(97).toFloat()
    val rim = shape.rimPx(d)

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

    return PaperArt(
        path = path,
        placed = placed,
        ink = ink,
        paperBrush = ShaderBrush(paper),
        inkBrush = ShaderBrush(inkShader),
    )
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

private const val SETTLE_MS = 120L

private class BakeHolder {
    var last: ImageBitmap? = null
    var job: Job? = null
    var tick by mutableIntStateOf(0)
}

@Suppress("CyclomaticComplexMethod")
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
    val holder = remember { BakeHolder() }

    Modifier.drawWithCache {
        val d = Density(density, fontScale)
        val ld = layoutDirection
        val w = size.width.roundToInt()
        val h = size.height.roundToInt()
        val drawSize = size
        val key = PaperBakeKey(shape.seed, shape.style, spec, wear, w, h, density, decalKey)

        holder.job?.cancel()
        var ready: ImageBitmap? = if (w > 0 && h > 0) PaperBakeCache.get(key) else null
        ready?.let { holder.last = it }
        var recordedInline = false
        var scheduled = false

        fun launchBake(settleMs: Long) {
            holder.job = scope.launch {
                if (settleMs > 0) delay(settleMs)
                if (!recordedInline) {
                    val art = buildArt(d, drawSize, shape, spec, wear, decals)
                    layer.record(d, ld, IntSize(w, h)) { drawArt(art) }
                }
                withFrameNanos { }
                val bmp = layer.toImageBitmap()
                PaperBakeCache.put(key, bmp)
                holder.last = bmp
                holder.tick++
            }
        }

        onDrawWithContent {
            holder.tick
            if (ready == null && w > 0 && h > 0) ready = PaperBakeCache.get(key)
            val last = holder.last
            val filter = pressFilter(spec.pressShade * press())
            when {
                ready != null -> drawImage(ready!!, dstSize = IntSize(w, h), colorFilter = filter)

                last != null -> {
                    drawImage(last, dstSize = IntSize(w, h), colorFilter = filter)
                    if (!scheduled) {
                        scheduled = true
                        launchBake(SETTLE_MS)
                    }
                }

                w > 0 && h > 0 -> {
                    if (!recordedInline) {
                        val art = buildArt(d, drawSize, shape, spec, wear, decals)
                        layer.record(d, ld, IntSize(w, h)) { drawArt(art) }
                        recordedInline = true
                    }
                    drawLayer(layer)
                    if (!scheduled) {
                        scheduled = true
                        launchBake(0)
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
    val geo = RoughCache.get(shape, size, this, spec.inkWidth.toPx())
    val placed = decals?.plan(size, this) ?: DecalPlan(emptyList(), null)
    val inkBrush = SolidColor(spec.ink)
    onDrawWithContent {
        clipPath(geo.path) {
            drawRect(spec.base)
            drawDecals(placed)
            this@onDrawWithContent.drawContent()
        }
        drawInk(geo.ink, inkBrush)
    }
}
