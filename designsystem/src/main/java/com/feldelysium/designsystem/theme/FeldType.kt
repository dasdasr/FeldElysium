package com.feldelysium.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.feldelysium.designsystem.R

public val Alegreya: FontFamily = FontFamily(
    Font(R.font.feld_alegreya_regular, FontWeight.Normal),
    Font(R.font.feld_alegreya_medium, FontWeight.Medium),
    Font(R.font.feld_alegreya_bold, FontWeight.Bold),
)

private val RobotoCondensed = FontFamily(
    Font(R.font.feld_roboto_condensed_regular, FontWeight.Normal),
    Font(R.font.feld_roboto_condensed_regular, FontWeight.Medium),
    Font(R.font.feld_roboto_condensed_bold, FontWeight.Bold),
    Font(R.font.feld_roboto_condensed_bold, FontWeight.Black),
)

internal val FeldTypography = Typography(
    titleLarge = TextStyle(
        fontFamily = RobotoCondensed,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.5.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = RobotoCondensed,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 24.sp,
        letterSpacing = 1.2.sp,
    ),
    bodyMedium = TextStyle(
        fontSize = 16.sp,
        fontFamily = Alegreya,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Normal,
    ),
    bodySmall = TextStyle(
        fontSize = 14.sp,
        fontFamily = Alegreya,
        lineHeight = 18.sp,
        fontWeight = FontWeight.Medium,
    ),
    labelLarge = TextStyle(
        fontFamily = RobotoCondensed,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 24.sp,
        letterSpacing = 1.2.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = RobotoCondensed,
        fontWeight = FontWeight.Black,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        letterSpacing = (-0.2.sp),
    ),
    labelSmall = TextStyle(
        fontSize = 12.sp,
        fontFamily = Alegreya,
        letterSpacing = 0.5.sp,
        fontWeight = FontWeight.Bold,
    ),
)

public val FeldContinueStyle: TextStyle = TextStyle(
    fontFamily = RobotoCondensed,
    fontWeight = FontWeight.Bold,
    fontSize = 16.sp,
    letterSpacing = 0.08.em,
)
