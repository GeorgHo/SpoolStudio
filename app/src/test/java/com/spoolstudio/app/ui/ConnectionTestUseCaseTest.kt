package com.spoolstudio.app.ui

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ConnectionTestUseCaseTest {
    @Test
    fun validationRejectsMissingScheme() {
        val useCase = ConnectionTestUseCase()

        assertEquals(
            "URL muss mit http:// oder https:// beginnen",
            useCase.validationError("printer.local")
        )
    }

    @Test
    fun moonrakerSuccessReturnsReachableStatus() = runBlocking {
        val useCase = ConnectionTestUseCase(
            testMoonrakerConnection = {
                MoonrakerConnectionResult(
                    reachable = true,
                    status = "Moonraker erreichbar"
                )
            },
            testSpoolmanConnection = { _, _ -> throw AssertionError("Spoolman must not be called") }
        )

        val result = useCase.testMoonraker("http://printer.local/")

        assertTrue(result is ConnectionTestResult.Moonraker)
        assertEquals(true, (result as ConnectionTestResult.Moonraker).reachable)
        assertEquals("Moonraker erreichbar", result.status)
    }

    @Test
    fun spoolmanFailureReturnsFriendlyError() = runBlocking {
        val useCase = ConnectionTestUseCase(
            testMoonrakerConnection = { throw AssertionError("Moonraker must not be called") },
            testSpoolmanConnection = { _, _ -> throw IllegalStateException("Unable to resolve host") }
        )

        val result = useCase.testSpoolman("http://spoolman.local", sortBy = "")

        assertTrue(result is ConnectionTestResult.Failed)
        assertEquals("Host nicht erreichbar", (result as ConnectionTestResult.Failed).error)
    }
}
