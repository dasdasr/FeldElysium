package com.example.feldelysium.presentation.editor.sections

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.feldelysium.R
import com.example.feldelysium.designsystem.components.content.FeldIconId
import com.example.feldelysium.designsystem.components.navigation.FeldToolbar
import com.example.feldelysium.designsystem.components.navigation.FeldToolbarItem


@Composable
internal fun EditorToolbar(
    hasEntries: Boolean,
    onPaste: () -> Unit,
    onPlayback: () -> Unit,
    onSettings: () -> Unit,
) {
    FeldToolbar(
        items = listOf(
            FeldToolbarItem(
                icon = FeldIconId.BulkImport,
                label = stringResource(R.string.editor_paste),
                onClick = onPaste,
            ),
            FeldToolbarItem(
                icon = FeldIconId.Play,
                label = stringResource(R.string.editor_playback),
                enabled = hasEntries,
                onClick = onPlayback,
            ),
            FeldToolbarItem(
                icon = FeldIconId.Settings,
                label = stringResource(R.string.editor_settings),
                onClick = onSettings,
            ),
        ),
    )
}
