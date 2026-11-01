package com.feldelysium.designsystem.components.container

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.feldelysium.designsystem.foundation.material.agsl.INK_AGSL
import com.feldelysium.designsystem.foundation.seed.rememberSeed
import com.feldelysium.designsystem.theme.FeldTheme
import kotlin.random.Random

@Composable
public fun FeldDivider(
    modifier: Modifier = Modifier,
    seed: Int = rememberSeed(),
    vertical: Boolean = false,
    thickness: Dp = 1.dp,
    color: Color = Color(0xFF666866),
    segment: Dp = 120.dp,
    changeChance: Float = 0.35f,
) {
    val sized = if (vertical) {
        Modifier
            .width(thickness)
            .fillMaxHeight()
    } else {
        Modifier
            .height(thickness)
            .fillMaxWidth()
    }

    Spacer(
        modifier
            .then(sized)
            .drawWithCache {
                val len = if (vertical) size.height else size.width
                val thick = thickness.toPx()
                val rnd = Random(seed * 131 + 7)
                val levels = floatArrayOf(0.45f, 0.7f, 1f)

                val starts = ArrayList<Float>()
                val ends = ArrayList<Float>()
                val alphas = ArrayList<Float>()
                var s = 0f
                var lvl = 2
                while (s < len) {
                    val l = segment.toPx() * (0.6f + rnd.nextFloat())
                    if (rnd.nextFloat() < changeChance) lvl = rnd.nextInt(3)
                    starts += s
                    ends += minOf(s + l, len)
                    alphas += levels[lvl]
                    s += l
                }

                val brush: Brush =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        ShaderBrush(
                            RuntimeShader(INK_AGSL).apply {
                                setFloatUniform("seed", seed.mod(97).toFloat())
                                setFloatUniform("grainScale", 18.dp.toPx())
                                setFloatUniform("minAlpha", 0.6f)
                                setFloatUniform("contrast", 1.6f)
                                setColorUniform("inkColor", color.toArgb())
                            },
                        )
                    } else {
                        SolidColor(color)
                    }

                onDrawBehind {
                    for (i in starts.indices) {
                        val a = starts[i]
                        val l = ends[i] - a
                        if (vertical) {
                            drawRect(brush, Offset(0f, a), Size(thick, l), alphas[i])
                        } else {
                            drawRect(brush, Offset(a, 0f), Size(l, thick), alphas[i])
                        }
                    }
                }
            },
    )
}

@Preview(
    widthDp = 400,
    heightDp = 260,
    showBackground = true,
    backgroundColor = 0xFF0B1011,
)
@Composable
private fun FeldDividerPreview() {
    FeldTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            FeldDivider(
                modifier = Modifier.fillMaxWidth(),
            )

            FeldDivider(
                modifier = Modifier.fillMaxWidth(),
                thickness = 2.dp,
                segment = 80.dp,
            )

            FeldDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp),
                segment = 50.dp,
                changeChance = 0.6f,
            )

            Row(
                modifier = Modifier.height(100.dp),
                horizontalArrangement = Arrangement.spacedBy(32.dp),
            ) {
                FeldDivider(
                    vertical = true,
                    thickness = 1.dp,
                    segment = 40.dp,
                    changeChance = 0.35f,
                )

                FeldDivider(
                    vertical = true,
                    thickness = 2.dp,
                    segment = 25.dp,
                    changeChance = 0.6f,
                )
            }
        }
    }
}
