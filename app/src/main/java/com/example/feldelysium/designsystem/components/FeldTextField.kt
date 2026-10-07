package com.example.feldelysium.designsystem.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.feldelysium.designsystem.material.FeldPaper
import com.example.feldelysium.designsystem.material.PaperSpec
import com.example.feldelysium.designsystem.material.paperMaterial
import com.example.feldelysium.designsystem.shape.RoughShape
import com.example.feldelysium.designsystem.shape.RoughStyle
import com.example.feldelysium.theme.DiscoDialogueTheme

object FeldRough {
    val Card = RoughStyle()
    val Field = RoughStyle(roughness = 1.4.dp, forceTorn = 1, tornChance = 0f, jerkChance = 0.2f)
}

@Composable
fun FeldTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    seed: Int = 5,
    spec: PaperSpec = FeldPaper.Field,
    rough: RoughStyle = FeldRough.Field,
    placeholder: String = "",
    maxLength: Int? = null,
    minLines: Int = 3,
    enabled: Boolean = true,
    textStyle: TextStyle = MaterialTheme.typography.bodyLarge.copy(color = colorScheme.onSurface),
    contentPadding: PaddingValues = PaddingValues(16.dp),
    time: () -> Float = { 0f },
) {
    val shape = remember(seed, rough) { RoughShape(seed = seed, style = rough) }
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val focus by animateFloatAsState(if (focused) 1f else 0f, tween(160), label = "focus")
    val requester = remember { FocusRequester() }

    Column(
        modifier = modifier
            .paperMaterial(
                shape = shape,
                spec = spec,
                wear = { spec.wear * (0.6f - 0.1f * focus) },
                time = time,
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = enabled,
            ) { requester.requestFocus() }
            .padding(contentPadding),
    ) {
        BasicTextField(
            value = value,
            onValueChange = { if (maxLength == null || it.length <= maxLength) onValueChange(it) },
            enabled = enabled,
            textStyle = textStyle,
            minLines = minLines,
            cursorBrush = SolidColor(colorScheme.primary),
            interactionSource = interaction,
            modifier = Modifier.fillMaxWidth().focusRequester(requester),
            decorationBox = { inner ->
                Box {
                    if (value.isEmpty()) {
                        Text(placeholder, style = textStyle, color = colorScheme.onSurface.copy(alpha = 0.4f))
                    }
                    inner()
                }
            },
        )
        if (maxLength != null) {
            Text(
                "${value.length}/$maxLength",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF7C7C7A),
                modifier = Modifier.align(Alignment.End).padding(top = 8.dp),
            )
        }
    }
}

@Preview(widthDp = 360, heightDp = 300, showBackground = true, backgroundColor = 0xFF0B1011)
@Composable
private fun FeldTextFieldPreview() {
    DiscoDialogueTheme {
        var text by remember { androidx.compose.runtime.mutableStateOf("Let's be precise. What do you actually remember about that night?") }
        FeldTextField(text, { text = it }, Modifier.padding(16.dp).fillMaxWidth(), maxLength = 500)
    }
}
