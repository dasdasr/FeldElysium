package com.example.feldelysium.presentation.editor.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.feldelysium.designsystem.components.FeldPalette
import com.example.feldelysium.designsystem.components.button.FeldDialogActions
import com.example.feldelysium.designsystem.components.dialog.FeldDialog
import com.example.feldelysium.designsystem.components.input.FeldTextField
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
    onDismiss: () -> Unit,
) {
    FeldDialog(
        title = "Bulk import",
        eyebrow = "Import",
        subtitle = "Paste dialogue as SPEAKER: text.",
        onDismiss = onDismiss,
        seed = 18,
        footer = {
            FeldDialogActions(
                confirmLabel = "Import",
                onConfirm = onConfirm,
                onCancel = onDismiss,
                seed = 16,
            )
        },
    ) {
        FeldTextField(
            value = text,
            onValueChange = onText,
            placeholder = "VOLITION:\nThis city is trying to kill you.\n\nKIM KITSURAGI:\nLet's focus.",
            minLines = 8,
            seed = 2,
        )
        if (preview != null) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = preview.caption.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = FeldPalette.Muted,
            )
        }
    }
}
