package com.feldelysium.designsystem.foundation.material

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
import androidx.compose.ui.util.lerp
import com.feldelysium.designsystem.foundation.material.agsl.INK_AGSL
import com.feldelysium.designsystem.foundation.material.agsl.PAPER_AGSL
import com.feldelysium.designsystem.foundation.shape.RoughShape
import com.feldelysium.designsystem.foundation.shape.buildInkLayers
import com.feldelysium.designsystem.foundation.shape.drawInk
import kotlin.math.PI
import kotlin.math.sin
import androidx.compose.ui.graphics.lerp as lerpColor

@Immutable
public data class PaperSpec(
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
    val inkMinAlpha: Float = 0.25f,
    val inkContrast: Float = 1.8f,

    val crackStrength: Float = 0f,
    val crackDensity: Float = 0f,
    val crackDepth: Dp = 0.dp,
    val crackWidth: Dp = 0.dp,
)

public object FeldPaper {

    public val Panel: PaperSpec = PaperSpec(
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

        inkWidth = 1.dp,
        inkMinAlpha = 0.65f,
        inkContrast = 1.8f,
    )

    public val Entry: PaperSpec = Panel.copy(
        wear = 0.16f,
        edgeWear = 0.5f,
        edgeBand = 10.dp,
        chipSize = 4.dp,
        chipCoverage = 0.4f,
        innerWear = 0.05f,
        maxLoss = 0.35f,
        scuffTint = 0.22f,

        mottleSize = 24.dp,
        mottleStrength = 0.15f,
        grainSize = 0.5.dp,
        grainStrength = 0.015f,
        colorGrain = 0f,
        toothStrength = 0.02f,
        speckleStrength = 0.006f,

        inkWidth = 0.9.dp,
        inkGrainSize = 26.dp,
        inkMinAlpha = 0.85f,
        inkContrast = 1.2f,
    )

    public val Field: PaperSpec = Entry.copy(
        wear = 0.2f,
        edgeWear = 0.55f,
        maxLoss = 0.4f,
        inkWidth = 1.0.dp,
        inkMinAlpha = 0.8f,
    )

    public val DryButtonCream: PaperSpec = PaperSpec(
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

    public val DryButtonRust: PaperSpec = PaperSpec(
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

    internal fun flat(color: Color, from: PaperSpec = Panel) = from.copy(
        base = color,
        scuff = color,
        wear = 0f,
        edgeWear = 0f,
        innerWear = 0f,
        maxLoss = 0f,
        scuffTint = 0f,
        mottleStrength = 0f,
        grainStrength = 0f,
        colorGrain = 0f,
        toothStrength = 0f,
        speckleStrength = 0f,
        edgeShade = 0f,
        pressShade = 0f,
        sinkShade = 0f,
    )
}

internal fun Modifier.paperMaterialMorph(
    shape: RoughShape,
    from: PaperSpec,
    to: PaperSpec,
    progress: () -> Float,
    wearBump: Float = 0.15f,
): Modifier = drawWithCache {
    val path = shape.buildPath(size, this)
    val inkLayers = if (from.inkWidth.value > 0f) {
        shape.buildInkLayers(path, this, from.inkWidth.toPx())
    } else {
        emptyList()
    }
    val seed = shape.seed.mod(97).toFloat()
    val rim = shape.rimPx(this)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val paper = RuntimeShader(PAPER_AGSL).apply {
            setFloatUniform("size", size.width, size.height)
            setFloatUniform("seed", seed)
            setFloatUniform("rim", rim)
            setFloatUniform("chipScale", lerp(from.chipSize.toPx(), to.chipSize.toPx(), 0.5f))
            setFloatUniform("mottleScale", lerp(from.mottleSize.toPx(), to.mottleSize.toPx(), 0.5f))
            setFloatUniform("grainScale", lerp(from.grainSize.toPx(), to.grainSize.toPx(), 0.5f))
            setFloatUniform("press", 0f)
            setFloatUniform("time", 0f)
        }
        val ink = RuntimeShader(INK_AGSL).apply {
            setFloatUniform("seed", seed)
            setFloatUniform("grainScale", from.inkGrainSize.toPx())
            setFloatUniform("minAlpha", from.inkMinAlpha)
            setFloatUniform("contrast", from.inkContrast)
            setColorUniform("inkColor", from.ink.toArgb())
        }
        val paperBrush = ShaderBrush(paper)
        val inkBrush = ShaderBrush(ink)

        onDrawWithContent {
            val t = progress().coerceIn(0f, 1f)
            val bump = sin(PI.toFloat() * t)

            fun f(name: String, a: Float, b: Float) = paper.setFloatUniform(name, lerp(a, b, t))

            fun px(name: String, a: Dp, b: Dp) = paper.setFloatUniform(name, lerp(a.toPx(), b.toPx(), t))

            f("wear", from.wear, to.wear)
            paper.setFloatUniform("wear", lerp(from.wear, to.wear, t) + bump * wearBump)
            f("edgeWear", from.edgeWear, to.edgeWear)
            px("band", from.edgeBand, to.edgeBand)
            f("chipCoverage", from.chipCoverage, to.chipCoverage)
            f("innerWear", from.innerWear, to.innerWear)
            f("maxLoss", from.maxLoss, to.maxLoss)
            f("scuffTint", from.scuffTint, to.scuffTint)
            f("mottleStrength", from.mottleStrength, to.mottleStrength)
            f("grainStrength", from.grainStrength, to.grainStrength)
            f("colorGrain", from.colorGrain, to.colorGrain)
            f("toothStrength", from.toothStrength, to.toothStrength)
            f("speckleStrength", from.speckleStrength, to.speckleStrength)
            f("edgeShade", from.edgeShade, to.edgeShade)
            f("pressShade", from.pressShade, to.pressShade)
            px("sinkBand", from.sinkBand, to.sinkBand)
            f("sinkShade", from.sinkShade, to.sinkShade)
            paper.setColorUniform("baseColor", lerpColor(from.base, to.base, t).toArgb())
            paper.setColorUniform("scuffColor", lerpColor(from.scuff, to.scuff, t).toArgb())

            clipPath(path) {
                drawRect(paperBrush)
                this@onDrawWithContent.drawContent()
            }
            drawInk(inkLayers, inkBrush, alpha = 1f - t)
        }
    } else {
        val inkBrush = SolidColor(from.ink)
        onDrawWithContent {
            val t = progress().coerceIn(0f, 1f)
            clipPath(path) {
                drawRect(lerpColor(from.base, to.base, t))
                this@onDrawWithContent.drawContent()
            }
            drawInk(inkLayers, inkBrush, alpha = 1f - t)
        }
    }
}
