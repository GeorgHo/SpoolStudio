package com.spoolstudio.app.domain.models

data class AppState(
    val currentSpool: FilamentSpool? = null,
    val isNfcEnabled: Boolean = false,
    val currentTag: NfcTag? = null,
    val lastResult: NfcResult = NfcResult.NoTag
)
