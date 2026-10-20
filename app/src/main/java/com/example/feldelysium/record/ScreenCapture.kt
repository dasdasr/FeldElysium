package com.example.feldelysium.record

import android.annotation.SuppressLint
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioPlaybackCaptureConfiguration
import android.media.AudioRecord
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.media.projection.MediaProjection
import android.os.Build
import android.os.Process
import androidx.annotation.RequiresApi
import java.io.File
import java.nio.ByteBuffer
import kotlin.concurrent.thread

internal class ScreenCapture(
    private val projection: MediaProjection,
    private val size: ScreenSize,
    withAudio: Boolean,
    output: File,
) {
    val hasAudio: Boolean get() = audioRecord != null
    private val startUs = nowUs()
    private val width = size.width and 1.inv()
    private val height = size.height and 1.inv()
    private val audioRecord: AudioRecord? =
        if (withAudio && Build.VERSION.SDK_INT >= 29) createAudioRecord() else null
    private val muxer = SyncMuxer(output.absolutePath, tracks = if (audioRecord != null) 2 else 1)

    @Volatile private var running = true
    private var videoCodec: MediaCodec? = null
    private var display: VirtualDisplay? = null
    private val threads = mutableListOf<Thread>()

    fun start() {
        val format = MediaFormat.createVideoFormat(VIDEO_MIME, width, height).apply {
            setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
            setInteger(MediaFormat.KEY_BIT_RATE, VIDEO_BITRATE)
            setInteger(MediaFormat.KEY_FRAME_RATE, FPS)
            setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
            setLong(MediaFormat.KEY_REPEAT_PREVIOUS_FRAME_AFTER, 1_000_000L / FPS)
        }
        val codec = MediaCodec.createEncoderByType(VIDEO_MIME)
        codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        val surface = codec.createInputSurface()
        codec.start()
        videoCodec = codec
        display = projection.createVirtualDisplay(
            "feld-record",
            width,
            height,
            size.dpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            surface,
            null,
            null,
        )
        threads += thread(name = "feld-video", isDaemon = true) { encodeVideo(codec) }
        audioRecord?.let { rec -> threads += thread(name = "feld-audio", isDaemon = true) { encodeAudio(rec) } }
    }

    fun stop() {
        running = false
        display?.release()
        runCatching { videoCodec?.signalEndOfInputStream() }
        threads.forEach { it.join(JOIN_MS) }
        muxer.finish()
    }

    private fun encodeVideo(codec: MediaCodec) {
        val info = MediaCodec.BufferInfo()
        var track = -1
        var firstPts = -1L
        var offsetUs = 0L
        var eos = false
        while (!eos) {
            val index = codec.dequeueOutputBuffer(info, TIMEOUT_US)
            if (index == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                track = muxer.addTrack(codec.outputFormat)
            } else if (index >= 0) {
                val data = codec.getOutputBuffer(index)
                if (data != null && info.isWritable() && track >= 0) {
                    if (firstPts < 0) {
                        firstPts = info.presentationTimeUs
                        offsetUs = nowUs() - startUs
                    }
                    info.presentationTimeUs = offsetUs + (info.presentationTimeUs - firstPts)
                    muxer.write(track, data, info)
                }
                codec.releaseOutputBuffer(index, false)
                eos = info.has(MediaCodec.BUFFER_FLAG_END_OF_STREAM)
            }
        }
        runCatching { codec.stop() }
        codec.release()
    }

    @Suppress("CyclomaticComplexMethod", "LongMethod", "LoopWithTooManyJumpStatements")
    private fun encodeAudio(record: AudioRecord) {
        val codec = MediaCodec.createEncoderByType(AUDIO_MIME)
        val format = MediaFormat.createAudioFormat(AUDIO_MIME, SAMPLE_RATE, CHANNELS).apply {
            setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC)
            setInteger(MediaFormat.KEY_BIT_RATE, AUDIO_BITRATE)
            setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, CHUNK_BYTES)
        }
        codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        codec.start()
        record.startRecording()

        val info = MediaCodec.BufferInfo()
        var track = -1
        var frames = 0L
        var anchorUs = -1L
        var inputDone = false
        var eos = false
        while (!eos) {
            if (!inputDone) {
                val index = codec.dequeueInputBuffer(TIMEOUT_US)
                if (index >= 0) {
                    val buf = codec.getInputBuffer(index)!!
                    buf.clear()
                    val read = if (running) record.read(buf, CHUNK_BYTES) else 0
                    if (read > 0 && anchorUs < 0) {
                        anchorUs = nowUs() - startUs - framesToUs((read / BYTES_PER_FRAME).toLong())
                    }
                    val ptsUs = maxOf(anchorUs, 0L) + framesToUs(frames)
                    when {
                        read > 0 -> {
                            codec.queueInputBuffer(index, 0, read, ptsUs, 0)
                            frames += read / BYTES_PER_FRAME
                        }

                        running -> codec.queueInputBuffer(index, 0, 0, ptsUs, 0)

                        else -> {
                            codec.queueInputBuffer(index, 0, 0, ptsUs, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                            inputDone = true
                        }
                    }
                }
            }
            while (true) {
                val out = codec.dequeueOutputBuffer(info, 0)
                if (out == MediaCodec.INFO_TRY_AGAIN_LATER) break
                if (out == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    track = muxer.addTrack(codec.outputFormat)
                    continue
                }
                if (out < 0) continue
                val data = codec.getOutputBuffer(out)
                if (data != null && info.isWritable() && track >= 0) {
                    muxer.write(track, data, info)
                }
                codec.releaseOutputBuffer(out, false)
                if (info.has(MediaCodec.BUFFER_FLAG_END_OF_STREAM)) {
                    eos = true
                    break
                }
            }
        }
        runCatching { record.stop() }
        record.release()
        runCatching { codec.stop() }
        codec.release()
    }

    @SuppressLint("MissingPermission")
    @RequiresApi(29)
    private fun createAudioRecord(): AudioRecord? = runCatching {
        val config = AudioPlaybackCaptureConfiguration.Builder(projection)
            .addMatchingUsage(AudioAttributes.USAGE_GAME)
            .addMatchingUsage(AudioAttributes.USAGE_MEDIA)
            .addMatchingUsage(AudioAttributes.USAGE_UNKNOWN)
            .addMatchingUid(Process.myUid())
            .build()
        val audioFormat = AudioFormat.Builder()
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .setSampleRate(SAMPLE_RATE)
            .setChannelMask(AudioFormat.CHANNEL_IN_STEREO)
            .build()
        val minBuf = AudioRecord.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_STEREO,
            AudioFormat.ENCODING_PCM_16BIT,
        )
        AudioRecord.Builder()
            .setAudioFormat(audioFormat)
            .setAudioPlaybackCaptureConfig(config)
            .setBufferSizeInBytes(maxOf(minBuf, CHUNK_BYTES * 4))
            .build()
            .takeIf { it.state == AudioRecord.STATE_INITIALIZED }
    }.getOrNull()

    private class SyncMuxer(path: String, private val tracks: Int) {
        private class Pending(val track: Int, val data: ByteBuffer, val info: MediaCodec.BufferInfo)

        private val muxer = MediaMuxer(path, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        private val queue = ArrayList<Pending>()
        private var added = 0
        private var started = false

        @Synchronized
        fun addTrack(format: MediaFormat): Int {
            val index = muxer.addTrack(format)
            if (++added == tracks) {
                muxer.start()
                started = true
                queue.forEach { muxer.writeSampleData(it.track, it.data, it.info) }
                queue.clear()
            }
            return index
        }

        @Synchronized
        fun write(track: Int, data: ByteBuffer, info: MediaCodec.BufferInfo) {
            if (started) {
                muxer.writeSampleData(track, data, info)
                return
            }
            data.position(info.offset)
            data.limit(info.offset + info.size)
            val copy = ByteBuffer.allocate(info.size).put(data).also { it.flip() }
            val copyInfo = MediaCodec.BufferInfo().apply { set(0, info.size, info.presentationTimeUs, info.flags) }
            queue += Pending(track, copy, copyInfo)
        }

        @Synchronized
        fun finish() {
            if (started) runCatching { muxer.stop() }
            muxer.release()
        }
    }

    private fun MediaCodec.BufferInfo.has(flag: Int) = (flags and flag) != 0

    private fun MediaCodec.BufferInfo.isWritable() = size > 0 && !has(MediaCodec.BUFFER_FLAG_CODEC_CONFIG)

    private fun nowUs() = System.nanoTime() / 1000

    private fun framesToUs(frames: Long) = frames * 1_000_000L / SAMPLE_RATE

    private companion object {
        const val VIDEO_MIME = "video/avc"
        const val AUDIO_MIME = "audio/mp4a-latm"
        const val VIDEO_BITRATE = 10_000_000
        const val AUDIO_BITRATE = 128_000
        const val FPS = 30
        const val SAMPLE_RATE = 44_100
        const val CHANNELS = 2
        const val BYTES_PER_FRAME = 4
        const val CHUNK_BYTES = 4096
        const val TIMEOUT_US = 10_000L
        const val JOIN_MS = 2_000L
    }
}
