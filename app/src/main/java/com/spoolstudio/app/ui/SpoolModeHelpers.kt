package com.spoolstudio.app.ui

import com.spoolstudio.app.domain.models.FilamentSpool
import com.spoolstudio.app.domain.models.OpenSpoolData

fun buildSpoolModeSourceData(
    selectedSpool: FilamentSpool?,
    readData: OpenSpoolData?
): OpenSpoolData? = when {
    selectedSpool != null -> selectedSpool.toFormSourceData()
    readData != null -> readData.copy(
        spoolId = null,
        lotNr = null
    )
    else -> null
}

private fun FilamentSpool.toFormSourceData(): OpenSpoolData = OpenSpoolData(
    type = material,
    colorHex = null,
    brand = brand,
    minTemp = minTemp?.toString() ?: "200",
    maxTemp = maxTemp?.toString() ?: "220",
    bedMinTemp = bedMinTemp?.toString(),
    bedMaxTemp = bedMaxTemp?.toString(),
    subtype = variant.ifBlank { "Basic" },
    spoolId = null,
    lotNr = null
)
