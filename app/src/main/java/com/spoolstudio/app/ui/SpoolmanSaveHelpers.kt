package com.spoolstudio.app.ui

import android.util.Log
import com.spoolstudio.app.domain.models.FilamentSpool
import com.spoolstudio.app.domain.models.OpenSpoolData

fun buildMaterialWithVariant(material: String, variant: String): String {
    val cleanMaterial = material.trim().ifBlank { "Unknown" }
    val cleanVariant = variant.trim().ifBlank { "Basic" }
    return "$cleanMaterial - $cleanVariant"
}

fun normalizedColorHex(colorHex: String?): String? =
    colorHex?.trim()?.removePrefix("#")?.uppercase()?.ifBlank { null }

fun parseRemainingWeight(value: String): Float? =
    value.trim()
        .replace(",", ".")
        .takeIf { it.isNotBlank() }
        ?.toFloatOrNull()
        ?.takeIf { it >= 0f }

fun remainingWeightWarningThreshold(value: Float?): Int? = when {
    value == null -> null
    value <= 50f -> 50
    value <= 100f -> 100
    value <= 150f -> 150
    else -> null
}

fun remainingWeightWarningThreshold(value: String): Int? =
    remainingWeightWarningThreshold(parseRemainingWeight(value))

fun buildTagData(
    spool: FilamentSpool,
    request: SpoolmanSaveRequest,
    resolvedLotNr: String
): OpenSpoolData? {
    return try {
        OpenSpoolData.toOpenSpoolData(spool).copy(
            minTemp = request.minTemp,
            maxTemp = request.maxTemp,
            bedMinTemp = request.bedMinTemp.ifBlank { null },
            bedMaxTemp = request.bedMaxTemp.ifBlank { null },
            subtype = request.variant.ifBlank { "Basic" },
            lotNr = resolvedLotNr.ifBlank { null }
        )
    } catch (e: IllegalArgumentException) {
        Log.w("SpoolStudio", "Saved spool cannot be converted to OpenSpool tag data", e)
        null
    }
}

fun validateBeforeSave(
    spoolmanUrl: String,
    material: String,
    brand: String,
    colorName: String,
    colorHex: String?,
    minTemp: String,
    maxTemp: String,
    remainingWeight: String
): String? {
    if (!isValidSpoolmanUrl(spoolmanUrl)) return "Please configure a valid Spoolman URL first"
    if (material.trim().isBlank()) return "Material is required"
    if (brand.trim().isBlank()) return "Brand is required"
    if (colorName.trim().isBlank()) return "Color name is required"

    val cleanHex = normalizedColorHex(colorHex)
    if (colorHex != null && cleanHex == null) return "HEX color is invalid"

    if (minTemp.isNotBlank() && minTemp.toIntOrNull() == null) return "Min nozzle temperature is invalid"
    if (maxTemp.isNotBlank() && maxTemp.toIntOrNull() == null) return "Max nozzle temperature is invalid"
    if (remainingWeight.isNotBlank() && parseRemainingWeight(remainingWeight) == null) {
        return "Remaining filament weight is invalid"
    }

    return null
}

private fun isValidSpoolmanUrl(url: String): Boolean {
    val normalized = url.trim().removeSuffix("/")
    return normalized.isNotEmpty() &&
        (normalized.startsWith("http://") || normalized.startsWith("https://"))
}
