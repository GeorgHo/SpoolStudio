package com.spoolstudio.app.ui

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PrinterMappingUseCaseTest {
    @Test
    fun loadReturnsMessageWhenActiveSpoolIsAvailable() = runBlocking {
        val snapshot = PrinterMappingSnapshot(1, 2, null, null, 1)
        val useCase = PrinterMappingUseCase(
            loadMapping = { PrinterMappingLoadResult(snapshot, activeSpoolAvailable = true) },
            saveMapping = { _, _, _, _, _, _ -> throw AssertionError("save must not be called") }
        )

        val result = useCase.load("http://printer.local")

        assertTrue(result is PrinterMappingOperationResult.Loaded)
        assertEquals("Printer mapping loaded", (result as PrinterMappingOperationResult.Loaded).message)
        assertEquals(snapshot, result.snapshot)
    }

    @Test
    fun loadReturnsMessageWhenActiveSpoolIsMissing() = runBlocking {
        val snapshot = PrinterMappingSnapshot(1, null, null, null, null)
        val useCase = PrinterMappingUseCase(
            loadMapping = { PrinterMappingLoadResult(snapshot, activeSpoolAvailable = false) },
            saveMapping = { _, _, _, _, _, _ -> throw AssertionError("save must not be called") }
        )

        val result = useCase.load("http://printer.local")

        assertTrue(result is PrinterMappingOperationResult.Loaded)
        assertEquals(
            "Printer mapping loaded (active spool not available)",
            (result as PrinterMappingOperationResult.Loaded).message
        )
    }

    @Test
    fun saveReturnsFriendlyFailureMessage() = runBlocking {
        val useCase = PrinterMappingUseCase(
            loadMapping = { throw AssertionError("load must not be called") },
            saveMapping = { _, _, _, _, _, _ -> throw IllegalStateException("timeout after 10 seconds") }
        )

        val result = useCase.save(
            baseUrl = "http://printer.local",
            toolhead1SpoolId = 1,
            toolhead2SpoolId = null,
            toolhead3SpoolId = null,
            toolhead4SpoolId = null,
            activeSpoolId = 1
        )

        assertTrue(result is PrinterMappingOperationResult.Failed)
        assertEquals(
            "Save failed: Printer script did not finish in time. Check the printer macro or active spool script.",
            (result as PrinterMappingOperationResult.Failed).message
        )
    }
}
