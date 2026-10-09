package com.example.feldelysium

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.example.feldelysium.designsystem.foundation.seed.ProvideSeedScope
import com.example.feldelysium.eggs.EggEvent
import com.example.feldelysium.eggs.ui.EggToastHost
import com.example.feldelysium.eggs.EggsViewModel
import com.example.feldelysium.eggs.ui.LocalEasterEggs
import com.example.feldelysium.navigation.AppNavGraph
import com.example.feldelysium.presentation.shared.DesignSystemPlayground
import com.example.feldelysium.theme.DiscoDialogueTheme

class MainActivity : ComponentActivity() {

    private val eggsVm: EggsViewModel by viewModels()

    companion object {
        private const val SHOW_PLAYGROUND = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
        )

        setContent {
            DiscoDialogueTheme {
                ProvideSeedScope {
                    val eggs = eggsVm.eggs

                    LaunchedEffect(eggs) {
                        if (savedInstanceState == null) {
                            eggs.awaitReady()
                            eggs.report(EggEvent.AppOpen)
                        }
                    }

                    CompositionLocalProvider(LocalEasterEggs provides eggs) {
                        if (SHOW_PLAYGROUND) {
                            DesignSystemPlayground()
                        } else {
                            Box(Modifier.fillMaxSize()) {
                                Surface(
                                    modifier = Modifier.fillMaxSize(),
                                    color = MaterialTheme.colorScheme.background
                                ) { AppNavGraph() }
                                EggToastHost(eggs)
                            }
                        }
                    }
                }
            }
        }
    }
}
