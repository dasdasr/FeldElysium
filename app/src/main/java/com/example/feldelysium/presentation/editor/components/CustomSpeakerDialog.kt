package com.example.feldelysium.presentation.editor.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.feldelysium.domain.model.Speaker
import com.example.feldelysium.domain.model.SpeakerType
import java.util.UUID

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
    onDismiss: () -> Unit
) {
    val editing = speaker != null
    var name      by remember { mutableStateOf(speaker?.name.orEmpty()) }
    var colorHex  by remember { mutableStateOf((speaker?.colorArgb ?: Speaker.DEFAULT_CUSTOM_COLOR).toArgbHex()) }
    var iconUri   by remember { mutableStateOf(speaker?.customIconUri) }
    var nameError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (editing) "Edit Speaker" else "Create Custom Speaker") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value         = name,
                    onValueChange = { name = it; nameError = false },
                    label         = { Text("Speaker Name") },
                    isError       = nameError,
                    modifier      = Modifier.fillMaxWidth()
                )
                if (nameError) Text("Name is required", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)

                // TODO: color picker widget — use a simple hex input for MVP
                OutlinedTextField(
                    value         = colorHex,
                    onValueChange = { colorHex = it },
                    label         = { Text("Color (ARGB hex)") },
                    modifier      = Modifier.fillMaxWidth()
                )

                // TODO: icon picker — show selected URI preview, trigger gallery via effect
                TextButton(onClick = { /* dispatch LaunchGalleryPicker effect */ }) {
                    Text(if (iconUri != null) "Icon selected ✓" else "Choose Icon (optional)")
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (name.isBlank()) { nameError = true; return@Button }
                val argb = colorHex.toLongOrNull(16) ?: Speaker.DEFAULT_CUSTOM_COLOR
                onConfirm(
                    Speaker(
                        id            = speaker?.id ?: UUID.randomUUID().toString(),
                        name          = name.trim(),
                        colorArgb     = argb,
                        iconResId     = speaker?.iconResId,
                        customIconUri = iconUri,
                        type          = SpeakerType.CUSTOM
                    )
                )
            }) { Text(if (editing) "Save" else "Create") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

private fun Long.toArgbHex(): String =
    (this and 0xFFFF_FFFFL).toString(16).uppercase().padStart(8, '0')
