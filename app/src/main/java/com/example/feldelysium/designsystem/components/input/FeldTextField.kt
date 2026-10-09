package com.example.feldelysium.designsystem.components.input

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.feldelysium.designsystem.foundation.utils.FeldPalette
import com.example.feldelysium.designsystem.foundation.material.FeldPaper
import com.example.feldelysium.designsystem.foundation.material.PaperSpec
import com.example.feldelysium.designsystem.foundation.material.paperMaterial
import com.example.feldelysium.designsystem.foundation.shape.RoughShape
import com.example.feldelysium.designsystem.foundation.shape.RoughStyle
import com.example.feldelysium.theme.DiscoDialogueTheme
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import com.example.feldelysium.designsystem.foundation.seed.rememberSeed

object FeldRough {
    val Field = RoughStyle(
        roughness = 0.8.dp,
        step = 48.dp,
        corner = 4.dp,
        tornChance = 0.1f,
    )
}

@Composable
fun FeldTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    seed: Int = rememberSeed(),
    spec: PaperSpec = FeldPaper.Field,
    rough: RoughStyle = FeldRough.Field,
    maxLength: Int? = null,
    showLength: Boolean = true,
    minLines: Int = 1,
    singleLine: Boolean = minLines <= 1,
    placeholder: String? = null,
    label: String? = null,
    enabled: Boolean = true,
    textStyle: TextStyle? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    contentPadding: PaddingValues = PaddingValues(
        start = 16.dp,
        end = 16.dp,
        top = 16.dp,
        bottom = 16.dp
    ),
    focusAnimation: Boolean = true,
    counterInside: Boolean = true,
    time: () -> Float = { 0f },
) {
    val shape = remember(seed, rough) { RoughShape(seed, rough) }
    val baseStyle = textStyle ?: MaterialTheme.typography.bodyLarge
    val showCounter = maxLength != null && showLength

    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val focus by animateFloatAsState(
        targetValue = if (focusAnimation && focused) 1f else 0f,
        animationSpec = tween(160),
        label = "focus",
    )
    val requester = remember { FocusRequester() }

    val layoutDirection = androidx.compose.ui.platform.LocalLayoutDirection.current
    val startPad = contentPadding.calculateStartPadding(layoutDirection)
    val endPad = contentPadding.calculateEndPadding(layoutDirection)
    val topPad = contentPadding.calculateTopPadding()
    val bottomPad = contentPadding.calculateBottomPadding()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = if (label != null) 5.dp else 0.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 64.dp)
                .paperMaterial(
                    shape = shape,
                    spec = spec,
                    wear = { spec.wear * (0.6f - 0.1f * focus) },
                    time = time,
                )
                .then(
                    if (focusAnimation) {
                        Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            enabled = enabled,
                        ) { requester.requestFocus() }
                    } else Modifier
                ),
        ) {
            BasicTextField(
                value = value,
                onValueChange = { text ->
                    if (maxLength == null || text.length <= maxLength) onValueChange(text)
                },
                enabled = enabled,
                singleLine = singleLine,
                minLines = minLines,
                visualTransformation = visualTransformation,
                textStyle = baseStyle.copy(color = MaterialTheme.colorScheme.onSurface),
                cursorBrush = SolidColor(FeldPalette.Orange),
                interactionSource = interaction,
                modifier = Modifier
                    .align(if (singleLine) Alignment.CenterStart else Alignment.TopStart)
                    .fillMaxWidth()
                    .focusRequester(requester)
                    .padding(
                        start = startPad,
                        end = if (showCounter && counterInside && singleLine) 56.dp else endPad,
                        top = topPad,
                        bottom = if (showCounter && counterInside && !singleLine) 34.dp else bottomPad,
                    ),
                decorationBox = { inner ->
                    Box {
                        if (value.isEmpty() && placeholder != null) {
                            Text(
                                text = placeholder,
                                style = baseStyle,
                                color = FeldPalette.Muted.copy(alpha = 0.6f),
                            )
                        }
                        inner()
                    }
                },
            )

            if (showCounter && counterInside) {
                Text(
                    text = "${value.length}/$maxLength",
                    style = MaterialTheme.typography.labelSmall,
                    color = FeldPalette.Muted,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 14.dp, bottom = 8.dp),
                )
            }
        }

        if (label != null) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.4.sp),
                color = FeldPalette.Muted,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 12.dp, y = (-4).dp)
                    .background(FeldPaper.Panel.base)
                    .padding(horizontal = 6.dp),
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0B1011)
@Composable
private fun FeldTextFieldPreview() {
    DiscoDialogueTheme {
        var text by remember { mutableStateOf("123") }
        FeldTextField(
            value = text,
            onValueChange = { text = it },
            label = "Label",
            maxLength = 500,
            modifier = Modifier.padding(16.dp),
        )
    }
}
