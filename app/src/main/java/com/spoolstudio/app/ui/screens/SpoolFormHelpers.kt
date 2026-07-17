package com.spoolstudio.app.ui.screens

import com.spoolstudio.app.domain.models.FilamentSpool
import com.spoolstudio.app.domain.models.OpenSpoolData
import com.spoolstudio.app.ui.SpoolMode
import com.spoolstudio.app.ui.SpoolmanSaveRequest
import com.spoolstudio.app.ui.normalizedColorHex
import com.spoolstudio.app.ui.parseRemainingWeight
import com.spoolstudio.app.utils.OpenSpoolMaterialMapper
import java.util.Locale

fun resolveMaterialName(filamentType: String, customMaterial: String): String =
    if (filamentType == "Other" && customMaterial.isNotBlank()) customMaterial else filamentType

fun resolveBrandName(brand: String, customBrand: String): String =
    if (brand == "Other" && customBrand.isNotBlank()) customBrand else brand

fun resolveVariantName(variant: String): String =
    if (variant == "Other") "" else variant

fun resolveLocationName(location: String, customLocation: String): String =
    when {
        location == "Other" -> customLocation.trim()
        location.isBlank() -> ""
        else -> location.trim()
    }

fun isSpoolVariantValid(variant: String): Boolean = variant != "Other"

fun isSpoolBrandValid(brand: String, customBrand: String): Boolean =
    brand != "Other" || customBrand.isNotBlank()

fun isSpoolMaterialValid(filamentType: String, customMaterial: String): Boolean =
    filamentType != "Other" || customMaterial.isNotBlank()

fun isRemainingWeightValid(remainingWeight: String): Boolean {
    val normalized = remainingWeight.trim().replace(",", ".")
    return normalized.isBlank() || normalized.toFloatOrNull()?.let { it >= 0f } == true
}

fun isEmptySpoolWeightValid(emptySpoolWeight: String): Boolean =
    isRemainingWeightValid(emptySpoolWeight)

fun formatRemainingWeightInput(remainingWeight: String): String? {
    val parsed = if (remainingWeight.isBlank()) {
        1000f
    } else {
        parseRemainingWeight(remainingWeight) ?: return null
    }

    return String.format(Locale.US, "%.2f", parsed)
}

fun formatLoadedRemainingWeight(remainingWeight: Float?): String {
    val weight = remainingWeight?.takeIf { it >= 0f } ?: return ""
    return String.format(Locale.US, "%.2f", weight)
}

fun formatLoadedEmptySpoolWeight(emptySpoolWeight: Float?): String =
    formatLoadedRemainingWeight(emptySpoolWeight)

fun isSpoolFormValid(
    variant: String,
    brand: String,
    customBrand: String,
    filamentType: String,
    customMaterial: String,
    remainingWeight: String,
    emptySpoolWeight: String
): Boolean =
    isSpoolVariantValid(variant) &&
        isSpoolBrandValid(brand, customBrand) &&
        isSpoolMaterialValid(filamentType, customMaterial) &&
        isRemainingWeightValid(remainingWeight) &&
        isEmptySpoolWeightValid(emptySpoolWeight)

fun spoolFormValidationMessage(
    variant: String,
    brand: String,
    customBrand: String,
    filamentType: String,
    customMaterial: String,
    remainingWeight: String,
    emptySpoolWeight: String
): String? = when {
    !isSpoolMaterialValid(filamentType, customMaterial) -> "Please enter a custom filament type"
    !isSpoolVariantValid(variant) -> "Please enter a custom variant"
    !isSpoolBrandValid(brand, customBrand) -> "Please enter a custom brand"
    !isRemainingWeightValid(remainingWeight) -> "Please enter a valid remaining weight"
    !isEmptySpoolWeightValid(emptySpoolWeight) -> "Please enter a valid empty spool weight"
    else -> null
}

fun buildSpoolmanSaveRequest(
    filamentType: String,
    customMaterial: String,
    variant: String,
    brand: String,
    customBrand: String,
    location: String,
    customLocation: String,
    colorHex: String?,
    colorName: String,
    minTemp: String,
    maxTemp: String,
    bedMinTemp: String,
    bedMaxTemp: String,
    lotNr: String,
    comment: String,
    remainingWeight: String,
    emptySpoolWeight: String,
    spoolMode: SpoolMode,
    selectedSpool: FilamentSpool?
): SpoolmanSaveRequest =
    SpoolmanSaveRequest(
        material = resolveMaterialName(filamentType, customMaterial),
        variant = resolveVariantName(variant),
        brand = resolveBrandName(brand, customBrand),
        location = resolveLocationName(location, customLocation),
        colorHex = colorHex,
        colorName = colorName,
        minTemp = minTemp,
        maxTemp = maxTemp,
        bedMinTemp = bedMinTemp,
        bedMaxTemp = bedMaxTemp,
        lotNr = lotNr,
        comment = comment,
        remainingWeight = remainingWeight,
        emptySpoolWeight = emptySpoolWeight,
        existingSpoolId = if (spoolMode == SpoolMode.UPDATE) selectedSpool?.id else null
    )

