package com.example.feldelysium.presentation.editor.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
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
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.feldelysium.designsystem.foundation.utils.FeldPalette
import com.example.feldelysium.designsystem.components.button.FeldDialogActions
import com.example.feldelysium.designsystem.components.card.FeldHandDiamond
import com.example.feldelysium.designsystem.components.dialog.FeldDialog
import com.example.feldelysium.designsystem.components.input.FeldTextField
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
        onDismiss = onDismiss,
        footer = {
            FeldDialogActions(
                confirmLabel = "Import",
                onConfirm = onConfirm,
                onCancel = onDismiss,
            )
        },
    ) {
        FormatHint()
        Spacer(Modifier.height(12.dp))
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

@Composable
private fun FormatHint() {
    Row(
        modifier = Modifier.padding(start = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "FORMAT",
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.4.sp),
            color = FeldPalette.Muted,
        )
        Text(
            text = "  ->  ",
            style = MaterialTheme.typography.labelSmall,
            color = FeldPalette.Muted,
        )
        Text(
            text = buildAnnotatedString {
                withStyle(
                    SpanStyle(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold,
                    )
                ) { append("SPEAKER:") }
                append(" Text")
            },
            style = MaterialTheme.typography.bodySmall,
            color = FeldPalette.Muted,
        )
    }
}

private class SpeakerHeaderTransformation(
    private val colors: Map<String, Color>,
) : VisualTransformation {

    private val newSpeakerColor = Color(Speaker.DEFAULT_CUSTOM_COLOR)

    override fun filter(text: AnnotatedString): TransformedText {
        val source = text.text
        val styled = buildAnnotatedString {
            append(source)
            var offset = 0
            for (line in source.split('\n')) {
                val name = ParseBulkImportUseCase.SPEAKER_LINE.matchEntire(line)?.groups?.get(1)
                if (name != null) {
                    val color = colors[name.value.trim().uppercase()] ?: newSpeakerColor
                    addStyle(
                        SpanStyle(color = color, fontWeight = FontWeight.SemiBold),
                        offset + name.range.first,
                        offset + name.range.last + 2,
                    )
                }
                offset += line.length + 1
            }
        }
        return TransformedText(styled, OffsetMapping.Identity)
    }
}
