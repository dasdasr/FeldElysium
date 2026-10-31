package com.feldelysium.designsystem.components.button

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.feldelysium.designsystem.components.content.FeldIcon
import com.feldelysium.designsystem.components.content.FeldIconId
import com.feldelysium.designsystem.components.input.FeldRough
import com.feldelysium.designsystem.foundation.material.FeldMarker
import com.feldelysium.designsystem.foundation.material.FeldPaper
import com.feldelysium.designsystem.foundation.material.PaperSpec
import com.feldelysium.designsystem.foundation.material.paperMaterial
import com.feldelysium.designsystem.foundation.seed.rememberSeed
import com.feldelysium.designsystem.foundation.shape.RoughShape
import com.feldelysium.designsystem.theme.FeldPalette

@Composable
public fun FeldFieldButton(
    text: String?,
    placeholder: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    seed: Int = rememberSeed(),
    spec: PaperSpec = FeldPaper.Field,
    leadingIconId: FeldIconId? = null,
    minHeight: Dp = 48.dp,
    enabled: Boolean = true,
) {
    val shape = remember(seed) { RoughShape(seed, FeldRough.field(seed)) }
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val press by animateFloatAsState(if (pressed) 1f else 0f, tween(90), label = "fieldPress")

    Row(
        modifier
            .fillMaxWidth()
            .heightIn(min = minHeight)
            .paperMaterial(shape, spec, press = { press })
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leadingIconId != null) {
            FeldIcon(
                leadingIconId,
                markerFilmSpec = FeldMarker.Chalk.copy(color = FeldPalette.Muted),
                size = 22.dp,
            )
            Spacer(Modifier.width(10.dp))
        }
        Text(
            text = text ?: placeholder,
            style = MaterialTheme.typography.bodyLarge,
            color = if (text == null) FeldPalette.Muted.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        FeldIcon(
            id = FeldIconId.Chevron,
            size = 20.dp,
        )
    }
}

@Preview
@Composable
private fun FeldFieldButtonPreview() {
    FeldFieldButton(
        text = "Button",
        placeholder = "Go with it",
        onClick = {},
    )
}
