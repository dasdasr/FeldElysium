package com.feldelysium.designsystem.components.button

import android.R.attr.translationY
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.feldelysium.designsystem.components.content.FeldIcon
import com.feldelysium.designsystem.components.content.FeldIconId
import com.feldelysium.designsystem.foundation.haptics.rememberFeldHaptics
import com.feldelysium.designsystem.foundation.material.ChipTexture
import com.feldelysium.designsystem.foundation.material.ChipTextures
import com.feldelysium.designsystem.foundation.material.FeldMarker
import com.feldelysium.designsystem.foundation.material.markerFilmMaterial

@Composable
public fun FeldFilmButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: FeldIconId? = null,
    seed: Int = 7,
) {
    val haptics = rememberFeldHaptics()
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val press by animateFloatAsState(
        if (pressed) 1f else 0f,
        tween(if (pressed) 75 else 150),
        label = "press",
    )

    Box(
        modifier
            .height(56.dp)
            .graphicsLayer {
                val s = 1f - 0.02f * press
                scaleX = s
                scaleY = s
                translationY = 1.5f * press
            }
            .shadow(6.dp, RoundedCornerShape(4.dp), clip = false, ambientColor = Color.Black, spotColor = Color.Black)
            .clickable(interactionSource = interaction, indication = null, role = Role.Button) {
                haptics.tick()
                onClick()
            },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.fillMaxSize().graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }) {
            val body = ChipTextures.brush(
                Color(0xFFE45720),
                ChipTexture.Base,
                density,
                size.width.toInt(),
                size.height.toInt(),
                seed,
            )
            drawRoundRect(brush = body, cornerRadius = CornerRadius(4.dp.toPx()))
            drawFilmPerforation(press)
        }

        Row(Modifier.padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                FeldIcon(icon, size = 18.dp, markerFilmSpec = FeldMarker.Ink)
                Spacer(Modifier.width(8.dp))
            }
            Box(Modifier.weight(1f, fill = false).markerFilmMaterial(FeldMarker.Ink, seed + 1)) {
                Text(
                    text = text.uppercase(),
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp,
                    ),
                    color = Color.White,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

internal fun DrawScope.drawFilmPerforation(press: Float) {
    val pitch = 10.dp.toPx()
    val hw = 5.dp.toPx()
    val hh = 3.5.dp.toPx()
    val inset = 3.dp.toPx()
    val edge = 3.dp.toPx()
    val fade = pitch * 0.6f
    val shift = pitch * 0.5f * press
    val count = (size.width / pitch).toInt() + 2

    for (k in -1..count) {
        val x = 6.dp.toPx() + k * pitch - shift
        val a = ((x - edge) / fade).coerceIn(0f, 1f) *
            ((size.width - edge - (x + hw)) / fade).coerceIn(0f, 1f)
        if (a <= 0f) continue
        val hole = Color.Black.copy(alpha = a)
        val r = CornerRadius(1.dp.toPx())
        drawRoundRect(hole, Offset(x, inset), Size(hw, hh), r, blendMode = BlendMode.DstOut)
        drawRoundRect(hole, Offset(x, size.height - inset - hh), Size(hw, hh), r, blendMode = BlendMode.DstOut)
    }
}
