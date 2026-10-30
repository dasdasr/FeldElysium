package com.feldelysium.app.presentation.editor.dialogs

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.feldelysium.app.R
import com.feldelysium.designsystem.components.button.FeldFilmButton
import com.feldelysium.designsystem.components.button.FeldGhostButton
import com.feldelysium.designsystem.components.container.FeldDivider
import com.feldelysium.designsystem.components.content.FeldIcon
import com.feldelysium.designsystem.components.content.FeldIconId
import com.feldelysium.designsystem.theme.FeldPalette
import com.feldelysium.designsystem.components.input.feldClickable
import com.feldelysium.designsystem.foundation.haptics.rememberFeldHaptics
import kotlinx.coroutines.delay

@Composable
internal fun ActionBar(canClear: Boolean, onClear: () -> Unit, onImport: () -> Unit, onExport: () -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        FeldDivider()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 14.dp,
                    bottom = 12.dp,
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ClearControl(enabled = canClear, onConfirm = onClear)

            Box(
                Modifier
                    .width(1.dp)
                    .height(32.dp)
                    .background(FeldPalette.Muted.copy(alpha = 0.4f)),
            )

            FeldGhostButton(
                text = stringResource(R.string.common_import),
                onClick = onImport,
                modifier = Modifier.weight(1f),
                icon = FeldIconId.Import,
                seed = 31,
            )
            FeldFilmButton(
                text = stringResource(R.string.common_export),
                onClick = onExport,
                modifier = Modifier.weight(1f),
                icon = FeldIconId.Export,
                seed = 8,
            )
        }
    }
}

@Composable
internal fun ClearControl(enabled: Boolean, onConfirm: () -> Unit) {
    val haptics = rememberFeldHaptics()
    var armed by remember { mutableStateOf(false) }
    val orange = rememberOrangeBrush(21)

    LaunchedEffect(armed) {
        if (armed) {
            delay(2500)
            armed = false
        }
    }

    LaunchedEffect(enabled) {
        if (!enabled) armed = false
    }

    Box(
        Modifier
            .animateContentSize()
            .height(56.dp)
            .alpha(if (enabled) 1f else 0.35f),
        contentAlignment = Alignment.Center,
    ) {
        if (armed) {
            Box(
                Modifier
                    .graphicsLayer { rotationZ = -6f }
                    .drawBehind { drawRect(orange) }
                    .feldClickable {
                        haptics.thud()
                        armed = false
                        onConfirm()
                    }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
            ) {
                Text(
                    text = stringResource(R.string.scenes_clear_confirm),
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = 18.sp,
                        letterSpacing = 1.6.sp,
                    ),
                    color = Ink,
                )
            }
        } else {
            Box(
                Modifier
                    .size(48.dp)
                    .feldClickable(enabled = enabled) {
                        haptics.tick()
                        armed = true
                    },
                contentAlignment = Alignment.Center,
            ) {
                FeldIcon(FeldIconId.Delete, size = 22.dp)
            }
        }
    }
}
