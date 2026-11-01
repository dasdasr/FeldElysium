package com.feldelysium.designsystem.components.input

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

public enum class FeldThumb { Diamond, Rectangle, Circle }

internal val ThumbRim = Color(0xFF3A3128)

internal fun FeldThumb.size(): DpSize = when (this) {
    FeldThumb.Diamond -> DpSize(26.dp, 26.dp)
    FeldThumb.Rectangle -> DpSize(12.dp, 26.dp)
    FeldThumb.Circle -> DpSize(22.dp, 22.dp)
}
