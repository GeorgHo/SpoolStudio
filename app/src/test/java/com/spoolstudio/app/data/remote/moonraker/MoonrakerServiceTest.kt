package com.spoolstudio.app.data.remote.moonraker

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
}
