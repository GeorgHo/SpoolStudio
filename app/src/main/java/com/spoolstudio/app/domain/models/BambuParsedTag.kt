package com.spoolstudio.app.domain.models

data class BambuParsedTag(
    val uid: String,
    val filamentType: String? = null,
    val detailedFilamentType: String? = null,
    val materialId: String? = null,
    val variantId: String? = null,
    val filamentColor: String? = null,
    val secondFilamentColor: String? = null,
    val filamentColorCount: Int? = null,
    val spoolWeightGrams: Int? = null,
    val filamentLengthMeters: Int? = null,
    val filamentDiameterMm: Float? = null,
    val spoolWidthMm: Float? = null,
    val nozzleDiameterMm: Float? = null,
    val dryingTempC: Int? = null,
    val dryingTimeHours: Int? = null,
    val bedTempType: Int? = null,
    val bedTempC: Int? = null,
    val maxHotendC: Int? = null,
    val minHotendC: Int? = null,
    val productionDate: String? = null,
    val unknown1: String? = null,
    val unknown2Hex: String? = null
) {
    fun toDisplayString(): String = buildString {
        appendLine("Bambu RFID Parsed")
        appendLine()

        appendLine("UID: $uid")
        filamentType?.takeIf { it.isNotBlank() }?.let { appendLine("Filament Type: $it") }
        detailedFilamentType?.takeIf { it.isNotBlank() }?.let { appendLine("Detailed Type: $it") }
        materialId?.takeIf { it.isNotBlank() }?.let { appendLine("Material ID: $it") }
        variantId?.takeIf { it.isNotBlank() }?.let { appendLine("Variant ID: $it") }

        filamentColor?.let { base ->
            val combined = secondFilamentColor?.let { "$base / $it" } ?: base
            appendLine("Filament Color: $combined")
        }

        filamentColorCount?.let { appendLine("Color Count: $it") }
        spoolWeightGrams?.let { appendLine("Spool Weight: ${it} g") }
        filamentLengthMeters?.let { appendLine("Filament Length: ${it} m") }
        filamentDiameterMm?.let { appendLine("Filament Diameter: ${"%.2f".format(it)} mm") }
        spoolWidthMm?.let { appendLine("Spool Width: ${"%.2f".format(it)} mm") }
        nozzleDiameterMm?.let { appendLine("Min Nozzle Diameter: ${"%.1f".format(it)} mm") }

        if (
            dryingTempC != null ||
            dryingTimeHours != null ||
            bedTempC != null ||
            maxHotendC != null ||
            minHotendC != null
        ) {
            appendLine()
            appendLine("Temperatures:")
            dryingTempC?.let { appendLine("- Drying Temp: ${it} C") }
            dryingTimeHours?.let { appendLine("- Drying Time: ${it} h") }
            bedTempType?.takeIf { it > 0 }?.let { appendLine("- Bed Temp Type: $it") }
            bedTempC?.let { appendLine("- Bed Temp: ${it} C") }
            maxHotendC?.let { appendLine("- Max Hotend: ${it} C") }
            minHotendC?.let { appendLine("- Min Hotend: ${it} C") }
        }

        productionDate?.let {
            appendLine()
            appendLine("Production Date: $it")
        }

        unknown1?.takeIf { it.isNotBlank() }?.let { appendLine("Unknown 1: $it") }
        unknown2Hex?.takeIf { it.isNotBlank() }?.let { appendLine("Unknown 2: $it") }
    }.trim()
}