package com.spoolstudio.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

@Composable
fun FilamentForm(
    filamentType: String,
    customMaterial: String,
    variant: String,
    colorHex: String?,
    colorName: String,
    brand: String,
    customBrand: String,
    availableMaterials: List<String> = emptyList(),
    availableBrands: List<String> = emptyList(),
    availableVariants: List<String> = emptyList(),
    onFilamentTypeChange: (String, String, String, String, String) -> Unit,
    onCustomMaterialChange: (String) -> Unit,
    onVariantChange: (String) -> Unit,
    onColorChange: (String?) -> Unit,
    onColorNameChange: (String) -> Unit,
    onBrandChange: (String) -> Unit,
    onCustomBrandChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        MaterialSelector(
            selectedMaterial = filamentType,
            customMaterial = customMaterial,
            dynamicMaterials = availableMaterials,
            onMaterialSelected = onFilamentTypeChange,
            onCustomMaterialChange = onCustomMaterialChange
        )

        VariantSelector(
            selectedVariant = variant,
            dynamicVariants = availableVariants,
            onVariantChange = onVariantChange
        )

        ColorSelector(
            selectedColor = colorHex,
            colorName = colorName,
            onColorSelected = onColorChange,
            onColorNameChange = onColorNameChange
        )

        BrandSelector(
            selectedBrand = brand,
            customBrand = customBrand,
            dynamicBrands = availableBrands,
            onBrandSelected = onBrandChange,
            onCustomBrandChange = onCustomBrandChange
        )
    }
}
