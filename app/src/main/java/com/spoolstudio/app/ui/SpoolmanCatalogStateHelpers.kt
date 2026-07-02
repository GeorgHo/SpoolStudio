package com.spoolstudio.app.ui

import com.spoolstudio.app.data.remote.spoolman.SpoolmanCatalog
import com.spoolstudio.app.domain.models.FilamentSpool

data class SpoolmanCatalogState(
    val spools: List<FilamentSpool>,
    val availableBrands: List<String>,
    val availableMaterials: List<String>,
    val availableVariants: List<String>,
    val availableLocations: List<String>
)

fun buildSpoolmanCatalogState(catalog: SpoolmanCatalog): SpoolmanCatalogState {
    return SpoolmanCatalogState(
        spools = catalog.spools,
        availableBrands = catalog.vendorNames,
        availableMaterials = catalog.materialNames,
        availableVariants = catalog.variantNames,
        availableLocations = catalog.locationNames
    )
}

fun emptySpoolmanCatalogState(): SpoolmanCatalogState {
    return SpoolmanCatalogState(
        spools = emptyList(),
        availableBrands = emptyList(),
        availableMaterials = emptyList(),
        availableVariants = emptyList(),
        availableLocations = emptyList()
    )
}
