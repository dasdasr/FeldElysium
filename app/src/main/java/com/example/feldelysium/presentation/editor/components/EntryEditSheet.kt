package com.example.feldelysium.presentation.editor.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.feldelysium.designsystem.components.FeldDiamond
import com.example.feldelysium.designsystem.components.FeldDivider
import com.example.feldelysium.designsystem.components.button.FeldIconButton
import com.example.feldelysium.designsystem.components.FeldIconId
import com.example.feldelysium.designsystem.components.FeldPalette
import com.example.feldelysium.designsystem.components.button.FeldDialogActions
import com.example.feldelysium.designsystem.components.button.FeldLabeledBlock
import com.example.feldelysium.designsystem.components.card.FeldCard
import com.example.feldelysium.designsystem.components.feldClickable
import com.example.feldelysium.designsystem.components.input.FeldTextField
import com.example.feldelysium.designsystem.components.speaker.FeldSpeakerDiamond
import com.example.feldelysium.designsystem.material.FeldPaper
import com.example.feldelysium.designsystem.material.paperMaterial
import com.example.feldelysium.designsystem.seed.rememberSeed
import com.example.feldelysium.designsystem.shape.RoughShape
import com.example.feldelysium.designsystem.shape.RoughStyle
import com.example.feldelysium.domain.model.DialogueEntry
import com.example.feldelysium.domain.model.Speaker
import com.example.feldelysium.domain.model.SpeakerType
import com.example.feldelysium.domain.registry.PredefinedSpeakers

private val SheetRough = RoughStyle(roughness = 1.1.dp, corner = 18.dp)
private val SelectorRough =
    RoughStyle(roughness = 0.8.dp, step = 48.dp, corner = 4.dp, tornChance = 0.1f)

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
            spec = FeldPaper.flat(Color(0xFF080D0D)),
            rough = SheetRough,
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = 16.dp,
                bottom = SheetBleed
            ),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                FeldDiamond(FeldPalette.Orange, size = 14.dp)
                Text(
                    text = if (entry == null) "NEW ENTRY" else "EDIT ENTRY",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 18.sp,
                        letterSpacing = 1.6.sp,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(start = 12.dp),
                )
                Spacer(Modifier.weight(1f))
                FeldIconButton(FeldIconId.Close, onDismiss)
            }
            Spacer(Modifier.height(8.dp))
            FeldDivider()

            FeldLabeledBlock("Speaker") {
                SpeakerRow(
                    speaker = speaker,
                    onClick = onSpeakerClick,
                    onEdit = if (speaker != null && speaker.type == SpeakerType.CUSTOM && onEditSpeaker != null) {
                        { onEditSpeaker(speaker) }
                    } else null,
                )
            }
            FeldDivider()

            FeldLabeledBlock("Dialogue") {
                FeldTextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = "Dialogue text",
                    minLines = 4,
                    maxLength = 500,
                )
            }
            FeldDivider()

            StyleBlock(styleName = styleName, onClick = onStyleClick)
            FeldDivider()

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
private fun SelectorBox(
    modifier: Modifier,
    seed: Int = rememberSeed(),
    onClick: (() -> Unit)?,
    content: @Composable RowScope.() -> Unit,
) {
    val shape = remember(seed) { RoughShape(seed, SelectorRough) }
    Row(
        modifier = modifier
            .paperMaterial(shape, FeldPaper.Field)
            .then(if (onClick != null) Modifier.feldClickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}

@Composable
private fun SquareButton(
    icon: ImageVector,
    description: String,
    onClick: () -> Unit,
) {
    val seed = rememberSeed()
    val shape = remember(seed) { RoughShape(seed, SelectorRough) }
    Box(
        modifier = Modifier
            .size(60.dp)
            .paperMaterial(shape, FeldPaper.Field)
            .feldClickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(24.dp),
        )
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
private fun SpeakerRow(
    speaker: Speaker?,
    onClick: (() -> Unit)?,
    onEdit: (() -> Unit)?,
) {
    val color = speaker?.let { Color(it.colorArgb) } ?: FeldPalette.Muted

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        SelectorBox(
            modifier = Modifier
                .weight(1f)
                .height(60.dp),
            onClick = onClick,
        ) {
            FeldSpeakerDiamond(color = color, size = 34.dp, seed = speaker?.id.hashCode())
            Spacer(Modifier.width(14.dp))
            Text(
                text = (speaker?.name ?: "No speaker").uppercase(),
                style = MaterialTheme.typography.labelLarge,
                color = color,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            if (onClick != null) Chevron()
        }
        if (onEdit != null) {
            Spacer(Modifier.width(8.dp))
            SquareButton(Icons.AutoMirrored.Filled.Sort, "Edit speaker", onClick = onEdit)
        }
    }
}

@Composable
private fun StyleBlock(styleName: String, onClick: (() -> Unit)?) {
    Row(Modifier.padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "STYLE",
            style = MaterialTheme.typography.labelLarge.copy(
                letterSpacing = 1.4.sp,
            ),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = "(OPTIONAL)",
            style = MaterialTheme.typography.labelSmall,
            color = FeldPalette.Muted,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp),
        )
    }
    Spacer(Modifier.height(10.dp))
    SelectorBox(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        onClick = onClick,
    ) {
        Icon(
            imageVector = Icons.Filled.Tune,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(24.dp),
        )
        Spacer(Modifier.width(16.dp))
        Text(
            text = styleName,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        Chevron()
    }
    Spacer(Modifier.height(12.dp))
}
