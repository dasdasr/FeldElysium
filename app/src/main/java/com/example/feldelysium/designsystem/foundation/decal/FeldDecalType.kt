package com.example.feldelysium.designsystem.foundation.decal

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.feldelysium.R

object FeldDecalType {
    val Mono = FontFamily(
        Font(R.font.plex_mono_regular, FontWeight.Normal),
        Font(R.font.plex_mono_semibold, FontWeight.SemiBold),
    )
    val Display = FontFamily(Font(R.font.chakra_petch_bold, FontWeight.Bold))

    val Base =
        TextStyle(fontFamily = Mono, fontSize = 7.sp, lineHeight = 9.5.sp, letterSpacing = 0.6.sp)
    val Label = TextStyle(
        fontFamily = Display,
        fontWeight = FontWeight.Bold,
        fontSize = 8.sp,
        lineHeight = 10.sp,
        letterSpacing = 1.6.sp,
    )
}
