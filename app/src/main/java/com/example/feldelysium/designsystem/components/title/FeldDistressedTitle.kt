package com.example.feldelysium.designsystem.components

import androidx.compose.ui.geometry.Rect
import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.Immutable
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import com.example.feldelysium.designsystem.material.FeldTape
import com.example.feldelysium.designsystem.material.agsl.NOISE_LIB
import com.example.feldelysium.designsystem.material.stickerTapeMaterial
import com.example.feldelysium.designsystem.seed.rememberSeed
import com.example.feldelysium.theme.DiscoDialogueTheme

@Composable
fun FeldDistressedTitle(
    text: String,
    modifier: Modifier = Modifier,
    seed: Int = rememberSeed(),
    spec: DistressedTitleSpec = FeldDistressedTitles.Small,
    textStyle: TextStyle = MaterialTheme.typography.titleLarge,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    showOutline: Boolean = false,
    roughness: Float = 1f,
    roughnessSize: Float = 1f,
    strength: Float = 1f,
) {
    val style = textStyle.copy(color = spec.color)
    val strokePx = with(LocalDensity.current) { spec.strokeWidth.toPx() }
    val upper = text.uppercase()

    val adjustedSpec = spec.copy(
        chipSize = spec.chipSize * roughnessSize,
        scratchScale = spec.scratchScale * roughnessSize,
        grainSize = spec.grainSize * roughnessSize,

        chipCoverage = spec.chipCoverage * roughness,
        scratchStrength = spec.scratchStrength * roughness,
        scuffStrength = spec.scuffStrength * roughness,
        speckStrength = spec.speckStrength * roughness,
        grainStrength = spec.grainStrength * roughness,

        strength = strength,
    )

    Box(modifier) {
        Text(
            text = upper,
            style = style,
            textAlign = textAlign,
            maxLines = maxLines,
            overflow = overflow,
            modifier = Modifier.erosionMask(
                adjustedSpec,
                seed,
                stroke = false,
            ),
        )

        Box(
            Modifier.erosionMask(
                adjustedSpec,
                seed,
                stroke = true,
            )
        ) {
            if (showOutline) {
                Text(
                    text = upper,
                    maxLines = maxLines,
                    overflow = overflow,
                    style = style.copy(
                        drawStyle = Stroke(
                            width = strokePx * 2f,
                            join = StrokeJoin.Round,
                        ),
                    ),
                    textAlign = textAlign,
                )
            }

            Text(
                text = upper,
                maxLines = maxLines,
                overflow = overflow,
                style = textStyle.copy(color = Color.Black),
                textAlign = textAlign,
                modifier = Modifier.clipToGlyphs(),
            )
        }
    }
}

@Immutable
data class DistressedTitleSpec(
    val color: Color,
    val edgeBand: Dp = 14.dp,
    val edgeBias: Float = 0.55f,
    val chipSize: Dp = 7.dp,
    val chipCoverage: Float = 0.5f,
    val scratchScale: Dp = 70.dp,
    val scratchStrength: Float = 0.85f,
    val scuffStrength: Float = 0.8f,
    val speckStrength: Float = 0.9f,
    val grainSize: Dp = 0.8.dp,
    val grainStrength: Float = 0.25f,
    val strength: Float = 1f,
    val strokeWidth: Dp = 1.1.dp,
    val strokeBreak: Float = 0.45f,
    val strokeWear: Float = 0.6f,
)

object FeldDistressedTitles {

    val Small = DistressedTitleSpec(
        color = Color(0xFF1B1612),
        edgeBand = 3.dp,
        edgeBias = 0.5f,
        chipSize = 5.dp,
        chipCoverage = 0.42f,
        scratchScale = 46.dp,
        scratchStrength = 0.75f,
        scuffStrength = 0.7f,
        speckStrength = 0.8f,
        grainStrength = 0.6f,
        strokeWidth = 0.8.dp,
        strokeBreak = 0.9f,
    )

    val Large = DistressedTitleSpec(
        color = Color(0xFFE6DCC4),
        edgeBand = 9.dp,
        edgeBias = 0.6f,
        chipSize = 9.dp,
        chipCoverage = 0.55f,
        scratchScale = 90.dp,
        scratchStrength = 0.9f,
        scuffStrength = 0.9f,
        speckStrength = 1f,
        grainStrength = 0.28f,
        strokeWidth = 1.3.dp,
        strokeBreak = 0.5f,
    )
}

