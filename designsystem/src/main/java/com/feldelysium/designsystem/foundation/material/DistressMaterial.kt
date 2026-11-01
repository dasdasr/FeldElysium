package com.feldelysium.designsystem.foundation.material

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.feldelysium.designsystem.foundation.material.agsl.DISTRESS_AGSL

@Immutable
public data class DistressSpec(
    val color: Color = Color.Unspecified,
    val strength: Float = 1f,

    val edgeSoftness: Dp = 0.6.dp,
    val edgeRough: Dp = 0.35.dp,
    val edgeSize: Dp = 2.dp,
    val edgeErode: Float = 0.45f,
    val bleed: Float = 0.12f,

    val patchy: Float = 0.2f,
    val patchSize: Dp = 38.dp,

    val dropout: Float = 0.7f,
    val dropoutSize: Dp = 8.dp,

    val scratch: Float = 0.8f,
    val scratchLength: Dp = 60.dp,
    val scratchWidth: Dp = 0.5.dp,
    val scratchAngle: Float = -14f,

    val speck: Float = 0.6f,
    val speckSize: Dp = 3.dp,
    val grain: Float = 0.35f,
    val grainSize: Dp = 0.7.dp,

    val splatter: Float = 0.3f,
    val splatterRadius: Dp = 2.dp,
) {
    internal fun scaled(amount: Float = 1f, size: Float = 1f, strength: Float = this.strength): DistressSpec = copy(
        strength = strength,
        dropout = dropout * amount,
        scratch = scratch * amount,
        speck = speck * amount,
        grain = grain * amount,
        edgeSize = edgeSize * size,
        dropoutSize = dropoutSize * size,
        speckSize = speckSize * size,
        scratchLength = scratchLength * size,
        grainSize = grainSize * size,
    )
}

internal fun Modifier.distressMaterial(spec: DistressSpec, seed: Int = 0, bakeKey: Any? = null): Modifier =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        if (bakeKey != null) {
            distressBaked(spec, seed, bakeKey)
        } else {
            distressShader(spec, seed)
        }
    } else {
        this
    }

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
private fun Modifier.distressShader(spec: DistressSpec, seed: Int): Modifier = composed {
    val shader = remember { RuntimeShader(DISTRESS_AGSL) }
    Modifier.graphicsLayer {
        if (size.width <= 0f || size.height <= 0f) return@graphicsLayer
        shader.applyDistressUniforms(spec, seed, this@graphicsLayer)
        renderEffect = RenderEffect.createRuntimeShaderEffect(shader, "content")
            .asComposeRenderEffect()
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
private fun Modifier.distressBaked(spec: DistressSpec, seed: Int, bakeKey: Any): Modifier = composed {
    val shader = remember { RuntimeShader(DISTRESS_AGSL) }
    val layer = rememberGraphicsLayer()
    val density = LocalDensity.current

    var layerSize by remember {
        mutableStateOf(IntSize.Zero)
    }

    var baked by remember(spec, seed, bakeKey, density, layerSize) {
        mutableStateOf<ImageBitmap?>(null)
    }

    var recorded by remember(spec, seed, bakeKey, density, layerSize) {
        mutableStateOf(false)
    }

    LaunchedEffect(spec, seed, bakeKey, density, layerSize, recorded) {
        if (layerSize == IntSize.Zero || !recorded || baked != null) {
            return@LaunchedEffect
        }

        withFrameNanos { }
        baked = layer.toImageBitmap()
    }

    Modifier
        .onSizeChanged { newSize ->
            if (layerSize != newSize) {
                layerSize = newSize
            }
        }
        .drawWithContent {
            val ready = baked

            if (
                ready != null &&
                ready.width == size.width.toInt() &&
                ready.height == size.height.toInt()
            ) {
                drawImage(ready)
                return@drawWithContent
            }

            if (size.width <= 0f || size.height <= 0f) {
                return@drawWithContent
            }

            shader.applyDistressUniforms(spec, seed, this)

            layer.renderEffect = RenderEffect
                .createRuntimeShaderEffect(shader, "content")
                .asComposeRenderEffect()

            layer.record {
                this@drawWithContent.drawContent()
            }

            drawLayer(layer)

            if (!recorded) {
                recorded = true
            }
        }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
private fun RuntimeShader.applyDistressUniforms(spec: DistressSpec, seed: Int, density: Density) {
    with(density) {
        setFloatUniform("seed", seed.mod(97).toFloat())
        setFloatUniform("strength", spec.strength)
        setFloatUniform("edgeSoft", spec.edgeSoftness.toPx())
        setFloatUniform("edgeRough", spec.edgeRough.toPx())
        setFloatUniform("edgeScale", spec.edgeSize.toPx())
        setFloatUniform("edgeErode", spec.edgeErode)
        setFloatUniform("bleed", spec.bleed)
        setFloatUniform("patchy", spec.patchy)
        setFloatUniform("patchScale", spec.patchSize.toPx())
        setFloatUniform("dropout", spec.dropout)
        setFloatUniform("dropoutScale", spec.dropoutSize.toPx())
        setFloatUniform("scratch", spec.scratch)
        setFloatUniform("scratchLen", spec.scratchLength.toPx())
        setFloatUniform("scratchWid", spec.scratchWidth.toPx())
        setFloatUniform(
            "scratchAngle",
            Math.toRadians(spec.scratchAngle.toDouble()).toFloat(),
        )
        setFloatUniform("speck", spec.speck)
        setFloatUniform("speckScale", spec.speckSize.toPx())
        setFloatUniform("grain", spec.grain)
        setFloatUniform("grainScale", spec.grainSize.toPx())
        setFloatUniform("splatter", spec.splatter)
        setFloatUniform("splatterRadius", spec.splatterRadius.toPx())

        val hasColor = spec.color != Color.Unspecified
        setFloatUniform("useInk", if (hasColor) 1f else 0f)
        setColorUniform(
            "inkColor",
            (if (hasColor) spec.color else Color.White).toArgb(),
        )
    }
}