fun hasSpoolmanSaveChanges(request: SpoolmanSaveRequest, selectedSpool: FilamentSpool?): Boolean {
    if (selectedSpool == null) return true

    fun normalizedInt(value: String): Int? = value.trim().toIntOrNull()

    val requestedRemainingWeight = parseRemainingWeight(request.remainingWeight)
        ?: selectedSpool.remainingWeight
    val selectedRemainingWeight = selectedSpool.remainingWeight
    val remainingWeightChanged = when {
        requestedRemainingWeight == null && selectedRemainingWeight == null -> false
        requestedRemainingWeight == null || selectedRemainingWeight == null -> true
        else -> kotlin.math.abs(requestedRemainingWeight - selectedRemainingWeight) > 0.01f
    }

    val requestedEmptySpoolWeight = parseRemainingWeight(request.emptySpoolWeight)
        ?: selectedSpool.emptySpoolWeight
    val selectedEmptySpoolWeight = selectedSpool.emptySpoolWeight
    val emptySpoolWeightChanged = when {
        requestedEmptySpoolWeight == null && selectedEmptySpoolWeight == null -> false
        requestedEmptySpoolWeight == null || selectedEmptySpoolWeight == null -> true
        else -> kotlin.math.abs(requestedEmptySpoolWeight - selectedEmptySpoolWeight) > 0.01f
    }

    val requestedLotNr = request.lotNr.trim()
        .ifBlank { selectedSpool.lotNr.orEmpty().trim() }

    val requestedColorName = request.colorName.trim().ifBlank { "Unknown" }
    val selectedColorName = selectedSpool.spoolmanName.orEmpty().trim().ifBlank { "Unknown" }

    return request.material.trim() != selectedSpool.material.trim() ||
        request.variant.trim().ifBlank { "Basic" } != selectedSpool.variant.trim().ifBlank { "Basic" } ||
        request.brand.trim() != selectedSpool.brand.trim() ||
        normalizedColorHex(request.colorHex) != normalizedColorHex(selectedSpool.colorHex) ||
        requestedColorName != selectedColorName ||
        normalizedInt(request.minTemp) != selectedSpool.minTemp ||
        normalizedInt(request.maxTemp) != selectedSpool.maxTemp ||
        normalizedInt(request.bedMinTemp) != selectedSpool.bedMinTemp ||
        normalizedInt(request.bedMaxTemp) != selectedSpool.bedMaxTemp ||
        request.location.trim().ifBlank { null } != selectedSpool.location?.trim()?.ifBlank { null } ||
        requestedLotNr != selectedSpool.lotNr.orEmpty().trim() ||
        request.comment.trim().ifBlank { null } != selectedSpool.comment?.trim()?.ifBlank { null } ||
        remainingWeightChanged ||
        emptySpoolWeightChanged
}

fun buildOpenSpoolTagData(
    filamentType: String,
    customMaterial: String,
    variant: String,
    brand: String,
    customBrand: String,
    colorHex: String?,
    minTemp: String,
    maxTemp: String,
    bedMinTemp: String,
    bedMaxTemp: String,
    lotNr: String,
    spoolMode: SpoolMode,
    selectedSpool: FilamentSpool?
): OpenSpoolData? {
    val materialName = resolveMaterialName(filamentType, customMaterial)
    val variantName = resolveVariantName(variant)
    val openSpoolType = OpenSpoolMaterialMapper.toOpenSpoolType(
        material = materialName,
        variant = variantName
    ) ?: return null

    return OpenSpoolData(
        type = openSpoolType,
        colorHex = colorHex,
        brand = resolveBrandName(brand, customBrand),
        minTemp = minTemp,
        maxTemp = maxTemp,
        bedMinTemp = bedMinTemp.ifBlank { null },
        bedMaxTemp = bedMaxTemp.ifBlank { null },
        subtype = variantName.ifBlank { "Basic" },
        spoolId = if (spoolMode == SpoolMode.UPDATE) selectedSpool?.id?.toString() else null,
        lotNr = lotNr
    )
}
