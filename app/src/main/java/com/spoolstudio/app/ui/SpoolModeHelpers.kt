package com.spoolstudio.app.ui

import com.spoolstudio.app.domain.models.FilamentSpool
import com.spoolstudio.app.domain.models.OpenSpoolData

fun buildSpoolModeSourceData(
    selectedSpool: FilamentSpool?,
    readData: OpenSpoolData?
): OpenSpoolData? = when {
    selectedSpool != null -> OpenSpoolData.toOpenSpoolData(selectedSpool).copy(
        spoolId = null,
        lotNr = null
    )
    readData != null -> readData.copy(
        spoolId = null,
        lotNr = null
    )
    else -> null
}
