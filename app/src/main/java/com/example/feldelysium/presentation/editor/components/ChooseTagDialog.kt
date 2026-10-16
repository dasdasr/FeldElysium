package com.example.feldelysium.presentation.editor.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.feldelysium.designsystem.components.button.FeldDialogActions
import com.example.feldelysium.designsystem.components.dialog.FeldDialog
import com.example.feldelysium.designsystem.components.input.FeldChip
import com.example.feldelysium.designsystem.components.input.FeldChipColors
import com.example.feldelysium.designsystem.components.input.FeldTextField
import com.example.feldelysium.domain.model.SpeakerType

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChooseTagDialog(
    existing: List<SpeakerType>,
    selected: SpeakerType,
    onConfirm: (SpeakerType) -> Unit,
    onDismiss: () -> Unit,
    onManage: (() -> Unit)? = null,
) {
    var current by remember { mutableStateOf(selected) }
    var created by remember { mutableStateOf(emptyList<SpeakerType>()) }
    var creating by remember { mutableStateOf(false) }
    var draft by remember { mutableStateOf("") }
    var allowsChecks by remember { mutableStateOf(false) }

    val all = (listOf(SpeakerType.CUSTOM) + existing + created + current).distinctBy { it.id }

    FeldDialog(
        title = "Choose tag",
        eyebrow = "Speaker tag",
        onDismiss = onDismiss,
        footer = {
            FeldDialogActions(
                confirmLabel = "Select",
                onConfirm = { onConfirm(current) },
                onCancel = onDismiss,
            )
        },
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            all.forEach { t ->
                FeldChip(
                    text = if (t.isCustom) "No tag" else t.name,
                    selected = t.id == current.id,
                    onClick = { current = t },
                    modifier = Modifier.height(34.dp),
                    accent = FeldChipColors.Cream,
                )
            }
            FeldChip(
                text = "+ New",
                selected = creating,
                onClick = { creating = !creating },
                modifier = Modifier.height(34.dp),
                accent = FeldChipColors.Orange,
            )
            if (onManage != null) {
                FeldChip(
                    text = "Edit tags",
                    selected = false,
                    onClick = onManage,
                    modifier = Modifier.height(34.dp),
                    accent = FeldChipColors.Cream,
                    outline = false,
                )
            }
        }

        if (creating) {
            Spacer(Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FeldTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    modifier = Modifier.weight(1f),
                    placeholder = "Tag name",
                    maxLength = SpeakerType.MAX_NAME,
                    showLength = false,
                    minHeight = 44.dp,
                    contentPadding = PaddingValues(
                        horizontal = 14.dp,
                        vertical = 10.dp,
                    ),
                )

                FeldChip(
                    text = "Allow checks",
                    selected = allowsChecks,
                    onClick = { allowsChecks = !allowsChecks },
                    modifier = Modifier.height(30.dp),
                    accent = FeldChipColors.Teal,
                    outline = false,
                )

                FeldChip(
                    text = "Add",
                    selected = false,
                    enabled = draft.isNotBlank(),
                    onClick = {
                        val t = SpeakerType.create(
                            draft,
                            allowsChecks = allowsChecks,
                        )
                        if (t != null) {
                            val known = all.firstOrNull { it.id == t.id }
                            if (known == null) created = created + t
                            current = known ?: t
                        }
                        draft = ""
                        allowsChecks = false
                        creating = false
                    },
                    modifier = Modifier.height(34.dp),
                    accent = FeldChipColors.Orange,
                )
            }
        }
    }
}

@Preview
@Composable
private fun ChooseTagDialogPreview() {
    ChooseTagDialog(
        existing = listOf(SpeakerType.NPC),
        selected = SpeakerType.NPC,
        onConfirm = {},
        onDismiss = {},
    )
}
