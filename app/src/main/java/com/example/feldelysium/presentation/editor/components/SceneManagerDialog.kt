package com.example.feldelysium.presentation.editor.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.feldelysium.designsystem.components.button.FeldDialogActions
import com.example.feldelysium.designsystem.components.button.FeldIconButton
import com.example.feldelysium.designsystem.components.button.FeldTextButton
import com.example.feldelysium.designsystem.components.dialog.FeldDialog
import com.example.feldelysium.designsystem.components.display.FeldIconId
import com.example.feldelysium.designsystem.foundation.utils.FeldPalette
import com.example.feldelysium.designsystem.foundation.utils.feldClickable
import com.example.feldelysium.domain.model.SceneInfo

@Composable
fun SceneManagerDialog(
    scenes: List<SceneInfo>,
    currentId: String,
    onNew: () -> Unit,
    onOpen: (String) -> Unit,
    onDelete: (String) -> Unit,
    onExport: () -> Unit,
    onImport: () -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit,
) {
    var confirmClear by remember { mutableStateOf(false) }

    FeldDialog(
        title = "Scenes",
        eyebrow = "Scene manager",
        onDismiss = onDismiss,
        footer = {
            FeldDialogActions(confirmLabel = "Done", onConfirm = onDismiss, onCancel = onDismiss)
        },
    ) {
        FieldLabel("Current scene")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FeldTextButton("New", onClick = onNew)
            FeldTextButton("Import", onClick = onImport)
            FeldTextButton("Export", onClick = onExport)
        }
        Spacer(Modifier.height(8.dp))
        FeldTextButton(
            if (confirmClear) "Tap again to clear all lines" else "Clear lines",
            onClick = { if (confirmClear) { onClear(); confirmClear = false } else confirmClear = true },
        )

        Spacer(Modifier.height(20.dp))
        FieldLabel("Saved scenes")
        scenes.forEach { s ->
            Row(
                Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f).feldClickable(onClick = { onOpen(s.id) })) {
                    Text(
                        s.title.uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (s.id == currentId) FeldPalette.Orange else MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        "${s.lines} lines",
                        style = MaterialTheme.typography.labelSmall,
                        color = FeldPalette.Muted,
                    )
                }
                FeldIconButton(FeldIconId.Delete, onClick = { onDelete(s.id) }, tint = FeldPalette.Muted)
            }
        }
    }
}
