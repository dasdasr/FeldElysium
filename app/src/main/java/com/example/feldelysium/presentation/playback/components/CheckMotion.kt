package com.example.feldelysium.presentation.playback.components

import com.example.feldelysium.R
import com.example.feldelysium.domain.model.CheckResult

object CheckMotion {
    const val TAPE_ROLL_START_DELAY_MS = 100
    const val TAPE_ROLL_MS = 1000
    const val TAPE_ROLL_TOTAL_MS = 1300L
    const val TAPE_ROLL_VIEWPORTS = 5f

    const val BG_FADE_DELAY_MS = 400L
    const val BG_FADE_MS = 700
    const val DICE_FADE_DELAY_MS = 1000L
    const val DICE_FADE_MS = 400
    const val TEXT_OUT_DELAY_MS = 1000L
    const val TEXT_OUT_MS = 300
    const val TEXT_SLIDE_FRACTION = 0.3f

    const val DICE_SIZE_FRACTION = 0.17f
    const val DICE_GAP_FRACTION = 0.019f
    const val RESULT_TEXT_HEIGHT_FRACTION = 0.074f
    const val OVERLAY_BOTTOM_FRACTION = 0.178f
}

object CheckAssets {
    fun background(result: CheckResult): Int = when (result) {
        CheckResult.Success -> R.drawable.check_success_background
        CheckResult.Failure -> R.drawable.check_failure_background
    }

    fun resultText(result: CheckResult): Int = when (result) {
//        CheckResult.Success -> R.drawable.check_success_text
//        CheckResult.Failure -> R.drawable.check_failure_text
        CheckResult.Success -> R.drawable.check_success_background
        CheckResult.Failure -> R.drawable.check_failure_background
    }
}
