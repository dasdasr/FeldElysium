package com.example.feldelysium.presentation.settings

import android.annotation.SuppressLint
import androidx.activity.ComponentActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.feldelysium.data.settings.AppSettings
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@SuppressLint("ContextCastToActivity")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val activity = LocalContext.current as ComponentActivity
    val vm: SettingsViewModel = viewModel(activity)
    val settings by vm.state.collectAsStateWithLifecycle()
    var showPlaybackUiText by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .fillMaxWidth(),
        ) {
            ListItem(
                modifier = Modifier.clickable {
                    vm.dispatch(
                        SettingsEvent.SetLockAspectRatio16x9(!settings.lockAspectRatio16x9)
                    )
                },
                headlineContent = { Text("Lock 16:9") },
                supportingContent = { Text("Keep playback inside a 16:9 frame") },
                trailingContent = {
                    Switch(
                        checked = settings.lockAspectRatio16x9,
                        onCheckedChange = { enabled ->
                            vm.dispatch(SettingsEvent.SetLockAspectRatio16x9(enabled))
                        },
                    )
                },
            )
            ListItem(
                modifier = Modifier.clickable { showPlaybackUiText = true },
                headlineContent = { Text("Playback UI text") },
                supportingContent = { Text("Reals, time, and day") },
            )
        }
    }

    if (showPlaybackUiText) {
        PlaybackUiTextDialog(
            money = settings.playbackMoney,
            time = settings.playbackTime,
            day = settings.playbackDay,
            onMoneyChange = { vm.dispatch(SettingsEvent.SetPlaybackMoney(it)) },
            onTimeChange = { vm.dispatch(SettingsEvent.SetPlaybackTime(it)) },
            onDayChange = { vm.dispatch(SettingsEvent.SetPlaybackDay(it)) },
            onReset = { vm.dispatch(SettingsEvent.ResetPlaybackUiText) },
            onDismiss = { showPlaybackUiText = false },
        )
    }
}

@Composable
private fun PlaybackUiTextDialog(
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

    Dialog(onDismissRequest = onDismiss) {
        Surface {
            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 20.dp)
                    .fillMaxWidth(),
            ) {
                Text("Playback UI text")
                PlaybackStatusField(
                    label = "Reals",
                    value = moneyText,
                    onValueChange = {
                        moneyText = it
                        onMoneyChange(it)
                    },
                )
                PlaybackStatusField(
                    label = "Time",
                    value = timeText,
                    onValueChange = {
                        timeText = it
                        onTimeChange(it)
                    },
                )
                PlaybackStatusField(
                    label = "Day",
                    value = dayText,
                    onValueChange = {
                        dayText = it
                        onDayChange(it)
                    },
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    TextButton(
                        onClick = {
                            moneyText = defaults.playbackMoney
                            timeText = defaults.playbackTime
                            dayText = defaults.playbackDay
                            onReset()
                        },
                    ) {
                        Text("Reset")
                    }

                    Button(
                        onClick = onDismiss,
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

@Composable
private fun PlaybackStatusField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        label = { Text(label) },
        singleLine = true,
    )
}
