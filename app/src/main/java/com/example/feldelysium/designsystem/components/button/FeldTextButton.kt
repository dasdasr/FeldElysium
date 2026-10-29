package com.example.feldelysium.designsystem.components.button

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.feldelysium.designsystem.components.content.FeldTitleTag
import com.example.feldelysium.designsystem.components.input.feldClickable
import com.example.feldelysium.designsystem.foundation.seed.rememberSeed
import com.example.feldelysium.designsystem.foundation.sound.rememberFeldSounds

@Composable
fun FeldTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    maxLines: Int = 1,
    overflow: TextOverflow = TextOverflow.Ellipsis,
) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 1.6.sp),
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = maxLines,
        softWrap = false,
        overflow = overflow,
        modifier = modifier
            .feldClickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    )
}

@Composable
fun FeldDialogActions(
    confirmLabel: String,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    cancelLabel: String,
    onCancel: (() -> Unit)? = null,
    confirmEnabled: Boolean = true,
    seed: Int = rememberSeed(),
) {
    val sounds = rememberFeldSounds()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
            if (onCancel != null) {
                FeldTextButton(
                    cancelLabel,
                    {
                        sounds.cancel()
                        onCancel()
                    },
                )
            }
        }
        FeldButton(
            text = confirmLabel,
            onClick = {
                if (confirmEnabled) {
                    sounds.click(seed)
                    onConfirm()
                }
            },
            spec = if (confirmEnabled) FeldButtonDefaults.OrangeFresh else FeldButtonDefaults.Cream,
            modifier = Modifier
                .weight(1f)
                .alpha(if (confirmEnabled) 1f else 0.45f),
            seed = seed,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 18.dp),
        )
    }
}

private fun Modifier.pullUp(amount: Dp): Modifier = layout { measurable, constraints ->
    val placeable = measurable.measure(constraints)
    val pull = amount.roundToPx()
    layout(placeable.width, (placeable.height - pull).coerceAtLeast(0)) {
        placeable.placeRelative(0, -pull)
    }
}

@Composable
fun FeldLabeledBlock(
    label: String,
    modifier: Modifier = Modifier,
    seed: Int = rememberSeed(),
    overlap: Dp = 14.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
    ) {
        FeldTitleTag(
            text = label,
            modifier = Modifier.zIndex(1f),
            seed = seed,
            distressed = false,
            textStyle = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.4.sp,
            ),
        )
        Column(Modifier.pullUp(overlap), content = content)
    }
}
