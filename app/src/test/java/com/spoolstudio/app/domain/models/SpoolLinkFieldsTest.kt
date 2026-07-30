package com.spoolstudio.app.domain.models

import org.junit.Assert.assertEquals
import org.junit.Test

class SpoolLinkFieldsTest {
    @Test
    fun normalizesPlusMaterialIntoBaseMaterialAndVariant() {
        val fields = normalizeSpoolLinkFilamentFields("PLA+", "Basic")

        assertEquals("PLA", fields.material)
        assertEquals("Plus", fields.variant)
    }

    @Test
    fun keepsExistingVariantWhenMaterialAddsModifier() {
        val fields = normalizeSpoolLinkFilamentFields("PLA+", "Silk")

        assertEquals("PLA", fields.material)
        assertEquals("Plus Silk", fields.variant)
    }

    @Test
    fun doesNotSplitHyphenatedBaseMaterialWithoutSeparatorSpaces() {
        val fields = normalizeSpoolLinkFilamentFields("PET-CF", "Basic")

        assertEquals("PET-CF", fields.material)
        assertEquals("Basic", fields.variant)
    }

    @Test
    fun splitsLegacyCombinedMaterialOnlyOnSpacedSeparator() {
        val fields = normalizeSpoolLinkFilamentFields("PLA - Silk", null)

        assertEquals("PLA", fields.material)
        assertEquals("Silk", fields.variant)
    }

    @Test
    fun directFieldsKeepMaterialAndVariantUnchangedForWrites() {
        val fields = spoolLinkFilamentFields("PLA+", "Silk")

        assertEquals("PLA+", fields.material)
        assertEquals("Silk", fields.variant)
    }

    @Test
    fun directFieldsDefaultBlankValuesForWrites() {
        val fields = spoolLinkFilamentFields("", "")

        assertEquals("PLA", fields.material)
        assertEquals("Basic", fields.variant)
    }

    @Test
    fun spoolmanFieldsStoreMaterialModifierSeparately() {
        val fields = spoolLinkSpoolmanFields("PLA+", "Matte")

        assertEquals("PLA", fields.material)
        assertEquals("Matte", fields.variant)
        assertEquals("Plus", fields.materialModifier)
    }

    @Test
    fun tagFieldsOmitMaterialModifierForPrinterCompatibility() {
        val fields = spoolLinkTagFields("PLA", "Matte", "Plus")

        assertEquals("PLA", fields.material)
        assertEquals("Matte", fields.variant)
    }

    @Test
    fun displayMaterialUsesPlusSymbolForModifier() {
        assertEquals("PLA+", displayMaterialWithModifier("PLA", "Plus"))
        assertEquals("PLA HS", displayMaterialWithModifier("PLA", "HS"))
    }

    @Test
    fun parsesCardUidsAsDistinctUppercaseHexValues() {
        val uids = parseCardUids("aa:bb:cc:dd,11223344,AABBCCDD")

        assertEquals(listOf("AABBCCDD", "11223344"), uids)
    }

    @Test
    fun encodesSpoolmanCustomFieldStringsAsNestedJsonString() {
        assertEquals("\"Silk\"", encodeSpoolmanExtraString("Silk"))
    }
}
