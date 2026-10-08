package com.example.feldelysium.designsystem.components.card

import android.os.SystemClock
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.feldelysium.designsystem.components.FeldDiamond
import com.example.feldelysium.designsystem.components.FeldIcon
import com.example.feldelysium.designsystem.components.button.FeldIconButton
import com.example.feldelysium.designsystem.components.FeldIconId
import com.example.feldelysium.designsystem.components.FeldPalette
import com.example.feldelysium.designsystem.material.DryPaperSpec
import com.example.feldelysium.designsystem.material.dryPaperMaterial
import com.example.feldelysium.designsystem.seed.rememberSeed
import com.example.feldelysium.designsystem.shape.RoughStyle
import com.example.feldelysium.theme.DiscoDialogueTheme
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.random.Random

private const val SWAY_STIFFNESS = 120f
private const val SWAY_DAMPING = 8f
private const val SWAY_GAIN = 0.24f
private const val SWAY_MAX_ANGLE = 8f
private const val SWAY_VELOCITY_TTL_NANOS = 80_000_000L

private class SwayState {
    var angle by mutableFloatStateOf(0f)
    var velocityY = 0f
    var velocityStamp = 0L
    var lastY = Float.NaN
    var lastStamp = 0L
}

@Composable
fun FeldEntryCard(
    speakerName: String,
    speakerColor: Color,
    text: String,
    modifier: Modifier = Modifier,
    seed: Int = rememberSeed(),
    isDragging: Boolean = false,
    onClick: (() -> Unit)? = null,
    onMore: (() -> Unit)? = null,
    maxLines: Int = 2,
    dragHandle: @Composable () -> Unit = {
        FeldIcon(FeldIconId.Drag, tint = FeldPalette.Muted, size = 22.dp)
    },
) {
    val lift by animateFloatAsState(if (isDragging) 1f else 0f, tween(120), label = "lift")

    val stripe = remember(speakerColor) {
        DryPaperSpec(
            base = speakerColor,
            crack = lerp(speakerColor, Color.Black, 0.55f),
            tearDepth = 2.dp,
            tearCoverage = 0.35f,
            crackCoverage = 0.35f,
        )
    }

    val baseRough = remember { RoughStyle() }
    var tick by remember { mutableIntStateOf(0) }
    LaunchedEffect(isDragging) {
        if (isDragging) {
            while (true) {
                delay(350)
                tick++
            }
        } else {
            tick = 0
        }
    }

    val seedShift = if (isDragging) (tick % 3) - 1 else 0
    val jitter = if (isDragging) 2f + Random(seed * 31 + tick).nextFloat() * 4f else 0f
    val rough = remember(jitter) {
        baseRough.copy(roughness = baseRough.roughness + jitter.dp)
    }

    val sway = remember { SwayState() }
    LaunchedEffect(isDragging) {
        if (!isDragging) sway.lastY = Float.NaN
        var angularVelocity = 0f
        var previous = SystemClock.elapsedRealtimeNanos()
        while (isDragging || abs(sway.angle) > 0.02f || abs(angularVelocity) > 0.05f) {
            withFrameNanos { }
            val now = SystemClock.elapsedRealtimeNanos()
            val dt = ((now - previous) / 1e9f).coerceAtMost(0.032f)
            previous = now

            val fresh = isDragging && now - sway.velocityStamp < SWAY_VELOCITY_TTL_NANOS
            val velocity = if (fresh) sway.velocityY else 0f
            val torque = -SWAY_STIFFNESS * sway.angle -
                    SWAY_DAMPING * angularVelocity +
                    SWAY_GAIN * velocity

            angularVelocity += torque * dt
            sway.angle = (sway.angle + angularVelocity * dt)
                .coerceIn(-SWAY_MAX_ANGLE, SWAY_MAX_ANGLE)
        }
        sway.angle = 0f
    }

    FeldCard(
        modifier = modifier
            .onGloballyPositioned { coordinates ->
                if (!isDragging) return@onGloballyPositioned
                val y = coordinates.positionInRoot().y
                val now = SystemClock.elapsedRealtimeNanos()
                if (!sway.lastY.isNaN() && now > sway.lastStamp) {
                    sway.velocityY = (y - sway.lastY) / ((now - sway.lastStamp) / 1e9f)
                    sway.velocityStamp = now
                }
                sway.lastY = y
                sway.lastStamp = now
            }
            .graphicsLayer {
                val s = 1f + 0.02f * lift
                scaleX = s
                scaleY = s
                alpha = 1f - 0.1f * lift
                transformOrigin = TransformOrigin(0.92f, 0.85f)
                rotationZ = sway.angle
            },
        rough = rough,
        contentPadding = PaddingValues(0.dp),
        onClick = onClick,
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            Box(
                Modifier
                    .width(10.dp)
                    .fillMaxHeight()
                    .dryPaperMaterial(stripe, seed)
            )
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 14.dp, top = 12.dp, bottom = 12.dp, end = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                FeldDiamond(speakerColor, size = 26.dp)
                Column(Modifier.weight(1f)) {
                    Text(
                        text = speakerName.uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = speakerColor,
                    )
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = maxLines,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
                Column(
                    modifier = Modifier.fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.End,
                ) {
                    if (onMore != null) FeldIconButton(FeldIconId.MoreHoriz, onMore)
                    else Spacer(Modifier.height(1.dp))
                    dragHandle()
                }
            }
        }
    }
}

@Preview(widthDp = 380, heightDp = 480, showBackground = true, backgroundColor = 0xFF0B1011)
@Composable
private fun FeldEntryCardPreview() {
    DiscoDialogueTheme {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            FeldEntryCard(
                "Logic", Color(0xFF4FB3A6),
                "Let's be precise. What do you actually remember about that night?",
                onClick = {}, onMore = {},
            )
            FeldEntryCard(
                "Harry", Color(0xFFE45720),
                "I remember the cold. And the taste of cheap whiskey.",
                onClick = {}, onMore = {},
            )
            FeldEntryCard(
                "Empathy", Color(0xFF8F6FD6),
                "You're not just tired, you're scared. That's different.",
                isDragging = true, onMore = {},
            )
        }
    }
}
