package com.feldelysium.designsystem.foundation.material

import android.graphics.BitmapShader
import android.graphics.Matrix
import android.graphics.Shader
import android.os.Build
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.GraphicsContext
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import kotlinx.coroutines.channels.Channel
import java.util.Random

internal enum class ChipTexture { Base, Filled, Rim }

internal object ChipTextures {
    const val W = 640
    const val H = 320

    private data class Key(val argb: Int, val kind: ChipTexture, val density: Float)

    private val ready = mutableStateMapOf<Key, ImageBitmap>()
    private val requested = HashSet<Key>()
    private val queue = Channel<Key>(Channel.UNLIMITED)

    fun brush(color: Color, kind: ChipTexture, density: Float, w: Int, h: Int, seed: Int): Brush {
        val bmp = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            bitmapFor(Key(color.toArgb(), kind, density))
        } else {
            null
        }
        if (bmp == null) return SolidColor(color)

        val rnd = Random(seed.toLong())
        val ox = rnd.nextInt((W - w).coerceAtLeast(1))
        val oy = rnd.nextInt((H - h).coerceAtLeast(1))
        val shader = BitmapShader(bmp.asAndroidBitmap(), Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
            .apply { setLocalMatrix(Matrix().apply { setTranslate(-ox.toFloat(), -oy.toFloat()) }) }
        return ShaderBrush(shader)
    }

    private fun bitmapFor(key: Key): ImageBitmap? = ready[key] ?: run {
        if (requested.add(key)) queue.trySend(key)
        null
    }

    fun prewarm(density: Float, base: List<Color>, filled: List<Color>, rim: List<Color>) {
        base.forEach { request(it, ChipTexture.Base, density) }
        filled.forEach { request(it, ChipTexture.Filled, density) }
        rim.forEach { request(it, ChipTexture.Rim, density) }
    }

    private fun request(c: Color, kind: ChipTexture, density: Float) {
        val key = Key(c.toArgb(), kind, density)
        if (requested.add(key)) queue.trySend(key)
    }

    suspend fun runBaker(gc: GraphicsContext) {
        for (key in queue) {
            if (!ready.containsKey(key)) ready[key] = bake(gc, key)
        }
    }

    private suspend fun bake(gc: GraphicsContext, key: Key): ImageBitmap {
        val d = Density(key.density)
        val c = Color(key.argb)
        val brush = when (key.kind) {
            ChipTexture.Base -> accentBrush(c, 0, key.density, fade = 0.5f, glow = 0.35f, grain = 0.05f)
            ChipTexture.Filled -> accentBrush(c, 1, key.density, fade = 0.9f, glow = 0.9f, grain = 0.09f)
            ChipTexture.Rim -> wornBrush(c, 2, key.density, wear = 0.45f, scale = 14f)
        }
        val layer = gc.createGraphicsLayer()
        return try {
            layer.record(d, LayoutDirection.Ltr, IntSize(W, H)) { drawRect(brush) }
            withFrameNanos { }
            layer.toImageBitmap()
        } finally {
            gc.releaseGraphicsLayer(layer)
        }
    }

    fun clear() {
        ready.clear()
        requested.clear()
    }
}
