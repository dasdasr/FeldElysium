package com.example.feldelysium.presentation.editor.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.feldelysium.designsystem.components.button.FeldDialogActions
import com.example.feldelysium.designsystem.components.button.FeldIconButton
import com.example.feldelysium.designsystem.components.button.FeldLabeledBlock
import com.example.feldelysium.designsystem.components.card.FeldCard
import com.example.feldelysium.designsystem.components.card.FeldHandDiamond
import com.example.feldelysium.designsystem.components.display.FeldIconId
import com.example.feldelysium.designsystem.components.input.FeldTextField
import com.example.feldelysium.designsystem.foundation.material.FeldPaper
import com.example.feldelysium.designsystem.foundation.shape.RoughStyle
import com.example.feldelysium.designsystem.foundation.utils.FeldPalette
import com.example.feldelysium.designsystem.foundation.utils.feldClickable
import com.example.feldelysium.domain.model.DialogueEntry
import com.example.feldelysium.domain.model.Speaker
import com.example.feldelysium.domain.model.SpeakerType
import com.example.feldelysium.domain.registry.PredefinedSpeakers

private val SheetRough = RoughStyle(
    roughness = 0.8.dp, step = 44.dp, corner = 4.dp,
    cornerVariation = 0.2f, cornerJitter = 0.2f,
    tornChance = 0.03f, jerkChance = 0.04f,
)

private val SheetBleed = 28.dp

@Preview
@Composable
private fun EntryEditSheetPreview() {
    EntryEditSheet(
        entry = DialogueEntry(
            id = "-1",
            speaker = PredefinedSpeakers.MEASUREHEAD,
            text = "You vs al-ghoul",
        ),
        speaker = PredefinedSpeakers.MEASUREHEAD,
        onConfirm = {},
        onDismiss = {},
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryEditSheet(
    entry: DialogueEntry?,
    speaker: Speaker?,
    onConfirm: (text: String) -> Unit,
    onDismiss: () -> Unit,
    onSpeakerClick: (() -> Unit)? = null,
    onEditSpeaker: ((Speaker) -> Unit)? = null,
    onStyleClick: (() -> Unit)? = null,
    styleName: String = "Normal",
) {
    var text by remember(entry?.id) { mutableStateOf(entry?.text ?: "") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.Transparent,
        dragHandle = null,
        shape = RectangleShape,
        tonalElevation = 0.dp,
        scrimColor = Color.Black.copy(alpha = 0.6f),
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
    ) {
        FeldCard(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .bleedBottom(SheetBleed),
            spec = FeldPaper.Entry,
            rough = SheetRough,
            contentPadding = PaddingValues(
                start = 20.dp, end = 20.dp, top = 12.dp, bottom = SheetBleed,
            ),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (entry == null) "NEW ENTRY" else "EDIT ENTRY",
                    style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.8.sp),
                    color = FeldPalette.Muted,
                    modifier = Modifier.weight(1f),
                )
                FeldIconButton(FeldIconId.Close, onDismiss)
            }

            SpeakerHeader(
                speaker = speaker,
                onClick = onSpeakerClick,
                onEdit = if (speaker != null && speaker.type == SpeakerType.CUSTOM && onEditSpeaker != null) {
                    { onEditSpeaker(speaker) }
                } else null,
            )

            Spacer(Modifier.height(8.dp))
            FeldLabeledBlock("Dialogue") {
                FeldTextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = "Dialogue text",
                    minLines = 4,
                    maxLength = 500,
                )
            }

            StyleRow(styleName = styleName, onClick = onStyleClick)

            Spacer(Modifier.height(12.dp))
            FeldDialogActions(
                confirmLabel = "Confirm",
                onConfirm = { if (text.isNotBlank()) onConfirm(text.trim()) },
                onCancel = onDismiss,
                confirmEnabled = text.isNotBlank(),
                modifier = Modifier.navigationBarsPadding(),
            )
        }
    }
}

private fun Modifier.bleedBottom(bleed: Dp): Modifier = layout { measurable, constraints ->
    val placeable = measurable.measure(constraints)
    val b = bleed.roundToPx()
    layout(placeable.width, (placeable.height - b).coerceAtLeast(0)) {
        placeable.placeRelative(0, 0)
    }
}

@Composable
private fun Chevron() {
    Icon(
        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
        contentDescription = null,
        tint = FeldPalette.Muted,
        modifier = Modifier.size(22.dp),
    )
}

@Composable
private fun SpeakerHeader(
    speaker: Speaker?,
    onClick: (() -> Unit)?,
    onEdit: (() -> Unit)?,
) {
    val color = speaker?.let { Color(it.colorArgb) } ?: FeldPalette.Muted
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.feldClickable(onClick = onClick) else Modifier)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FeldHandDiamond(color, speaker?.id.hashCode(), 28.dp)
        Spacer(Modifier.width(14.dp))
        Text(
            text = (speaker?.name ?: "No speaker").uppercase(),
            style = MaterialTheme.typography.titleMedium.copy(letterSpacing = 1.4.sp),
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        if (onEdit != null) {
            Box(
                Modifier.size(40.dp).feldClickable(onClick = onEdit),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Sort,
                    contentDescription = "Edit speaker",
                    tint = FeldPalette.Muted,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
        if (onClick != null) Chevron()
    }
}

@Composable
private fun StyleRow(styleName: String, onClick: (() -> Unit)?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.feldClickable(onClick = onClick) else Modifier)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Filled.Tune,
            contentDescription = null,
            tint = FeldPalette.Muted,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = "STYLE",
            style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.4.sp),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
        )
        Spacer(Modifier.weight(1f))
        Text(
            text = styleName,
            style = MaterialTheme.typography.bodyMedium,
            color = FeldPalette.Muted,
        )
        if (onClick != null) {
            Spacer(Modifier.width(4.dp))
            Chevron()
        }
    }
}
