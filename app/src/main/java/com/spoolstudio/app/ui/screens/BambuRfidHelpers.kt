package com.spoolstudio.app.ui.screens

import com.spoolstudio.app.data.local.VariantDatabase
import com.spoolstudio.app.domain.models.FilamentSpool

data class BambuRfidFormData(
    val material: String?,
    val detailedType: String?,
    val colorHex: String?,
    val spoolWeightGrams: Int?,
    val minHotend: Int?,
    val maxHotend: Int?,
    val bedTemp: Int?,
    val uid: String?,
    val normalizedVariant: String
)

sealed class BambuRfidApplyDecision {
    data class ApplyNewData(val data: BambuRfidFormData) : BambuRfidApplyDecision()
    data class UseExistingSpool(val spool: FilamentSpool) : BambuRfidApplyDecision()
    data class ShowDifference(
        val data: BambuRfidFormData,
        val diffText: String
    ) : BambuRfidApplyDecision()
}

fun parseBambuRfidFormData(text: String, fallbackMaterial: String): BambuRfidFormData {
    val material = parsedBambuValue(text, "Filament Type")
    val detailedType = parsedBambuValue(text, "Detailed Type")
    val colorHex = parsedBambuValue(text, "Filament Color")
        ?.substringBefore(" / ")
        ?.removePrefix("#")
        ?.uppercase()

    return BambuRfidFormData(
        material = material,
        detailedType = detailedType,
        colorHex = colorHex,
        spoolWeightGrams = parsedBambuInt(text, "Spool Weight"),
        minHotend = parsedBambuInt(text, "Min Hotend"),
        maxHotend = parsedBambuInt(text, "Max Hotend"),
        bedTemp = parsedBambuInt(text, "Bed Temp"),
        uid = parsedBambuValue(text, "UID")?.trim(),
        normalizedVariant = normalizeBambuVariant(
            material = material ?: fallbackMaterial,
            detailedType = detailedType
        )
    )
}

fun resolveBambuRfidApplyDecision(
    text: String,
    fallbackMaterial: String,
    spools: List<FilamentSpool>
): BambuRfidApplyDecision {
    val bambuData = parseBambuRfidFormData(
        text = text,
        fallbackMaterial = fallbackMaterial
    )
    val matchingSpool = findMatchingSpoolByLotNr(spools, bambuData.uid)

    return when {
        matchingSpool == null -> {
            BambuRfidApplyDecision.ApplyNewData(bambuData)
        }

        isSameBambuData(
            spool = matchingSpool,
            material = bambuData.material,
            normalizedVariant = bambuData.normalizedVariant,
            colorHexValue = bambuData.colorHex
        ) -> {
            BambuRfidApplyDecision.UseExistingSpool(matchingSpool)
        }

        else -> {
            BambuRfidApplyDecision.ShowDifference(
                data = bambuData,
                diffText = buildBambuDiffText(
                    spool = matchingSpool,
                    material = bambuData.material,
                    normalizedVariant = bambuData.normalizedVariant,
                    colorHexValue = bambuData.colorHex
                )
            )
        }
    }
}

fun isBambuRfidDump(text: String): Boolean =
    text.contains("Bambu RFID Parsed") ||
        text.contains("=== Sector") ||
        text.contains("=== Sektor") ||
        text.contains("Block 0 (abs")

fun parsedBambuValue(text: String, label: String): String? {
    return text
        .lineSequence()
        .map { it.trim().removePrefix("-").trimStart() }
        .firstOrNull { it.startsWith("$label: ") }
        ?.substringAfter(": ")
        ?.trim()
        ?.takeIf { it.isNotBlank() }
}

fun parsedBambuInt(text: String, label: String): Int? {
    return parsedBambuValue(text, label)
        ?.replace(" C", "")
        ?.replace(" g", "")
        ?.replace(" m", "")
        ?.replace(" mm", "")
        ?.trim()
        ?.toIntOrNull()
}

fun normalizeHexForCompare(value: String?): String {
    return value
        ?.trim()
        ?.removePrefix("#")
        ?.uppercase()
        .orEmpty()
}

fun findMatchingSpoolByLotNr(spools: List<FilamentSpool>, lotNrValue: String?): FilamentSpool? {
    val normalizedLotNr = lotNrValue?.trim().orEmpty()
    if (normalizedLotNr.isBlank()) return null

    return spools.firstOrNull { spool ->
        spool.lotNr?.trim().equals(normalizedLotNr, ignoreCase = true)
    }
}

fun isSameBambuData(
    spool: FilamentSpool,
    material: String?,
    normalizedVariant: String,
    colorHexValue: String?
): Boolean {
    return spool.material.equals(material.orEmpty(), ignoreCase = true) &&
        spool.variant.equals(normalizedVariant, ignoreCase = true) &&
        normalizeHexForCompare(spool.colorHex) == normalizeHexForCompare(colorHexValue)
}

fun buildBambuDiffText(
    spool: FilamentSpool,
    material: String?,
    normalizedVariant: String,
    colorHexValue: String?
): String {
    val lines = mutableListOf<String>()
    lines += "Spool with matching lot number found."
    lines += ""
    lines += "ID: ${spool.id ?: "-"}"
    lines += "Lot Number: ${spool.lotNr ?: "-"}"
    lines += ""

    if (!spool.material.equals(material.orEmpty(), ignoreCase = true)) {
        lines += "Material:"
        lines += "- Database: ${spool.material}"
        lines += "- Bambu: ${material ?: "-"}"
        lines += ""
    }

    if (!spool.variant.equals(normalizedVariant, ignoreCase = true)) {
        lines += "Variant:"
        lines += "- Database: ${spool.variant.ifBlank { "Basic" }}"
        lines += "- Bambu: ${normalizedVariant.ifBlank { "Basic" }}"
        lines += ""
    }

    if (normalizeHexForCompare(spool.colorHex) != normalizeHexForCompare(colorHexValue)) {
        lines += "Color:"
        lines += "- Database: ${spool.colorHex?.let { "#$it" } ?: "-"}"
        lines += "- Bambu: ${colorHexValue?.let { "#$it" } ?: "-"}"
        lines += ""
    }

    return lines.joinToString("\n").trim()
}

fun normalizeBambuVariant(material: String, detailedType: String?): String {
    val raw = detailedType.orEmpty().trim()
    if (raw.isBlank()) return "Basic"

    val cleaned = raw
        .removePrefix(material)
        .removePrefix("$material ")
        .removePrefix("$material-")
        .removePrefix("$material -")
        .trim()

    val knownVariants = VariantDatabase.variants.filter { it.isNotBlank() }

    val match = knownVariants.firstOrNull { variant ->
        cleaned.equals(variant, ignoreCase = true) ||
            cleaned.contains(variant, ignoreCase = true)
    }

    return match ?: cleaned.ifBlank { "Basic" }
}
