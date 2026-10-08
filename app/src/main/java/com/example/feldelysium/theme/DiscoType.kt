package com.example.feldelysium.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.feldelysium.R

// TODO: replace with matching the Disco Elysium UI font

private val SinaNova = FontFamily(Font(R.font.sina_nova_regular))
private val BebasNeue = FontFamily(Font(R.font.bebas_neue_regular, FontWeight.Normal))

val DiscoTypography = Typography(
    titleLarge = TextStyle(
        fontFamily = BebasNeue,
        fontWeight = FontWeight.Normal,
        fontSize = 30.sp,
        lineHeight = 32.sp,
        letterSpacing = 1.2.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = BebasNeue,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 24.sp,
        letterSpacing = 1.2.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = BebasNeue,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 24.sp,
        letterSpacing = 1.6.sp,
    ),
    bodyMedium = TextStyle(
        fontSize = 16.sp,
        fontFamily = SinaNova,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.1.sp,
    ),
    labelSmall = TextStyle(
        fontSize = 14.sp,
        fontFamily = SinaNova,
        letterSpacing = 1.5.sp,
        fontWeight = FontWeight.Bold
    )
)
