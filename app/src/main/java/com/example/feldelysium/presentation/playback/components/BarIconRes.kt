package com.example.feldelysium.presentation.playback.components

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import com.example.feldelysium.R
import com.example.feldelysium.data.settings.BarIconState
import com.example.feldelysium.data.settings.BarSlot
import com.example.feldelysium.data.settings.InventoryIcon
import com.example.feldelysium.data.settings.InventorySlot

@get:DrawableRes
val BarSlot.iconRes: Int
    get() = when (this) {
        BarSlot.BAG -> R.drawable.ic_bar_bag
        BarSlot.JOURNAL -> R.drawable.ic_bar_journal
        BarSlot.HEAD -> R.drawable.ic_bar_head
    }

@get:DrawableRes
val InventoryIcon.iconRes: Int
    get() = when (this) {
        InventoryIcon.HAND -> R.drawable.ic_inv_hand
        InventoryIcon.BAG -> R.drawable.ic_inv_bag
        InventoryIcon.LEDGER -> R.drawable.ic_inv_ledger
        InventoryIcon.CHAINCUTTERS -> R.drawable.ic_inv_chaincutters
    }

val InventorySlot.mirrored: Boolean get() = this == InventorySlot.RIGHT

val BarIconState.tint: Color
    get() = when (this) {
        BarIconState.INACTIVE -> Color(0xFF6F6F6F)
        BarIconState.ACTIVE -> Color(0xFFE8E8E8)
        BarIconState.NOTIFY -> Color(0xFFE8742A)
    }

val BarIconState.label: String
    get() = when (this) {
        BarIconState.INACTIVE -> "OFF"
        BarIconState.ACTIVE -> "ON"
        BarIconState.NOTIFY -> "ALERT"
    }
