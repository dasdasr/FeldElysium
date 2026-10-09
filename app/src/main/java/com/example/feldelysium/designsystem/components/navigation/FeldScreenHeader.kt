package com.example.feldelysium.designsystem.components.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.feldelysium.designsystem.components.FeldDistressedTitle
import com.example.feldelysium.designsystem.components.FeldDistressedTitles
import com.example.feldelysium.designsystem.components.button.FeldIconButton
import com.example.feldelysium.designsystem.components.display.FeldDiamond
import com.example.feldelysium.designsystem.components.display.FeldDivider
import com.example.feldelysium.designsystem.components.display.FeldIconId
import com.example.feldelysium.designsystem.components.input.LocalTapOutsideRegistry
import com.example.feldelysium.designsystem.foundation.haptics.rememberFeldHaptics
import com.example.feldelysium.designsystem.foundation.material.DistressSpec
import com.example.feldelysium.designsystem.foundation.seed.rememberSeed
import com.example.feldelysium.designsystem.foundation.utils.FeldPalette
import com.example.feldelysium.designsystem.foundation.utils.feldClickable
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
    onDiamondClick: (() -> Unit)? = null,
    titlePlaceholder: String = "Untitled scene",
    titleSpec: DistressSpec = FeldDistressedTitles.Large,
    titleStyle: TextStyle = MaterialTheme.typography.titleLarge.copy(
        fontSize = 52.sp,
        lineHeight = 54.sp,
        letterSpacing = 1.sp,
    ),
    divider: Boolean = true,
) {
    val haptics = rememberFeldHaptics()

    Column(modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (navIcon != null) {
                FeldIconButton(
                    navIcon,
                    onNav,
                    tint = FeldPalette.Muted,
                    modifier = Modifier.padding(start = 0.dp)
                )
            }
            if (eyebrow != null) {
                Box(
                    modifier = Modifier
                        .offset(x = (-6).dp)
                        .size(width = 22.dp, height = 24.dp)
                        .feldClickable(onClick = {
                            haptics.tick()
                            onDiamondClick?.invoke()
                        }),
                    contentAlignment = Alignment.Center,
                ) {
                    FeldDiamond(FeldPalette.Orange, size = 10.dp)
                }
                Text(
                    text = eyebrow.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = FeldPalette.Muted,
                )
            }
            Spacer(Modifier.weight(1f))
            actions()
        }

        Spacer(Modifier.height(6.dp))
        if (onTitleChange == null) {
            FeldDistressedTitle(text = title, spec = titleSpec, textStyle = titleStyle)
        } else {
            EditableTitle(
                text = title,
                onTextChange = onTitleChange,
                placeholder = titlePlaceholder,
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                metaStart?.let {
                    Text(
                        it.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = FeldPalette.Muted
                    )
                }
                FeldDivider(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp),
                )
                metaEnd?.let {
                    Text(
                        it.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = FeldPalette.Muted
                    )
                }
            }
        }

        if (divider) {
            Spacer(Modifier.height(12.dp))
            FeldDivider(thickness = 1.dp)
        }
    }
}

private val UpperCaseTransformation = VisualTransformation { t ->
    val upper = buildString { t.text.forEach { append(it.uppercaseChar()) } }
    TransformedText(AnnotatedString(upper), OffsetMapping.Identity)
}

private val EditSelectionColors = TextSelectionColors(
    handleColor = FeldPalette.Orange,
    backgroundColor = FeldPalette.Orange.copy(alpha = 0.4f),
)

@Composable
private fun EditableTitle(
    text: String,
    onTextChange: (String) -> Unit,
    placeholder: String,
    spec: DistressSpec,
    style: TextStyle,
) {
    val haptics = rememberFeldHaptics()
    val focusManager = LocalFocusManager.current
    val requester = remember { FocusRequester() }
    val registry = LocalTapOutsideRegistry.current
    val registryKey = remember { Any() }
    var editing by remember { mutableStateOf(false) }
    var hadFocus by remember { mutableStateOf(false) }
    var field by remember { mutableStateOf(TextFieldValue(text)) }

    LaunchedEffect(text) { if (field.text != text) field = field.copy(text = text) }
    LaunchedEffect(editing) { if (!editing) registry?.unregister(registryKey) }
    DisposableEffect(registry) { onDispose { registry?.unregister(registryKey) } }

    val shown = if (editing) field.text else text
    val blank = shown.isBlank()

    Box(
        Modifier
            .fillMaxWidth()
            .onGloballyPositioned {
                if (editing) registry?.register(
                    registryKey,
                    it.boundsInWindow()
                )
            },
    ) {
        FeldDistressedTitle(
            text = if (blank) placeholder else shown,
            spec = spec,
            textStyle = style,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth()
                .alpha(if (editing && blank) 0.35f else 1f)
                .then(
                    if (editing) Modifier
                    else Modifier.feldClickable(onClick = {
                        haptics.tick()
                        field = TextFieldValue(text, TextRange(0, text.length))
                        editing = true
                    })
                ),
        )

        if (editing) {
            CompositionLocalProvider(LocalTextSelectionColors provides EditSelectionColors) {
                BasicTextField(
                    value = field,
                    onValueChange = { value ->
                        field = value
                        if (value.text != text) onTextChange(value.text)
                    },
                    singleLine = true,
                    textStyle = style.copy(color = Color.Transparent),
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
                            else if (hadFocus) {
                                hadFocus = false
                                editing = false
                            }
                        },
                )
            }
            LaunchedEffect(Unit) { requester.requestFocus() }
        }
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
                actions = { FeldIconButton(FeldIconId.More, onClick = {}) },
            )
            FeldScreenHeader(
                title = "Choose speaker",
                eyebrow = "Choose speaker",
                subtitle = "Select who will say this line.",
                navIcon = FeldIconId.Close,
            )
        }
    }
}
