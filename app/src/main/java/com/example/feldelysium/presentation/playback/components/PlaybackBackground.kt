package com.example.feldelysium.presentation.playback.components

import android.graphics.SurfaceTexture
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.view.Surface
import android.view.TextureView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import coil.compose.AsyncImage
import com.example.feldelysium.domain.model.settings.AppSettings
import com.example.feldelysium.domain.model.settings.BackgroundKind

@Composable
fun PlaybackBackground(settings: AppSettings, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val mediaOk = remember(settings.backgroundMediaUri) {
        settings.backgroundMediaUri.isNotBlank() && runCatching {
            context.contentResolver.openAssetFileDescriptor(settings.backgroundMediaUri.toUri(), "r")
                ?.close()
        }.isSuccess
    }
    val showMedia = settings.backgroundKind == BackgroundKind.MEDIA && mediaOk
    val color = Color(settings.backgroundColorArgb.toInt())

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(color),
    ) {
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

@Suppress("EmptyFunctionBlock")
@Composable
fun BackgroundVideo(
    uri: String,
    sound: Boolean,
    fill: Boolean,
    modifier: Modifier = Modifier,
    onError: () -> Unit = {},
) {
    val context = LocalContext.current
    val mp = remember(uri) { MediaPlayer() }
    var videoSize by remember(uri) { mutableStateOf<IntSize?>(null) }
    var surface by remember(uri) { mutableStateOf<Surface?>(null) }
    val currentOnError by rememberUpdatedState(onError)

    DisposableEffect(mp) {
        onDispose { runCatching { mp.release() } }
    }

    LaunchedEffect(mp) {
        runCatching {
            mp.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MOVIE)
                    .build(),
            )
            mp.setDataSource(context, uri.toUri())
            mp.isLooping = true
            mp.setOnVideoSizeChangedListener { _, w, h ->
                if (w > 0 && h > 0) videoSize = IntSize(w, h)
            }
            mp.setOnPreparedListener { it.start() }
            mp.setOnErrorListener { _, _, _ ->
                currentOnError()
                true
            }
            mp.prepareAsync()
        }.onFailure { currentOnError() }
    }

    LaunchedEffect(mp, surface) {
        runCatching { mp.setSurface(surface) }
    }

    LaunchedEffect(mp, sound) {
        val v = if (sound) 1f else 0f
        runCatching { mp.setVolume(v, v) }
    }

    BoxWithConstraints(modifier = modifier.clipToBounds(), contentAlignment = Alignment.Center) {
        val density = LocalDensity.current
        val cw = constraints.maxWidth.toFloat()
        val ch = constraints.maxHeight.toFloat()

        val videoModifier = videoSize
            ?.takeIf { it.width > 0 && it.height > 0 }
            ?.let { s ->
                val kw = cw / s.width
                val kh = ch / s.height
                val k = if (fill) maxOf(kw, kh) else minOf(kw, kh)
                with(density) {
                    Modifier.requiredSize((s.width * k).toDp(), (s.height * k).toDp())
                }
            } ?: Modifier.fillMaxSize()

        AndroidView(
            factory = { ctx ->
                TextureView(ctx).apply {
                    surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                        override fun onSurfaceTextureAvailable(st: SurfaceTexture, w: Int, h: Int) {
                            surface = Surface(st)
                        }

                        override fun onSurfaceTextureSizeChanged(st: SurfaceTexture, w: Int, h: Int) {}
                        override fun onSurfaceTextureDestroyed(st: SurfaceTexture): Boolean {
                            runCatching { mp.setSurface(null) }

                            surface?.let {
                                it.release()
                                surface = null
                            }

                            return true
                        }

                        override fun onSurfaceTextureUpdated(st: SurfaceTexture) {}
                    }
                }
            },
            modifier = videoModifier,
        )
    }
}
