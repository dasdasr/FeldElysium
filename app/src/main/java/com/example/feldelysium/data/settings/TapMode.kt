package com.example.feldelysium.data.settings

enum class TapMode(val label: String) {
    WHOLE_SCREEN("Anywhere"),
    CONTINUE_BUTTON("Button"),
    NOWHERE("Nowhere");

    fun next(autoPlay: Boolean): TapMode {
        val available = if (autoPlay) {
            entries
        } else {
            entries.filter { it != NOWHERE }
        }

        return available[(available.indexOf(this) + 1) % available.size]
    }
}
