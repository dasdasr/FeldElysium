package com.example.feldelysium.presentation.editor

import android.content.Context
import com.example.feldelysium.presentation.shared.FeldError
import com.example.feldelysium.presentation.shared.toastText
import androidx.annotation.StringRes

sealed interface EditorMessage {
    data class Text(@StringRes val resId: Int) : EditorMessage
    data class Error(val error: FeldError) : EditorMessage
}

fun EditorMessage.resolve(context: Context): String = when (this) {
    is EditorMessage.Text -> context.getString(resId)
    is EditorMessage.Error -> error.toastText(context)
}
