package com.example.feldelysium.presentation.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.feldelysium.data.settings.AppSettings
import com.example.feldelysium.designsystem.components.FeldDivider
import com.example.feldelysium.designsystem.components.dialog.FeldDialog
import com.example.feldelysium.designsystem.components.dialog.FeldDialogFooter
import com.example.feldelysium.designsystem.components.input.FeldTextField
import com.example.feldelysium.designsystem.components.title.FeldTitleTag

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
            icon = Icons.Filled.MonetizationOn,
            label = "Reals",
            value = moneyText,
            maxLength = 10,
            seed = 10,
            onValueChange = { moneyText = it; onMoneyChange(it) },
        )
        FeldDivider(seed = 51)
        PlaybackField(
            icon = Icons.Filled.Schedule,
            label = "Time",
            value = timeText,
            maxLength = 5,
            seed = 20,
            onValueChange = { timeText = it; onTimeChange(it) },
        )
        FeldDivider(seed = 52)
        PlaybackField(
            icon = Icons.Filled.CalendarMonth,
            label = "Day",
            value = dayText,
            maxLength = 12,
            seed = 30,
            onValueChange = { dayText = it; onDayChange(it) },
        )
    }
}

@Composable
private fun PlaybackField(
    icon: ImageVector,
    label: String,
    value: String,
    maxLength: Int,
    seed: Int,
    onValueChange: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Teal,
            modifier = Modifier
                .padding(top = 4.dp)
                .size(28.dp),
        )
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            FeldTitleTag(
                text = label,
                seed = seed * 3 + 1,
                distressed = false,
                textStyle = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.6.sp,
                ),
            )
            Spacer(Modifier.height(12.dp))
            FeldTextField(
                value = value,
                onValueChange = onValueChange,
                seed = seed * 3 + 2,
                maxLength = maxLength,
            )
        }
    }
}
