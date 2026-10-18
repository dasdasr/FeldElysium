package com.example.feldelysium.presentation.settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.feldelysium.data.settings.BarIconState
import com.example.feldelysium.data.settings.BarSlot
import com.example.feldelysium.data.settings.InventoryIcon
import com.example.feldelysium.data.settings.InventorySlot
import com.example.feldelysium.designsystem.components.card.FeldCard
import com.example.feldelysium.designsystem.components.dialog.FeldDialog
import com.example.feldelysium.designsystem.components.dialog.FeldDialogFooter
import com.example.feldelysium.designsystem.components.display.FeldDivider
import com.example.feldelysium.designsystem.foundation.decal.FeldDecalType
import com.example.feldelysium.designsystem.foundation.utils.FeldPalette
import com.example.feldelysium.designsystem.foundation.utils.feldClickable
import com.example.feldelysium.presentation.playback.components.BarGlyph
import com.example.feldelysium.presentation.playback.components.iconRes
import com.example.feldelysium.presentation.playback.components.label
import com.example.feldelysium.presentation.playback.components.mirrored
import com.example.feldelysium.presentation.playback.components.tint

@Composable
fun BarIconsDialog(
    icons: Map<BarSlot, BarIconState>,
    inventory: Map<InventorySlot, InventoryIcon>,
    onCycleIcon: (BarSlot) -> Unit,
    onCycleInventory: (InventorySlot) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit,
) {
    FeldDialog(
        title = "Panel icons",
        eyebrow = "Playback",
        onDismiss = onDismiss,
        seed = 21,
        footer = {
            FeldDialogFooter(
                onReset = onReset,
                onCancel = onDismiss,
                cancelLabel = "RESET",
                confirmLabel = "OK",
                seed = 22,
            )
        },
    ) {
        SectionLabel("Panel", hint = "Tap to cycle: off, on, alert")
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            BarSlot.entries.forEachIndexed { i, slot ->
                val state = icons[slot] ?: slot.defaultState
                IconTile(
                    caption = state.label,
                    captionColor = state.tint,
                    seed = 30 + i,
                    onClick = { onCycleIcon(slot) },
                    modifier = Modifier.weight(1f),
                ) {
                    BarGlyph(slot.iconRes, state, TILE_ICON_SIZE)
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        FeldDivider(seed = 41)
        Spacer(Modifier.height(16.dp))

        SectionLabel("Inventory", hint = "Tap to switch the item")
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            InventorySlot.entries.forEachIndexed { i, slot ->
                val icon = inventory[slot] ?: slot.defaultIcon
                IconTile(
                    caption = icon.name,
                    captionColor = BarIconState.INACTIVE.tint,
                    seed = 50 + i,
                    onClick = { onCycleInventory(slot) },
                    modifier = Modifier.weight(1f),
                ) {
                    BarGlyph(icon.iconRes, BarIconState.INACTIVE, TILE_ICON_SIZE, mirrored = slot.mirrored)
                }
            }
        }
    }
}

private val TILE_ICON_SIZE = 44.dp

@Composable
private fun SectionLabel(text: String, hint: String) {
    Text(
        text.uppercase(),
        style = FeldDecalType.Base.copy(fontSize = 12.sp, letterSpacing = 1.6.sp),
        color = FeldPalette.Cream,
    )
    Text(
        hint,
        style = MaterialTheme.typography.bodySmall,
        color = FeldPalette.Muted,
        modifier = Modifier.padding(top = 2.dp, bottom = 10.dp),
    )
}

@Composable
private fun IconTile(
    caption: String,
    captionColor: Color,
    seed: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    glyph: @Composable () -> Unit,
) {
    FeldCard(
        modifier = modifier.feldClickable(onClick = onClick),
        seed = seed,
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 14.dp),
    ) {
        Box(Modifier.fillMaxWidth().height(64.dp), contentAlignment = Alignment.Center) { glyph() }
        Text(
            caption,
            style = FeldDecalType.Base.copy(fontSize = 10.sp, letterSpacing = 1.4.sp, textAlign = TextAlign.Center),
            color = captionColor,
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
        )
    }
}
