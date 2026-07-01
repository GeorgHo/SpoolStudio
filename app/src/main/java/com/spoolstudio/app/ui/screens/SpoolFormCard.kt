package com.spoolstudio.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.spoolstudio.app.domain.models.FilamentSpool
import com.spoolstudio.app.ui.components.FilamentForm
import com.spoolstudio.app.ui.components.SpoolInfoCard
import com.spoolstudio.app.ui.components.SpoolmanFilamentDropdown
import com.spoolstudio.app.utils.formatColorName
import com.spoolstudio.app.utils.suggestColorName
import com.spoolstudio.app.utils.suggestHexFromName

@Composable
fun SpoolFormCard(
    form: SpoolFormState,
    spools: List<FilamentSpool>,
    selectedSpool: FilamentSpool?,
    spoolmanUrl: String,
    currentSpoolId: String?,
    isLoadingSpools: Boolean,
    availableMaterials: List<String>,
    availableBrands: List<String>,
    availableVariants: List<String>,
    availableLocations: List<String>,
    showLotNumber: Boolean,
    showCommentField: Boolean,
    isRemainingWeightValid: Boolean,
    onSpoolSelected: (FilamentSpool?) -> Unit,
    onClearAllSpoolFields: () -> Unit,
    onRefreshSelectedSpool: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 0.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            SpoolmanSelectionSection(
                spools = spools,
                selectedSpool = selectedSpool,
                spoolmanUrl = spoolmanUrl,
                currentSpoolId = currentSpoolId,
                isLoadingSpools = isLoadingSpools,
                onSpoolSelected = onSpoolSelected,
                onClearAllSpoolFields = onClearAllSpoolFields,
                onRefreshSelectedSpool = onRefreshSelectedSpool
            )

            FilamentForm(
                filamentType = form.filamentType,
                customMaterial = form.customMaterial,
                variant = form.variant,
                colorHex = form.colorHex,
                colorName = form.colorName,
                brand = form.brand,
                customBrand = form.customBrand,
                availableMaterials = availableMaterials,
                availableBrands = availableBrands,
                availableVariants = availableVariants,
                onFilamentTypeChange = { material, min, max, bedMin, bedMax ->
                    form.filamentType = material
                    form.minTemp = min
                    form.maxTemp = max
                    form.bedMinTemp = bedMin
                    form.bedMaxTemp = bedMax
                },
                onCustomMaterialChange = { form.customMaterial = it },
                onVariantChange = { form.variant = it },
                onColorChange = { newHex ->
                    form.colorHex = newHex
                    form.colorHexInput = newHex ?: ""
                    form.isHexManuallySet = false

                    if (!form.colorNameWasManuallyEdited) {
                        val suggested = suggestColorName(newHex)
                        if (suggested.isNotBlank()) {
                            form.colorName = suggested
                        }
                    }
                },
                onColorNameChange = { newName ->
                    val formatted = formatColorName(newName.take(40))
                    form.colorName = formatted
                    form.colorNameWasManuallyEdited = formatted.isNotBlank()

                    if (!form.isHexManuallySet) {
                        val matchedHex = suggestHexFromName(formatted)
                        if (matchedHex != null) {
                            form.colorHex = matchedHex
                            form.colorHexInput = matchedHex
                        }
                    }
                },
                onBrandChange = { form.brand = it },
                onCustomBrandChange = { form.customBrand = it }
            )

            Spacer(modifier = Modifier.height(12.dp))

            LocationSection(
                location = form.location,
                customLocation = form.customLocation,
                availableLocations = availableLocations,
                onLocationChange = { form.location = it },
                onCustomLocationChange = { form.customLocation = it }
            )

            SpoolDetailsSection(
                lotNr = form.lotNr,
                remainingWeight = form.remainingWeight,
                comment = form.comment,
                showLotNumber = showLotNumber,
                showCommentField = showCommentField,
                isRemainingWeightValid = isRemainingWeightValid,
                onLotNrChange = { form.lotNr = it },
                onRemainingWeightChange = { form.remainingWeight = it },
                onCommentChange = { form.comment = it }
            )

            Spacer(modifier = Modifier.height(10.dp))
            key(form.filamentType) {
                TemperatureSection(
                    nozzleMin = form.minTemp,
                    nozzleMax = form.maxTemp,
                    bedMin = form.bedMinTemp,
                    bedMax = form.bedMaxTemp,
                    onNozzleMinChange = { form.minTemp = it },
                    onNozzleMaxChange = { form.maxTemp = it },
                    onBedMinChange = { form.bedMinTemp = it },
                    onBedMaxChange = { form.bedMaxTemp = it }
                )
            }
        }
    }
}

@Composable
private fun SpoolmanSelectionSection(
    spools: List<FilamentSpool>,
    selectedSpool: FilamentSpool?,
    spoolmanUrl: String,
    currentSpoolId: String?,
    isLoadingSpools: Boolean,
    onSpoolSelected: (FilamentSpool?) -> Unit,
    onClearAllSpoolFields: () -> Unit,
    onRefreshSelectedSpool: (Int) -> Unit
) {
    if (spools.isNotEmpty()) {
        SpoolmanFilamentDropdown(
            modifier = Modifier.fillMaxWidth(),
            filaments = spools,
            selectedFilament = selectedSpool,
            onFilamentSelected = onSpoolSelected,
            spoolmanUrl = spoolmanUrl,
            currentSpoolId = currentSpoolId,
            isLoading = isLoadingSpools,
            onClearAll = onClearAllSpoolFields,
            infoButton = {
                if (selectedSpool != null) {
                    SpoolInfoCard(
                        spool = selectedSpool,
                        onOpenRefreshRequested = {
                            selectedSpool.id?.let(onRefreshSelectedSpool)
                        }
                    )
                }
            }
        )
        Spacer(modifier = Modifier.height(16.dp))
        return
    }

    if (isLoadingSpools) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Loading Spoolman filaments...")
        }
        Spacer(modifier = Modifier.height(16.dp))
        return
    }

    if (spoolmanUrl.isBlank()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Text(
                text = "Tip: Connect Spoolman server in settings for easy filament selection",
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}
