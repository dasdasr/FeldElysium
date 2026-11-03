package com.feldelysium.benchmark

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PlaybackBenchmark {
    @get:Rule
    val rule = MacrobenchmarkRule()

    @Test
    fun playbackTapThrough() = rule.measureRepeated(
        packageName = APP_PACKAGE,
        metrics = listOf(FrameTimingMetric()),
        compilationMode = CompilationMode.Full(),
        iterations = 6,
        startupMode = StartupMode.WARM,
        setupBlock = { pressHome(); startActivityAndWait(); ensureSeeded() },
    ) {
        waitForText(UiText.PLAYBACK).click()
        Thread.sleep(1_500)
        repeat(12) {
            device.click(device.displayWidth / 2, device.displayHeight / 2)
            Thread.sleep(600)
        }
        device.pressBack()
        waitForText(UiText.PLAYBACK)
    }
}
