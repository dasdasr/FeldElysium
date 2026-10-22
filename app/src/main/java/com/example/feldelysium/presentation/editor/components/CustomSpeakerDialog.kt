package com.example.feldelysium.presentation.editor.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.feldelysium.designsystem.components.button.FeldDialogActions
import com.example.feldelysium.designsystem.components.button.FeldFieldButton
import com.example.feldelysium.designsystem.components.button.FeldTextButton
import com.example.feldelysium.designsystem.components.dialog.FeldDialog
import com.example.feldelysium.designsystem.components.display.FeldIconId
import com.example.feldelysium.designsystem.components.input.FeldColorSwatches
import com.example.feldelysium.designsystem.components.input.FeldTextField
import com.example.feldelysium.designsystem.foundation.seed.rememberSeed
import com.example.feldelysium.domain.model.Speaker
import com.example.feldelysium.domain.model.SpeakerColor
import com.example.feldelysium.domain.model.SpeakerType
import com.example.feldelysium.domain.registry.PredefinedSpeakers
import java.util.UUID
import kotlin.math.absoluteValue

private val KnownIconResIds: List<Int> by lazy {
    PredefinedSpeakers.ALL.mapNotNull { it.iconResId }.distinct()
}

@Composable
fun CustomSpeakerDialog(
    speaker: Speaker? = null,
    onConfirm: (Speaker) -> Unit,
    onDismiss: () -> Unit,
    existingTypes: List<SpeakerType> = emptyList(),
    onManageTags: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    iconResIds: List<Int> = KnownIconResIds,
) {
    val editing = speaker != null
    val canChangeIcon = speaker == null || !speaker.isBundled
    val seed = rememberSeed()
    val fileNo = remember { ((speaker?.id ?: seed.toString()).hashCode().absoluteValue % 99) + 1 }
    var name by remember { mutableStateOf(speaker?.name.orEmpty()) }
    var color by remember { mutableLongStateOf(speaker?.colorArgb ?: SpeakerColor.DEFAULT.argb) }
    var colorTouched by remember { mutableStateOf(editing) }
    var type by remember { mutableStateOf(speaker?.type ?: SpeakerType.CUSTOM) }
    var iconRes by remember { mutableStateOf(speaker?.iconResId) }
    var iconUri by remember { mutableStateOf(speaker?.customIconUri) }
    var showIcons by remember { mutableStateOf(false) }
    var showTags by remember { mutableStateOf(false) }

    var customColors by remember {
        mutableStateOf(listOfNotNull(color.takeIf { SpeakerColor.isCustom(it) }))
    }
    val palette = SpeakerColor.AllArgb + customColors
    val accent = Color(color)

    FeldDialog(
        title = if (editing) "Edit speaker" else "Create speaker",
        eyebrow = "Custom speaker",
        onDismiss = onDismiss,
        footer = {
            FeldDialogActions(
                confirmLabel = if (editing) "Save" else "Create",
                onConfirm = {
                    onConfirm(
                        Speaker(
                            id = speaker?.id ?: UUID.randomUUID().toString(),
                            name = name.trim(),
                            colorArgb = color,
                            iconResId = iconRes,
                            customIconUri = iconUri,
                            type = type,
                        ),
                    )
                },
                onCancel = onDismiss,
                confirmEnabled = name.isNotBlank(),
            )
        },
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            SpeakerPhotoBlock(
                iconRes = iconRes,
                iconUri = iconUri,
                accent = accent,
                onClick = if (canChangeIcon) {
                    { showIcons = true }
                } else {
                    {}
                },
                modifier = Modifier.width(132.dp),
            )
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                FieldLabel("Name")
                FeldTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = "Speaker name",
                    maxLength = 32,
                    showLength = false,
                    minHeight = 48.dp,
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                )

                Spacer(Modifier.height(14.dp))
                FieldLabel("Tag", optional = true)
                FeldFieldButton(
                    text = type.takeUnless { it.isCustom }?.name,
                    placeholder = "No tag",
                    leadingIconId = FeldIconId.Tag,
                    onClick = { showTags = true },
                )
            }
        }

        Spacer(Modifier.height(20.dp))
        FieldLabel("Color")
        FeldColorSwatches(
            colors = palette,
            selected = color,
            onSelect = {
                color = it
                colorTouched = true
            },
            columns = 6,
            minRows = 2,
            diamondFraction = 0.5f,
        )

        Spacer(Modifier.height(22.dp))
        SpeakerPreview(name = name, color = accent, seed = seed)
        if (onDelete != null) {
            Spacer(Modifier.height(10.dp))
            FeldTextButton("Delete speaker", onClick = onDelete)
        }
    }

    if (showTags) {
        ChooseTagDialog(
            existing = existingTypes,
            selected = type,
            onManage = onManageTags,
            onConfirm = { picked ->
                type = picked
                val suggested = picked.colorArgb
                if (!colorTouched && suggested != null) {
                    color = suggested
                    if (SpeakerColor.isCustom(suggested) && suggested !in customColors) {
                        customColors = customColors + suggested
                    }
                }
                showTags = false
            },
            onDismiss = { showTags = false },
        )
    }

    if (showIcons && canChangeIcon) {
        ChooseImageDialog(
            iconResIds = iconResIds,
            selectedResId = iconRes,
            selectedUri = iconUri,
            onConfirm = { res, uri ->
                iconRes = res
                iconUri = uri
                showIcons = false
            },
            onDismiss = { showIcons = false },
        )
    }
}
