package com.spoolstudio.app.ui.screens

import com.spoolstudio.app.ui.SpoolMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SpoolFormHelpersTest {
    @Test
    fun resolveNamesUseCustomValuesOnlyForOtherSelection() {
        assertEquals("PLA", resolveMaterialName("PLA", "PETG-CF"))
        assertEquals("PETG-CF", resolveMaterialName("Other", "PETG-CF"))
        assertEquals("Generic", resolveBrandName("Generic", "CustomBrand"))
        assertEquals("CustomBrand", resolveBrandName("Other", "CustomBrand"))
        assertEquals("", resolveVariantName("Other"))
        assertEquals("Kitchen", resolveLocationName("Other", " Kitchen "))
    }

    @Test
    fun remainingWeightAcceptsBlankCommaDecimalAndPositiveValues() {
        assertTrue(isRemainingWeightValid(""))
        assertTrue(isRemainingWeightValid("123"))
        assertTrue(isRemainingWeightValid("123,45"))
        assertTrue(isRemainingWeightValid("123.45"))
        assertFalse(isRemainingWeightValid("-1"))
        assertFalse(isRemainingWeightValid("abc"))
    }

    @Test
    fun validationMessageReportsFirstInvalidField() {
        assertEquals(
            "Please enter a custom filament type",
            spoolFormValidationMessage(
                variant = "Basic",
                brand = "Generic",
                customBrand = "",
                filamentType = "Other",
                customMaterial = "",
                remainingWeight = ""
            )
        )

        assertEquals(
            "Please enter a valid remaining weight",
            spoolFormValidationMessage(
                variant = "Basic",
                brand = "Generic",
                customBrand = "",
                filamentType = "PLA",
                customMaterial = "",
                remainingWeight = "-1"
            )
        )

        assertNull(
            spoolFormValidationMessage(
                variant = "Basic",
                brand = "Generic",
                customBrand = "",
                filamentType = "PLA",
                customMaterial = "",
                remainingWeight = "42,50"
            )
        )
    }

    @Test
    fun buildSaveRequestKeepsCreateModeWithoutExistingSpoolId() {
        val request = buildSpoolmanSaveRequest(
            filamentType = "Other",
            customMaterial = "ASA",
            variant = "Basic",
            brand = "Other",
            customBrand = "CustomBrand",
            location = "Other",
            customLocation = "Shelf A",
            colorHex = "FFAA00",
            colorName = "Orange",
            minTemp = "240",
            maxTemp = "260",
            bedMinTemp = "90",
            bedMaxTemp = "100",
            lotNr = "LOT-1",
            comment = "Test",
            remainingWeight = "750",
            spoolMode = SpoolMode.CREATE,
            selectedSpool = null
        )

        assertEquals("ASA", request.material)
        assertEquals("CustomBrand", request.brand)
        assertEquals("Shelf A", request.location)
        assertEquals("750", request.remainingWeight)
        assertNull(request.existingSpoolId)
    }
}
