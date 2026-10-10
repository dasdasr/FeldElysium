package com.example.feldelysium.designsystem.components.input

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalFocusManager

class TapOutsideRegistry {
    private val rects = HashMap<Any, Rect>()

    fun register(key: Any, rect: Rect) {
        rects[key] = rect
    }

    fun unregister(key: Any) {
        rects.remove(key)
    }

    val isEmpty: Boolean get() = rects.isEmpty()

    fun contains(point: Offset): Boolean = rects.values.any { it.contains(point) }
}

val LocalTapOutsideRegistry = staticCompositionLocalOf<TapOutsideRegistry?> { null }

@Composable
fun FeldTapOutsideDismiss(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val registry = remember { TapOutsideRegistry() }
    var origin by remember { mutableStateOf(Offset.Zero) }

    CompositionLocalProvider(LocalTapOutsideRegistry provides registry) {
        Box(
            modifier = modifier
                .onGloballyPositioned { origin = it.positionInWindow() }
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val down = awaitFirstDown(
                            requireUnconsumed = false,
                            pass = PointerEventPass.Initial,
                        )
                        if (!registry.isEmpty && !registry.contains(down.position + origin)) {
                            focusManager.clearFocus()
                        }
                    }
                },
        ) {
            content()
        }
    }
}
