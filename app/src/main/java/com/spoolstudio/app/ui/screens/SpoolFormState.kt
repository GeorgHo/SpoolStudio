package com.spoolstudio.app.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.spoolstudio.app.domain.models.Material
import com.spoolstudio.app.domain.models.OpenSpoolData

class SpoolFormState(defaultMaterial: Material) {
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
    var lotNr by mutableStateOf(OpenSpoolData.generateLotNr())
    var comment by mutableStateOf("Created by Spool Studio")
    var remainingWeight by mutableStateOf("")
    var colorHexInput by mutableStateOf("")
    var colorNameWasManuallyEdited by mutableStateOf(false)
    var isHexManuallySet by mutableStateOf(false)

    fun clearLocation() {
        location = ""
        customLocation = ""
    }
}
