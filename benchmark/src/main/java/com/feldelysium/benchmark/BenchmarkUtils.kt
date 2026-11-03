package com.feldelysium.benchmark

import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import java.util.regex.Pattern

const val APP_PACKAGE = "com.feldelysium.app"

object UiText {
    const val PASTE = "Paste"
    const val PLAYBACK = "Playback"
    const val SETTINGS = "Settings"
    const val IMPORT = "Import"
}

fun MacrobenchmarkScope.waitFor(tag: String, timeoutMs: Long = 5_000): UiObject2 =
    device.wait(Until.findObject(By.res(tag)), timeoutMs)
        ?: error("testTag=$tag not found")

fun MacrobenchmarkScope.waitForText(text: String, timeoutMs: Long = 5_000): UiObject2 =
    device.wait(
        Until.findObject(By.text(Pattern.compile(Pattern.quote(text), Pattern.CASE_INSENSITIVE))),
        timeoutMs,
    ) ?: error("text=$text not found")

private fun seedScript(lines: Int = 80): String = buildString {
    val names = listOf("WORTH", "VOLITION", "KIM KITSURAGI")
    repeat(lines) { i ->
        append(names[i % names.size]).append(": ")
        append("Line number ${i + 1}. This city is trying to kill you, and it is taking its time.\n")
    }
}

fun MacrobenchmarkScope.ensureSeeded() {
    if (device.wait(Until.hasObject(By.res("entry_list")), 1_500)) return

    waitForText(UiText.PASTE).click()

    val field = device.wait(
        Until.findObject(By.clazz("android.widget.EditText")),
        500,
    ) ?: error("Bulk import input field not found")

    field.text = seedScript()
    device.waitForIdle()

    waitForText(UiText.IMPORT).click()

    waitFor("entry_list", 1000)
}