internal const val DISTRESSED_TITLE_AGSL = """
uniform float2 size;
uniform float seed;
uniform float isStroke;

uniform float band;
uniform float edgeBias;
uniform float chipScale;
uniform float chipCoverage;
uniform float scratchScale;
uniform float scratchStrength;
uniform float scuffStrength;
uniform float speckStrength;
uniform float grainScale;
uniform float grainStrength;
uniform float strength;
uniform float strokeBreak;
uniform float strokeWear;
${NOISE_LIB}

half4 main(float2 p) {
    float2 sp = p + seed * 37.0 + isStroke * 211.0;

    float dx = min(p.x, size.x - p.x);
    float dy = min(p.y, size.y - p.y) * 1.6;
    float d = min(dx, dy);
    float edge = pow(1.0 - smoothstep(0.0, band, d), 1.4);

    float2 w = float2(fbm(sp / (chipScale * 3.0)), fbm(sp / (chipScale * 3.0) + 7.7)) - 0.5;
    float blotch = fbm(sp / chipScale + w * 1.6);
    float t = 0.80 - 0.30 * chipCoverage;
    float chip = smoothstep(t, t + 0.10, blotch + edge * edgeBias);

    float2 r = float2(sp.x * 0.94 + sp.y * 0.34, -sp.x * 0.34 + sp.y * 0.94);
    float sc = noise(float2(r.x / scratchScale, r.y / (scratchScale * 0.04)));
    float scZone = smoothstep(0.45, 0.70, fbm(sp / (scratchScale * 0.6) + 9.0));
    float line = smoothstep(0.80, 0.86, sc) * scZone * scratchStrength;

    float v = noise(float2(sp.x / (grainScale * 2.5), sp.y / (scratchScale * 0.5)));
    float vZone = smoothstep(0.40, 0.70, fbm(sp / (chipScale * 4.0) + 21.0));
    float streak = smoothstep(0.70, 0.92, v) * vZone * edge * scuffStrength;

    float cell = grainScale * 3.0;
    float2 g = sp / cell;
    float2 gi = floor(g);
    float h = hash(gi);
    float rad = 0.18 + 0.30 * hash(gi + 3.1);
    float pin = step(0.90 - 0.25 * edge, h) * (1.0 - smoothstep(rad * 0.6, rad, length(fract(g) - 0.5)));
    pin *= smoothstep(0.35, 0.65, fbm(sp / (chipScale * 2.0) + 4.0)) * speckStrength;

    float fine = smoothstep(0.62, 0.98, noise(sp / grainScale)) * grainStrength * (0.3 + edge);

    float keep = (1.0 - chip) * (1.0 - line) * (1.0 - streak) * (1.0 - pin) * (1.0 - fine);
    float erosion = 1.0 - keep;

    if (isStroke > 0.5) {
        float brk = strokeBreak * smoothstep(0.55, 0.80, fbm(sp / (chipScale * 0.7)));
        erosion = max(erosion * strokeWear, brk);
    }

    erosion = clamp(erosion * strength, 0.0, 1.0);
    return half4(0.0, 0.0, 0.0, half(erosion));
}
"""

private fun Modifier.erosionMask(
    spec: DistressedTitleSpec,
    seed: Int,
    stroke: Boolean,
): Modifier = this
    .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
    .drawWithCache {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val shader = RuntimeShader(DISTRESSED_TITLE_AGSL).apply {
                setFloatUniform("size", size.width, size.height)
                setFloatUniform("seed", seed.mod(97).toFloat())
                setFloatUniform("isStroke", if (stroke) 1f else 0f)
                setFloatUniform("band", spec.edgeBand.toPx())
                setFloatUniform("edgeBias", spec.edgeBias)
                setFloatUniform("chipScale", spec.chipSize.toPx())
                setFloatUniform("chipCoverage", spec.chipCoverage)
                setFloatUniform("scratchScale", spec.scratchScale.toPx())
                setFloatUniform("scratchStrength", spec.scratchStrength)
                setFloatUniform("scuffStrength", spec.scuffStrength)
                setFloatUniform("speckStrength", spec.speckStrength)
                setFloatUniform("grainScale", spec.grainSize.toPx())
                setFloatUniform("grainStrength", spec.grainStrength)
                setFloatUniform("strength", spec.strength)
                setFloatUniform("strokeBreak", spec.strokeBreak)
                setFloatUniform("strokeWear", spec.strokeWear)
            }
            val brush = ShaderBrush(shader)
            onDrawWithContent {
                drawContent()
                drawRect(brush, blendMode = BlendMode.DstOut)
            }
        } else {
            onDrawWithContent { drawContent() }
        }
    }

private fun Modifier.clipToGlyphs(): Modifier = this.drawWithContent {
    drawIntoCanvas { canvas ->
        val paint = Paint().apply { blendMode = BlendMode.DstIn }
        canvas.saveLayer(Rect(0f, 0f, size.width, size.height), paint)
        drawContent()
        canvas.restore()
    }
}

@Preview(
    widthDp = 500,
    heightDp = 500,
    showBackground = true,
    backgroundColor = 0xFF0B1011,
)
@Composable
private fun FeldDistressedTitlePreview() {
    DiscoDialogueTheme {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            val tape = FeldTape.Cream
            val largeStyle = MaterialTheme.typography.titleLarge.copy(
                fontSize = 52.sp,
                lineHeight = 54.sp,
                letterSpacing = 1.sp,
            )
            Box(
                Modifier
                    .stickerTapeMaterial(tape, seed = 3)
                    .padding(horizontal = tape.edgeInset + 16.dp, vertical = 12.dp)
            ) {
                FeldDistressedTitle(
                    text = "Choose speaker",
                    seed = 5,
                    spec = FeldDistressedTitles.Small,
                )
            }

            Box(
                Modifier
                    .stickerTapeMaterial(tape, seed = 9)
                    .padding(horizontal = tape.edgeInset + 16.dp, vertical = 12.dp)
            ) {
                FeldDistressedTitle(
                    text = "Entry edit",
                    seed = 12,
                    spec = FeldDistressedTitles.Small,
                )
            }

            FeldDistressedTitle(
                text = "The final interview",
                seed = 2,
                spec = FeldDistressedTitles.Large,
                textStyle = largeStyle,
            )

            FeldDistressedTitle(
                text = "App settings",
                seed = 7,
                spec = FeldDistressedTitles.Large,
                textStyle = largeStyle,
            )
        }
    }
}
