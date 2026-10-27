package com.example.feldelysium.presentation.shared.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.feldelysium.R
import com.example.feldelysium.designsystem.components.button.FeldDialogActions
import com.example.feldelysium.designsystem.components.container.FeldHandDiamond
import com.example.feldelysium.designsystem.components.container.FeldDialog
import com.example.feldelysium.designsystem.components.container.FeldDivider
import com.example.feldelysium.designsystem.components.content.FeldIcon
import com.example.feldelysium.designsystem.components.content.FeldIconId
import com.example.feldelysium.designsystem.components.input.FeldChip
import com.example.feldelysium.designsystem.components.input.FeldChipColors
import com.example.feldelysium.designsystem.components.input.FeldColorSwatches
import com.example.feldelysium.designsystem.components.input.FeldTextField
import com.example.feldelysium.designsystem.foundation.material.FeldMarker
import com.example.feldelysium.designsystem.components.input.FeldPalette
import com.example.feldelysium.designsystem.components.input.feldClickable
import com.example.feldelysium.domain.model.SoundKeys
import com.example.feldelysium.domain.model.Speaker
import com.example.feldelysium.domain.model.SpeakerColor
import com.example.feldelysium.domain.model.SpeakerType
import com.example.feldelysium.domain.registry.PredefinedSpeakers
import com.example.feldelysium.presentation.editor.entry.FieldLabel

