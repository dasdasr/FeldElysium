package com.example.feldelysium.designsystem.foundation.material

import android.content.ComponentCallbacks2
import android.content.res.Configuration
import android.graphics.RuntimeShader
import android.os.Build
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalGraphicsContext
import androidx.compose.ui.unit.dp
import com.example.feldelysium.designsystem.foundation.material.agsl.DRY_PAPER_AGSL
import com.example.feldelysium.designsystem.foundation.shape.RoughShape
import com.example.feldelysium.designsystem.foundation.shape.RoughStyle
import com.example.feldelysium.designsystem.components.input.FeldPalette
import com.example.feldelysium.domain.model.DamageType
import com.example.feldelysium.presentation.playback.components.CheckAssets

@Composable
fun FeldWarmup() {
    PaperCacheTrim()
    ChipTextureBaker()

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    var done by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        repeat(4) { withFrameNanos { } }
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
            setColorUniform("baseColor", Color.White.hashCode().let { android.graphics.Color.WHITE })
            setColorUniform("crackColor", android.graphics.Color.BLACK)
        }
    }

    Box(
        Modifier
            .size(8.dp)
            .graphicsLayer { alpha = 0.01f },
    ) {
        Box(Modifier
            .size(8.dp)
            .markerFilmMaterial(FeldMarker.Chalk, seed = 0, bake = false)
            .background(Color.White))
        Box(Modifier
            .size(8.dp)
            .distressMaterial(DistressSpec(), 0)
            .background(Color.White))
        Box(Modifier
            .size(8.dp)
            .paperMaterial(shape, FeldPaper.Panel, bake = false))
        Box(Modifier
            .size(8.dp)
            .drawBehind { drawRect(ShaderBrush(dry)) })
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
private fun ChipTextureBaker() {
    val gc = LocalGraphicsContext.current
    val density = LocalDensity.current.density

    val damageColors = DamageType.entries.map { CheckAssets.DamageColor(it) }

    LaunchedEffect(gc, density, damageColors) {
        ChipTextures.prewarm(
            density = density,
            base = listOf(Color(0xFF111A1A), Color(0xFF131817), Color(0xFFE45720)),
            filled = listOf(FeldPalette.Orange, FeldPalette.Teal, FeldPalette.Cream) + damageColors,
            rim = listOf(FeldPalette.Cream, FeldPalette.Line),
        )
        ChipTextures.runBaker(gc)
    }
}
