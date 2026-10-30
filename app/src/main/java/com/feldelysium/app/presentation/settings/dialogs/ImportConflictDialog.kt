package com.feldelysium.app.presentation.settings.dialogs

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.feldelysium.app.R
import com.feldelysium.app.domain.model.ImportPolicy
import com.feldelysium.app.domain.model.ImportPreview
import com.feldelysium.designsystem.components.button.FeldDialogActions
import com.feldelysium.designsystem.components.container.FeldDialog
import com.feldelysium.designsystem.components.input.FeldChip
import com.feldelysium.designsystem.components.input.FeldAccentColors
import com.feldelysium.designsystem.theme.FeldPalette

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ImportConflictDialog(preview: ImportPreview, onConfirm: (ImportPolicy) -> Unit, onDismiss: () -> Unit) {
    var policy by remember { mutableStateOf(ImportPolicy.SKIP) }
    val names = preview.conflicts.take(5).joinToString { it.name } +
        if (preview.conflicts.size > 5) "…" else ""

    FeldDialog(
        title = stringResource(R.string.import_conflict_title),
        eyebrow = stringResource(R.string.import_conflict_eyebrow),
        onDismiss = onDismiss,
        footer = {
            FeldDialogActions(
                confirmLabel = stringResource(R.string.import_conflict_confirm),
                onConfirm = { onConfirm(policy) },
                onCancel = onDismiss,
                cancelLabel = stringResource(R.string.common_cancel),
            )
        },
    ) {
        Text(
            stringResource(
                R.string.import_conflict_count,
                preview.conflicts.size,
                names,
            ),
            style = MaterialTheme.typography.bodyMedium,
        )

        Spacer(Modifier.height(16.dp))

        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(
                ImportPolicy.SKIP to R.string.import_conflict_skip,
                ImportPolicy.REPLACE to R.string.import_conflict_replace,
                ImportPolicy.KEEP_BOTH to R.string.import_conflict_keep_both,
            ).forEach { (p, labelRes) ->
                FeldChip(
                    text = stringResource(labelRes),
                    selected = policy == p,
                    onClick = { policy = p },
                    modifier = Modifier.height(34.dp),
                    accent = FeldAccentColors.Cream,
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        Text(
            stringResource(
                when (policy) {
                    ImportPolicy.SKIP ->
                        R.string.import_conflict_skip_description

                    ImportPolicy.REPLACE ->
                        R.string.import_conflict_replace_description

                    ImportPolicy.KEEP_BOTH ->
                        R.string.import_conflict_keep_both_description
                },
            ),
            style = MaterialTheme.typography.labelSmall,
            color = FeldPalette.Muted,
            modifier = Modifier.padding(bottom = 4.dp),
        )

        if (preview.bundledSkipped > 0) {
            Text(
                stringResource(
                    R.string.import_conflict_bundled_skipped,
                    preview.bundledSkipped,
                ),
                style = MaterialTheme.typography.labelSmall,
                color = FeldPalette.Muted,
            )
        }
    }
}
