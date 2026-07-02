package com.spoolstudio.app.ui.screens

import com.spoolstudio.app.domain.models.FilamentSpool
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BambuRfidHelpersTest {
    private val bambuText = """
        Filament Type: PLA
        Detailed Type: PLA Basic
        Filament Color: #FFFFFF / White
        Min Hotend: 190 C
        Max Hotend: 220 C
        Bed Temp: 65 C
        UID: TAG-123
    """.trimIndent()

    @Test
    fun detectsBambuRfidDumpText() {
        assertTrue(isBambuRfidDump("Bambu RFID Parsed\nFilament Type: PLA"))
        assertTrue(isBambuRfidDump("=== Sektor 1 ==="))
        assertTrue(isBambuRfidDump("Block 0 (abs 4): 00 11"))
    }

    @Test
    fun decisionAppliesNewDataWhenLotNumberIsUnknown() {
        val decision = resolveBambuRfidApplyDecision(
            text = bambuText,
            fallbackMaterial = "PLA",
            spools = emptyList()
        )

        assertTrue(decision is BambuRfidApplyDecision.ApplyNewData)
        assertEquals("TAG-123", (decision as BambuRfidApplyDecision.ApplyNewData).data.uid)
    }

    @Test
    fun decisionUsesExistingSpoolWhenBambuDataMatches() {
        val matchingSpool = FilamentSpool(
            id = 42,
            material = "PLA",
            variant = "Basic",
            brand = "Bambu Lab",
            colorHex = "FFFFFF",
            minTemp = 190,
            maxTemp = 220,
            bedMinTemp = 55,
            bedMaxTemp = 75,
            lotNr = "TAG-123",
            spoolmanName = "PLA White"
        )

        val decision = resolveBambuRfidApplyDecision(
            text = bambuText,
            fallbackMaterial = "PLA",
            spools = listOf(matchingSpool)
        )

        assertTrue(decision is BambuRfidApplyDecision.UseExistingSpool)
        assertEquals(42, (decision as BambuRfidApplyDecision.UseExistingSpool).spool.id)
    }

    @Test
    fun decisionShowsDifferenceWhenLotNumberMatchesButContentDiffers() {
        val differentSpool = FilamentSpool(
            id = 7,
            material = "PETG",
            variant = "Basic",
            brand = "Bambu Lab",
            colorHex = "000000",
            minTemp = 230,
            maxTemp = 250,
            bedMinTemp = 70,
            bedMaxTemp = 90,
            lotNr = "TAG-123",
            spoolmanName = "PETG Black"
        )

        val decision = resolveBambuRfidApplyDecision(
            text = bambuText,
            fallbackMaterial = "PLA",
            spools = listOf(differentSpool)
        )

        assertTrue(decision is BambuRfidApplyDecision.ShowDifference)
        assertTrue((decision as BambuRfidApplyDecision.ShowDifference).diffText.contains("Material:"))
    }
}
