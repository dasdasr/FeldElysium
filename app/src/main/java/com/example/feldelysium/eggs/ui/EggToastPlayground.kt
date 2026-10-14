package com.example.feldelysium.eggs.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.feldelysium.domain.registry.PredefinedSpeakers
import com.example.feldelysium.eggs.CheckResult
import com.example.feldelysium.eggs.Difficulty
import com.example.feldelysium.eggs.EasterEggs
import com.example.feldelysium.eggs.EggReply

@Composable
fun EggToastPlayground(text: String, modifier: Modifier = Modifier) {
    val toast = remember {
        EasterEggs.ActiveToast(
            id = 1L,
            eggId = "playground",
            reply = EggReply(
                skill = PredefinedSpeakers.LOGIC,
                text = text,
                difficulty = Difficulty.Medium,
                result = CheckResult.Success,
            ),
            durationMillis = Long.MAX_VALUE,
        )
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter,
    ) {
        EggToast(
            toast = toast,
            onDismiss = {},
        )
    }
}
