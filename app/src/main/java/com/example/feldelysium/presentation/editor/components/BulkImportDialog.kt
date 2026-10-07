package com.example.feldelysium.presentation.editor.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.feldelysium.presentation.editor.ImportPreview

@Preview
@Composable
private fun BulkImportDialogPreview() {
    BulkImportDialog(
        text = "VOLITION:\nThis city is trying to kill you.\n\nKIM KITSURAGI:\nLet's focus.",
        preview = ImportPreview(lineCount = 2, newSpeakerCount = 0),
        onText = {},
        onConfirm = {},
        onDismiss = {},
    )
}

@Composable
fun BulkImportDialog(
    text: String,
    preview: ImportPreview?,
    onText: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Bulk Import") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text  = "Paste dialogue in SPEAKER:\nText format.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value         = text,
                    onValueChange = onText,
                    modifier      = Modifier.fillMaxWidth().heightIn(min = 160.dp),
                    placeholder   = { Text("VOLITION:\nThis city is trying to kill you.\n\nKIM KITSURAGI:\nLet's focus.") },
                )
                if (preview != null) {
                    Text(
                        text  = preview.caption,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) { Text("Import") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
