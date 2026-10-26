package com.example.feldelysium.presentation.settings.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.feldelysium.R
import com.example.feldelysium.data.settings.AppSettings
import com.example.feldelysium.designsystem.components.dialog.FeldDialog
import com.example.feldelysium.designsystem.components.dialog.FeldDialogFooter
import com.example.feldelysium.designsystem.components.display.FeldDivider
import com.example.feldelysium.designsystem.components.display.FeldIcon
import com.example.feldelysium.designsystem.components.display.FeldIconId
import com.example.feldelysium.designsystem.components.input.FeldTextField
import com.example.feldelysium.designsystem.foundation.seed.rememberSeed

private val Teal = Color(0xFF4FB3A6)

@Composable
fun PlaybackUiTextDialog(
    money: String,
    time: String,
    day: String,
    onMoneyChange: (String) -> Unit,
    onTimeChange: (String) -> Unit,
    onDayChange: (String) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit,
) {
    val defaults = remember { AppSettings() }
    var moneyText by remember { mutableStateOf(money) }
    var timeText by remember { mutableStateOf(time) }
    var dayText by remember { mutableStateOf(day) }

    val seed = rememberSeed()

    FeldDialog(
        title = stringResource(R.string.playback_ui_title),
        eyebrow = stringResource(R.string.playback_ui_eyebrow),
        subtitle = stringResource(R.string.playback_ui_subtitle),
        onDismiss = onDismiss,
        seed = seed,
        footer = {
            FeldDialogFooter(
                onReset = {
                    moneyText = defaults.playbackMoney
                    timeText = defaults.playbackTime
                    dayText = defaults.playbackDay
                    onReset()
                },
                onCancel = onDismiss,
                cancelLabel = stringResource(R.string.common_reset),
                confirmLabel = stringResource(R.string.common_ok),
                seed = rememberSeed("footer"),
            )
        },
    ) {
        PlaybackField(
            icon = FeldIconId.Reals,
            label = stringResource(R.string.playback_ui_reals),
            value = moneyText,
            maxLength = 6,
            seed = rememberSeed("money"),
            onValueChange = {
                moneyText = it
                onMoneyChange(it)
            },
        )
        FeldDivider(seed = seed + 1)
        PlaybackField(
            icon = FeldIconId.Time,
            label = stringResource(R.string.playback_ui_time),
            value = timeText,
            maxLength = 5,
            seed = rememberSeed("time"),
            onValueChange = {
                timeText = it
                onTimeChange(it)
            },
        )
        FeldDivider(seed = seed + 2)
        PlaybackField(
            icon = FeldIconId.Calendar,
            label = stringResource(R.string.playback_ui_day),
            value = dayText,
            maxLength = 6,
            seed = rememberSeed("day"),
            onValueChange = {
                dayText = it
                onDayChange(it)
            },
        )
    }
}

@Composable
private fun PlaybackField(
    icon: FeldIconId,
    label: String,
    value: String,
    maxLength: Int,
    seed: Int,
    onValueChange: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FeldIcon(
            id = icon,
            contentDescription = null,
            modifier = Modifier.size(28.dp),
        )
        Spacer(Modifier.width(16.dp))
        FeldTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            label = label,
            maxLength = maxLength,
            seed = seed,
        )
    }
}
