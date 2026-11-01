package com.feldelysium.app.record

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.util.DisplayMetrics
import androidx.compose.runtime.staticCompositionLocalOf

object ScreenRecordCoordinator {
    class Grant(val resultCode: Int, val data: Intent, val withAudio: Boolean)

    @Volatile
    private var grant: Grant? = null

    fun put(value: Grant) {
        grant = value
    }

    fun take(): Grant? = grant.also { grant = null }
}

val LocalScreenRecordRequest =
    staticCompositionLocalOf<(withAudio: Boolean, onGranted: () -> Unit, onDenied: () -> Unit) -> Unit> {
        { _, _, _ -> }
    }

data class ScreenSize(val width: Int, val height: Int, val dpi: Int)

@Suppress("DEPRECATION")
fun Activity.realScreenSize(): ScreenSize {
    val dpi = resources.displayMetrics.densityDpi
    return if (Build.VERSION.SDK_INT >= 30) {
        val b = windowManager.maximumWindowMetrics.bounds
        ScreenSize(b.width(), b.height(), dpi)
    } else {
        val m = DisplayMetrics()
        windowManager.defaultDisplay.getRealMetrics(m)
        ScreenSize(m.widthPixels, m.heightPixels, dpi)
    }
}
