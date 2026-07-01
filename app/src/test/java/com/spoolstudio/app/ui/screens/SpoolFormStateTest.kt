package com.spoolstudio.app.ui.screens

import com.spoolstudio.app.domain.models.Material
import com.spoolstudio.app.ui.SpoolMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SpoolFormStateTest {
    private fun formState(): SpoolFormState =
        SpoolFormState(Material("PLA", 190, 220, 40, 65))

    @Test
    fun buildSaveRequestUsesCurrentStateValues() {
        val form = formState().apply {
            filamentType = "Other"
            customMaterial = "ASA"
            variant = "Basic"
            brand = "Other"
            customBrand = "CustomBrand"
            location = "Other"
            customLocation = "Shelf A"
            colorHex = "FFAA00"
            colorName = "Orange"
            minTemp = "240"
            maxTemp = "260"
            bedMinTemp = "90"
            bedMaxTemp = "100"
            lotNr = "LOT-1"
            comment = "Test"
            remainingWeight = "750"
        }

        val request = form.buildSaveRequest(SpoolMode.CREATE, selectedSpool = null)

        assertEquals("ASA", request.material)
        assertEquals("CustomBrand", request.brand)
        assertEquals("Shelf A", request.location)
        assertEquals("750", request.remainingWeight)
        assertNull(request.existingSpoolId)
    }

    @Test
    fun validationUsesCurrentStateValues() {
        val form = formState()

        assertTrue(form.isValid())

        form.remainingWeight = "-1"

        assertEquals("Please enter a valid remaining weight", form.validationMessage())
    }
}
