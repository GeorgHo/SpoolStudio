package com.spoolstudio.app.ui

import com.spoolstudio.app.domain.models.OpenSpoolData

data class NfcTagDataResult(
    val readData: OpenSpoolData,
    val currentSpoolId: String?,
    val spoolMode: SpoolMode
)

data class NfcTagReadStateUpdate(
    val rawReadText: String?,
    val rawReadVersion: Int,
    val readData: OpenSpoolData?,
    val currentSpoolId: String?,
    val spoolMode: SpoolMode?,
    val clearSelectedSpool: Boolean,
    val incrementDataVersion: Boolean
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

fun buildNfcTagReadStateUpdate(
    rawData: String?,
    currentRawReadVersion: Int,
    parsedTagData: NfcTagDataResult?
): NfcTagReadStateUpdate =
    NfcTagReadStateUpdate(
        rawReadText = rawData,
        rawReadVersion = currentRawReadVersion + 1,
        readData = parsedTagData?.readData,
        currentSpoolId = parsedTagData?.currentSpoolId,
        spoolMode = parsedTagData?.spoolMode,
        clearSelectedSpool = parsedTagData != null,
        incrementDataVersion = parsedTagData != null
    )
