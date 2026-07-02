package com.spoolstudio.app.ui

import com.spoolstudio.app.domain.models.FilamentSpool
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SaveOrUpdateSpoolmanSpoolUseCaseTest {
    @Test
    fun validationFailureReturnsMessageWithoutCallingRepository() = runBlocking {
        var saveWasCalled = false
        val useCase = SaveOrUpdateSpoolmanSpoolUseCase(
            saveSpool = {
                saveWasCalled = true
                throw AssertionError("Repository must not be called")
            }
        )

        val result = useCase.execute(
            input = SpoolmanSaveInput(
                baseUrl = "",
                request = saveRequest(),
                mode = SpoolMode.CREATE,
                selectedSpool = null,
                readData = null,
                currentSpoolId = null
            )
        )

        assertTrue(result is SaveOrUpdateSpoolmanSpoolResult.ValidationFailed)
        assertEquals(
            "Please configure a valid Spoolman URL first",
            (result as SaveOrUpdateSpoolmanSpoolResult.ValidationFailed).message
        )
        assertFalse(saveWasCalled)
    }

    @Test
    fun successfulCreateReturnsCreateSuccessMessage() = runBlocking {
        val finalSpool = FilamentSpool(
            id = 12,
            material = "PLA",
            variant = "Basic",
            brand = "Generic",
            colorHex = "FFFFFF",
            minTemp = 190,
            maxTemp = 220,
            bedMinTemp = 40,
            bedMaxTemp = 65,
            spoolmanName = "PLA White"
        )
        val useCase = SaveOrUpdateSpoolmanSpoolUseCase(
            saveSpool = {
                SpoolmanSaveResult(
                    finalSpool = finalSpool,
                    tagData = null,
                    resolvedLotNr = "",
                    actionMode = SpoolMode.CREATE
                )
            }
        )

        val result = useCase.execute(
            input = SpoolmanSaveInput(
                baseUrl = "http://spoolman.local",
                request = saveRequest(),
                mode = SpoolMode.CREATE,
                selectedSpool = null,
                readData = null,
                currentSpoolId = null
            )
        )

        assertTrue(result is SaveOrUpdateSpoolmanSpoolResult.Saved)
        assertEquals(
            "Spool saved to Spoolman",
            (result as SaveOrUpdateSpoolmanSpoolResult.Saved).successMessage
        )
    }

    private fun saveRequest(): SpoolmanSaveRequest =
        SpoolmanSaveRequest(
            material = "PLA",
            variant = "Basic",
            brand = "Generic",
            location = "",
            colorHex = "FFFFFF",
            colorName = "White",
            minTemp = "190",
            maxTemp = "220",
            bedMinTemp = "40",
            bedMaxTemp = "65",
            lotNr = "",
            comment = "Created by Spool Studio",
            remainingWeight = "1000",
            existingSpoolId = null
        )
}
