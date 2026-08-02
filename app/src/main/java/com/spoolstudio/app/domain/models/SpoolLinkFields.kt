package com.spoolstudio.app.domain.models

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.spoolstudio.app.utils.OpenSpoolMaterialMapper

data class SpoolLinkFilamentFields(
    val material: String,
    val variant: String
)

data class SpoolStudioFilamentFields(
    val material: String,
    val variant: String,
    val materialModifier: String = ""
)

private val gson = Gson()

fun encodeSpoolmanExtraString(value: String): String =
    gson.toJson(value.trim())

fun Map<String, JsonElement>?.toRequestExtraMap(): MutableMap<String, Any?> =
    this?.mapValues { (_, value) ->
        when {
            value.isJsonNull -> null
            value.isJsonPrimitive && value.asJsonPrimitive.isString -> value.asString
            else -> value
        }
    }?.toMutableMap() ?: mutableMapOf()

fun normalizeCardUid(value: String?): String {
    val trimmed = value?.trim().orEmpty()
    val withoutKnownPrefix = trimmed
        .replace(Regex("""(?i)^card[_\s-]*uid\s*[:=]\s*"""), "")
        .replace(Regex("""(?i)^uid\s*[:=]\s*"""), "")

    return withoutKnownPrefix
        .filter { it.isDigit() || it.uppercaseChar() in 'A'..'F' }
        .uppercase()
}

fun parseCardUids(value: String?): List<String> =
    value
        ?.split(',', ';', ' ', '\n', '\t')
        ?.map(::normalizeCardUid)
        ?.filter { it.isNotBlank() }
        ?.distinct()
        .orEmpty()

fun formatCardUidsForSpoolman(cardUids: List<String>): String =
    cardUids
        .map(::normalizeCardUid)
        .filter { it.isNotBlank() }
        .distinct()
        .joinToString(",")

fun splitLegacyMaterialAndVariant(rawMaterial: String?): Pair<String, String> {
    val clean = rawMaterial?.trim().orEmpty()
    if (clean.isBlank()) return "" to ""

    val delimiter = Regex("\\s+-\\s+")
    val parts = delimiter.split(clean, limit = 2)
    return if (parts.size == 2) {
        parts[0].trim() to parts[1].trim()
    } else {
        clean to ""
    }
}

fun normalizeSpoolLinkFilamentFields(
    material: String?,
    variant: String?
): SpoolLinkFilamentFields {
    val (legacyMaterial, legacyVariant) = splitLegacyMaterialAndVariant(material)
    val rawMaterial = legacyMaterial.ifBlank { "PLA" }
    val rawVariant = variant?.trim().orEmpty().ifBlank { legacyVariant }

    val materialAlias = materialAlias(rawMaterial)
    val normalizedMaterial = materialAlias?.first
        ?: OpenSpoolMaterialMapper.toOpenSpoolType(rawMaterial, rawVariant)
        ?: rawMaterial.trim()

    val variantParts = buildList {
        materialAlias?.second?.takeIf { it.isNotBlank() }?.let(::add)
        rawVariant.takeUnless { it.equals("Basic", ignoreCase = true) }?.takeIf { it.isNotBlank() }?.let(::add)
    }
        .distinctBy { it.uppercase() }

    return SpoolLinkFilamentFields(
        material = normalizedMaterial.ifBlank { "PLA" },
        variant = variantParts.joinToString(" ").ifBlank { "Basic" }
    )
}

fun spoolLinkFilamentFields(
    material: String?,
    variant: String?
): SpoolLinkFilamentFields =
    SpoolLinkFilamentFields(
        material = material?.trim()?.ifBlank { "PLA" } ?: "PLA",
        variant = variant?.trim()?.ifBlank { "Basic" } ?: "Basic"
    )

