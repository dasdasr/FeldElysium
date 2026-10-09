package com.example.feldelysium.presentation.editor.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.feldelysium.designsystem.components.display.FeldDivider
import com.example.feldelysium.designsystem.components.button.FeldDialogActions
import com.example.feldelysium.designsystem.components.button.FeldLabeledBlock
import com.example.feldelysium.designsystem.components.dialog.FeldDialog
import com.example.feldelysium.designsystem.components.display.FeldIconId
import com.example.feldelysium.designsystem.components.input.FeldColorSwatches
import com.example.feldelysium.designsystem.components.input.FeldTextField
import com.example.feldelysium.designsystem.components.settings.FeldSettingRow
import com.example.feldelysium.domain.model.Speaker
import com.example.feldelysium.domain.model.SpeakerType
import com.example.feldelysium.domain.registry.PredefinedSpeakers
import java.util.UUID

private val SkillColors: List<Long> by lazy {
    PredefinedSpeakers.ALL
        .filter { it.type == SpeakerType.SKILL }
        .map { it.colorArgb }
        .distinct()
}

private val KnownIconResIds: List<Int> by lazy {
    PredefinedSpeakers.ALL.mapNotNull { it.iconResId }.distinct()
}

@Preview
@Composable
private fun CustomSpeakerDialogPreview() {
    CustomSpeakerDialog(
        onConfirm = {},
        onDismiss = {}
    )
}

@Composable
fun CustomSpeakerDialog(
    speaker: Speaker? = null,
    onConfirm: (Speaker) -> Unit,
    onDismiss: () -> Unit,
    colorChoices: List<Long> = SkillColors,
    iconResIds: List<Int> = KnownIconResIds,
) {
    val editing = speaker != null
    var name by remember { mutableStateOf(speaker?.name.orEmpty()) }
    var color by remember {
        mutableLongStateOf(speaker?.colorArgb ?: Speaker.DEFAULT_CUSTOM_COLOR)
    }
    var iconRes by remember { mutableStateOf(speaker?.iconResId) }
    var iconUri by remember { mutableStateOf(speaker?.customIconUri) }
    var showIcons by remember { mutableStateOf(false) }

    val initialColor = remember { color }
    val palette = remember(colorChoices) {
        (listOf(Speaker.DEFAULT_CUSTOM_COLOR) + colorChoices + initialColor).distinct()
    }

    FeldDialog(
        title = if (editing) "Edit speaker" else "Create speaker",
        eyebrow = "Custom speaker",
        subtitle = "Name, color and icon of your voice.",
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
                            type = SpeakerType.CUSTOM,
                        )
                    )
                },
                onCancel = onDismiss,
                confirmEnabled = name.isNotBlank(),
            )
        },
    ) {
        FeldLabeledBlock("Name") {
            FeldTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = "Speaker name",
                maxLength = 32,
                showLength = false,
                minLines = 1,
            )
        }
        FeldDivider()

        FeldLabeledBlock("Color") {
            FeldColorSwatches(
                colors = palette,
                selected = color,
                onSelect = { color = it },
                columns = 9,
                minRows = 2,
            )
        }
        FeldDivider()

        val res = iconRes
        val uri = iconUri
        FeldSettingRow(
            icon = FeldIconId.Image,
            title = "Icon",
            subtitle = if (res != null || uri != null) "Selected" else "Optional",
            showChevron = true,
            contentPadding = PaddingValues(vertical = 14.dp),
            onClick = { showIcons = true },
            trailing = {
                val thumb = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(2.dp))
                when {
                    uri != null -> AsyncImage(
                        model = uri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = thumb,
                    )

                    res != null -> Image(
                        painter = painterResource(res),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = thumb,
                    )
                }
            },
        )
        FeldDivider()
    }

    if (showIcons) {
        ChooseIconDialog(
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
