package com.example.feldelysium.domain.model.settings

enum class SamplesMode { AUTO, ALWAYS_SHOW, ALWAYS_HIDE }

fun SamplesMode.next(): SamplesMode = when (this) {
    SamplesMode.AUTO -> SamplesMode.ALWAYS_SHOW
    SamplesMode.ALWAYS_SHOW -> SamplesMode.ALWAYS_HIDE
    SamplesMode.ALWAYS_HIDE -> SamplesMode.AUTO
}
