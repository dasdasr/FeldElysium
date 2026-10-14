package com.example.feldelysium.presentation.editor.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.feldelysium.designsystem.components.button.FeldDialogActions
import com.example.feldelysium.designsystem.components.dialog.FeldDialog
import com.example.feldelysium.designsystem.components.input.FeldTextField
import com.example.feldelysium.designsystem.foundation.utils.FeldPalette
import com.example.feldelysium.domain.model.Speaker
import com.example.feldelysium.domain.registry.PredefinedSpeakers
import com.example.feldelysium.domain.usecase.ParseBulkImportUseCase
import com.example.feldelysium.presentation.editor.ImportPreview

private val ImportTextStyle: TextStyle
    @Composable get() = MaterialTheme.typography.bodyMedium.copy(
        lineHeight = 24.sp,
        letterSpacing = 0.2.sp,
    )

@Preview
@Composable
private fun BulkImportDialogPreview() {
    BulkImportDialog(
        text = "VOLITION: This city is trying to kill you.\nKIM KITSURAGI:\nLet's focus.",
        preview = ImportPreview(lineCount = 2, newSpeakerCount = 0),
        onText = {},
        onConfirm = {},
        onDismiss = {},
    )
}

@Composable
fun BulkImportDialog(
    text: String,
    preview: ImportPreview?,
    onText: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    speakers: List<Speaker> = emptyList(),
) {
    val transformation = remember(speakers) {
        val colors = buildMap<String, Color> {
            (PredefinedSpeakers.ALL + speakers).forEach {
                put(
                    it.name.uppercase(),
                    Color(it.colorArgb)
                )
            }
        }
        SpeakerHeaderTransformation(colors)
    }

    FeldDialog(
        title = "Paste dialogue",
        eyebrow = "Quick add",
        subtitle = "Known names: any case. New ones: CAPS.",
        onDismiss = onDismiss,
        footer = {
            FeldDialogActions(
                confirmLabel = "Import",
                onConfirm = onConfirm,
                onCancel = onDismiss,
                confirmEnabled = text.isNotBlank(),
            )
        },
    ) {
        FeldTextField(
            value = text,
            onValueChange = onText,
            placeholder = "VOLITION: This city is trying to kill you.\nKIM KITSURAGI: Let's focus.",
            minLines = 8,
            textStyle = ImportTextStyle,
            visualTransformation = transformation,
        )
        if (preview != null) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = preview.caption.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = FeldPalette.Muted,
            )
        }
    }
}

private class SpeakerHeaderTransformation(
    private val colors: Map<String, Color>,
) : VisualTransformation {

    private val newSpeakerColor = Color(Speaker.DEFAULT_CUSTOM_COLOR)

    override fun filter(text: AnnotatedString): TransformedText {
        val source = text.text
        val shown = StringBuilder(source)
        val spans = mutableListOf<Triple<Int, Int, Color>>()
        var offset = 0
        for (line in source.split('\n')) {
            val name = ParseBulkImportUseCase.SPEAKER_LINE.matchEntire(line)?.groups?.get(1)
            if (name != null) {
                val trimmed = name.value.trim()
                val key = trimmed.uppercase()
                val color = colors[key] ?: if (trimmed == key) newSpeakerColor else null
                if (color != null) {
                    val start = offset + name.range.first
                    val end = offset + name.range.last + 1
                    val upper = name.value.uppercase()
                    if (upper.length == name.value.length) shown.replace(start, end, upper)
                    spans += Triple(start, end + 1, color)
                }
            }
            offset += line.length + 1
        }
        val styled = buildAnnotatedString {
            append(shown.toString())
            spans.forEach { (start, end, color) ->
                addStyle(SpanStyle(color = color, fontWeight = FontWeight.SemiBold), start, end)
            }
        }
        return TransformedText(styled, OffsetMapping.Identity)
    }
}
