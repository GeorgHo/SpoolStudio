package com.spoolstudio.app.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PrinterMappingErrorMessagesTest {
    @Test
    fun saveErrorHidesActiveSpoolDumpBehindFriendlyMessage() {
        val error = IllegalStateException(
            """
            Moonraker active spool write failed (500): {
              "error": {
                "message": "Traceback (most recent call last):\n  File script.py\nRuntimeError: macro failed"
              }
            }
            """.trimIndent()
        )

        val message = printerMappingSaveErrorMessage(error)

        assertEquals(
            "Save failed: Active spool could not be written. Check the printer Spoolman macro or script.",
            message
        )
        assertFalse(message.contains("Traceback"))
    }

    @Test
    fun loadErrorUsesFriendlyConnectionMessageForUnreachableHost() {
        val error = IllegalStateException("Unable to resolve host printer.local")

        assertEquals(
            "Loading printer mapping failed: Printer host is not reachable. Check the Moonraker URL.",
            printerMappingLoadErrorMessage(error)
        )
    }

    @Test
    fun saveTimeoutPointsToPrinterScriptInsteadOfConnection() {
        val error = IllegalStateException("timeout after 10 seconds")

        assertEquals(
            "Save failed: Printer script did not finish in time. Check the printer macro or active spool script.",
            printerMappingSaveErrorMessage(error)
        )
    }

    @Test
    fun unknownPrinterErrorIsCompacted() {
        val error = IllegalStateException(
            """Moonraker failed (500): {"error":{"message":"Macro returned invalid spool id"}}"""
        )

        val message = printerMappingSaveErrorMessage(error)

        assertTrue(message.contains("HTTP 500"))
        assertTrue(message.contains("Macro returned invalid spool id"))
        assertFalse(message.contains("{\"error\""))
    }
}
