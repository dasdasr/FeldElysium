package com.example.feldelysium.theme

import androidx.compose.ui.graphics.Color

val DiscoBackground  = Color(0xFF_0D_0A_08)
val DiscoSurface     = Color(0xFF_1A_14_0F)
val DiscoOnSurface   = Color(0xFF_E8_D8_C0)
val DiscoPrimary     = Color(0xFF_B5_A6_42)
val DiscoSecondary   = Color(0xFF_8E_7E_4E)
val DiscoError       = Color(0xFF_C8_4E_3E)
val DiscoOnError     = Color(0xFF_FF_FF_FF)

/** Exact chroma-key green for external keying. Do NOT add transparency here. */
val ChromaKeyGreen   = Color(0xFF_00_B1_40)

val TapeBase         = Color(0xFF_12_0E_0B)
val TapeGrain        = Color(0xFF_1E_18_12)
val DialogueText     = Color(0xFF_E8_D8_C0)
val DialogueSubtext  = Color(0xFF_A8_98_80)

/** Helper: convert a Speaker's stored ARGB Long to a Compose Color. */
fun Long.toSpeakerColor() = Color(this)