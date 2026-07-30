package com.spoolstudio.app.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class SettingsStateHelpersTest {
    @Test
    fun oldPrinterModeSettingsResolveToSpoolLink() {
        assertEquals(PrinterIntegrationMode.PAXX12_SPOOL_LINK, PrinterIntegrationMode.fromStoredValue("auto_detect"))
        assertEquals(PrinterIntegrationMode.PAXX12_SPOOL_LINK, PrinterIntegrationMode.fromStoredValue("legacy_paxx12"))
        assertEquals(PrinterIntegrationMode.PAXX12_SPOOL_LINK, PrinterIntegrationMode.fromStoredValue(null))
    }

    @Test
    fun loadStateCopiesStoredSettingsForViewModel() {
        val state = buildSettingsLoadState(
            AppSettings(
                spoolmanUrl = "http://spoolman.local",
                moonrakerUrl = "http://printer.local",
                printerIntegrationMode = PrinterIntegrationMode.PAXX12_SPOOL_LINK,
                spoolmanSortBy = "last_used:desc",
                bambuMasterKey = "ABCD1234",
                showLotNumber = true,
                showCommentField = true,
                showEmptySpoolWeight = true,
                materialModifierFieldDeclined = true
            )
        )

        assertEquals(true, state.showLotNumber)
        assertEquals(true, state.showCommentField)
        assertEquals(true, state.showEmptySpoolWeight)
        assertEquals("http://spoolman.local", state.spoolmanUrl)
        assertEquals("last_used:desc", state.spoolmanSortBy)
        assertEquals("http://printer.local", state.moonrakerUrl)
        assertEquals(PrinterIntegrationMode.PAXX12_SPOOL_LINK, state.printerIntegrationMode)
        assertEquals("ABCD1234", state.bambuMasterKey)
        assertEquals(true, state.materialModifierFieldDeclined)
    }

    @Test
    fun saveStateNormalizesUrlsSortAndBambuKey() {
        val state = buildSettingsSaveState(
            SettingsSaveInput(
                spoolmanUrl = " http://spoolman.local/ ",
                moonrakerUrl = " http://printer.local/ ",
                printerIntegrationMode = PrinterIntegrationMode.PAXX12_SPOOL_LINK,
                spoolmanSortBy = "name:asc",
                bambuMasterKey = " abcd1234 ",
                showCommentField = true
            )
        )

        assertEquals("http://spoolman.local", state.spoolmanUrl)
        assertEquals("http://printer.local", state.moonrakerUrl)
        assertEquals(PrinterIntegrationMode.PAXX12_SPOOL_LINK, state.printerIntegrationMode)
        assertEquals("name:asc", state.spoolmanSortBy)
        assertEquals("ABCD1234", state.bambuMasterKey)
        assertEquals(true, state.showCommentField)
    }

    @Test
    fun saveStateKeepsBlankSortAsEmptyString() {
        val state = buildSettingsSaveState(
            SettingsSaveInput(
                spoolmanUrl = "",
                moonrakerUrl = "",
                printerIntegrationMode = PrinterIntegrationMode.PAXX12_SPOOL_LINK,
                spoolmanSortBy = " ",
                bambuMasterKey = "",
                showCommentField = false
            )
        )

        assertEquals("", state.spoolmanSortBy)
        assertFalse(state.showCommentField)
    }
}
