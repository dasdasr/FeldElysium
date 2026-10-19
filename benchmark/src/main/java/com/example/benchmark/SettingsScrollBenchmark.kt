package com.example.benchmark

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsScrollBenchmark {
    @get:Rule
    val rule = MacrobenchmarkRule()

    @Test
    fun scrollSettings() = rule.measureRepeated(
        packageName = "com.example.feldelysium",
        metrics = listOf(FrameTimingMetric()),
        compilationMode = CompilationMode.Full(),
        iterations = 4,
        startupMode = StartupMode.WARM,
        setupBlock = { pressHome(); startActivityAndWait() },
    ) {
        val list = device.findObject(By.res("settings_scroll"))
        list.setGestureMargin(device.displayWidth / 5)
        repeat(2) {
            list.fling(Direction.DOWN); device.waitForIdle()
            list.fling(Direction.UP); device.waitForIdle()
        }
    }
}
