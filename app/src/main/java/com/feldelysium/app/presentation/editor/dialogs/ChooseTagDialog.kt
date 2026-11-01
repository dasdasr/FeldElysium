package com.feldelysium.app.presentation.editor.dialogs

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.feldelysium.app.R
import com.feldelysium.app.domain.model.SpeakerType
import com.feldelysium.designsystem.components.button.FeldDialogActions
import com.feldelysium.designsystem.components.container.FeldDialog
import com.feldelysium.designsystem.components.input.FeldAccentColors
import com.feldelysium.designsystem.components.input.FeldChip
import com.feldelysium.designsystem.components.input.FeldTextField

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
        title = stringResource(R.string.choose_tag_title),
        eyebrow = stringResource(R.string.choose_tag_eyebrow),
        onDismiss = onDismiss,
        footer = {
            FeldDialogActions(
                confirmLabel = stringResource(R.string.choose_tag_select),
                onConfirm = { onConfirm(current) },
                onCancel = onDismiss,
                cancelLabel = stringResource(R.string.common_cancel),
            )
        },
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            all.forEach { t ->
                FeldChip(
                    text = if (t.isCustom) {
                        stringResource(R.string.choose_tag_none)
                    } else {
                        t.name
                    },
                    selected = t.id == current.id,
                    onClick = { current = t },
                    height = 34.dp,
                    accent = FeldAccentColors.Cream,
                )
            }
            FeldChip(
                text = stringResource(R.string.choose_tag_new),
                selected = creating,
                onClick = { creating = !creating },
                height = 34.dp,
                accent = FeldAccentColors.Orange,
            )
            if (onManage != null) {
                FeldChip(
                    text = stringResource(R.string.choose_tag_edit),
                    selected = false,
                    onClick = onManage,
                    height = 34.dp,
                    accent = FeldAccentColors.Cream,
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
                    placeholder = stringResource(R.string.choose_tag_name_placeholder),
                    maxLength = SpeakerType.MAX_NAME,
                    showLength = false,
                    minHeight = 44.dp,
                    contentPadding = PaddingValues(
                        horizontal = 14.dp,
                        vertical = 10.dp,
                    ),
                )

                FeldChip(
                    text = stringResource(R.string.choose_tag_allow_checks),
                    selected = allowsChecks,
                    onClick = { allowsChecks = !allowsChecks },
                    height = 30.dp,
                    accent = FeldAccentColors.Teal,
                    outline = false,
                )

                FeldChip(
                    text = stringResource(R.string.choose_tag_add),
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
                    height = 34.dp,
                    accent = FeldAccentColors.Orange,
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
