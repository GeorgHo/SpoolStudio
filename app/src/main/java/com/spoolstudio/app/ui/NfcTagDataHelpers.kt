package com.spoolstudio.app.ui

import com.spoolstudio.app.domain.models.OpenSpoolData

data class NfcTagDataResult(
    val readData: OpenSpoolData,
    val currentSpoolId: String?,
    val spoolMode: SpoolMode
)

fun parseNfcTagData(data: String?): NfcTagDataResult? {
    val openSpoolData = data?.let { OpenSpoolData.fromJson(it) } ?: return null

    return NfcTagDataResult(
        readData = openSpoolData,
        currentSpoolId = openSpoolData.spoolId,
        spoolMode = if (openSpoolData.spoolId.isNullOrBlank()) {
            SpoolMode.CREATE
        } else {
            SpoolMode.UPDATE
        }
    )
}
