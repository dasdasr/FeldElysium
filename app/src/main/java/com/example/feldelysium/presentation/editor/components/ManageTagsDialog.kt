package com.example.feldelysium.presentation.editor.components

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
import androidx.compose.ui.unit.dp
import com.example.feldelysium.designsystem.components.button.FeldDialogActions
import com.example.feldelysium.designsystem.components.card.FeldHandDiamond
import com.example.feldelysium.designsystem.components.dialog.FeldDialog
import com.example.feldelysium.designsystem.components.display.FeldDivider
import com.example.feldelysium.designsystem.components.display.FeldIcon
import com.example.feldelysium.designsystem.components.display.FeldIconId
import com.example.feldelysium.designsystem.components.input.FeldChip
import com.example.feldelysium.designsystem.components.input.FeldChipColors
import com.example.feldelysium.designsystem.components.input.FeldColorSwatches
import com.example.feldelysium.designsystem.components.input.FeldTextField
import com.example.feldelysium.designsystem.foundation.utils.FeldPalette
import com.example.feldelysium.designsystem.foundation.utils.feldClickable
import com.example.feldelysium.domain.model.SoundKeys
import com.example.feldelysium.domain.model.Speaker
import com.example.feldelysium.domain.model.SpeakerColor
import com.example.feldelysium.domain.model.SpeakerType
import com.example.feldelysium.domain.registry.PredefinedSpeakers

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
    val counts = remember(speakers) { speakers.groupingBy { it.type.id }.eachCount() }

    FeldDialog(
        title = "Tags",
        eyebrow = "Manage tags",
        onDismiss = onDismiss,
        footer = { FeldDialogActions(confirmLabel = "Done", onConfirm = onDismiss) },
    ) {
        val list = tags.filterNot { it.isCustom }
        if (list.none { !PredefinedSpeakers.isBundledTag(it.id) }) {
            Text(
                "You have no tags yet. Create one while choosing a tag for a speaker.",
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
                        "BUILT-IN",
                        style = MaterialTheme.typography.labelSmall,
                        color = FeldPalette.Muted,
                    )
                } else {
                    FeldIcon(
                        FeldIconId.Edit,
                        tint = FeldPalette.Muted,
                        size = 20.dp,
                        modifier = Modifier.feldClickable { editing = tag },
                    )
                    Spacer(Modifier.width(14.dp))
                    FeldIcon(
                        FeldIconId.Delete,
                        tint = FeldPalette.Orange,
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
            takenNames = tags.filter { it.id != tag.id }.map { it.name.lowercase() }.toSet(),
            onSave = {
                onSave(it);
                editing = null
            },
            onDismiss = { editing = null },
        )
    }

    deleting?.let { tag ->
        val n = counts[tag.id] ?: 0
        FeldDialog(
            title = "Delete tag",
            eyebrow = tag.name,
            onDismiss = { deleting = null },
            footer = {
                FeldDialogActions(
                    confirmLabel = "Delete",
                    onConfirm = {
                        onDelete(tag);
                        deleting = null
                    },
                    onCancel = { deleting = null },
                )
            },
        ) {
            Text(
                if (n == 0) {
                    "This tag is not used by any speaker."
                } else {
                    "$n speaker(s) will lose this tag and move to Custom. Their colors stay."
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
        if (c != null && SpeakerColor.isCustom(c)) SpeakerColor.AllArgb + c else SpeakerColor.AllArgb
    }

    FeldDialog(
        title = "Edit tag",
        eyebrow = tag.name,
        onDismiss = onDismiss,
        footer = {
            FeldDialogActions(
                confirmLabel = "Save",
                onConfirm = { onSave(tag.copy(name = clean, colorArgb = color, soundKey = sound)) },
                onCancel = onDismiss,
                confirmEnabled = valid,
            )
        },
    ) {
        FieldLabel("Name")
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
                "A tag with this name already exists",
                style = MaterialTheme.typography.labelSmall,
                color = FeldPalette.Orange,
            )
        }

        Spacer(Modifier.height(18.dp))
        FieldLabel("Default color", optional = true)
        FeldChip(
            text = "None",
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
        FieldLabel("Sound")
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SoundKeys.All.forEach { key ->
                val on = (sound ?: SoundKeys.DEFAULT) == key
                FeldChip(
                    text = key,
                    selected = on,
                    onClick = { sound = key.takeUnless { it == SoundKeys.DEFAULT } },
                    modifier = Modifier.height(30.dp),
                    accent = FeldChipColors.Orange,
                )
            }
        }
    }
}
