package com.spoolstudio.app.ui.screens

import com.spoolstudio.app.domain.models.Material
import com.spoolstudio.app.ui.SpoolMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SpoolStudioDerivedStateTest {
    private fun formState(): SpoolFormState =
        SpoolFormState(Material("PLA", 190, 220, 40, 65))

    @Test
    fun actionLabelFollowsSpoolMode() {
        assertEquals("Write to Spoolman", spoolActionLabel(SpoolMode.CREATE))
        assertEquals("Update in Spoolman", spoolActionLabel(SpoolMode.UPDATE))
        assertEquals("Duplicate in Spoolman", spoolActionLabel(SpoolMode.DUPLICATE))
    }

    @Test
    fun printerMappingChangeDetectionComparesDialogAndPrinterState() {
        assertFalse(
            hasPrinterMappingChanges(
                toolhead1SpoolId = 1,
                toolhead2SpoolId = null,
                toolhead3SpoolId = 3,
                toolhead4SpoolId = null,
                activeDialogSpoolId = 1,
                printerTool1SpoolId = 1,
                printerTool2SpoolId = null,
                printerTool3SpoolId = 3,
                printerTool4SpoolId = null,
                activePrinterSpoolId = 1
            )
        )

        assertTrue(
            hasPrinterMappingChanges(
                toolhead1SpoolId = 2,
                toolhead2SpoolId = null,
                toolhead3SpoolId = 3,
                toolhead4SpoolId = null,
                activeDialogSpoolId = 1,
                printerTool1SpoolId = 1,
                printerTool2SpoolId = null,
                printerTool3SpoolId = 3,
                printerTool4SpoolId = null,
                activePrinterSpoolId = 1
            )
        )
    }

    @Test
    fun writeActionRequiresValidFormAndColor() {
        val form = formState()

        assertFalse(isWriteActionEnabled(form))

        form.colorHex = "FFFFFF"
        assertTrue(isWriteActionEnabled(form))

        form.remainingWeight = "-1"
        assertFalse(isWriteActionEnabled(form))
    }

    @Test
    fun newFromSelectedIsOnlyAvailableOutsideCreateModeWithSourceData() {
        assertFalse(isNewFromSelectedEnabled(SpoolMode.CREATE, selectedSpool = null, readData = null))
        assertFalse(isNewFromSelectedEnabled(SpoolMode.UPDATE, selectedSpool = null, readData = null))
    }
}
