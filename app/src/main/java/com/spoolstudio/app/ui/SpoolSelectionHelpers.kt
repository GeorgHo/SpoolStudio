package com.spoolstudio.app.ui

import com.spoolstudio.app.domain.models.FilamentSpool
import com.spoolstudio.app.domain.models.OpenSpoolData

data class SpoolSelectionResult(
    val readData: OpenSpoolData?,
    val currentSpoolId: String?,
    val spoolMode: SpoolMode
)

fun buildSpoolSelectionResult(filament: FilamentSpool?): SpoolSelectionResult {
    return if (filament != null) {
        SpoolSelectionResult(
            readData = OpenSpoolData.toOpenSpoolData(filament),
            currentSpoolId = filament.id?.toString(),
            spoolMode = SpoolMode.UPDATE
        )
    } else {
        SpoolSelectionResult(
            readData = null,
            currentSpoolId = null,
            spoolMode = SpoolMode.CREATE
        )
    }
}
