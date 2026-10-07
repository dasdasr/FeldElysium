package com.example.feldelysium.presentation.playback

import android.media.MediaPlayer
import android.net.Uri
import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.example.feldelysium.data.settings.AppSettings
import com.example.feldelysium.data.settings.BackgroundKind

@Composable
fun PlaybackBackground(
    settings: AppSettings,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val mediaOk = remember(settings.backgroundMediaUri) {
        settings.backgroundMediaUri.isNotBlank() && runCatching {
            context.contentResolver.openAssetFileDescriptor(Uri.parse(settings.backgroundMediaUri), "r")
                ?.close()
        }.isSuccess
    }
    val showMedia = settings.backgroundKind == BackgroundKind.MEDIA && mediaOk
    // Always the chosen color: it shows through letterbox bars around media.
    val color = Color(settings.backgroundColorArgb.toInt())

    Box(modifier = modifier.fillMaxSize().background(color)) {
        if (!showMedia) return@Box
        if (settings.backgroundMediaIsVideo) {
            var failed by remember(settings.backgroundMediaUri) { mutableStateOf(false) }
            if (failed) return@Box
            BackgroundVideo(
                uri = settings.backgroundMediaUri,
                sound = settings.backgroundVideoSound,
                fill = settings.backgroundVideoFill,
                onError = { failed = true },
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            AsyncImage(
                model = settings.backgroundMediaUri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
fun BackgroundVideo(
    uri: String,
    sound: Boolean,
    fill: Boolean,
    modifier: Modifier = Modifier,
    onError: () -> Unit = {},
) {
    var player by remember(uri) { mutableStateOf<MediaPlayer?>(null) }
    var videoSize by remember(uri) { mutableStateOf<IntSize?>(null) }

    LaunchedEffect(sound, player) {
        val v = if (sound) 1f else 0f
        runCatching { player?.setVolume(v, v) }
    }

    BoxWithConstraints(modifier = modifier.clipToBounds(), contentAlignment = Alignment.Center) {
        val density = LocalDensity.current
        val cw = constraints.maxWidth.toFloat()
        val ch = constraints.maxHeight.toFloat()
        val videoModifier = videoSize
            ?.takeIf { fill && it.width > 0 && it.height > 0 }
            ?.let { s ->
                val k = maxOf(cw / s.width, ch / s.height)
                with(density) {
                    Modifier.requiredSize((s.width * k).toDp(), (s.height * k).toDp())
                }
            } ?: Modifier.fillMaxSize()

        key(uri) {
            AndroidView(
                factory = { ctx ->
                    VideoView(ctx).apply {
                        setVideoURI(Uri.parse(uri))
                        setOnErrorListener { _, _, _ -> onError(); true }
                        setOnPreparedListener { mp ->
                            mp.isLooping = true
                            mp.setVolume(0f, 0f)
                            videoSize = IntSize(mp.videoWidth, mp.videoHeight)
                            player = mp
                            start()
                        }
                    }
                },
                modifier = videoModifier,
            )
        }
    }
}
