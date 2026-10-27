package com.spoolstudio.app.data.remote.moonraker

import org.junit.Assert.*
import org.junit.Test

class SpoolLinkCommandsTest {
    @Test fun clearingMappingCannotFallBackToFirmware24UnlinkMacro() {
        for (tool in 0..3) {
            val scripts = spoolLinkAssignmentScripts(tool, 0)
            assertEquals(listOf("SET_PRINT_FILAMENT_CONFIG CONFIG_EXTRUDER=$tool FILAMENT_SPOOL_ID=0 FORCE=1"), scripts)
            assertFalse(scripts.any { it.startsWith("SET_SPOOL_ID") })
        }
    }
    @Test fun assigningSpoolRetainsCompatibilityFallback() {
        assertEquals(listOf(
            "SET_PRINT_FILAMENT_CONFIG CONFIG_EXTRUDER=2 FILAMENT_SPOOL_ID=123 FORCE=1",
            "SET_SPOOL_ID LANE=E2 SPOOL_ID=123"
        ), spoolLinkAssignmentScripts(2, 123))
    }
}
