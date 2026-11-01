package com.feldelysium.app.presentation.settings.sections

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.feldelysium.app.R
import com.feldelysium.app.eggs.EggEvent
import com.feldelysium.app.eggs.ui.LocalEasterEggs
import com.feldelysium.designsystem.components.content.FeldIconId
import com.feldelysium.designsystem.components.settings.FeldSettingStatus
import com.feldelysium.designsystem.components.settings.FeldSettingSwitchRow
import com.feldelysium.designsystem.components.settings.FeldSettingsSection

@Composable
internal fun ComingSoonSection() {
    FeldSettingsSection(title = stringResource(R.string.settings_coming_soon_section)) {
        val title = stringResource(R.string.settings_lock_landscape_title)

        SoonTap(title) {
            FeldSettingSwitchRow(
                icon = FeldIconId.Cinema,
                title = title,
                subtitle = stringResource(R.string.settings_lock_landscape_subtitle),
                checked = false,
                onCheckedChange = {},
                status = FeldSettingStatus.SOON,
            )
        }
    }
}

@Composable
private fun SoonTap(title: String, content: @Composable () -> Unit) {
    val eggs = LocalEasterEggs.current

    Box(
        Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
        ) { eggs?.report(EggEvent.SoonTap, title) },
    ) { content() }
}
