package com.example.feldelysium.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.feldelysium.theme.DiscoDialogueTheme

@Composable
fun FeldScreenHeader(
    title: String,
    modifier: Modifier = Modifier,
    eyebrow: String? = null,
    subtitle: String? = null,
    metaStart: String? = null,
    metaEnd: String? = null,
    navIcon: FeldIconId? = FeldIconId.Back,
    onNav: () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    onTitleChange: ((String) -> Unit)? = null,
    titlePlaceholder: String = "Untitled scene",
    seed: Int = 0,
    titleSpec: DistressedTitleSpec = FeldDistressedTitles.Large,
    titleStyle: TextStyle = MaterialTheme.typography.titleLarge.copy(
        fontSize = 52.sp,
        lineHeight = 54.sp,
        letterSpacing = 1.sp,
    ),
    divider: Boolean = true,
) {
    Column(modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (navIcon != null) {
                FeldIconButton(navIcon, onNav, tint = FeldPalette.Muted, modifier = Modifier.padding(start = 0.dp))
            }
            if (eyebrow != null) {
                FeldDiamond(FeldPalette.Orange, size = 10.dp)
                Text(
                    text = eyebrow.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = FeldPalette.Muted,
                    modifier = Modifier.padding(start = 10.dp),
                )
            }
            Spacer(Modifier.weight(1f))
            actions()
        }

        Spacer(Modifier.height(6.dp))
        if (onTitleChange == null) {
            FeldDistressedTitle(text = title, seed = seed, spec = titleSpec, textStyle = titleStyle)
        } else {
            EditableTitle(
                text = title,
                onTextChange = onTitleChange,
                placeholder = titlePlaceholder,
                seed = seed,
                spec = titleSpec,
                style = titleStyle,
            )
        }

        if (subtitle != null) {
            Text(
                text = subtitle.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = FeldPalette.Muted,
                modifier = Modifier.padding(top = 8.dp),
            )
        }

        if (metaStart != null || metaEnd != null) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                metaStart?.let {
                    Text(it.uppercase(), style = MaterialTheme.typography.labelSmall, color = FeldPalette.Muted)
                }
                FeldDivider(
                    modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                    seed = seed + 5,
                )
                metaEnd?.let {
                    Text(it.uppercase(), style = MaterialTheme.typography.labelSmall, color = FeldPalette.Muted)
                }
            }
        }

        if (divider) {
            Spacer(Modifier.height(12.dp))
            FeldDivider(seed = seed + 9, thickness = 1.dp)
        }
    }
}

private val UpperCaseTransformation = VisualTransformation { t ->
    val upper = buildString { t.text.forEach { append(it.uppercaseChar()) } }
    TransformedText(AnnotatedString(upper), OffsetMapping.Identity)
}

@Composable
private fun EditableTitle(
    text: String,
    onTextChange: (String) -> Unit,
    placeholder: String,
    seed: Int,
    spec: DistressedTitleSpec,
    style: TextStyle,
) {
    val haptics = rememberFeldHaptics()
    val focusManager = LocalFocusManager.current
    val requester = remember { FocusRequester() }
    var editing by remember { mutableStateOf(false) }
    var hadFocus by remember { mutableStateOf(false) }

    if (editing) {
        BasicTextField(
            value = text,
            onValueChange = onTextChange,
            singleLine = true,
            textStyle = style.copy(color = spec.color),
            cursorBrush = SolidColor(FeldPalette.Orange),
            visualTransformation = UpperCaseTransformation,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(onDone = {
                haptics.confirm()
                focusManager.clearFocus()
            }),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(requester)
                .onFocusChanged {
                    if (it.isFocused) hadFocus = true
                    else if (hadFocus) { hadFocus = false; editing = false }
                },
        )
        LaunchedEffect(Unit) { requester.requestFocus() }
    } else {
        FeldDistressedTitle(
            text = text.ifBlank { placeholder },
            seed = seed,
            spec = spec,
            textStyle = style,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth()
                .feldClickable(onClick = { haptics.tick(); editing = true }),
        )
    }
}

@Preview(widthDp = 380, heightDp = 360, showBackground = true, backgroundColor = 0xFF0B1011)
@Composable
private fun FeldScreenHeaderPreview() {
    DiscoDialogueTheme {
        Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
            FeldScreenHeader(
                title = "The final interview",
                eyebrow = "Scene editor",
                metaStart = "4 / 12",
                metaEnd = "Dialogue scene",
                seed = 2,
                actions = { FeldIconButton(FeldIconId.More, onClick = {}) },
            )
            FeldScreenHeader(
                title = "Choose speaker",
                eyebrow = "Choose speaker",
                subtitle = "Select who will say this line.",
                navIcon = FeldIconId.Close,
                seed = 7,
            )
        }
    }
}
