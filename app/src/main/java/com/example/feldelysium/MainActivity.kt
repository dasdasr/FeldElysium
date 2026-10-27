package com.example.feldelysium

import android.Manifest
import android.content.pm.PackageManager
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
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
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
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
import com.example.feldelysium.record.LocalScreenRecordRequest
import com.example.feldelysium.record.ScreenRecordCoordinator
import com.example.feldelysium.theme.DiscoDialogueTheme
import android.graphics.Color as AndroidColor

class MainActivity : AppCompatActivity() {

    private val eggsVm: EggsViewModel by viewModels()

    private var onGranted: (() -> Unit)? = null
    private var onDenied: (() -> Unit)? = null
    private var grantWithAudio = false

    private val captureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        val data = result.data
        if (result.resultCode == RESULT_OK && data != null) {
            ScreenRecordCoordinator.put(
                ScreenRecordCoordinator.Grant(result.resultCode, data, grantWithAudio),
            )
            onGranted?.invoke()
        } else {
            onDenied?.invoke()
        }
        onGranted = null
        onDenied = null
    }

    private val audioPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted -> launchCapture(withAudio = granted) }

    private fun requestScreenRecording(withAudio: Boolean, onGranted: () -> Unit, onDenied: () -> Unit) {
        this.onGranted = onGranted
        this.onDenied = onDenied
        val needAudio = withAudio && Build.VERSION.SDK_INT >= 29
        val hasPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED
        if (needAudio && !hasPermission) {
            audioPermission.launch(Manifest.permission.RECORD_AUDIO)
        } else {
            launchCapture(needAudio)
        }
    }

    private fun launchCapture(withAudio: Boolean) {
        grantWithAudio = withAudio
        val mpm = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        captureLauncher.launch(mpm.createScreenCaptureIntent())
    }

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
                        LocalScreenRecordRequest provides ::requestScreenRecording,
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
