package com.example.feldelysium.designsystem.components.input

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import com.example.feldelysium.designsystem.components.FeldPalette
import com.example.feldelysium.designsystem.material.FeldPaper
import com.example.feldelysium.designsystem.material.paperMaterial
import com.example.feldelysium.designsystem.shape.RoughShape
import com.example.feldelysium.designsystem.shape.RoughStyle

private val FieldRough = RoughStyle(
    roughness = 0.8.dp,
    step = 48.dp,
    corner = 4.dp,
    tornChance = 0.1f,
)

@Composable
fun FeldTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    seed: Int = 0,
    maxLength: Int? = null,
    singleLine: Boolean = true,
    placeholder: String? = null,
) {
    val shape = remember(seed) { RoughShape(seed, FieldRough) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .paperMaterial(shape, FeldPaper.Field),
    ) {
        BasicTextField(
            value = value,
            onValueChange = { text ->
                if (maxLength == null || text.length <= maxLength) onValueChange(text)
            },
            singleLine = singleLine,
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurface,
            ),
            cursorBrush = SolidColor(FeldPalette.Orange),
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxWidth()
                .padding(
                    start = 16.dp,
                    end = if (maxLength != null) 56.dp else 16.dp,
                    top = 16.dp,
                    bottom = 16.dp,
                ),
            decorationBox = { inner ->
                if (value.isEmpty() && placeholder != null) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyLarge,
                        color = FeldPalette.Muted.copy(alpha = 0.6f),
                    )
                }
                inner()
            },
        )

        if (maxLength != null) {
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
}
