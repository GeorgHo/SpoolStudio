package com.spoolstudio.app.ui.screens

import com.spoolstudio.app.domain.models.FilamentSpool
import com.spoolstudio.app.ui.SpoolMode
import com.spoolstudio.app.ui.SpoolmanSaveRequest

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

fun isSpoolFormValid(
    variant: String,
    brand: String,
    customBrand: String,
    filamentType: String,
    customMaterial: String,
    remainingWeight: String
): Boolean =
    isSpoolVariantValid(variant) &&
        isSpoolBrandValid(brand, customBrand) &&
        isSpoolMaterialValid(filamentType, customMaterial) &&
        isRemainingWeightValid(remainingWeight)

fun spoolFormValidationMessage(
    variant: String,
    brand: String,
    customBrand: String,
    filamentType: String,
    customMaterial: String,
    remainingWeight: String
): String? = when {
    !isSpoolMaterialValid(filamentType, customMaterial) -> "Please enter a custom filament type"
    !isSpoolVariantValid(variant) -> "Please enter a custom variant"
    !isSpoolBrandValid(brand, customBrand) -> "Please enter a custom brand"
    !isRemainingWeightValid(remainingWeight) -> "Please enter a valid remaining weight"
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
        existingSpoolId = if (spoolMode == SpoolMode.UPDATE) selectedSpool?.id else null
    )
