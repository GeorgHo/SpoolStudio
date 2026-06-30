package com.spoolstudio.app.ui.screens

import com.spoolstudio.app.data.local.VariantDatabase
import com.spoolstudio.app.domain.models.FilamentSpool

fun parsedBambuValue(text: String, label: String): String? {
    return text
        .lineSequence()
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
    lines += "Spule mit gleicher Lot Nummer gefunden."
    lines += ""
    lines += "ID: ${spool.id ?: "-"}"
    lines += "Lot Number: ${spool.lotNr ?: "-"}"
    lines += ""

    if (!spool.material.equals(material.orEmpty(), ignoreCase = true)) {
        lines += "Material:"
        lines += "- Datenbank: ${spool.material}"
        lines += "- Bambu: ${material ?: "-"}"
        lines += ""
    }

    if (!spool.variant.equals(normalizedVariant, ignoreCase = true)) {
        lines += "Variant:"
        lines += "- Datenbank: ${spool.variant.ifBlank { "Basic" }}"
        lines += "- Bambu: ${normalizedVariant.ifBlank { "Basic" }}"
        lines += ""
    }

    if (normalizeHexForCompare(spool.colorHex) != normalizeHexForCompare(colorHexValue)) {
        lines += "Color:"
        lines += "- Datenbank: ${spool.colorHex?.let { "#$it" } ?: "-"}"
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
