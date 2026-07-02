package com.spoolstudio.app.ui.screens

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PrinterMappingSelectionTest {
    @Test
    fun changingToolheadClearsActiveSpoolWhenItIsNoLongerMapped() {
        val selection = PrinterMappingSelection(
            toolhead1SpoolId = 1,
            activeSpoolId = 1
        )

        val updated = selection.withToolhead1(null)

        assertNull(updated.activeSpoolId)
    }

    @Test
    fun changingToolheadKeepsActiveSpoolWhenItRemainsMappedElsewhere() {
        val selection = PrinterMappingSelection(
            toolhead1SpoolId = 1,
            toolhead2SpoolId = 2,
            activeSpoolId = 2
        )

        val updated = selection.withToolhead1(null)

        assertEquals(2, updated.activeSpoolId)
    }
}