fun spoolLinkSpoolmanFields(
    material: String?,
    variant: String?,
    materialModifier: String? = null,
    allowMaterialModifier: Boolean = true
): SpoolStudioFilamentFields {
    val materialParts = splitMaterialModifier(material)
    val variantParts = splitVariantModifier(variant)
    val baseMaterial = OpenSpoolMaterialMapper.toOpenSpoolType(materialParts.material, variantParts.material)
        ?: materialParts.material.ifBlank { "PLA" }
    val modifier = if (allowMaterialModifier) {
        materialModifier?.trim()?.ifBlank { null }
            ?: materialParts.modifier.ifBlank { null }
            ?: variantParts.modifier
    } else {
        ""
    }

    return SpoolStudioFilamentFields(
        material = baseMaterial,
        variant = variantParts.material.ifBlank { "Basic" },
        materialModifier = modifier.trim()
    )
}

fun spoolLinkTagFields(
    material: String?,
    variant: String?,
    materialModifier: String? = null
): SpoolLinkFilamentFields {
    val fields = spoolLinkSpoolmanFields(material, variant, materialModifier)
    return SpoolLinkFilamentFields(
        material = fields.material,
        variant = fields.variant.ifBlank { "Basic" }
    )
}

fun displayMaterialWithModifier(
    material: String,
    materialModifier: String?
): String {
    val cleanMaterial = material.trim().ifBlank { "PLA" }
    val cleanModifier = materialModifier?.trim().orEmpty()
    return when {
        cleanModifier.isBlank() -> cleanMaterial
        cleanModifier.equals("Plus", ignoreCase = true) -> "$cleanMaterial+"
        else -> "$cleanMaterial $cleanModifier"
    }
}

private fun materialAlias(material: String): Pair<String, String>? {
    val compact = material.trim().uppercase().replace(" ", "")
    return when (compact) {
        "PLA+", "PLAPLUS" -> "PLA" to "Plus"
        "PETG+", "PETGPLUS" -> "PETG" to "Plus"
        "ABS+", "ABSPLUS" -> "ABS" to "Plus"
        "ASA+", "ASAPLUS" -> "ASA" to "Plus"
        "APLA" -> "PLA" to "APLA"
        "HT-PLA", "HTPLA" -> "PLA" to "HT"
        else -> null
    }
}

private data class SplitMaterialModifier(
    val material: String,
    val modifier: String
)

private fun splitMaterialModifier(material: String?): SplitMaterialModifier {
    val raw = material?.trim().orEmpty()
    if (raw.isBlank()) return SplitMaterialModifier("PLA", "")
    materialAlias(raw)?.let { return SplitMaterialModifier(it.first, it.second) }

    val plusSuffix = Regex("^(.+?)(?:\\s+PLUS|\\+)$", RegexOption.IGNORE_CASE)
    plusSuffix.matchEntire(raw)?.let { match ->
        return SplitMaterialModifier(match.groupValues[1].trim(), "Plus")
    }

    val highSpeedSuffix = Regex("^(.+?)(?:\\s+HS|\\s+HIGH[- ]?SPEED)$", RegexOption.IGNORE_CASE)
    highSpeedSuffix.matchEntire(raw)?.let { match ->
        return SplitMaterialModifier(match.groupValues[1].trim(), "HS")
    }

    return SplitMaterialModifier(raw, "")
}

private fun splitVariantModifier(variant: String?): SplitMaterialModifier {
    val raw = variant?.trim().orEmpty()
    if (raw.isBlank()) return SplitMaterialModifier("Basic", "")

    val tokens = raw.split(Regex("\\s+")).filter { it.isNotBlank() }.toMutableList()
    val first = tokens.firstOrNull().orEmpty()
    val modifier = when {
        first.equals("Plus", ignoreCase = true) || first == "+" -> "Plus"
        first.equals("HS", ignoreCase = true) -> "HS"
        else -> ""
    }
    if (modifier.isNotBlank()) {
        tokens.removeAt(0)
    }

    return SplitMaterialModifier(
        material = tokens.joinToString(" ").ifBlank { "Basic" },
        modifier = modifier
    )
}
