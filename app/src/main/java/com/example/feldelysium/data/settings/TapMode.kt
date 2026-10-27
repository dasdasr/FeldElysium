package com.example.feldelysium.data.settings

enum class TapMode {
    WHOLE_SCREEN,
    CONTINUE_BUTTON,
    NOWHERE;

    fun next(autoPlay: Boolean): TapMode {
        val available = if (autoPlay) {
            entries
        } else {
            entries.filter { it != NOWHERE }
        }

        return available[(available.indexOf(this) + 1) % available.size]
    }
}
