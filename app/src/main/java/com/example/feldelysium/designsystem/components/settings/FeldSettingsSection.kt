package com.example.feldelysium.designsystem.components.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.feldelysium.designsystem.components.FeldDivider
import com.example.feldelysium.designsystem.components.FeldIcon
import com.example.feldelysium.designsystem.components.FeldIconId
import com.example.feldelysium.designsystem.components.title.FeldTitleTag
import com.example.feldelysium.designsystem.seed.rememberSeed

private val SectionTagStyle: TextStyle
    @Composable get() = MaterialTheme.typography.labelLarge.copy(
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.6.sp,
    )

@Composable
fun FeldSettingsSection(
    title: String,
    modifier: Modifier = Modifier,
    seed: Int = rememberSeed(),
    tagDrop: Dp = 10.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier.fillMaxWidth()) {
        Box(Modifier.fillMaxWidth()) {
            FeldDivider(
                modifier = Modifier.align(Alignment.BottomStart),
                seed = seed + 31,
            )
            FeldTitleTag(
                text = title,
                modifier = Modifier.offset(y = tagDrop),
                seed = seed,
                distressed = true,
                textStyle = SectionTagStyle,
            )
        }
        content()
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
fun FeldSettingsDivider(modifier: Modifier = Modifier) {
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
        content = { FeldIcon(id = FeldIconId.Settings) }
    )
}
