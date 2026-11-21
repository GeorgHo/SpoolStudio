package com.spoolstudio.app.ui.screens

import com.spoolstudio.app.data.local.MaterialDatabase
import com.spoolstudio.app.domain.models.FilamentSpool
import com.spoolstudio.app.ui.SpoolMode
import com.spoolstudio.app.utils.suggestColorName
import org.junit.Assert.*
import org.junit.Test

class FilamentNamePreservationTest {
    private fun spool(name: String) = FilamentSpool(
        id = 7, material = "PETG", materialModifier = "2.0", variant = "Basic",
        brand = "eSUN", colorHex = "000000", minTemp = 240, maxTemp = 260,
        bedMinTemp = 70, bedMaxTemp = 80, spoolmanName = name, remainingWeight = 500f
    )
    private fun form() = SpoolFormState(MaterialDatabase.materials.first())

    @Test fun weightUpdatePreservesLongProductNameAndExactSpelling() {
        val name = "eSUN  PETG 2.0 / BLACK - Special Edition Product Name Longer Than Forty Characters"
        val source = spool(name)
        val form = form()
        form.applySpoolSource(source, SpoolMode.UPDATE, emptyList())
        form.applyAutomaticFilamentName()
        form.remainingWeight = "420"
        val request = form.buildSaveRequest(SpoolMode.UPDATE, source)
        assertEquals(name, request.colorName)
        assertEquals("420", request.remainingWeight)
        assertEquals("2.0", request.materialModifier)
        assertTrue(hasSpoolmanSaveChanges(request, source))
    }

    @Test fun changingOrClearingColorDoesNotRenameExistingFilament() {
        val form = form()
        form.applySpoolSource(spool("my PETG / 2.0 - SKU-ABC"), SpoolMode.UPDATE, emptyList())
        for (hex in listOf("FFFFFF", null, "FF0000")) {
            form.colorHex = hex
            form.applyAutomaticFilamentName()
            assertEquals("my PETG / 2.0 - SKU-ABC", form.colorName)
        }
    }

    @Test fun explicitRenamePreservesFullInputAndSurvivesColorChange() {
        val form = form()
        form.applySpoolSource(spool("Old Name"), SpoolMode.UPDATE, emptyList())
        val name = "a CUSTOM name / SKU-abcdef - " + "X".repeat(60)
        form.editFilamentName(name)
        form.colorHex = "FFFFFF"
        form.applyAutomaticFilamentName()
        assertEquals(name, form.colorName)
    }

    @Test fun typingColorNameUpdatesHexAndKeepsTheEnteredFilamentName() {
        val form = form()
        for (name in listOf("Cyan", "Cynan")) {
            form.colorHex = "000000"
            form.editFilamentNameWithColorLookup(name, "000000", false)
            form.applyAutomaticFilamentName()
            assertEquals("00FFFF", form.colorHex)
            assertEquals("00FFFF", form.colorHexInput)
            assertEquals("Cyan", suggestColorName(form.colorHex))
            assertEquals(name, form.colorName)
        }
    }

    @Test fun typingAProductNameDoesNotGuessHexFromPartOfTheName() {
        val form = form()
        form.colorHex = "123456"
        form.editFilamentNameWithColorLookup("Black", "123456", true)
        assertEquals("000000", form.colorHex)
        form.editFilamentNameWithColorLookup("Black PETG", "123456", true)
        assertEquals("123456", form.colorHex)
        assertEquals("Black PETG", form.colorName)
        assertTrue(form.isHexManuallySet)
    }

    @Test fun clearingNameDoesNotSilentlyInsertAnotherName() {
        val form = form()
        form.applySpoolSource(spool("Black PETG"), SpoolMode.UPDATE, emptyList())
        form.editFilamentName("")
        form.applyAutomaticFilamentName()
        assertEquals("", form.colorName)
    }

    @Test fun freshSpoolStillGetsColorSuggestionUntilUserNamesIt() {
        val form = form()
        form.colorHex = "000000"
        form.applyAutomaticFilamentName()
        assertTrue(form.colorName.isNotBlank())
        form.editFilamentName("my product")
        form.colorHex = "FFFFFF"
        form.applyAutomaticFilamentName()
        assertEquals("my product", form.colorName)
        form.resetForNewSpool()
        assertFalse(form.colorNameWasManuallyEdited)
    }

    @Test fun importingRfidMetadataDoesNotReplaceAnExistingProductName() {
        val form = form()
        form.applySpoolSource(spool("BAMBU PETG 2.0 - my spool"), SpoolMode.UPDATE, emptyList())
        val data = BambuRfidFormData("PETG", "PETG Basic", "FFFFFF", 1000, 240, 260, 80, "AABB", "Basic")
        form.applyBambuRfidData(data, "White")
        form.applyAutomaticFilamentName()
        assertEquals("BAMBU PETG 2.0 - my spool", form.colorName)
        assertEquals("FFFFFF", form.colorHex)
    }
}
