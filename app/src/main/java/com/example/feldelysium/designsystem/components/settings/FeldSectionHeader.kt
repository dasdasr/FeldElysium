package com.example.feldelysium.designsystem.components.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.feldelysium.designsystem.components.FeldDivider
import com.example.feldelysium.designsystem.components.feldClickable
import com.example.feldelysium.designsystem.components.title.FeldTitleTag

private val HeaderTagStyle: TextStyle
    @Composable get() = MaterialTheme.typography.labelLarge.copy(
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.6.sp,
    )

@Composable
fun FeldSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    seed: Int = 0,
    tagDrop: Dp = 10.dp,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable RowScope.() -> Unit)? = null,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.feldClickable(onClick = onClick) else Modifier),
    ) {
        FeldDivider(
            modifier = Modifier.align(Alignment.BottomStart),
            seed = seed + 31,
        )
        FeldTitleTag(
            text = title,
            modifier = Modifier.offset(y = tagDrop),
            seed = seed,
            distressed = true,
            textStyle = HeaderTagStyle,
        )
        if (trailing != null) {
            Row(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .offset(y = tagDrop / 2)
                    .padding(end = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                content = trailing,
            )
        }
    }
}
