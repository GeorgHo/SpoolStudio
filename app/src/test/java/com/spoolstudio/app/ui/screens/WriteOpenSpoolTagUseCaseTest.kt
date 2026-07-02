package com.spoolstudio.app.ui.screens

import com.spoolstudio.app.domain.models.FilamentSpool
import com.spoolstudio.app.domain.models.Material
import com.spoolstudio.app.ui.SpoolMode
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WriteOpenSpoolTagUseCaseTest {
    private val useCase = WriteOpenSpoolTagUseCase(
        serialize = { data ->
            listOfNotNull(
                data.type?.let { "type=$it" },
                data.spoolId?.let { "spool_id=$it" },
                data.lotNr?.let { "lot_nr=$it" }
            ).joinToString(";")
        }
    )

    @Test
    fun buildPayloadIncludesSelectedSpoolIdForUpdateMode() {
        val form = formState().apply {
            filamentType = "PLA"
            variant = "Basic"
            brand = "Generic"
            colorHex = "FFFFFF"
            minTemp = "190"
            maxTemp = "220"
            bedMinTemp = "40"
            bedMaxTemp = "65"
            lotNr = "LOT-42"
        }
        val selectedSpool = FilamentSpool(
            id = 42,
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

        val payload = useCase.buildPayload(form, SpoolMode.UPDATE, selectedSpool)

        assertNotNull(payload)
        assertTrue(payload!!.contains("type=PLA"))
        assertTrue(payload.contains("spool_id=42"))
        assertTrue(payload.contains("lot_nr=LOT-42"))
    }

    @Test
    fun buildPayloadReturnsNullForUnsupportedOpenSpoolMaterial() {
        val form = formState().apply {
            filamentType = "Other"
            customMaterial = "Mystery"
            variant = "Basic"
            brand = "Generic"
            colorHex = "FFFFFF"
            minTemp = "190"
            maxTemp = "220"
        }

        assertNull(useCase.buildPayload(form, SpoolMode.CREATE, selectedSpool = null))
    }

    private fun formState(): SpoolFormState =
        SpoolFormState(Material("PLA", 190, 220, 40, 65))
}
