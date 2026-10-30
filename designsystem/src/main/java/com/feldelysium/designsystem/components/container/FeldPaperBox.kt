package com.feldelysium.designsystem.components.container

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.feldelysium.designsystem.foundation.material.FeldPaper
import com.feldelysium.designsystem.foundation.material.PaperSpec
import com.feldelysium.designsystem.foundation.material.paperSurface
import com.feldelysium.designsystem.foundation.seed.rememberSeed
import com.feldelysium.designsystem.foundation.shape.RoughStyle

@Composable
public fun FeldPaperBox(
    modifier: Modifier = Modifier,
    seed: Int = rememberSeed(),
    spec: PaperSpec = FeldPaper.Panel,
    rough: RoughStyle = RoughStyle(),
    textured: Boolean = true,
    contentAlignment: Alignment = Alignment.TopStart,
    content: @Composable BoxScope.() -> Unit = {},
) {
    Box(
        modifier = modifier.paperSurface(seed, rough, spec, textured),
        contentAlignment = contentAlignment,
        content = content,
    )
}
