package com.spoolstudio.app.ui.screens

import com.spoolstudio.app.domain.models.FilamentSpool
import com.spoolstudio.app.ui.SpoolMode
import com.spoolstudio.app.utils.formatColorName
import com.spoolstudio.app.utils.suggestColorName

fun SpoolFormState.applySpoolSource(
    sourceSpool: FilamentSpool,
    spoolMode: SpoolMode,
    availableLocations: List<String>
) {
    filamentType = sourceSpool.material
    variant = sourceSpool.variant.ifBlank { "Basic" }
    colorHex = sourceSpool.colorHex
    colorName = formatColorName(
        sourceSpool.spoolmanName?.takeIf { it.isNotBlank() }
            ?: sourceSpool.colorHex?.let(::suggestColorName)
            ?: ""
    )
    brand = sourceSpool.brand

    val loadedLocation = sourceSpool.location.orEmpty().trim()
    if (loadedLocation.isBlank()) {
        clearLocation()
    } else if (loadedLocation in availableLocations) {
        location = loadedLocation
        customLocation = ""
    } else {
        location = "Other"
        customLocation = loadedLocation
    }

    minTemp = sourceSpool.minTemp?.toString() ?: minTemp
    maxTemp = sourceSpool.maxTemp?.toString() ?: maxTemp
    bedMinTemp = sourceSpool.bedMinTemp?.toString() ?: bedMinTemp
    bedMaxTemp = sourceSpool.bedMaxTemp?.toString() ?: bedMaxTemp
    lotNr = if (spoolMode == SpoolMode.UPDATE) {
        sourceSpool.lotNr.orEmpty()
    } else {
        ""
    }
    comment = if (spoolMode == SpoolMode.UPDATE) {
        sourceSpool.comment ?: ""
    } else {
        "Created by Spool Studio"
    }
    remainingWeight = formatLoadedRemainingWeight(sourceSpool.remainingWeight)
    emptySpoolWeight = formatLoadedEmptySpoolWeight(sourceSpool.emptySpoolWeight)
    colorHexInput = colorHex ?: ""
    colorNameWasManuallyEdited = false
}
