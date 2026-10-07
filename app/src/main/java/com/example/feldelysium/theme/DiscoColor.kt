package com.example.feldelysium.theme

import androidx.compose.ui.graphics.Color

val DiscoBackground       = Color(0xFF121A1C)
val DiscoSurface          = Color(0xFF1A2528)
val DiscoSurfaceVariant   = Color(0xFF223034)
val DiscoSurfaceHigh      = Color(0xFF2A3C40)
val DiscoOutline          = Color(0xFF3A4D51)

val DiscoOnSurface        = Color(0xFFFEFEFE)
val DiscoTextSecondary    = Color(0xFFB7C4C2)
val DiscoTextTertiary     = Color(0xFF7F8F8D)

val DiscoPrimary          = Color(0xFFEF6302)
val DiscoOnPrimary        = Color(0xFF1A0E04)
val DiscoPrimaryPressed   = Color(0xFFC9520A)
val DiscoPrimarySoft      = DiscoPrimary.copy(alpha = 0.16f)

val DiscoInactiveOption   = Color(0xFFA94D38)
val DiscoInactiveText     = Color(0xFFCC6A52)

val DiscoSecondary        = Color(0xFF2C5157)
val DiscoButtonConfirm    = Color(0xFF922810)

val DiscoPlateDone        = Color(0xFFC17400)
val DiscoOnPlateDone      = Color(0xFFFFFADD)
val DiscoPlateRare        = Color(0xFFF1B823)
val DiscoOnPlateRare      = Color(0xFF2F322B)
val DiscoPlateLight       = Color(0xFFDDDDDB)
val DiscoOnPlateLight     = Color(0xFF000100)

val DiscoSuccess          = Color(0xFF19BC6D)
val DiscoError            = Color(0xFFAF1E01)
val DiscoOnError          = Color(0xFFFEFEFE)
val ChromaKeyGreen   = Color(0xFF_00_B1_40)

val TapeBase         = Color(0xFF_12_0E_0B)
val TapeGrain        = Color(0xFF_1E_18_12)
val DialogueText     = Color(0xFF_E8_D8_C0)
val DialogueSubtext  = Color(0xFF_A8_98_80)

/** Helper: convert a Speaker's stored ARGB Long to a Compose Color. */
fun Long.toSpeakerColor() = Color(this)
