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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModelProvider
import com.example.feldelysium.designsystem.foundation.material.FeldWarmup
import com.example.feldelysium.designsystem.foundation.seed.ProvideSeedScope
import com.example.feldelysium.designsystem.foundation.sound.LocalFeldSounds
import com.example.feldelysium.di.PlayerFeldSounds
import com.example.feldelysium.domain.sound.SoundCue
import com.example.feldelysium.eggs.EggEvent
import com.example.feldelysium.eggs.EggsViewModel
import com.example.feldelysium.eggs.ui.EggToastHost
import com.example.feldelysium.eggs.ui.LocalEasterEggs
import com.example.feldelysium.navigation.AppNavGraph
import com.example.feldelysium.theme.DiscoDialogueTheme
import android.graphics.Color as AndroidColor

class MainActivity : ComponentActivity() {

    private val eggsVm: EggsViewModel by viewModels()

    private val container get() = (application as FeldApp).container

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        splash.setKeepOnScreenCondition { !container.sceneSession.isLoaded.value }

        container.soundPlayer.preload(SoundCue.uiSet)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
        )

        setContent {
            DiscoDialogueTheme {
                FeldWarmup()

                ProvideSeedScope {
                    val eggs = eggsVm.eggs

                    LaunchedEffect(eggs) {
                        if (savedInstanceState == null) {
                            eggs.awaitReady()
                            eggs.report(EggEvent.AppOpen)
                        }
                    }

                    val sounds = remember { PlayerFeldSounds(container.soundPlayer) }

                    CompositionLocalProvider(
                        LocalEasterEggs provides eggs,
                        LocalFeldSounds provides sounds,
                    ) {
                        Box(Modifier.fillMaxSize().semantics { testTagsAsResourceId = true }) {
                            Surface(
                                modifier = Modifier.fillMaxSize(),
                                color = MaterialTheme.colorScheme.background,
                            ) {
                                AppNavGraph()
                            }
                            EggToastHost(eggs)
                        }
                    }
                }
            }
        }
    }

    override fun onStop() {
        container.sceneSession.flush()
        super.onStop()
    }

    override fun onDestroy() {
        if (isFinishing) container.soundPlayer.release()
        super.onDestroy()
    }
}
