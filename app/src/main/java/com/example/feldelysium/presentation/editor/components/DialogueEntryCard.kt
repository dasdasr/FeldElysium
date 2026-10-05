package com.example.feldelysium.presentation.editor.components


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.feldelysium.domain.model.DialogueEntry
import com.example.feldelysium.domain.registry.PredefinedSpeakers

@Preview
@Composable
fun DialogueEntryCardViewer() {
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
    modifier: Modifier = Modifier
) {
    val speakerColor = Color(entry.speaker.colorArgb)
    val elevation    = if (isDragging) 8.dp else 1.dp

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        modifier  = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Drag handle
            dragHandle()

            // Speaker color accent bar
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(40.dp)
                    .background(speakerColor)
            )

            // Content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text  = entry.speaker.name.uppercase(),
                    color = speakerColor,
                    style = MaterialTheme.typography.labelSmall
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text     = entry.text,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style    = MaterialTheme.typography.bodyMedium
                )
            }

            // Actions
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, "Edit", modifier = Modifier.size(18.dp))
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, "Delete", modifier = Modifier.size(18.dp))
            }
        }
    }
}