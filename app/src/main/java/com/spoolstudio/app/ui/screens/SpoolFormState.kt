package com.spoolstudio.app.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.spoolstudio.app.domain.models.FilamentSpool
import com.spoolstudio.app.domain.models.Material
import com.spoolstudio.app.domain.models.OpenSpoolData
import com.spoolstudio.app.ui.SpoolMode
import com.spoolstudio.app.ui.SpoolmanSaveRequest

class SpoolFormState(private val defaultMaterial: Material) {
    var colorHex by mutableStateOf<String?>(null)
    var colorName by mutableStateOf("")
    var filamentType by mutableStateOf("PLA")
    var customMaterial by mutableStateOf("")
    var variant by mutableStateOf("Basic")
    var brand by mutableStateOf("Generic")
    var customBrand by mutableStateOf("")
    var location by mutableStateOf("")
    var customLocation by mutableStateOf("")
    var minTemp by mutableStateOf(defaultMaterial.defaultMinTemp.toString())
    var maxTemp by mutableStateOf(defaultMaterial.defaultMaxTemp.toString())
    var bedMinTemp by mutableStateOf(defaultMaterial.defaultBedMinTemp.toString())
    var bedMaxTemp by mutableStateOf(defaultMaterial.defaultBedMaxTemp.toString())
    var lotNr by mutableStateOf("")
    var comment by mutableStateOf("Created by Spool Studio")
    var remainingWeight by mutableStateOf("")
    var emptySpoolWeight by mutableStateOf("")
    var colorHexInput by mutableStateOf("")
    var colorNameWasManuallyEdited by mutableStateOf(false)
    var isHexManuallySet by mutableStateOf(false)

    fun clearLocation() {
        location = ""
        customLocation = ""
    }

    fun resetForNewSpool() {
        colorHex = null
        colorName = ""
        filamentType = "PLA"
        customMaterial = ""
        variant = "Basic"
        brand = "Generic"
        customBrand = ""
        clearLocation()
        minTemp = defaultMaterial.defaultMinTemp.toString()
        maxTemp = defaultMaterial.defaultMaxTemp.toString()
        bedMinTemp = defaultMaterial.defaultBedMinTemp.toString()
        bedMaxTemp = defaultMaterial.defaultBedMaxTemp.toString()
        lotNr = ""
        comment = "Created by Spool Studio"
        remainingWeight = ""
        emptySpoolWeight = ""
        colorHexInput = ""
        colorNameWasManuallyEdited = false
        isHexManuallySet = false
    }

    fun isRemainingWeightValid(): Boolean =
        isRemainingWeightValid(remainingWeight)

    fun isValid(): Boolean =
        isSpoolFormValid(variant, brand, customBrand, filamentType, customMaterial, remainingWeight, emptySpoolWeight)

    fun validationMessage(): String? =
        spoolFormValidationMessage(variant, brand, customBrand, filamentType, customMaterial, remainingWeight, emptySpoolWeight)

    fun buildSaveRequest(spoolMode: SpoolMode, selectedSpool: FilamentSpool?): SpoolmanSaveRequest =
        buildSpoolmanSaveRequest(
            filamentType = filamentType,
            customMaterial = customMaterial,
            variant = variant,
            brand = brand,
            customBrand = customBrand,
            location = location,
            customLocation = customLocation,
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
            spoolMode = spoolMode,
            selectedSpool = selectedSpool
        )

    fun buildOpenSpoolTagData(spoolMode: SpoolMode, selectedSpool: FilamentSpool?): OpenSpoolData? =
        buildOpenSpoolTagData(
            filamentType = filamentType,
            customMaterial = customMaterial,
            variant = variant,
            brand = brand,
            customBrand = customBrand,
            colorHex = colorHex,
            minTemp = minTemp,
            maxTemp = maxTemp,
            bedMinTemp = bedMinTemp,
            bedMaxTemp = bedMaxTemp,
            lotNr = lotNr,
            spoolMode = spoolMode,
            selectedSpool = selectedSpool
        )

    fun applyBambuRfidData(data: BambuRfidFormData, suggestedColorName: String) {
        data.material?.let {
            filamentType = it
            customMaterial = ""
        }

        variant = data.normalizedVariant

        data.colorHex?.let { hex ->
            colorHex = hex
            colorHexInput = hex
            isHexManuallySet = false
            colorNameWasManuallyEdited = false
            colorName = if (suggestedColorName.isNotBlank()) suggestedColorName else "#$hex"
        }

        brand = "Bambu Lab"
        customBrand = ""
        clearLocation()
        remainingWeight = ""
        emptySpoolWeight = ""
        comment = ""

        data.minHotend?.let { minTemp = it.toString() }
        data.maxHotend?.let { maxTemp = it.toString() }

        data.bedTemp?.let { temp ->
            if (temp <= 0) {
                bedMinTemp = "0"
                bedMaxTemp = "0"
            } else {
                bedMinTemp = (temp - 10).coerceAtLeast(0).toString()
                bedMaxTemp = (temp + 10).toString()
            }
        }

        data.uid?.let {
            lotNr = it.take(32)
        }
    }
}
