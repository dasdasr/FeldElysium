package com.example.feldelysium.presentation.editor.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Preview
@Composable
private fun BulkImportDialogPreview() {
    BulkImportDialog(
        text = "VOLITION:\nThis city is trying to kill you.\n\nKIM KITSURAGI:\nLet's focus.",
        error = null,
        onText = {},
        onConfirm = {},
        onDismiss = {},
    )
}

@Composable
fun BulkImportDialog(
    text: String,
    error: String?,
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
                    isError       = error != null
                )
                if (error != null) {
                    Text(
                        text  = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onConfirm, enabled = text.isNotBlank()) { Text("Import") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}