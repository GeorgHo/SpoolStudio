package com.spoolstudio.app.ui

import com.spoolstudio.app.data.remote.spoolman.SpoolmanCatalog
import com.spoolstudio.app.domain.models.FilamentSpool
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SpoolmanCatalogStateHelpersTest {
    @Test
    fun catalogStateCopiesSpoolsAndFilterLists() {
        val spool = FilamentSpool(
            id = 7,
            material = "PLA",
            variant = "Basic",
            brand = "Generic",
            colorHex = "FFFFFF",
            minTemp = 190,
            maxTemp = 220,
            bedMinTemp = 40,
            bedMaxTemp = 65,
            spoolmanName = "PLA Basic White"
        )
        val state = buildSpoolmanCatalogState(
            SpoolmanCatalog(
                spools = listOf(spool),
                vendorNames = listOf("Generic", "GST 3D"),
                materialNames = listOf("PLA", "PETG"),
                variantNames = listOf("Basic", "Matte"),
                locationNames = listOf("Shelf A")
            )
        )

        assertEquals(listOf(spool), state.spools)
        assertEquals(listOf("Generic", "GST 3D"), state.availableBrands)
        assertEquals(listOf("PLA", "PETG"), state.availableMaterials)
        assertEquals(listOf("Basic", "Matte"), state.availableVariants)
        assertEquals(listOf("Shelf A"), state.availableLocations)
    }

    @Test
    fun emptyCatalogStateClearsEveryCatalogBackedList() {
        val state = emptySpoolmanCatalogState()

        assertTrue(state.spools.isEmpty())
        assertTrue(state.availableBrands.isEmpty())
        assertTrue(state.availableMaterials.isEmpty())
        assertTrue(state.availableVariants.isEmpty())
        assertTrue(state.availableLocations.isEmpty())
    }
}
