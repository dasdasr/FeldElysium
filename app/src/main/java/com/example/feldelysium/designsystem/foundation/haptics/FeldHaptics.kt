package com.example.feldelysium.designsystem.foundation.haptics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

@Stable
class FeldHaptics(private val feedback: HapticFeedback) {
    fun tick() = feedback.performHapticFeedback(HapticFeedbackType.SegmentTick)

    fun toggle(on: Boolean) = feedback.performHapticFeedback(
        if (on) HapticFeedbackType.ToggleOn else HapticFeedbackType.ToggleOff
    )

    fun confirm() = feedback.performHapticFeedback(HapticFeedbackType.Confirm)

    fun handleMove() = feedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)

    fun thud() = feedback.performHapticFeedback(HapticFeedbackType.LongPress)
}

@Composable
fun rememberFeldHaptics(): FeldHaptics {
    val feedback = LocalHapticFeedback.current
    return remember(feedback) { FeldHaptics(feedback) }
}
