package com.example.feldelysium.presentation.settings

import android.annotation.SuppressLint
import androidx.activity.ComponentActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
        ListItem(
            modifier = Modifier
                .padding(padding)
                .clickable {
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
    }
}
