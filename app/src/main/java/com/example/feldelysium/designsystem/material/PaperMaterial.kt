package com.example.feldelysium.designsystem.material

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.feldelysium.designsystem.shape.RoughShape
import com.example.feldelysium.theme.DiscoPrimary
import com.example.feldelysium.theme.DiscoPrimaryPressed

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

    val inkWidth: Dp = 1.5.dp,
    val inkGrainSize: Dp = 4.dp,
    val inkPatchSize: Dp = 36.dp,
    val inkMinAlpha: Float = 0.14f,
    val inkContrast: Float = 3.0f,
    val inkPatchWeight: Float = 0.55f,
)

object FeldPaper {
    val Panel = PaperSpec(
        base = Color(0xFF0C1212),
        scuff = Color(0xFF4A5757),
        ink = Color(0xFF7C7C7A),
    )
    val Field = Panel.copy(wear = 0.3f, inkWidth = 1.4.dp)
    val Cream = PaperSpec(
        base = Color(0xFFD9CDB4),
        scuff = Color(0xFF8B7B5F),
        ink = Color(0xFF3A3024),
        wear = 0.45f,
    )
    val Ember = PaperSpec(
        base = DiscoPrimary,
        scuff = Color(0xFFFFB27A),
        ink = DiscoPrimaryPressed,
        wear = 0.5f,
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
    val inkPath = shape.buildInkPath(path, this, spec.inkWidth.toPx())
    val seed = shape.seed.mod(97).toFloat()
    val rim = shape.rimPx(this)

    val band = spec.edgeBand.toPx()
    val chipScale = spec.chipSize.toPx()
    val mottleScale = spec.mottleSize.toPx()
    val grainScale = spec.grainSize.toPx()
    val inkGrain = spec.inkGrainSize.toPx()
    val inkPatch = spec.inkPatchSize.toPx()

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
        }
        val ink = RuntimeShader(INK_AGSL).apply {
            setFloatUniform("seed", seed)
            setFloatUniform("grainScale", inkGrain)
            setFloatUniform("patchScale", inkPatch)
            setFloatUniform("patchWeight", spec.inkPatchWeight)
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
            ink.setFloatUniform("time", time())

            clipPath(path) {
                drawRect(paperBrush)
                this@onDrawWithContent.drawContent()
            }
            drawPath(inkPath, inkBrush)
        }
    } else {
        // TODO: Baked Material fallback. For now: flat fill + solid outline.
        onDrawWithContent {
            clipPath(path) {
                drawRect(spec.base)
                this@onDrawWithContent.drawContent()
            }
            drawPath(inkPath, spec.ink)
        }
    }
}
