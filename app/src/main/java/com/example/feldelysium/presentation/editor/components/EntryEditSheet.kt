package com.example.feldelysium.presentation.editor.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.feldelysium.designsystem.components.display.FeldIcon
import com.example.feldelysium.designsystem.components.display.FeldIconId
import com.example.feldelysium.designsystem.components.input.FeldSwitch
import com.example.feldelysium.designsystem.components.input.FeldTextField
import com.example.feldelysium.designsystem.foundation.material.FeldPaper
import com.example.feldelysium.designsystem.foundation.shape.RoughStyle
import com.example.feldelysium.designsystem.foundation.utils.FeldPalette
import com.example.feldelysium.designsystem.foundation.utils.feldClickable
import com.example.feldelysium.domain.model.DamageEffect
import com.example.feldelysium.domain.model.DialogueEntry
import com.example.feldelysium.domain.model.ReplyChoices
import com.example.feldelysium.domain.model.SkillCheck
import com.example.feldelysium.domain.model.Speaker
import com.example.feldelysium.domain.model.SpeakerType
import com.example.feldelysium.domain.registry.PredefinedSpeakers

private val SheetRough = RoughStyle(
    roughness = 0.8.dp,
    step = 44.dp,
    corner = 4.dp,
    cornerVariation = 0.2f,
    cornerJitter = 0.2f,
    tornChance = 0.03f,
    jerkChance = 0.04f,
)

private val SheetBleed = 28.dp

@Preview
@Composable
private fun EntryEditSheetPreview() {
    EntryEditSheet(
        entry = DialogueEntry(
            id = "-1",
            speaker = PredefinedSpeakers.LEGO_KIM,
            text = "You vs al-ghoul",
        ),
        speaker = PredefinedSpeakers.LEGO_HARRY,
        onConfirm = { _, _, _, _ -> },
        onDismiss = {},
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryEditSheet(
    entry: DialogueEntry?,
    speaker: Speaker?,
    onConfirm: (text: String, check: SkillCheck?, damage: DamageEffect?, replies: ReplyChoices?) -> Unit,
    onDismiss: () -> Unit,
    onSpeakerClick: (() -> Unit)? = null,
    onEditSpeaker: ((Speaker) -> Unit)? = null,
) {
    var text by remember(entry?.id) { mutableStateOf(entry?.text ?: "") }
    var check by remember(entry?.id) { mutableStateOf(entry?.check) }
    var lastCheck by remember(entry?.id) { mutableStateOf(entry?.check) }

    var damage by remember(entry?.id) { mutableStateOf(entry?.damage) }
    var lastDamage by remember(entry?.id) { mutableStateOf(entry?.damage) }
    val isYou = speaker?.type == SpeakerType.YOU
    var replies by remember(entry?.id) { mutableStateOf(entry?.replies) }

    LaunchedEffect(isYou) { if (!isYou) replies = null }

    LaunchedEffect(damage) { if (damage != null) lastDamage = damage }

    LaunchedEffect(check) { if (check != null) lastCheck = check }

    LaunchedEffect(speaker?.type?.allowsChecks) {
        if (speaker?.type?.allowsChecks != true) {
            check = null
            damage = null
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true,
            confirmValueChange = { it != SheetValue.Hidden },
        ),
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
                .heightIn(max = 840.dp)
                .imePadding()
                .bleedBottom(SheetBleed),
            spec = FeldPaper.Entry,
            rough = SheetRough,
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = 12.dp,
                bottom = SheetBleed,
            ),
        ) {
            Column(
                Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState()),
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
                    onEdit = if (speaker != null && !speaker.isBundled && onEditSpeaker != null) {
                        { onEditSpeaker(speaker) }
                    } else {
                        null
                    },
                )

                Spacer(Modifier.height(8.dp))
                if (replies == null) {
                    FeldLabeledBlock("Dialogue") {
                        FeldTextField(
                            value = text,
                            onValueChange = { text = it },
                            placeholder = "Dialogue text",
                            minLines = 4,
                            maxLines = 12,
                            maxLength = 500,
                        )
                    }
                }

                if (isYou) {
                    ToggleRow("REPLY OPTIONS", FeldIconId.Oprions, replies != null) { on ->
                        replies = if (on) {
                            ReplyChoices(listOf(text.take(ReplyChoices.MAX_LENGTH), ""))
                        } else {
                            null
                        }
                    }
                    AnimatedVisibility(
                        visible = replies != null,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut(),
                    ) {
                        replies?.let { ReplyOptionsEditor(it) { new -> replies = new } }
                    }
                }

                if (speaker?.type?.allowsChecks == true) {
                    ToggleRow("SKILL CHECK", FeldIconId.Casino, check != null) {
                        check = if (it) SkillCheck() else null
                    }
                    AnimatedVisibility(
                        visible = check != null,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut(),
                    ) {
                        EntryCheckEditor(speaker = speaker, check = check ?: lastCheck, onChange = { check = it })
                    }

                    ToggleRow("DAMAGE", FeldIconId.Heart, damage != null) {
                        damage = if (it) DamageEffect() else null
                    }
                    AnimatedVisibility(
                        visible = damage != null,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut(),
                    ) {
                        EntryDamageEditor(damage = damage ?: lastDamage, onChange = { damage = it })
                    }
                }
            }

            val replyValid = replies?.normalized() != null
            FeldDialogActions(
                confirmLabel = "Confirm",
                confirmEnabled = if (replies != null) replyValid else text.isNotBlank(),
                onConfirm = {
                    val allowed = speaker?.type?.allowsChecks == true
                    val r = replies?.normalized()
                    when {
                        replies != null && r != null ->
                            onConfirm(r.chosen, null, null, r)

                        replies == null && text.isNotBlank() ->
                            onConfirm(text.trim(), check?.takeIf { allowed }, damage?.takeIf { allowed }, null)
                    }
                },
                onCancel = onDismiss,
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
    FeldIcon(
        id = FeldIconId.Chevron,
        size = 16.dp,
    )
}

@Composable
private fun SpeakerHeader(speaker: Speaker?, onClick: (() -> Unit)?, onEdit: (() -> Unit)?) {
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
                Modifier
                    .size(32.dp)
                    .padding(end = 8.dp)
                    .feldClickable(onClick = onEdit),
                contentAlignment = Alignment.Center,
            ) {
                FeldIcon(
                    id = FeldIconId.Edit,
                    contentDescription = "Edit speaker",
                    size = 22.dp,
                )
            }
        }
        if (onClick != null) Chevron()
    }
}

