package com.example.feldelysium.designsystem.components.input

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.feldelysium.designsystem.foundation.utils.feldClickable
import com.example.feldelysium.designsystem.foundation.material.FeldPaper
import com.example.feldelysium.designsystem.foundation.material.paperMaterial
import com.example.feldelysium.designsystem.foundation.seed.rememberSeed
import com.example.feldelysium.designsystem.foundation.shape.RoughShape
import com.example.feldelysium.designsystem.foundation.shape.RoughStyle
import kotlin.math.abs

private val TrackRough = RoughStyle(
    roughness = 0.7.dp,
    step = 40.dp,
    corner = 20.dp,
    cornerVariation = 0.1f,
    tornChance = 0f,
    jerkChance = 0.05f,
)

private val PillRough = RoughStyle(
    roughness = 0.9.dp,
    step = 30.dp,
    corner = 16.dp,
    cornerVariation = 0.2f,
    tornChance = 0f,
)

private val OnPill = Color(0xFF1B1612)
private val OffPill = Color(0xFFE6DCC4)

@Composable
fun FeldSegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val seed = rememberSeed()
    val count = options.size.coerceAtLeast(1)
    val trackShape = remember(seed) { RoughShape(seed, TrackRough) }

    val position by animateFloatAsState(
        targetValue = selectedIndex.toFloat(),
        animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
        label = "segmentPosition",
    )

    val leftRound = if (count == 1) 1f else (1f - position).coerceIn(0.1f, 1f)
    val rightRound = if (count == 1) 1f else (position - (count - 2)).coerceIn(0.1f, 1f)

    val pillShape = remember(seed, leftRound, rightRound) {
        RoughShape(
            seed = seed,
            style = PillRough.copy(leftCornerScale = leftRound, rightCornerScale = rightRound),
        )
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .paperMaterial(trackShape, FeldPaper.Field)
            .padding(3.dp),
    ) {
        val segmentWidth = maxWidth / count

        Box(
            Modifier
                .offset(x = segmentWidth * position)
                .width(segmentWidth)
                .fillMaxHeight()
                .paperMaterial(pillShape, FeldPaper.DryButtonCream),
        )

        Row(Modifier.fillMaxSize()) {
            options.forEachIndexed { i, label ->
                val activeness = (1f - abs(position - i)).coerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .feldClickable(role = Role.Tab, onClick = { onSelect(i) }),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = label.uppercase(),
                        style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 1.6.sp),
                        color = lerp(OffPill, OnPill, activeness),
                    )
                }
            }
        }
    }
}
