package com.feldelysium.app.presentation.editor.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.feldelysium.app.R
import com.feldelysium.designsystem.components.button.FeldFilmButton
import com.feldelysium.designsystem.components.button.FeldGhostButton
import com.feldelysium.designsystem.components.container.FeldCard
import com.feldelysium.designsystem.components.container.FeldDivider
import com.feldelysium.designsystem.components.container.entryRough
import com.feldelysium.designsystem.components.content.FeldIconId
import com.feldelysium.designsystem.theme.FeldPalette
import com.feldelysium.designsystem.foundation.material.FeldPaper

@Composable
internal fun RenameSceneDialog(initial: String, onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var field by remember { mutableStateOf(TextFieldValue(initial, TextRange(0, initial.length))) }
    val requester = remember { FocusRequester() }
    val rough = remember { entryRough(17) }

    fun commit() {
        val t = field.text.trim()
        if (t.isEmpty()) onDismiss() else onConfirm(t)
    }

    Dialog(onDismissRequest = onDismiss) {
        FeldCard(
            modifier = Modifier.fillMaxWidth(),
            seed = 17,
            spec = FeldPaper.Entry,
            rough = rough,
            contentPadding = PaddingValues(16.dp),
            showDecals = false,
        ) {
            Text(
                text = stringResource(R.string.scenes_rename_title).uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.6.sp),
                color = FeldPalette.Muted,
            )
            Spacer(Modifier.height(10.dp))
            BasicTextField(
                value = field,
                onValueChange = { field = it },
                singleLine = true,
                textStyle = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 32.sp,
                    color = FeldPalette.Cream,
                ),
                cursorBrush = SolidColor(FeldPalette.Orange),
                visualTransformation = UpperCaseTransformation,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(onDone = { commit() }),
                modifier = Modifier.fillMaxWidth().focusRequester(requester),
            )
            Spacer(Modifier.height(4.dp))
            FeldDivider(thickness = 1.dp)
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FeldGhostButton(
                    stringResource(R.string.common_cancel),
                    onDismiss,
                    Modifier.weight(1f),
                    FeldIconId.Close,
                    seed = 33,
                )
                FeldFilmButton(
                    stringResource(R.string.common_save),
                    ::commit,
                    Modifier.weight(1f),
                    FeldIconId.Save,
                    seed = 9,
                )
            }
        }
    }
    LaunchedEffect(Unit) { requester.requestFocus() }
}

private val UpperCaseTransformation = VisualTransformation { t ->
    TransformedText(AnnotatedString(t.text.uppercase()), OffsetMapping.Identity)
}
