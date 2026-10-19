package com.example.feldelysium.data.settings

enum class BarIconState {
    INACTIVE,
    ACTIVE,
    NOTIFY,
    ;

    fun next(): BarIconState = entries[(ordinal + 1) % entries.size]
}

enum class BarSlot(val defaultState: BarIconState) {
    BAG(BarIconState.INACTIVE),
    JOURNAL(BarIconState.ACTIVE),
    HEAD(BarIconState.ACTIVE),
}

enum class InventoryIcon {
    HAND,
    BAG,
    LEDGER,
    CHAINCUTTERS,
    ;

    fun next(): InventoryIcon = entries[(ordinal + 1) % entries.size]
}

enum class InventorySlot(val defaultIcon: InventoryIcon) {
    LEFT(InventoryIcon.HAND),
    RIGHT(InventoryIcon.BAG),
}
