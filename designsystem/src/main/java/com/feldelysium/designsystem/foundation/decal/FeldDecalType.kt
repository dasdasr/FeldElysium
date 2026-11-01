package com.feldelysium.designsystem.foundation.decal

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.feldelysium.designsystem.R

public object FeldDecalType {
    public val Mono: FontFamily = FontFamily(
        Font(R.font.feld_plex_mono_regular, FontWeight.Normal),
        Font(R.font.feld_plex_mono_semibold, FontWeight.SemiBold),
    )
    public val Display: FontFamily = FontFamily(Font(R.font.feld_chakra_petch_bold, FontWeight.Bold))

    public val Base: TextStyle =
        TextStyle(fontFamily = Mono, fontSize = 7.sp, lineHeight = 9.5.sp, letterSpacing = 0.6.sp)
    public val Label: TextStyle = TextStyle(
        fontFamily = Display,
        fontWeight = FontWeight.Bold,
        fontSize = 8.sp,
        lineHeight = 10.sp,
        letterSpacing = 1.6.sp,
    )
}
