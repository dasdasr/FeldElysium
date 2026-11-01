package com.feldelysium.app.presentation.shared

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

fun Long.toComposeColor(): Color = Color(this.toInt())
fun Color.toArgbLong(): Long = toArgb().toLong() and 0xFFFFFFFFL
