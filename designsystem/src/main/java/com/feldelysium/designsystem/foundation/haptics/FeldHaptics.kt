package com.feldelysium.designsystem.foundation.haptics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

@Stable
public class FeldHaptics(private val feedback: HapticFeedback) {
    public fun tick(): Unit = feedback.performHapticFeedback(HapticFeedbackType.SegmentTick)

    public fun toggle(on: Boolean): Unit = feedback.performHapticFeedback(
        if (on) HapticFeedbackType.ToggleOn else HapticFeedbackType.ToggleOff,
    )

    public fun confirm(): Unit = feedback.performHapticFeedback(HapticFeedbackType.Confirm)

    public fun handleMove(): Unit = feedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)

    public fun thud(): Unit = feedback.performHapticFeedback(HapticFeedbackType.LongPress)
}

@Composable
public fun rememberFeldHaptics(): FeldHaptics {
    val feedback = LocalHapticFeedback.current
    return remember(feedback) { FeldHaptics(feedback) }
}