@Composable
fun ManageTagsDialog(
    tags: List<SpeakerType>,
    speakers: List<Speaker>,
    onSave: (SpeakerType) -> Unit,
    onDelete: (SpeakerType) -> Unit,
    onDismiss: () -> Unit,
) {
    var editing by remember { mutableStateOf<SpeakerType?>(null) }
    var deleting by remember { mutableStateOf<SpeakerType?>(null) }
    val counts = remember(speakers) {
        speakers.groupingBy { it.type.id }.eachCount()
    }

    FeldDialog(
        title = stringResource(R.string.tags_title),
        eyebrow = stringResource(R.string.tags_manage_eyebrow),
        onDismiss = onDismiss,
        footer = {
            FeldDialogActions(
                confirmLabel = stringResource(R.string.common_done),
                onConfirm = onDismiss,
            )
        },
    ) {
        val list = tags.filterNot { it.isCustom }

        if (list.none { !PredefinedSpeakers.isBundledTag(it.id) }) {
            Text(
                stringResource(R.string.tags_empty_message),
                style = MaterialTheme.typography.bodySmall,
                color = FeldPalette.Muted,
                modifier = Modifier.padding(bottom = 12.dp),
            )
        }

        list.forEachIndexed { i, tag ->
            if (i > 0) FeldDivider(Modifier.padding(vertical = 2.dp))

            val builtIn = PredefinedSpeakers.isBundledTag(tag.id)

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FeldHandDiamond(
                    tag.colorArgb?.let { Color(it) } ?: FeldPalette.Muted,
                    tag.id.hashCode(),
                    14.dp,
                )
                Spacer(Modifier.width(12.dp))

                Text(
                    tag.name.uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    "${counts[tag.id] ?: 0}",
                    style = MaterialTheme.typography.labelSmall,
                    color = FeldPalette.Muted,
                )
                Spacer(Modifier.width(12.dp))

                if (builtIn) {
                    Text(
                        stringResource(R.string.tags_built_in),
                        style = MaterialTheme.typography.labelSmall,
                        color = FeldPalette.Muted,
                    )
                } else {
                    FeldIcon(
                        FeldIconId.Edit,
                        size = 20.dp,
                        modifier = Modifier.feldClickable { editing = tag },
                    )
                    Spacer(Modifier.width(14.dp))
                    FeldIcon(
                        FeldIconId.Delete,
                        markerFilmSpec = FeldMarker.Dry,
                        size = 20.dp,
                        modifier = Modifier.feldClickable { deleting = tag },
                    )
                }
            }
        }
    }

    editing?.let { tag ->
        EditTagDialog(
            tag = tag,
            takenNames = tags.filter { it.id != tag.id }
                .map { it.name.lowercase() }
                .toSet(),
            onSave = {
                onSave(it)
                editing = null
            },
            onDismiss = { editing = null },
        )
    }

    deleting?.let { tag ->
        val n = counts[tag.id] ?: 0

        FeldDialog(
            title = stringResource(R.string.tags_delete_title),
            eyebrow = tag.name,
            onDismiss = { deleting = null },
            footer = {
                FeldDialogActions(
                    confirmLabel = stringResource(R.string.common_delete),
                    onConfirm = {
                        onDelete(tag)
                        deleting = null
                    },
                    onCancel = { deleting = null },
                )
            },
        ) {
            Text(
                if (n == 0) {
                    stringResource(R.string.tags_delete_unused_message)
                } else {
                    stringResource(R.string.tags_delete_used_message, n)
                },
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EditTagDialog(
    tag: SpeakerType,
    takenNames: Set<String>,
    onSave: (SpeakerType) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf(tag.name) }
    var color by remember { mutableStateOf(tag.colorArgb) }
    var sound by remember { mutableStateOf(tag.soundKey) }

    val clean = SpeakerType.normalizeName(name)
    val valid = clean.isNotEmpty() && clean.lowercase() !in takenNames

    val palette = remember {
        val c = tag.colorArgb
        if (c != null && SpeakerColor.isCustom(c)) {
            SpeakerColor.AllArgb + c
        } else {
            SpeakerColor.AllArgb
        }
    }

    var allowsChecks by remember { mutableStateOf(tag.allowsChecks) }

    FeldDialog(
        title = stringResource(R.string.tags_edit_title),
        eyebrow = tag.name,
        onDismiss = onDismiss,
        footer = {
            FeldDialogActions(
                confirmLabel = stringResource(R.string.common_save),
                onConfirm = {
                    onSave(
                        tag.copy(
                            name = clean,
                            colorArgb = color,
                            soundKey = sound,
                            allowsChecks = allowsChecks,
                        ),
                    )
                },
                onCancel = onDismiss,
                confirmEnabled = valid,
            )
        },
    ) {
        FieldLabel(stringResource(R.string.tags_name_label))
        FeldTextField(
            value = name,
            onValueChange = { name = it },
            maxLength = SpeakerType.MAX_NAME,
            showLength = false,
            minHeight = 48.dp,
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
        )

        if (clean.lowercase() in takenNames) {
            Text(
                stringResource(R.string.tags_duplicate_name),
                style = MaterialTheme.typography.labelSmall,
                color = FeldPalette.Orange,
            )
        }

        Spacer(Modifier.height(18.dp))
        FieldLabel(stringResource(R.string.tags_default_color), optional = true)
        FeldChip(
            text = stringResource(R.string.common_none),
            selected = color == null,
            onClick = { color = null },
            modifier = Modifier.height(30.dp),
            accent = FeldChipColors.Cream,
        )

        Spacer(Modifier.height(10.dp))
        FeldColorSwatches(
            colors = palette,
            selected = color ?: 0L,
            onSelect = { color = it },
            columns = 6,
            minRows = 2,
        )

        Spacer(Modifier.height(18.dp))
        FieldLabel(stringResource(R.string.tags_sound_label))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SoundKeys.All.forEach { key ->
                val on = (sound ?: SoundKeys.DEFAULT) == key
                FeldChip(
                    text = key,
                    selected = on,
                    onClick = {
                        sound = key.takeUnless { it == SoundKeys.DEFAULT }
                    },
                    modifier = Modifier.height(30.dp),
                    accent = FeldChipColors.Orange,
                )
            }
        }

        Spacer(Modifier.height(18.dp))
        FieldLabel(stringResource(R.string.tags_skill_checks_label))
        FeldChip(
            text = stringResource(R.string.tags_allow_skill_checks),
            selected = allowsChecks,
            onClick = { allowsChecks = !allowsChecks },
            modifier = Modifier.height(30.dp),
            accent = FeldChipColors.Teal,
        )
    }
}
