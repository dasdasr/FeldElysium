package com.example.feldelysium.data.settings

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri

class MediaUriPermissions(private val resolver: ContentResolver) {

    fun swap(previous: String, next: String) {
        val nextUri = runCatching { Uri.parse(next) }.getOrNull() ?: return
        if (previous.isNotBlank() && previous != next) release(previous)
        runCatching { resolver.takePersistableUriPermission(nextUri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
    }

    private fun release(uri: String) {
        runCatching {
            resolver.releasePersistableUriPermission(Uri.parse(uri), Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}
