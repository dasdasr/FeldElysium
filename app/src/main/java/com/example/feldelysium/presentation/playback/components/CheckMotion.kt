package com.example.feldelysium.presentation.playback.components

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import com.example.feldelysium.R
import com.example.feldelysium.domain.model.CheckResult
import com.example.feldelysium.domain.model.DamageType
import com.example.feldelysium.theme.disco

object CheckMotion {
    const val TAPE_ROLL_START_DELAY_MS = 100
    const val TAPE_ROLL_MS = 2800
    const val TAPE_ROLL_TOTAL_MS = TAPE_ROLL_START_DELAY_MS + TAPE_ROLL_MS.toLong()

    const val CHECK_RESULT_EARLY_MS = 1100L
    const val TAPE_ROLL_VIEWPORTS = 6f

    val TAPE_ROLL_EASING: Easing = CubicBezierEasing(0.5f, 0f, 1f, 0.6f)

    const val BG_FADE_DELAY_MS = 400L
    const val BG_FADE_MS = 700
    const val DICE_FADE_DELAY_MS = 1000L
    const val DICE_FADE_MS = 400
    const val TEXT_OUT_DELAY_MS = 1000L
    const val TEXT_OUT_MS = 300
    const val TEXT_SLIDE_FRACTION = 0.3f
    const val CHECK_OVERLAY_TOTAL_MS = 1400L

    const val DICE_SIZE_FRACTION = 0.17f
    const val DICE_GAP_FRACTION = 0.019f
    const val RESULT_TEXT_HEIGHT_FRACTION = 0.074f
    const val OVERLAY_BOTTOM_FRACTION = 0.178f

    val BG_BLEND = BlendMode.Screen

    const val DMG_BACKDROP_IN_MS = 1000
    const val DMG_BACKDROP_ALPHA = 0.8f
    const val DMG_OUT_DELAY_MS = 1400L
    const val DMG_BACKDROP_OUT_MS = 3000
    const val DMG_ICON_FADE_MS = 400
    const val DMG_TEXT_OUT_MS = 300
    const val DMG_SLIDE_FRACTION = 0.3f
    const val DMG_ICON_SIZE_FRACTION = 0.17f
}

object PanelMotion {
    const val SLIDE_MS = 450
}

object CheckAssets {
    val Plaque = R.drawable.check_plaque
    val Vignette = R.drawable.damage_vignette

    val VignetteFilter = ColorFilter.colorMatrix(
        ColorMatrix(
            floatArrayOf(
                0f, 0f, 0f, 0f, 192f,
                0f, 0f, 0f, 0f, 20f,
                0f, 0f, 0f, 0f, 30f,
                0.299f, 0.587f, 0.114f, 0f, 0f,
            ),
        ),
    )

    fun background(result: CheckResult): Int = when (result) {
        CheckResult.Success -> R.drawable.check_success_background
        CheckResult.Failure -> R.drawable.check_failure_background
    }

    fun label(result: CheckResult) = if (result == CheckResult.Success) "Check success" else "Check failure"

    @Composable
    fun ResultColor(result: CheckResult) =
        if (result == CheckResult.Success) MaterialTheme.disco.success else MaterialTheme.disco.checkFailure

    fun damageLabel(type: DamageType) = if (type == DamageType.Morale) "Damaged morale" else "Damaged health"

    @Composable
    fun DamageColor(type: DamageType) =
        if (type == DamageType.Morale) MaterialTheme.disco.damageMorale else MaterialTheme.disco.checkFailure

    fun damageIcon(): Int = R.drawable.damage_icon
}
