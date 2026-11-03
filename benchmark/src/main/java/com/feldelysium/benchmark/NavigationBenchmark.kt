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
class NavigationBenchmark {
    @get:Rule val rule = MacrobenchmarkRule()

    @Test
    fun editorToSettingsAndBack() = rule.measureRepeated(
        packageName = APP_PACKAGE,
        metrics = listOf(FrameTimingMetric()),
        compilationMode = CompilationMode.Full(),
        iterations = 8,
        startupMode = StartupMode.WARM,
        setupBlock = { pressHome(); startActivityAndWait() },
    ) {
        waitForText(UiText.SETTINGS).click()
        waitFor("settings_scroll")
        device.pressBack()
        waitForText(UiText.SETTINGS)
    }
}
