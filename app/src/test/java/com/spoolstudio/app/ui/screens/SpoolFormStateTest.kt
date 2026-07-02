package com.spoolstudio.app.ui.screens

import com.spoolstudio.app.domain.models.Material
import com.spoolstudio.app.domain.models.FilamentSpool
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

    @Test
    fun applyBambuRfidDataUpdatesFormState() {
        val form = formState().apply {
            remainingWeight = "123.45"
            comment = "Existing comment"
        }
        val data = BambuRfidFormData(
            material = "PETG",
            detailedType = "PETG Basic",
            colorHex = "00AACC",
            minHotend = 230,
            maxHotend = 250,
            bedTemp = 80,
            uid = "1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ",
            normalizedVariant = "Basic"
        )

        form.applyBambuRfidData(data, suggestedColorName = "Cyan")

        assertEquals("PETG", form.filamentType)
        assertEquals("", form.customMaterial)
        assertEquals("Basic", form.variant)
        assertEquals("00AACC", form.colorHex)
        assertEquals("00AACC", form.colorHexInput)
        assertEquals("Cyan", form.colorName)
        assertEquals("Bambu Lab", form.brand)
        assertEquals("", form.customBrand)
        assertEquals("", form.location)
        assertEquals("", form.customLocation)
        assertEquals("230", form.minTemp)
        assertEquals("250", form.maxTemp)
        assertEquals("70", form.bedMinTemp)
        assertEquals("90", form.bedMaxTemp)
        assertEquals("1234567890ABCDEFGHIJKLMNOPQRSTUV", form.lotNr)
        assertEquals("", form.remainingWeight)
        assertEquals("", form.comment)
    }

    @Test
    fun applySpoolSourceMapsKnownAndCustomLocation() {
        val form = formState()
        val sourceSpool = FilamentSpool(
            id = 7,
            material = "PETG",
            variant = "",
            brand = "Brand A",
            colorHex = "00AACC",
            location = "Rack 3",
            minTemp = 230,
            maxTemp = 250,
            bedMinTemp = 70,
            bedMaxTemp = 90,
            lotNr = "LOT-7",
            comment = "Existing",
            remainingWeight = 512.5f,
            spoolmanName = "PETG Cyan"
        )

        form.applySpoolSource(
            sourceSpool = sourceSpool,
            spoolMode = SpoolMode.UPDATE,
            availableLocations = listOf("Shelf A")
        )

        assertEquals("PETG", form.filamentType)
        assertEquals("Basic", form.variant)
        assertEquals("Brand A", form.brand)
        assertEquals("00AACC", form.colorHex)
        assertEquals("Petg Cyan", form.colorName)
        assertEquals("Other", form.location)
        assertEquals("Rack 3", form.customLocation)
        assertEquals("230", form.minTemp)
        assertEquals("250", form.maxTemp)
        assertEquals("70", form.bedMinTemp)
        assertEquals("90", form.bedMaxTemp)
        assertEquals("LOT-7", form.lotNr)
        assertEquals("Existing", form.comment)
        assertEquals("512.50", form.remainingWeight)
    }
}
