package com.feldelysium.designsystem.components.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.feldelysium.designsystem.components.container.FeldDivider
import com.feldelysium.designsystem.components.container.FeldSectionHeader
import com.feldelysium.designsystem.components.content.FeldIcon
import com.feldelysium.designsystem.components.content.FeldIconId
import com.feldelysium.designsystem.foundation.seed.rememberSeed

@Composable
public fun FeldSettingsSection(
    title: String,
    modifier: Modifier = Modifier,
    seed: Int = rememberSeed(),
    tagDrop: Dp = 10.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier.fillMaxWidth()) {
        FeldSectionHeader(title = title, seed = seed, tagDrop = tagDrop)
        content()
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
public fun FeldSettingsDivider(modifier: Modifier = Modifier) {
    FeldDivider(
        modifier = modifier.padding(horizontal = 16.dp),
        color = Color(0xFF3A3D3C),
    )
}

@Preview(widthDp = 380, heightDp = 360, showBackground = true, backgroundColor = 0xFF0B1011)
@Composable
private fun FeldSettingsSectionPreview() {
    FeldSettingsSection(
        title = "SETTINGS",
        content = { FeldIcon(id = FeldIconId.Settings) },
    )
}
