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
import androidx.compose.ui.unit.dp
import com.example.feldelysium.data.settings.AppSettings
import com.example.feldelysium.designsystem.components.dialog.FeldDialog
import com.example.feldelysium.designsystem.components.dialog.FeldDialogFooter
import com.example.feldelysium.designsystem.components.display.FeldDivider
import com.example.feldelysium.designsystem.components.display.FeldIcon
import com.example.feldelysium.designsystem.components.display.FeldIconId
import com.example.feldelysium.designsystem.components.input.FeldTextField

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

    FeldDialog(
        title = "Scene info",
        eyebrow = "Set details",
        subtitle = "Add some context to your scene.",
        onDismiss = onDismiss,
        seed = 15,
        footer = {
            FeldDialogFooter(
                onReset = {
                    moneyText = defaults.playbackMoney
                    timeText = defaults.playbackTime
                    dayText = defaults.playbackDay
                    onReset()
                },
                onCancel = onDismiss,
                cancelLabel = "RESET",
                confirmLabel = "OK",
                seed = 9,
            )
        },
    ) {
        PlaybackField(
            icon = FeldIconId.More,
            label = "Reals",
            value = moneyText,
            maxLength = 6,
            seed = 1,
            onValueChange = {
                moneyText = it
                onMoneyChange(it)
            },
        )
        FeldDivider(seed = 51)
        PlaybackField(
            icon = FeldIconId.More,
            label = "Time",
            value = timeText,
            maxLength = 5,
            seed = 2,
            onValueChange = {
                timeText = it
                onTimeChange(it)
            },
        )
        FeldDivider(seed = 52)
        PlaybackField(
            icon = FeldIconId.More,
            label = "Day",
            value = dayText,
            maxLength = 6,
            seed = 3,
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
            tint = Teal,
            modifier = Modifier.size(28.dp),
        )
        Spacer(Modifier.width(16.dp))
        FeldTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            label = label,
            maxLength = maxLength,
            seed = seed * 3 + 2,
        )
    }
}
