package com.example.feldelysium.record

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.IBinder
import android.provider.MediaStore
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import androidx.core.content.IntentCompat
import com.example.feldelysium.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import kotlin.concurrent.thread

enum class RecordState { Idle, Recording }

sealed interface RecordResult {
    /** uri == null: Android 7–9,file is in app folder, not in gallery. */
    data class Saved(val uri: Uri?, val hasAudio: Boolean) : RecordResult
    data object Failed : RecordResult
}

class ScreenRecordService : Service() {

    private var projection: MediaProjection? = null
    private var capture: ScreenCapture? = null
    private var tempFile: File? = null
    private var hasAudio = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                startAsForeground()
                val data = IntentCompat.getParcelableExtra(intent, EXTRA_DATA, Intent::class.java)
                if (data == null) {
                    _result.value = RecordResult.Failed
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                } else {
                    runCatching {
                        startRecording(
                            resultCode = intent.getIntExtra(EXTRA_CODE, 0),
                            data = data,
                            size = ScreenSize(
                                intent.getIntExtra(EXTRA_W, 0),
                                intent.getIntExtra(EXTRA_H, 0),
                                intent.getIntExtra(EXTRA_DPI, 0),
                            ),
                            withAudio = intent.getBooleanExtra(EXTRA_AUDIO, false),
                        )
                    }.onFailure {
                        _result.value = RecordResult.Failed
                        stopSelf()
                    }
                }
            }

            ACTION_STOP -> stopRecording()
        }
        return START_NOT_STICKY
    }

    private fun startRecording(resultCode: Int, data: Intent, size: ScreenSize, withAudio: Boolean) {
        val file = File(cacheDir, "feld_${System.currentTimeMillis()}.mp4").also { tempFile = it }
        val mpm = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        val proj = mpm.getMediaProjection(resultCode, data)
            ?: error("Failed to obtain MediaProjection")

        proj.registerCallback(object : MediaProjection.Callback() {
            override fun onStop() {
                stopRecording()
            }
        }, null)

        projection = proj
        capture = ScreenCapture(proj, size, withAudio, file).also {
            it.start()
            hasAudio = it.hasAudio
        }
        _state.value = RecordState.Recording
    }

    private fun stopRecording() {
        if (_state.value == RecordState.Idle) return
        _state.value = RecordState.Idle
        val c = capture
        val p = projection
        val f = tempFile
        capture = null
        projection = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        val audio = hasAudio
        thread(name = "feld-finish") {
            runCatching { c?.stop() }
            p?.stop()
            _result.value = f?.takeIf { it.length() > 0 }?.let { saveToGallery(it, audio) }
                ?: RecordResult.Failed
            stopSelf()
        }
    }

    private fun saveToGallery(file: File, hasAudio: Boolean): RecordResult = runCatching {
        val name = "FeldElysium_${System.currentTimeMillis()}.mp4"
        val uri: Uri? = if (Build.VERSION.SDK_INT >= 29) {
            val values = ContentValues().apply {
                put(MediaStore.Video.Media.DISPLAY_NAME, name)
                put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES + "/FeldElysium")
            }
            val target = contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)
                ?: error("MediaStore insert failed")
            contentResolver.openOutputStream(target)?.use { out ->
                file.inputStream().use { it.copyTo(out) }
            } ?: error("Failed to open output stream")
            target
        } else {
            file.copyTo(File(getExternalFilesDir(Environment.DIRECTORY_MOVIES), name), overwrite = true)
            null
        }
        file.delete()
        RecordResult.Saved(uri, hasAudio)
    }.getOrDefault(RecordResult.Failed)

    private fun startAsForeground() {
        if (Build.VERSION.SDK_INT >= 26) {
            getSystemService(NotificationManager::class.java).createNotificationChannel(
                NotificationChannel(CHANNEL, "Screen recording", NotificationManager.IMPORTANCE_LOW),
            )
        }
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Recording scene…")
            .setOngoing(true)
            .build()
        ServiceCompat.startForeground(this, 1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION)
    }

    companion object {
        private const val ACTION_START = "feld.record.START"
        private const val ACTION_STOP = "feld.record.STOP"
        private const val EXTRA_CODE = "code"
        private const val EXTRA_DATA = "data"
        private const val EXTRA_W = "w"
        private const val EXTRA_H = "h"
        private const val EXTRA_DPI = "dpi"
        private const val EXTRA_AUDIO = "audio"
        private const val CHANNEL = "record"

        private val _state = MutableStateFlow(RecordState.Idle)
        val state: StateFlow<RecordState> = _state

        private val _result = MutableStateFlow<RecordResult?>(null)
        val result: StateFlow<RecordResult?> = _result

        fun consumeResult() {
            _result.value = null
        }

        fun start(
            context: Context,
            resultCode: Int,
            data: Intent,
            width: Int,
            height: Int,
            dpi: Int,
            withAudio: Boolean
        ) {
            val i = Intent(context, ScreenRecordService::class.java)
                .setAction(ACTION_START)
                .putExtra(EXTRA_CODE, resultCode)
                .putExtra(EXTRA_DATA, data)
                .putExtra(EXTRA_W, width)
                .putExtra(EXTRA_H, height)
                .putExtra(EXTRA_DPI, dpi)
                .putExtra(EXTRA_AUDIO, withAudio)
            ContextCompat.startForegroundService(context, i)
        }

        fun stop(context: Context) {
            context.startService(Intent(context, ScreenRecordService::class.java).setAction(ACTION_STOP))
        }
    }
}
