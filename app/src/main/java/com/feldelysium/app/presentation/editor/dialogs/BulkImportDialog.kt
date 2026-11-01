package com.feldelysium.app.presentation.editor.dialogs

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
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
import com.feldelysium.app.R
import com.feldelysium.app.domain.model.Speaker
import com.feldelysium.app.domain.registry.PredefinedSpeakers
import com.feldelysium.app.domain.usecase.ParseBulkImportUseCase
import com.feldelysium.designsystem.components.button.FeldDialogActions
import com.feldelysium.designsystem.components.container.FeldDialog
import com.feldelysium.designsystem.components.input.FeldTextField
import com.feldelysium.designsystem.theme.FeldPalette

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
        previewCaption = "2 lines",
        onText = {},
        onConfirm = {},
        onDismiss = {},
    )
}

@Composable
fun BulkImportDialog(
    text: String,
    previewCaption: String?,
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
                    Color(it.colorArgb),
                )
            }
        }
        SpeakerHeaderTransformation(colors)
    }

    FeldDialog(
        title = stringResource(R.string.bulk_import_title),
        eyebrow = stringResource(R.string.bulk_import_eyebrow),
        onDismiss = onDismiss,
        footer = {
            FeldDialogActions(
                confirmLabel = stringResource(R.string.common_import),
                onConfirm = onConfirm,
                onCancel = onDismiss,
                confirmEnabled = text.isNotBlank(),
                cancelLabel = stringResource(R.string.common_cancel),
            )
        },
    ) {
        FeldTextField(
            value = text,
            onValueChange = onText,
            placeholder = "WORTH: I have a plan.\nYOU: Is it legal?",
            minLines = 8,
            textStyle = ImportTextStyle,
            visualTransformation = transformation,
        )
        if (previewCaption != null) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = previewCaption.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = FeldPalette.Muted,
            )
        }
    }
}

private class SpeakerHeaderTransformation(private val colors: Map<String, Color>) : VisualTransformation {

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
