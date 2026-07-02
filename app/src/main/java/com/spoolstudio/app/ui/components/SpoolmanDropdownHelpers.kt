package com.spoolstudio.app.ui.components

import com.spoolstudio.app.domain.models.FilamentSpool

fun spoolmanDropdownLabel(filament: FilamentSpool): String {
    return "${filament.brand} - ${filament.spoolmanName} - ${filament.material}"
}

fun filterSpoolmanDropdownFilaments(
    filaments: List<FilamentSpool>,
    searchQuery: String
): List<FilamentSpool> {
    val query = searchQuery.trim()
    if (query.isBlank()) return filaments

    return filaments.filter { filament ->
        listOf(
            filament.id?.toString().orEmpty(),
            filament.brand,
            filament.spoolmanName.orEmpty(),
            filament.material,
            filament.variant,
            filament.location.orEmpty(),
            filament.lotNr.orEmpty(),
            filament.colorHex.orEmpty()
        ).any { value ->
            value.contains(query, ignoreCase = true)
        }
    }
}
