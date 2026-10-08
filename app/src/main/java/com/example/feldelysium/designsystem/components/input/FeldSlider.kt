package com.example.feldelysium.designsystem.components.input

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.example.feldelysium.designsystem.material.DryPaperSpec
import com.example.feldelysium.designsystem.material.dryPaperMaterial
import com.example.feldelysium.designsystem.seed.rememberSeed

private val ThumbSpec = DryPaperSpec(
    base = Color(0xFFDCCFB5),
    crack = Color(0xFF5E4F38),
    crackDepth = 6.dp,
)
private val TrackOn = Color(0xFFDCCFB5)
private val TrackOff = Color(0xFF2A2C2B)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeldSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    seed: Int = rememberSeed(),
    onValueChangeFinished: (() -> Unit)? = null,
) {
    val interaction = remember { MutableInteractionSource() }
    val dragged by interaction.collectIsDraggedAsState()
    val pressed by interaction.collectIsPressedAsState()
    val thumbScale by animateFloatAsState(
        targetValue = if (dragged || pressed) 1.22f else 1f,
        animationSpec = tween(120),
        label = "thumbScale",
    )

    Slider(
        value = value,
        onValueChange = onValueChange,
        onValueChangeFinished = onValueChangeFinished,
        valueRange = valueRange,
        interactionSource = interaction,
        modifier = modifier.fillMaxWidth(),
        thumb = {
            Box(
                Modifier
                    .size(22.dp)
                    .graphicsLayer {
                        scaleX = thumbScale
                        scaleY = thumbScale
                    }
                    .clip(CircleShape)
                    .dryPaperMaterial(ThumbSpec, seed),
            )
        },
        track = { state ->
            val range = state.valueRange
            val fraction = ((state.value - range.start) / (range.endInclusive - range.start))
                .coerceIn(0f, 1f)
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(TrackOff, RoundedCornerShape(2.dp)),
            ) {
                Box(
                    Modifier
                        .fillMaxWidth(fraction)
                        .fillMaxHeight()
                        .background(TrackOn, RoundedCornerShape(2.dp)),
                )
            }
        },
    )
}
