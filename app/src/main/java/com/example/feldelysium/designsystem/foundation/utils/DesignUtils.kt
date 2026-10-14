package com.example.feldelysium.designsystem.foundation.utils

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role

internal object FeldPalette {
    val Muted = Color(0xFF8C8E8A)
    val Line = Color(0xFF666866)
    val Orange = Color(0xFFDD6224)
    val Teal = Color(0xFF3FA58E)
    val Ink = Color(0xFF1B1612)
    val Cream = Color(0xFFDCCFB5)
}

@Composable
internal fun Modifier.feldClickable(enabled: Boolean = true, role: Role = Role.Button, onClick: () -> Unit): Modifier =
    clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        enabled = enabled,
        role = role,
        onClick = onClick,
    )
