package com.spoolstudio.app.ui.screens

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsScreenHelpersTest {
    @Test
    fun normalizeSettingsUrlTrimsAddsSchemeAndRemovesTrailingSlash() {
        assertEquals("http://spoolman.local", normalizeSettingsUrl(" http://spoolman.local/ "))
    }

    @Test
    fun normalizeSettingsUrlAcceptsBareSpoolmanAddress() {
        assertEquals("http://10.201.0.1:8000", normalizeSettingsUrl("10.201.0.1:8000/"))
    }

    @Test
    fun normalizeMoonrakerSettingsUrlAddsSchemeAndConvertsSlashPort() {
        assertEquals("http://printer.local:7125", normalizeMoonrakerSettingsUrl(" printer.local/7125/ "))
    }

    @Test
    fun hasSettingsChangesIgnoresUrlSlashAndBambuKeyCase() {
        val hasChanges = hasSettingsChanges(
            tempSpoolmanUrl = "http://spoolman.local/",
            savedSpoolmanUrl = "http://spoolman.local",
            tempMoonrakerUrl = "http://printer.local/",
            savedMoonrakerUrl = "http://printer.local",
            tempSort = "",
            savedSort = "",
            tempBambuKey = "abcd",
            savedBambuKey = "ABCD",
            tempShowCommentField = false,
            savedShowCommentField = false
        )

        assertFalse(hasChanges)
    }

    @Test
    fun hasSettingsChangesDetectsCommentFieldChange() {
        val hasChanges = hasSettingsChanges(
            tempSpoolmanUrl = "",
            savedSpoolmanUrl = "",
            tempMoonrakerUrl = "",
            savedMoonrakerUrl = "",
            tempSort = "",
            savedSort = "",
            tempBambuKey = "",
            savedBambuKey = "",
            tempShowCommentField = true,
            savedShowCommentField = false
        )

        assertTrue(hasChanges)
    }

    @Test
    fun runUrlRetestIfNeededSkipsBlankTestingAndManualTrigger() {
        var testCalls = 0

        runUrlRetestIfNeeded("", "", isTesting = false, triggeredManually = false, onTest = { testCalls++ }, onLastTestedChange = {})
        runUrlRetestIfNeeded("http://spoolman.local", "", isTesting = true, triggeredManually = false, onTest = { testCalls++ }, onLastTestedChange = {})
        runUrlRetestIfNeeded("http://spoolman.local", "", isTesting = false, triggeredManually = true, onTest = { testCalls++ }, onLastTestedChange = {})

        assertEquals(0, testCalls)
    }

    @Test
    fun runUrlRetestIfNeededRunsForChangedUrl() {
        var testedValue = ""
        var lastTestedValue = ""

        runUrlRetestIfNeeded(
            currentValue = "http://spoolman.local/",
            lastTestedValue = "http://old.local",
            isTesting = false,
            triggeredManually = false,
            onTest = { testedValue = it },
            onLastTestedChange = { lastTestedValue = it }
        )

        assertEquals("http://spoolman.local", testedValue)
        assertEquals("http://spoolman.local", lastTestedValue)
    }
}
