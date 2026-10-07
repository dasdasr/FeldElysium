package com.example.feldelysium.presentation.editor.components


import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.feldelysium.designsystem.components.card.FeldEntryCard
import com.example.feldelysium.domain.model.DialogueEntry
import com.example.feldelysium.domain.registry.PredefinedSpeakers

@Preview(
    widthDp = 360,
    heightDp = 520,
    showBackground = true,
    backgroundColor = 0xFF0B1011,
)
@Composable
private fun DialogueEntryCardPreview() {
    DialogueEntry(
        speaker = PredefinedSpeakers.MEASUREHEAD,
        text = "You are weak as al-ghoul swallows you"
    )
}

@Composable
fun DialogueEntryCard(
    entry: DialogueEntry,
    isDragging: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    dragHandle: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    FeldEntryCard(
        speakerName = entry.speaker.name,
        speakerColor = Color(entry.speaker.colorArgb),
        text = entry.text,
        seed = entry.id.hashCode(),
        isDragging = isDragging,
        onClick = onEdit,
        onMore = onDelete,
        dragHandle = dragHandle,
        modifier = modifier.padding(horizontal = 16.dp, vertical = 4.dp),
    )
}
