package com.example.feldelysium.designsystem.material

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.feldelysium.designsystem.shape.RoughShape
import com.example.feldelysium.designsystem.shape.buildInkLayers
import com.example.feldelysium.designsystem.shape.drawInk

@Immutable
data class PaperSpec(
    val base: Color,
    val scuff: Color,
    val ink: Color,

    val wear: Float = 0.35f,
    val edgeWear: Float = 0.8f,
    val edgeBand: Dp = 16.dp,
    val chipSize: Dp = 4.dp,
    val chipCoverage: Float = 0.7f,
    val innerWear: Float = 0.12f,
    val maxLoss: Float = 0.8f,
    val scuffTint: Float = 0.5f,

    val mottleSize: Dp = 30.dp,
    val mottleStrength: Float = 0.5f,
    val grainSize: Dp = 0.4.dp,
    val grainStrength: Float = 0.12f,
    val colorGrain: Float = 0.035f,
    val toothStrength: Float = 0.05f,
    val speckleStrength: Float = 0.14f,
    val edgeShade: Float = 0.08f,
    val pressShade: Float = 0.12f,

    val sinkBand: Dp = 5.dp,
    val sinkShade: Float = 0f,

    val inkWidth: Dp = 1.1.dp,
    val inkGrainSize: Dp = 18.dp,
    val inkMinAlpha: Float = 0.35f,
    val inkContrast: Float = 3.0f,

    val crackStrength: Float = 0f,
    val crackDensity: Float = 0f,
    val crackDepth: Dp = 0.dp,
    val crackWidth: Dp = 0.dp,
)

object FeldPaper {

    val Panel = PaperSpec(
        base = Color(0xFF0C1212),
        scuff = Color(0xFF1F2929),
        ink = Color(0xFF666866),

        wear = 0.22f,
        edgeWear = 0.6f,
        chipCoverage = 0.5f,
        innerWear = 0.04f,
        maxLoss = 0.5f,
        scuffTint = 0.3f,

        mottleStrength = 0.15f,
        grainStrength = 0.04f,
        colorGrain = 0.008f,
        toothStrength = 0.02f,
        speckleStrength = 0.03f,
    )

    val Field = Panel.copy(
        wear = 0.3f,
        inkWidth = 1.4.dp,
    )

    val DryButtonCream = PaperSpec(
        base = Color(0xFFDCCFB5),
        scuff = Color(0xFFB7A88D),
        ink = Color(0xFF29231D),

        wear = 0.10f,

        edgeWear = 0.85f,
        edgeBand = 7.dp,
        chipSize = 3.dp,
        chipCoverage = 0.30f,

        innerWear = 0.015f,
        maxLoss = 0.38f,
        scuffTint = 0.18f,

        mottleSize = 32.dp,
        mottleStrength = 0.035f,

        grainSize = 1.2.dp,
        grainStrength = 0.012f,
        colorGrain = 0.004f,
        toothStrength = 0.006f,
        speckleStrength = 0.008f,

        edgeShade = 0f,
        pressShade = 0f,

        sinkBand = 0.dp,
        sinkShade = 0f,

        inkWidth = 0.dp,

        crackStrength = 0.72f,
        crackDensity = 0.34f,
        crackDepth = 5.dp,
        crackWidth = 1.2.dp,
    )

    val DryButtonRust = PaperSpec(
        base = Color(0xFFE45720),
        scuff = Color(0xFF9F3D1B),
        ink = Color(0xFF241A14),

        wear = 0.10f,

        edgeWear = 0.9f,
        edgeBand = 7.dp,
        chipSize = 3.dp,
        chipCoverage = 0.32f,

        innerWear = 0.015f,
        maxLoss = 0.42f,
        scuffTint = 0.16f,

        mottleSize = 32.dp,
        mottleStrength = 0.03f,

        grainSize = 1.2.dp,
        grainStrength = 0.012f,
        colorGrain = 0.004f,
        toothStrength = 0.006f,
        speckleStrength = 0.008f,

        edgeShade = 0f,
        pressShade = 0f,

        sinkBand = 0.dp,
        sinkShade = 0f,

        inkWidth = 0.dp,

        crackStrength = 0.78f,
        crackDensity = 0.38f,
        crackDepth = 5.dp,
        crackWidth = 1.2.dp,
    )
}

fun Modifier.paperMaterial(
    shape: RoughShape,
    spec: PaperSpec,
    wear: () -> Float = { spec.wear },
    press: () -> Float = { 0f },
    time: () -> Float = { 0f },
): Modifier = drawWithCache {
    val path = shape.buildPath(size, this)
    val inkLayers = if (spec.inkWidth.value > 0f)
        shape.buildInkLayers(path, this, spec.inkWidth.toPx()) else emptyList()
    val sinkBand = spec.sinkBand.toPx()
    val seed = shape.seed.mod(97).toFloat()
    val rim = shape.rimPx(this)

    val band = spec.edgeBand.toPx()
    val chipScale = spec.chipSize.toPx()
    val mottleScale = spec.mottleSize.toPx()
    val grainScale = spec.grainSize.toPx()
    val inkGrain = spec.inkGrainSize.toPx()

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val paper = RuntimeShader(PAPER_AGSL).apply {
            setFloatUniform("size", size.width, size.height)
            setFloatUniform("seed", seed)
            setFloatUniform("rim", rim)
            setFloatUniform("edgeWear", spec.edgeWear)
            setFloatUniform("band", band)
            setFloatUniform("chipScale", chipScale)
            setFloatUniform("chipCoverage", spec.chipCoverage)
            setFloatUniform("innerWear", spec.innerWear)
            setFloatUniform("maxLoss", spec.maxLoss)
            setFloatUniform("scuffTint", spec.scuffTint)
            setFloatUniform("mottleScale", mottleScale)
            setFloatUniform("mottleStrength", spec.mottleStrength)
            setFloatUniform("grainScale", grainScale)
            setFloatUniform("grainStrength", spec.grainStrength)
            setFloatUniform("edgeShade", spec.edgeShade)
            setFloatUniform("pressShade", spec.pressShade)
            setColorUniform("baseColor", spec.base.toArgb())
            setColorUniform("scuffColor", spec.scuff.toArgb())
            setFloatUniform("colorGrain", spec.colorGrain)
            setFloatUniform("toothStrength", spec.toothStrength)
            setFloatUniform("speckleStrength", spec.speckleStrength)
            setFloatUniform("sinkBand", sinkBand)
            setFloatUniform("sinkShade", spec.sinkShade)
        }
        val ink = RuntimeShader(INK_AGSL).apply {
            setFloatUniform("seed", seed)
            setFloatUniform("grainScale", inkGrain)
            setFloatUniform("minAlpha", spec.inkMinAlpha)
            setFloatUniform("contrast", spec.inkContrast)
            setColorUniform("inkColor", spec.ink.toArgb())
        }
        val paperBrush = ShaderBrush(paper)
        val inkBrush = ShaderBrush(ink)

        onDrawWithContent {
            paper.setFloatUniform("wear", wear())
            paper.setFloatUniform("press", press())
            paper.setFloatUniform("time", time())

            clipPath(path) {
                drawRect(paperBrush)
                this@onDrawWithContent.drawContent()
            }
            drawInk(inkLayers, inkBrush)
        }
    } else {
        val inkBrush = SolidColor(spec.ink)
        onDrawWithContent {
            clipPath(path) {
                drawRect(spec.base)
                this@onDrawWithContent.drawContent()
            }
            drawInk(inkLayers, inkBrush)
        }
    }
}
