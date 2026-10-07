package com.example.feldelysium.presentation.editor.components


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
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
        speakers = PredefinedSpeakers.ALL + Speaker(
            id = "custom",
            name = "Ancient Reptilian Brain",
            colorArgb = Speaker.DEFAULT_CUSTOM_COLOR,
            type = SpeakerType.CUSTOM,
        ),
        onSpeakerSelected = {},
        onCreateCustom = {},
        onEditCustom = {},
        onDismiss = {},
    )
}

@Composable
fun SpeakerPickerDialog(
    speakers: List<Speaker>,
    onSpeakerSelected: (Speaker) -> Unit,
    onCreateCustom: () -> Unit,
    onEditCustom: (Speaker) -> Unit,
    onDismiss: () -> Unit
) {
    val skills = speakers.filter { it.type == SpeakerType.SKILL }
    val npcs   = speakers.filter { it.type == SpeakerType.NPC }
    val custom = speakers.filter { it.type == SpeakerType.CUSTOM }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose Speaker") },
        text = {
            LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
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

                if (npcs.isNotEmpty()) {
                    item { SectionDivider("NPCs") }
                    items(npcs) { speaker ->
                        SpeakerRow(speaker = speaker, onClick = { onSpeakerSelected(speaker) })
                    }
                }

                if (custom.isNotEmpty()) {
                    item { SectionDivider("Custom") }
                    items(custom, key = { it.id }) { speaker ->
                        SpeakerRow(
                            speaker = speaker,
                            onClick = { onSpeakerSelected(speaker) },
                            onEdit = { onEditCustom(speaker) },
                        )
                    }
                }

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
private fun SpeakerRow(
    speaker: Speaker,
    onClick: () -> Unit,
    onEdit: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = if (onEdit != null) 0.dp else 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            color  = Color(speaker.colorArgb),
            shape  = CircleShape,
            modifier = Modifier.size(10.dp)
        ) {}
        Text(text = speaker.name, modifier = Modifier.weight(1f))
        if (onEdit != null) {
            IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = "Edit",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
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
