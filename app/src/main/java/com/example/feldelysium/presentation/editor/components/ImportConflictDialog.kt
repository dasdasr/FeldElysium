package com.example.feldelysium.presentation.editor.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.feldelysium.designsystem.components.button.FeldDialogActions
import com.example.feldelysium.designsystem.components.dialog.FeldDialog
import com.example.feldelysium.designsystem.components.input.FeldChip
import com.example.feldelysium.designsystem.components.input.FeldChipColors
import com.example.feldelysium.designsystem.foundation.utils.FeldPalette
import com.example.feldelysium.domain.model.ImportPolicy
import com.example.feldelysium.domain.model.ImportPreview

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ImportConflictDialog(preview: ImportPreview, onConfirm: (ImportPolicy) -> Unit, onDismiss: () -> Unit) {
    var policy by remember { mutableStateOf(ImportPolicy.SKIP) }
    val names = preview.conflicts.take(5).joinToString { it.name } +
        if (preview.conflicts.size > 5) "…" else ""

    FeldDialog(
        title = "Already exists",
        eyebrow = "Import pack",
        onDismiss = onDismiss,
        footer = {
            FeldDialogActions(
                confirmLabel = "Import",
                onConfirm = { onConfirm(policy) },
                onCancel = onDismiss,
            )
        },
    ) {
        Text(
            "${preview.conflicts.size} speaker(s) from the pack are already in your list: $names",
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(Modifier.height(16.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(
                ImportPolicy.SKIP to "Skip",
                ImportPolicy.REPLACE to "Replace",
                ImportPolicy.KEEP_BOTH to "Keep both",
            ).forEach { (p, label) ->
                FeldChip(
                    label,
                    selected = policy == p,
                    onClick = { policy = p },
                    modifier = Modifier.height(34.dp),
                    accent = FeldChipColors.Cream,
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        Text(
            when (policy) {
                ImportPolicy.SKIP -> "Existing speakers stay as they are."
                ImportPolicy.REPLACE -> "Existing speakers are overwritten by the pack versions."
                ImportPolicy.KEEP_BOTH -> "Pack speakers are added as new copies."
            },
            style = MaterialTheme.typography.labelSmall,
            color = FeldPalette.Muted,
            modifier = Modifier.padding(bottom = 4.dp),
        )
        if (preview.bundledSkipped > 0) {
            Text(
                "${preview.bundledSkipped} built-in speaker(s) in the pack are always skipped.",
                style = MaterialTheme.typography.labelSmall,
                color = FeldPalette.Muted,
            )
        }
    }
}
