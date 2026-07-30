package com.spoolstudio.app.ui.screens

import com.spoolstudio.app.data.local.VariantDatabase
import com.spoolstudio.app.domain.models.FilamentSpool
import com.spoolstudio.app.domain.models.normalizeCardUid
import com.spoolstudio.app.domain.models.normalizeSpoolLinkFilamentFields

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

    val normalizedVariant = normalizeBambuVariant(
        material = material ?: fallbackMaterial,
        detailedType = detailedType
    )
    val fields = normalizeSpoolLinkFilamentFields(material ?: fallbackMaterial, normalizedVariant)

    return BambuRfidFormData(
        material = fields.material,
        detailedType = detailedType,
        colorHex = colorHex,
        spoolWeightGrams = parsedBambuInt(text, "Spool Weight"),
        minHotend = parsedBambuInt(text, "Min Hotend"),
        maxHotend = parsedBambuInt(text, "Max Hotend"),
        bedTemp = parsedBambuInt(text, "Bed Temp"),
        uid = normalizeCardUid(parsedBambuValue(text, "UID")).takeIf { it.isNotBlank() },
        normalizedVariant = fields.variant
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
    val matchingSpool = findMatchingSpoolByCardUid(spools, bambuData.uid)

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

fun findMatchingSpoolByCardUid(spools: List<FilamentSpool>, cardUidValue: String?): FilamentSpool? {
    val normalizedUid = normalizeCardUid(cardUidValue)
    if (normalizedUid.isBlank()) return null

    return spools.firstOrNull { spool ->
        spool.cardUids.any { it.equals(normalizedUid, ignoreCase = true) }
    }
}

fun isSameBambuData(
    spool: FilamentSpool,
    material: String?,
    normalizedVariant: String,
    colorHexValue: String?
): Boolean {
    val fields = normalizeSpoolLinkFilamentFields(material, normalizedVariant)
    return spool.material.equals(fields.material, ignoreCase = true) &&
        spool.variant.equals(fields.variant, ignoreCase = true) &&
        normalizeHexForCompare(spool.colorHex) == normalizeHexForCompare(colorHexValue)
}

fun buildBambuDiffText(
    spool: FilamentSpool,
    material: String?,
    normalizedVariant: String,
    colorHexValue: String?
): String {
    val lines = mutableListOf<String>()
    val fields = normalizeSpoolLinkFilamentFields(material, normalizedVariant)
    lines += "Spool with matching card UID found."
    lines += ""
    lines += "ID: ${spool.id ?: "-"}"
    lines += "Card UID: ${spool.cardUids.firstOrNull() ?: "-"}"
    lines += ""

    if (!spool.material.equals(fields.material, ignoreCase = true)) {
        lines += "Material:"
        lines += "- Database: ${spool.material}"
        lines += "- Bambu: ${fields.material}"
        lines += ""
    }

    if (!spool.variant.equals(fields.variant, ignoreCase = true)) {
        lines += "Variant:"
        lines += "- Database: ${spool.variant.ifBlank { "Basic" }}"
        lines += "- Bambu: ${fields.variant.ifBlank { "Basic" }}"
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
