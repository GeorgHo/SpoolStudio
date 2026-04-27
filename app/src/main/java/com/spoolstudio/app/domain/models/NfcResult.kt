package com.spoolstudio.app.domain.models

sealed class NfcResult {
    data class Success(val data: String) : NfcResult()
    data class Error(val message: String) : NfcResult()
    object TagDetected : NfcResult()
    object NoTag : NfcResult()
}
