package com.feldelysium.designsystem.foundation.material

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.feldelysium.designsystem.foundation.material.agsl.STICKER_TAPE_AGSL
import kotlin.math.abs
import kotlin.math.max

@Immutable
public data class StickerTapeSpec(
    val base: Color,
    val stain: Color,
    val rim: Color,

    val tearAmp: Dp = 7.dp,
    val tearScale: Dp = 22.dp,
    val slantLeft: Dp = 14.dp,
    val slantRight: Dp = 4.dp,
    val fray: Dp = 1.8.dp,
    val rimWidth: Dp = 2.6.dp,
    val cutAmp: Dp = 0.8.dp,

    val stainScale: Dp = 46.dp,
    val stainStrength: Float = 0.30f,
    val mottleScale: Dp = 30.dp,
    val mottleStrength: Float = 0.07f,
    val fiberStrength: Float = 0.10f,
    val grainStrength: Float = 0.05f,
    val speckStrength: Float = 0.35f,
    val ageStrength: Float = 0.22f,

    val cornerRx: Dp = 44.dp,
    val cornerRy: Dp = 24.dp,
    val layerZone: Dp = 56.dp,
    val layerAmp: Dp = 6.dp,
    val layerScale: Dp = 14.dp,
    val layerStrength: Float = 0.8f,
) {
    val edgeInset: Dp get() = tearAmp + max(abs(slantLeft.value), abs(slantRight.value)).dp
}

internal object FeldTape {
    val Cream = StickerTapeSpec(
        base = Color(0xFFD9C5A1),
        stain = Color(0xFF8A7352),
        rim = Color(0xFFEFE3C8),
    )
}

internal fun Modifier.stickerTapeMaterial(spec: StickerTapeSpec, seed: Int = 0): Modifier = drawWithCache {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val shader = RuntimeShader(STICKER_TAPE_AGSL).apply {
            setFloatUniform("size", size.width, size.height)
            setFloatUniform("seed", seed.mod(97).toFloat())
            setFloatUniform("tearAmp", spec.tearAmp.toPx())
            setFloatUniform("tearScale", spec.tearScale.toPx())
            setFloatUniform("slantL", spec.slantLeft.toPx())
            setFloatUniform("slantR", spec.slantRight.toPx())
            setFloatUniform("fray", spec.fray.toPx())
            setFloatUniform("rimWidth", spec.rimWidth.toPx())
            setFloatUniform("cutAmp", spec.cutAmp.toPx())
            setFloatUniform("stainScale", spec.stainScale.toPx())
            setFloatUniform("stainStrength", spec.stainStrength)
            setFloatUniform("mottleScale", spec.mottleScale.toPx())
            setFloatUniform("mottleStrength", spec.mottleStrength)
            setFloatUniform("fiberStrength", spec.fiberStrength)
            setFloatUniform("grainStrength", spec.grainStrength)
            setFloatUniform("speckStrength", spec.speckStrength)
            setFloatUniform("ageStrength", spec.ageStrength)
            setColorUniform("baseColor", spec.base.toArgb())
            setColorUniform("stainColor", spec.stain.toArgb())
            setColorUniform("rimColor", spec.rim.toArgb())
            setFloatUniform("cornerRx", spec.cornerRx.toPx())
            setFloatUniform("cornerRy", spec.cornerRy.toPx())
            setFloatUniform("layerZone", spec.layerZone.toPx())
            setFloatUniform("layerAmp", spec.layerAmp.toPx())
            setFloatUniform("layerScale", spec.layerScale.toPx())
            setFloatUniform("layerStrength", spec.layerStrength)
        }
        val brush = ShaderBrush(shader)
        onDrawWithContent {
            drawRect(brush)
            drawContent()
        }
    } else {
        onDrawWithContent {
            drawRect(spec.base)
            drawContent()
        }
    }
}
