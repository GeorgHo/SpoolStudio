package com.spoolstudio.app.ui

import com.spoolstudio.app.domain.models.OpenSpoolData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NfcTagDataHelpersTest {
    @Test
    fun stateUpdateMarksOpenSpoolTagAsLoaded() {
        val readData = OpenSpoolData(
            type = "PLA",
            colorHex = "FFFFFF",
            brand = "Generic",
            minTemp = "190",
            maxTemp = "220",
            spoolId = "42"
        )
        val parsed = NfcTagDataResult(
            readData = readData,
            currentSpoolId = "42",
            spoolMode = SpoolMode.UPDATE
        )

        val update = buildNfcTagReadStateUpdate(
            rawData = "{...}",
            currentRawReadVersion = 3,
            parsedTagData = parsed
        )

        assertEquals("{...}", update.rawReadText)
        assertEquals(4, update.rawReadVersion)
        assertEquals(readData, update.readData)
        assertEquals("42", update.currentSpoolId)
        assertEquals(SpoolMode.UPDATE, update.spoolMode)
        assertTrue(update.clearSelectedSpool)
        assertTrue(update.incrementDataVersion)
    }

    @Test
    fun stateUpdateKeepsRawTextOnlyForUnknownTagData() {
        val update = buildNfcTagReadStateUpdate(
            rawData = "not openspool",
            currentRawReadVersion = 5,
            parsedTagData = null
        )

        assertEquals("not openspool", update.rawReadText)
        assertEquals(6, update.rawReadVersion)
        assertNull(update.readData)
        assertNull(update.currentSpoolId)
        assertNull(update.spoolMode)
        assertFalse(update.clearSelectedSpool)
        assertFalse(update.incrementDataVersion)
    }
}
