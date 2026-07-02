package com.spoolstudio.app.ui.components

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ColorSelectorHelpersTest {
    @Test
    fun presetNameForColorMatchesNormalizedHex() {
        assertEquals("White", presetNameForColor("#ffffff"))
        assertEquals("Black", presetNameForColor("000000"))
    }

    @Test
    fun displayNameUsesNoColorPresetCustomNameThenSuggestion() {
        assertEquals("No Color", displayNameForSelectedColor(null, "Ignored"))
        assertEquals("Red", displayNameForSelectedColor("FF0000", "Custom Red"))
        assertEquals("Custom Blue", displayNameForSelectedColor("123456", "Custom Blue"))
    }

    @Test
    fun sanitizeHexColorInputKeepsOnlySixHexCharacters() {
        assertEquals("A1B2C3", sanitizeHexColorInput(" #a1-b2-c3-ff "))
        assertNull(sanitizeHexColorInput(" ### "))
    }

    @Test
    fun colorNameForSelectedColorUsesPresetOrEmptyName() {
        assertEquals("", colorNameForSelectedColor(null))
        assertEquals("", colorNameForSelectedColor(""))
        assertEquals("Blue", colorNameForSelectedColor("0000FF"))
    }
}
