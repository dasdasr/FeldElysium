package com.example.feldelysium.presentation.settings.speakerpack

import android.content.Context
import com.example.feldelysium.R

sealed interface PackMessage {
    data object Exported : PackMessage
    data object ExportFailed : PackMessage
    data object CannotRead : PackMessage
    data class NotAPack(val detail: String?) : PackMessage
    data class Imported(val added: Int, val replaced: Int, val skipped: Int) : PackMessage
}

fun PackMessage.text(context: Context): String = when (this) {
    PackMessage.Exported -> context.getString(R.string.pack_exported)

    PackMessage.ExportFailed -> context.getString(R.string.pack_export_failed)

    PackMessage.CannotRead -> context.getString(R.string.pack_cannot_read)

    is PackMessage.NotAPack -> detail ?: context.getString(R.string.pack_not_a_pack)

    is PackMessage.Imported -> buildString {
        append(context.getString(R.string.pack_added, added))
        if (replaced > 0) append(context.getString(R.string.pack_replaced, replaced))
        if (skipped > 0) append(context.getString(R.string.pack_skipped, skipped))
    }
}
