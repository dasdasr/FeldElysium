package com.example.feldelysium.presentation.editor.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.feldelysium.domain.model.DialogueEntry
import com.example.feldelysium.domain.model.Speaker
import com.example.feldelysium.domain.registry.PredefinedSpeakers
import com.example.feldelysium.designsystem.components.input.FeldTextField

@Preview
@Composable
private fun EntryEditSheetPreview() {
    EntryEditSheet(
        entry = DialogueEntry(
            id = "-1",
            speaker = PredefinedSpeakers.MEASUREHEAD,
            text = "You vs al-ghoul",
        ),
        speaker = PredefinedSpeakers.MEASUREHEAD,
        onConfirm = {},
    ) { }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryEditSheet(
    entry: DialogueEntry?,
    speaker: Speaker?,
    onConfirm: (text: String) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember(entry) { mutableStateOf(entry?.text ?: "") }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            speaker?.let {
                Text(
                    text = it.name.uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(it.colorArgb)
                )
            }

            FeldTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = "Dialogue text",
                minLines = 4,
                maxLength = 500,
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }

                Button(
                    onClick = {
                        if (text.isNotBlank()) {
                            onConfirm(text.trim())
                        }
                    },
                    enabled = text.isNotBlank()
                ) {
                    Text(if (entry == null) "Add" else "Save")
                }
            }
        }
    }
}
