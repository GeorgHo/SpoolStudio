package com.spoolstudio.app.ui

import com.spoolstudio.app.domain.models.FilamentSpool
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SpoolModeHelpersTest {
    @Test
    fun buildSpoolModeSourceDataKeepsSelectedSpoolMaterialForDuplicateForm() {
        val source = FilamentSpool(
            id = 42,
            material = "PLA+",
            variant = "Silk",
            brand = "GST 3D",
            colorHex = "00B1E7",
            minTemp = 200,
            maxTemp = 230,
            bedMinTemp = 50,
            bedMaxTemp = 70,
            remainingWeight = 60f,
            spoolmanName = "Cyan Blue",
            lotNr = "LOT-1"
        )

        val result = buildSpoolModeSourceData(source, readData = null)

        assertEquals("PLA+", result?.type)
        assertEquals("Silk", result?.subtype)
        assertEquals("GST 3D", result?.brand)
        assertNull(result?.colorHex)
        assertNull(result?.spoolId)
        assertNull(result?.lotNr)
    }
}
