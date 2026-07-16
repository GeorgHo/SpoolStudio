package com.spoolstudio.app.ui.screens

import com.spoolstudio.app.utils.formatColorName
import com.spoolstudio.app.utils.suggestHexFromName
import org.junit.Assert.assertEquals
import org.junit.Test

class ColorNameRecognitionTest {
    @Test
    fun silverAndGermanSilverNamesResolveToHex() {
        assertEquals("Silver", formatColorName("Silver"))
        assertEquals("Silver", formatColorName("Silber"))
        assertEquals("C0C0C0", suggestHexFromName("Silver"))
        assertEquals("C0C0C0", suggestHexFromName("Silber"))
    }

    @Test
    fun commonFilamentSilverNamesResolveToHex() {
        assertEquals("C0C0C0", suggestHexFromName("Silk Silver"))
        assertEquals("C0C0C0", suggestHexFromName("Metallsilber"))
    }
}
