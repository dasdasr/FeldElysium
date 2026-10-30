package com.feldelysium.app.eggs.ui

import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.feldelysium.app.eggs.EasterEggs

@Composable
fun EggToastHost(eggs: EasterEggs, modifier: Modifier = Modifier) {
    val toast by eggs.toast.collectAsStateWithLifecycle()
    var last by remember { mutableStateOf<EasterEggs.ActiveToast?>(null) }
    LaunchedEffect(toast) { toast?.let { last = it } }
    val current = toast ?: last

    val visible = remember { MutableTransitionState(false) }
    visible.targetState = toast != null

    if (visible.currentState || visible.targetState) {
        ToastScrim(visible)

        ToastWindow(touchable = true) {
            Box(
                modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 6.dp)
                    .padding(top = ToastSlack),
                contentAlignment = Alignment.BottomCenter,
            ) {
                AnimatedVisibility(
                    visibleState = visible,
                    enter = fadeIn(tween(160)) + slideInVertically(tween(260)) { it / 2 },
                    exit = fadeOut(tween(200)) + slideOutVertically(tween(220)) { it / 2 },
                ) {
                    current?.let { t ->
                        key(t.id) { EggToast(t, eggs::dismiss) }
                    }
                }
            }
        }
    }
}

@Composable
private fun ToastScrim(visible: MutableTransitionState<Boolean>) {
    ToastWindow(touchable = false) {
        Box(Modifier.fillMaxSize()) {
            AnimatedVisibility(
                visibleState = visible,
                enter = fadeIn(tween(320)),
                exit = fadeOut(tween(260)),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .fillMaxHeight(0.5f),
            ) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                0f to Color.Transparent,
                                0.6f to Color.Black.copy(alpha = 0.40f),
                                1f to Color.Black.copy(alpha = 0.80f),
                            ),
                        ),
                )
            }
        }
    }
}

@Composable
private fun ToastWindow(touchable: Boolean, content: @Composable () -> Unit) {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        ),
    ) {
        val window = (LocalView.current.parent as? DialogWindowProvider)?.window
        SideEffect {
            window?.apply {
                clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                var flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                if (!touchable) flags = flags or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                addFlags(flags)
                setDimAmount(0f)
                if (touchable) {
                    setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL)
                    setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                } else {
                    setGravity(Gravity.BOTTOM)
                    setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                }
            }
        }
        content()
    }
}
