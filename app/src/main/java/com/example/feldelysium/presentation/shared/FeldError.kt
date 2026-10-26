package com.example.feldelysium.presentation.shared

import android.content.Context
import com.example.feldelysium.R

sealed interface FeldError {
    data object RecordDenied : FeldError
    data object RecordFailed : FeldError
    data object ExportFailed : FeldError
    data object ImportFailed : FeldError
    data object EmptyImport : FeldError
    data object NoScriptPasted : FeldError
}

fun FeldError.toastText(context: Context): String = context.getString(
    when (this) {
        FeldError.RecordDenied -> R.string.error_record_denied
        FeldError.RecordFailed -> R.string.error_record_failed
        FeldError.ExportFailed -> R.string.error_export_failed
        FeldError.ImportFailed -> R.string.error_import_failed
        FeldError.EmptyImport -> R.string.error_empty_import
        FeldError.NoScriptPasted -> R.string.error_no_script_pasted
    },
)
