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
        assertTrue(isBambuRfidDump("=== Sector 1 ==="))
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
    fun parsedBambuDumpMapsDisplayedFieldsIntoFormData() {
        val text = """
            Bambu RFID Parsed

            UID: E653F0E1
            Filament Type: PLA
            Detailed Type: PLA Basic
            Material ID: GFA00
            Variant ID: A00-P0
            Filament Color: #F7E6DE
            Color Count: 1
            Spool Weight: 1000 g
            Filament Length: 330 m
            Filament Diameter: 1,75 mm
            Spool Width: 66,25 mm
            Min Nozzle Diameter: 0,2 mm

            Temperatures:
            - Drying Temp: 55 C
            - Drying Time: 8 h
            - Bed Temp: 0 C
        """.trimIndent()

        val data = parseBambuRfidFormData(text, fallbackMaterial = "PLA")

        assertEquals("E653F0E1", data.uid)
        assertEquals("PLA", data.material)
        assertEquals("Basic", data.normalizedVariant)
        assertEquals("F7E6DE", data.colorHex)
        assertEquals(1000, data.spoolWeightGrams)
        assertEquals(0, data.bedTemp)
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
