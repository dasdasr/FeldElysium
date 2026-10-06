package com.example.feldelysium.presentation.playback.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.feldelysium.domain.model.DialogueEntry
import com.example.feldelysium.domain.registry.PredefinedSpeakers
import com.example.feldelysium.presentation.playback.PlaybackMotion

@Preview
@Composable
private fun DialogueEntryItemPreview() {
    DialogueEntryItem(
        entry = DialogueEntry(
            speaker = PredefinedSpeakers.STEBAN,
            text = "Go-go Gyro",
        ),
        isLatest = true,
    )
}

@Composable
fun DialogueEntryItem(
    entry: DialogueEntry,
    isLatest: Boolean,
    modifier: Modifier = Modifier,
) {
    val alpha = remember(entry.id) { Animatable(0f) }
    LaunchedEffect(isLatest) {
        val target = if (isLatest) 1f else PlaybackMotion.PREVIOUS_LINES_ALPHA
        val duration = if (isLatest) PlaybackMotion.TEXT_FADE_IN_MS else PlaybackMotion.PREVIOUS_DIM_MS
        alpha.animateTo(target, tween(durationMillis = duration))
    }
    val animatedAlpha = alpha.value

    val speakerColor = Color(entry.speaker.colorArgb)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer { this.alpha = animatedAlpha }
    ) {
        Text(
            text = entry.speaker.name.uppercase(),
            color = speakerColor,
            style = MaterialTheme.typography.labelSmall,
            letterSpacing = 1.8.sp
        )
        Spacer(Modifier.height(3.dp))
        Text(
            text = entry.text,
            color = Color(0xFF_E8_D8_C0),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