@Composable
private fun ToggleRow(label: String, iconId: FeldIconId, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FeldIcon(
            iconId,
            size = 20.dp,
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.4.sp),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
        )
        Spacer(Modifier.weight(1f))
        FeldSwitch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun ReplyOptionsEditor(value: ReplyChoices, onChange: (ReplyChoices) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            "Select one option for dialogue",
            style = MaterialTheme.typography.labelSmall,
            color = FeldPalette.Muted,
        )
        value.options.forEachIndexed { i, option ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                FeldIcon(
                    id = if (i == value.chosenIndex) {
                        FeldIconId.RadioButtonChecked
                    } else {
                        FeldIconId.RadioButtonUnchecked
                    },
                    contentDescription = "Spoken by YOU",
                    tint = FeldPalette.Muted,
                    modifier = Modifier
                        .size(24.dp)
                        .feldClickable { onChange(value.copy(chosenIndex = i)) },
                )
                Spacer(Modifier.width(8.dp))
                Box(Modifier.weight(1f)) {
                    FeldTextField(
                        value = option,
                        onValueChange = { v ->
                            onChange(
                                value.copy(
                                    options = value.options.toMutableList().also {
                                        it[i] = v.take(ReplyChoices.MAX_LENGTH)
                                    },
                                ),
                            )
                        },
                        placeholder = "Option ${i + 1}",
                        minLines = 1,
                        maxLines = 2,
                        maxLength = ReplyChoices.MAX_LENGTH,
                    )
                }
                if (value.options.size > 2) {
                    FeldIconButton(FeldIconId.Close, {
                        val opts = value.options.toMutableList().also { it.removeAt(i) }
                        val chosen = when {
                            value.chosenIndex == i -> 0
                            value.chosenIndex > i -> value.chosenIndex - 1
                            else -> value.chosenIndex
                        }
                        onChange(ReplyChoices(opts, chosen))
                    })
                }
            }
        }
        if (value.options.size < ReplyChoices.MAX_OPTIONS) {
            Text(
                "+ ADD OPTION",
                style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.4.sp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                modifier = Modifier
                    .feldClickable { onChange(value.copy(options = value.options + "")) }
                    .padding(vertical = 8.dp),
            )
        }
    }
}
