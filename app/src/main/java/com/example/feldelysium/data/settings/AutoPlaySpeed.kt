package com.example.feldelysium.data.settings

enum class AutoPlaySpeed(val factor: Float, val label: String) {
    HALF(0.5f, "0.5×"),
    SLOW(0.75f, "0.75×"),
    NORMAL(1f, "1×"),
    FAST(1.5f, "1.5×"),
    TURBO(2f, "2×");

    fun next(): AutoPlaySpeed = entries[(ordinal + 1) % entries.size]
}
