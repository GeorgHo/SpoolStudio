package com.spoolstudio.app.data.remote.moonraker

import com.spoolstudio.app.ui.friendlyPrinterMappingError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MoonrakerServiceTest {
    @Test
    fun versionGateKeepsPaxx12Firmware141InLegacyMode() {
        assertFalse(moonrakerVersionAtLeast("1.4.1.6_20260608141446", 1, 5, 0))
    }

    @Test
    fun versionGateAllowsPaxx12Firmware15AndNewer() {
        assertTrue(moonrakerVersionAtLeast("1.5.0", 1, 5, 0))
        assertTrue(moonrakerVersionAtLeast("1.5.1_20260730", 1, 5, 0))
    }

    @Test
    fun componentsDetectEnabledSpoolmanIntegration() {
        assertTrue(
            moonrakerComponentsHaveSpoolmanIntegration(
                listOf("database", "spoolman", "spoollink")
            ) == true
        )
    }

    @Test
    fun componentsDetectDisabledSpoolmanIntegration() {
        assertFalse(
            moonrakerComponentsHaveSpoolmanIntegration(
                listOf("database", "mqtt", "extensions")
            ) == true
        )
    }

    @Test
    fun missingSetSpoolIdReturnsPaxx12PrerequisiteMessage() {
        assertEquals(
            "Paxx12 SET_SPOOL_ID is not available on the printer. Enable the AFC/SpoolLink firmware configuration, restart Klipper/Moonraker, then try again.",
            friendlyPrinterMappingError(
                IllegalStateException(
                    "SET_SPOOL_ID is not available on the printer. Enable the Paxx12 AFC/SpoolLink firmware configuration and restart Klipper/Moonraker."
                )
            )
        )
    }
}
