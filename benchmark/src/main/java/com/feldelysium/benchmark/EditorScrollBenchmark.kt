package com.feldelysium.benchmark

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.Direction
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class EditorScrollBenchmark {
    @get:Rule val rule = MacrobenchmarkRule()

    @Test
    fun scrollEntries() = rule.measureRepeated(
        packageName = APP_PACKAGE,
        metrics = listOf(FrameTimingMetric()),
        compilationMode = CompilationMode.Full(),
        iterations = 8,
        startupMode = StartupMode.WARM,
        setupBlock = {
            pressHome()
            startActivityAndWait()
            ensureSeeded()
        },
    ) {
        val list = waitFor("entry_list")
        list.setGestureMargin(device.displayWidth / 5)
        repeat(3) {
            list.fling(Direction.DOWN); device.waitForIdle()
            list.fling(Direction.UP); device.waitForIdle()
        }
    }
}
