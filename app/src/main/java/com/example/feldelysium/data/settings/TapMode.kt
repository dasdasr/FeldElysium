package com.example.feldelysium.data.settings

enum class TapMode(val label: String) {
    WHOLE_SCREEN("Anywhere"),
    CONTINUE_BUTTON("Button"),
    ;

    fun next() = entries[(ordinal + 1) % entries.size]
}
