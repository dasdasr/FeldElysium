package com.example.feldelysium

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.feldelysium.designsystem.foundation.material.MarkerFilmWarmup
import com.example.feldelysium.designsystem.foundation.seed.ProvideSeedScope
import com.example.feldelysium.eggs.EggEvent
import com.example.feldelysium.eggs.EggsViewModel
import com.example.feldelysium.eggs.ui.EggToastHost
import com.example.feldelysium.eggs.ui.LocalEasterEggs
import com.example.feldelysium.navigation.AppNavGraph
import com.example.feldelysium.playground.DesignSystemPlayground
import com.example.feldelysium.playground.FeldDecalPlayground
import com.example.feldelysium.playground.FeldShaderPlayground
import com.example.feldelysium.playground.PlaygroundEntry
import com.example.feldelysium.playground.PlaygroundHubScreen
import com.example.feldelysium.theme.DiscoDialogueTheme
import android.graphics.Color as AndroidColor

class MainActivity : ComponentActivity() {

    private val eggsVm: EggsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
        )

        setContent {
            DiscoDialogueTheme {
                MarkerFilmWarmup()
                ProvideSeedScope {
                    val eggs = eggsVm.eggs

                    LaunchedEffect(eggs) {
                        if (savedInstanceState == null) {
                            eggs.awaitReady()
                            eggs.report(EggEvent.AppOpen)
                        }
                    }

                    var launchApp by rememberSaveable {
                        mutableStateOf(false)
                    }

                    CompositionLocalProvider(LocalEasterEggs provides eggs) {
                        if (launchApp) {
                            Box(Modifier.fillMaxSize()) {
                                Surface(
                                    modifier = Modifier.fillMaxSize(),
                                    color = MaterialTheme.colorScheme.background,
                                ) {
                                    AppNavGraph()
                                }
                                EggToastHost(eggs)
                            }
                        } else {
                            PlaygroundHubScreen(
                                onLaunchApp = { launchApp = true },
                                playgrounds = listOf(
                                    PlaygroundEntry(
                                        id = "decals",
                                        title = "Decal Playground",
                                        description = "Test decorative marks, textures and placement.",
                                        content = {
                                            FeldDecalPlayground()
                                        },
                                    ),
                                    PlaygroundEntry(
                                        id = "design-system",
                                        title = "Design System",
                                        description = "Preview and test reusable UI components.",
                                        content = {
                                            DesignSystemPlayground()
                                        },
                                    ),
                                    PlaygroundEntry(
                                        id = "shaders",
                                        title = "Shaders",
                                        description = "All shaders: grid and frame set up",
                                        content = { FeldShaderPlayground() },
                                    ),
                                ),
                            )
                        }
                    }
                }
            }
        }
    }
}
