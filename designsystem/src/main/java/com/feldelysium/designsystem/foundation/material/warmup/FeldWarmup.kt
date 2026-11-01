package com.feldelysium.designsystem.foundation.material.warmup

import android.content.ComponentCallbacks2
import android.content.res.Configuration
import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalGraphicsContext
import androidx.compose.ui.unit.dp
import com.feldelysium.designsystem.foundation.material.ChipTextures
import com.feldelysium.designsystem.foundation.material.DistressSpec
import com.feldelysium.designsystem.foundation.material.FeldMarker
import com.feldelysium.designsystem.foundation.material.FeldPaper
import com.feldelysium.designsystem.foundation.material.PaperBakeCache
import com.feldelysium.designsystem.foundation.material.agsl.DRY_PAPER_AGSL
import com.feldelysium.designsystem.foundation.material.distressMaterial
import com.feldelysium.designsystem.foundation.material.markerFilmMaterial
import com.feldelysium.designsystem.foundation.material.paperMaterial
import com.feldelysium.designsystem.foundation.shape.RoughShape
import com.feldelysium.designsystem.foundation.shape.RoughStyle
import com.feldelysium.designsystem.theme.FeldPalette

/**
 * Pre-compiles AGSL shaders and pre-bakes chip textures so the first real screen doesn't jank.
 *
 * Place it ONCE, high in the tree (e.g. next to the root theme in `setContent`). It also
 * registers memory-trim callbacks for the paper bake cache, so do not call it per screen
 *
 * The warmup box is invisible and removed after a few frames; chip baking continues in the
 * background.
 *
 * @param extraChipColors app-specific colors used by filled chips (e.g. damage-type colors),
 * baked in addition to the palette defaults.
 */
@Composable
public fun FeldWarmup(extraChipColors: List<Color> = emptyList()) {
    PaperCacheTrim()
    ChipTextureBaker(extraChipColors)

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
    ShaderWarmup()
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
private fun ShaderWarmup() {
    var done by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        repeat(WARMUP_FRAMES) { withFrameNanos { } }
        done = true
    }
    if (done) return

    val shape = remember { RoughShape(0, RoughStyle()) }
    val dry = remember {
        RuntimeShader(DRY_PAPER_AGSL).apply {
            setFloatUniform("size", 8f, 8f)
            setFloatUniform("tearScale", 1f)
            setFloatUniform("crackScale", 1f)
            setFloatUniform("grainScale", 1f)
            setFloatUniform("mottleScale", 1f)
            setColorUniform("baseColor", Color.White.toArgb())
            setColorUniform("crackColor", Color.Black.toArgb())
        }
    }

    // Near-zero alpha (not 0) so the shaders are actually drawn and compiled.
    Box(Modifier.size(8.dp).graphicsLayer { alpha = 0.01f }) {
        Box(Modifier.size(8.dp).markerFilmMaterial(FeldMarker.Chalk, seed = 0, bake = false).background(Color.White))
        Box(Modifier.size(8.dp).distressMaterial(DistressSpec(), 0).background(Color.White))
        Box(Modifier.size(8.dp).paperMaterial(shape, FeldPaper.Panel, bake = false))
        Box(Modifier.size(8.dp).drawBehind { drawRect(ShaderBrush(dry)) })
    }
}

@Composable
private fun PaperCacheTrim() {
    val context = LocalContext.current.applicationContext
    DisposableEffect(context) {
        val callbacks = object : ComponentCallbacks2 {
            override fun onTrimMemory(level: Int) {
                when {
                    level >= ComponentCallbacks2.TRIM_MEMORY_BACKGROUND -> PaperBakeCache.clear()
                    level >= ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN -> PaperBakeCache.trimHalf()
                }
            }

            override fun onConfigurationChanged(newConfig: Configuration) = Unit

            @Suppress("OVERRIDE_DEPRECATION")
            override fun onLowMemory() = PaperBakeCache.clear()
        }
        context.registerComponentCallbacks(callbacks)
        onDispose { context.unregisterComponentCallbacks(callbacks) }
    }
}

@Composable
private fun ChipTextureBaker(extraChipColors: List<Color>) {
    val gc = LocalGraphicsContext.current
    val density = LocalDensity.current.density

    LaunchedEffect(gc, density, extraChipColors) {
        ChipTextures.prewarm(
            density = density,
            base = listOf(Color(0xFF111A1A), Color(0xFF131817), Color(0xFFE45720)),
            filled = listOf(FeldPalette.Orange, FeldPalette.Teal, FeldPalette.Cream) + extraChipColors,
            rim = listOf(FeldPalette.Cream, FeldPalette.Line),
        )
        ChipTextures.runBaker(gc)
    }
}

private const val WARMUP_FRAMES = 4
