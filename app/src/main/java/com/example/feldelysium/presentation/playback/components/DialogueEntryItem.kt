package com.example.feldelysium.presentation.playback.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.feldelysium.domain.model.DialogueEntry
import com.example.feldelysium.domain.registry.PredefinedSpeakers

@Preview
@Composable
private fun DialogueEntryItemPreview() {
    DialogueEntryItem(
        entry = DialogueEntry(
            speaker = PredefinedSpeakers.STEBAN,
            text = "Go-go Gyro",
        ),
        alpha = 1.0f,
    )
}

@Composable
fun DialogueEntryItem(
    entry: DialogueEntry,
    alpha: Float,
    modifier: Modifier = Modifier
) {
    val speakerColor = Color(entry.speaker.colorArgb)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 6.dp, bottom = 6.dp)
            .graphicsLayer { this.alpha = alpha }
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
            lineHeight = 20.sp
        )
    }
}