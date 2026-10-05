package com.example.feldelysium.presentation.editor.components


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.feldelysium.domain.model.AttributeGroup
import com.example.feldelysium.domain.model.Speaker
import com.example.feldelysium.domain.model.SpeakerType
import com.example.feldelysium.domain.registry.PredefinedSpeakers

@Preview
@Composable
private fun SpeakerPickerDialogPreview() {
    SpeakerPickerDialog(
        speakers = PredefinedSpeakers.ALL,
        onSpeakerSelected = {},
        onCreateCustom = {},
        onDismiss = {},
    )
}

@Composable
fun SpeakerPickerDialog(
    speakers: List<Speaker>,
    onSpeakerSelected: (Speaker) -> Unit,
    onCreateCustom: () -> Unit,
    onDismiss: () -> Unit
) {
    // Group speakers by type/attribute for organized display
    val skills = speakers.filter { it.type == SpeakerType.SKILL }
    val npcs   = speakers.filter { it.type == SpeakerType.NPC }
    val custom = speakers.filter { it.type == SpeakerType.CUSTOM }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose Speaker") },
        text = {
            LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                // SKILLS grouped by attribute
                AttributeGroup.entries.forEach { group ->
                    val groupSkills = skills.filter { it.attributeGroup == group }
                    if (groupSkills.isNotEmpty()) {
                        item {
                            Text(
                                text  = group.displayName.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                        items(groupSkills) { speaker ->
                            SpeakerRow(speaker = speaker, onClick = { onSpeakerSelected(speaker) })
                        }
                    }
                }

                // NPCs
                if (npcs.isNotEmpty()) {
                    item { SectionDivider("NPCs") }
                    items(npcs) { speaker ->
                        SpeakerRow(speaker = speaker, onClick = { onSpeakerSelected(speaker) })
                    }
                }

                // Custom
                if (custom.isNotEmpty()) {
                    item { SectionDivider("Custom") }
                    items(custom) { speaker ->
                        SpeakerRow(speaker = speaker, onClick = { onSpeakerSelected(speaker) })
                    }
                }

                // Create new
                item {
                    TextButton(
                        onClick = onCreateCustom,
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    ) {
                        Text("+ Create Custom Speaker")
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun SpeakerRow(speaker: Speaker, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Color dot
        Surface(
            color  = Color(speaker.colorArgb),
            shape  = CircleShape,
            modifier = Modifier.size(10.dp).align(Alignment.CenterVertically)
        ) {}
        Text(speaker.name)
    }
}

@Composable
private fun SectionDivider(label: String) {
    Text(
        text     = label.uppercase(),
        style    = MaterialTheme.typography.labelSmall,
        color    = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
    )
}