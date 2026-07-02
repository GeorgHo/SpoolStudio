package com.spoolstudio.app.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class SettingsStateHelpersTest {
    @Test
    fun saveStateNormalizesUrlsSortAndBambuKey() {
        val state = buildSettingsSaveState(
            SettingsSaveInput(
                spoolmanUrl = " http://spoolman.local/ ",
                moonrakerUrl = " http://printer.local/ ",
                spoolmanSortBy = "name:asc",
                bambuMasterKey = " abcd1234 ",
                showCommentField = true
            )
        )

        assertEquals("http://spoolman.local", state.spoolmanUrl)
        assertEquals("http://printer.local", state.moonrakerUrl)
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
                spoolmanSortBy = " ",
                bambuMasterKey = "",
                showCommentField = false
            )
        )

        assertEquals("", state.spoolmanSortBy)
        assertFalse(state.showCommentField)
    }
}
