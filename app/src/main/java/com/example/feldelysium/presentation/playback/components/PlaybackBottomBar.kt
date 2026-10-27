package com.example.feldelysium.presentation.playback.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.example.feldelysium.R
import com.example.feldelysium.domain.model.settings.BarIconState
import com.example.feldelysium.domain.model.settings.BarSlot
import com.example.feldelysium.domain.model.settings.InventoryIcon
import com.example.feldelysium.domain.model.settings.InventorySlot
import com.example.feldelysium.domain.model.StatusLine

private object BarLayout {
    const val ICON_SIZE = 0.075f
    const val INV_SIZE = 0.045f
    const val ICON_BOTTOM = 0.056f
    const val INV_BOTTOM = 0.051f

    const val MENU_X = 0.135f
    const val BAG_X = 0.292f
    const val JOURNAL_X = 0.447f
    const val HEAD_X = 0.603f

    const val INV_LEFT_X = 0.785f
    const val INV_RIGHT_X = 0.873f
}

private fun BarSlot.x(): Float = when (this) {
    BarSlot.BAG -> BarLayout.BAG_X
    BarSlot.JOURNAL -> BarLayout.JOURNAL_X
    BarSlot.HEAD -> BarLayout.HEAD_X
}

private fun InventorySlot.x(): Float = when (this) {
    InventorySlot.LEFT -> BarLayout.INV_LEFT_X
    InventorySlot.RIGHT -> BarLayout.INV_RIGHT_X
}

private fun Modifier.anchoredAt(barWidth: Dp, barHeight: Dp, xFraction: Float, bottomFraction: Float, size: Dp) =
    offset(x = barWidth * xFraction - size / 2, y = barHeight - barWidth * bottomFraction - size / 2)

@Composable
fun PlaybackBottomBar(
    icons: Map<BarSlot, BarIconState>,
    inventory: Map<InventorySlot, InventoryIcon>,
    modifier: Modifier = Modifier,
    status: StatusLine? = null,
) {
    Box(modifier) {
        Image(
            painter = painterResource(R.drawable.bottom_ui_bar),
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier.fillMaxWidth(),
        )

        BoxWithConstraints(Modifier.matchParentSize()) {
            val w = maxWidth
            val h = maxHeight
            val iconSize = w * BarLayout.ICON_SIZE
            val invSize = w * BarLayout.INV_SIZE

            BarGlyph(
                res = R.drawable.ic_bar_menu,
                state = BarIconState.ACTIVE,
                size = iconSize,
                modifier = Modifier.anchoredAt(w, h, BarLayout.MENU_X, BarLayout.ICON_BOTTOM, iconSize),
            )

            BarSlot.entries.forEach { slot ->
                BarGlyph(
                    res = slot.iconRes,
                    state = icons[slot] ?: slot.defaultState,
                    size = iconSize,
                    modifier = Modifier.anchoredAt(w, h, slot.x(), BarLayout.ICON_BOTTOM, iconSize),
                )
            }

            InventorySlot.entries.forEach { slot ->
                BarGlyph(
                    res = (inventory[slot] ?: slot.defaultIcon).iconRes,
                    state = BarIconState.INACTIVE,
                    size = invSize,
                    mirrored = slot.mirrored,
                    modifier = Modifier.anchoredAt(w, h, slot.x(), BarLayout.INV_BOTTOM, invSize),
                )
            }

            if (status != null) {
                PlaybackStatusHud(
                    money = status.money,
                    time = status.time,
                    day = status.day,
                    modifier = Modifier.matchParentSize(),
                )
            }
        }
    }
}

@Preview(
    name = "Playback Bottom Bar",
    showBackground = true,
    backgroundColor = 0xFF000000,
)
@Composable
private fun PlaybackBottomBarPreview() {
    PlaybackBottomBar(
        icons = emptyMap(),
        inventory = emptyMap(),
        modifier = Modifier.fillMaxWidth(),
    )
}
