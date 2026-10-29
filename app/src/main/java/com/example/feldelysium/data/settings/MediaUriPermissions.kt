package com.example.feldelysium.data.settings

import android.content.ContentResolver
import android.content.Intent
import androidx.core.net.toUri

class MediaUriPermissions(private val resolver: ContentResolver) {

    fun swap(previous: String, next: String) {
        val nextUri = runCatching { next.toUri() }.getOrNull() ?: return
        if (previous.isNotBlank() && previous != next) release(previous)
        runCatching { resolver.takePersistableUriPermission(nextUri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
    }

    private fun release(uri: String) {
        runCatching {
            resolver.releasePersistableUriPermission(uri.toUri(), Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}
