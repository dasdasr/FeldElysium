package com.feldelysium.designsystem.components.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.feldelysium.designsystem.components.container.FeldDivider
import com.feldelysium.designsystem.components.content.FeldIcon
import com.feldelysium.designsystem.components.content.FeldIconId
import com.feldelysium.designsystem.foundation.material.FeldMarker
import com.feldelysium.designsystem.foundation.seed.rememberSeed

public data class FeldToolbarItem(
    val icon: FeldIconId,
    val label: String,
    val enabled: Boolean = true,
    val accent: Color? = null,
    val onClick: () -> Unit,
)

@Composable
public fun FeldToolbar(items: List<FeldToolbarItem>, modifier: Modifier = Modifier, seed: Int = rememberSeed()) {
    Column(modifier.fillMaxWidth()) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
        ) {
            items.forEachIndexed { i, item ->
                if (i > 0) {
                    FeldDivider(
                        vertical = true,
                        seed = seed + i,
                        modifier = Modifier.padding(vertical = 14.dp),
                    )
                }
                ToolbarCell(item)
            }
        }
        FeldDivider(seed = seed + 10, segment = 160.dp)
    }
}

private const val ICON_FULL = 32f
private const val ICON_SMALL = 24f

@Composable
private fun RowScope.ToolbarCell(item: FeldToolbarItem) {
    val primary = item.accent != null && item.enabled

    val weight by animateFloatAsState(
        targetValue = if (primary) 1.5f else 1f,
        animationSpec = spring(dampingRatio = 0.62f, stiffness = Spring.StiffnessLow),
        label = "toolbarWeight",
    )
    val t by animateFloatAsState(
        targetValue = if (primary) 1f else 0f,
        animationSpec = tween(durationMillis = 420),
        label = "toolbarPrimary",
    )

    val targetTint = when {
        !item.enabled -> colorScheme.onSurface.copy(alpha = 0.35f)
        primary -> item.accent
        else -> colorScheme.onSurface.copy(alpha = 0.9f)
    }
    val tint by animateColorAsState(targetTint, tween(450), label = "toolbarTint")

    val flicker = remember { Animatable(1f) }
    LaunchedEffect(primary) {
        if (primary) {
            flicker.snapTo(0.15f)
            flicker.animateTo(
                targetValue = 1f,
                animationSpec = keyframes {
                    durationMillis = 520
                    0.15f at 0
                    0.9f at 70
                    0.35f at 140
                    1f at 220
                    0.65f at 300
                    1f at 400
                    0.9f at 460
                },
            )
        } else {
            flicker.snapTo(1f)
        }
    }

    val iconDp = (ICON_SMALL + (ICON_FULL - ICON_SMALL) * t).dp
    val iconScale = (ICON_SMALL + (ICON_FULL - ICON_SMALL) * t) / ICON_FULL
    val textScale = 1f + 0.12f * t

    Column(
        modifier = Modifier
            .weight(weight)
            .fillMaxHeight()
            .clickable(enabled = item.enabled, onClick = item.onClick)
            .padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
    ) {
        Box(Modifier.size(iconDp), contentAlignment = Alignment.Center) {
            FeldIcon(
                id = item.icon,
                size = ICON_FULL.dp,
                film = true,
                markerFilmSpec = if (item.accent != null && item.enabled) {
                    FeldMarker.Chalk.copy(color = item.accent)
                } else {
                    FeldMarker.Chalk
                },
                modifier = Modifier
                    .wrapContentSize(unbounded = true)
                    .graphicsLayer {
                        scaleX = iconScale
                        scaleY = iconScale
                        alpha = flicker.value
                    },
            )
        }
        Box(Modifier.height((6f - 2f * t).dp))
        Text(
            text = item.label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = tint,
            modifier = Modifier.graphicsLayer {
                scaleX = textScale
                scaleY = textScale
            },
        )
    }
}
