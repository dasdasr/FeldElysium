package com.example.feldelysium.presentation.shared

sealed interface FeldError {
    data object RecordDenied : FeldError
    data object RecordFailed : FeldError
    data object ExportFailed : FeldError
    data object ImportFailed : FeldError
    data object EmptyImport : FeldError
    data object NoScriptPasted : FeldError
}

fun FeldError.toastText(): String = when (this) {
    FeldError.RecordDenied ->
        "NO TAPE, NO SHOW\nAllow screen recording, or turn off «Record playback» in Settings"

    FeldError.RecordFailed ->
        "TAPE JAMMED\nVideo wasn't saved. The record choked, try again"

    FeldError.ExportFailed ->
        "EXPORT FAILED\nThe scene refused to leave"

    FeldError.ImportFailed ->
        "CAN'T READ THIS\nThat file is not what I was expecting"

    FeldError.EmptyImport ->
        "NOTHING FOUND\nTry «NAME: text»"

    FeldError.NoScriptPasted ->
        "EMPTY\nPaste some lines first, I can't read minds"
}
