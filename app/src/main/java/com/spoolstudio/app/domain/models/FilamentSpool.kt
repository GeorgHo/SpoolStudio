package com.spoolstudio.app.domain.models

import com.spoolstudio.app.data.local.MaterialDatabase

data class FilamentSpool(
    val id: Int? = null,
    val material: String,
    val variant: String = "",
    val brand: String,
    val colorHex: String?,
    val minTemp: Int?,
    val maxTemp: Int?,
    val bedMinTemp: Int?,
    val bedMaxTemp: Int?,
    val remainingWeight: Float? = null,
    val usedWeight: Float = 0f,
    val location: String? = null,
    val lotNr: String? = null,
    val archived: Boolean = false,
    val spoolmanName: String?,
    val filamentId: Int? = null,
    val comment: String? = null
) {
    val displayName: String
        get() = if (variant.isNotEmpty()) "$material $variant" else material

    companion object {
        fun normalizeHexColor(input: String?): String? {
            val hex = input?.removePrefix("#")?.uppercase() ?: return null
            return when (hex.length) {
                8 -> hex.substring(0, 6) // ARGB → RGB
                6 -> hex
                else -> null
            }
        }
        fun splitMaterialAndVariant(rawMaterial: String?): Pair<String, String> {
            val clean = rawMaterial?.trim().orEmpty()
            if (clean.isBlank()) return "" to ""
            val idx = clean.indexOf('-')
            return if (idx <= 0 || idx >= clean.length - 1) {
                clean to ""
            } else {
                clean.substring(0, idx).trim() to clean.substring(idx + 1).trim()
            }
        }

        fun fromSpoolman(spool: SpoolmanSpool): FilamentSpool {
            val (baseMaterial, variant) = splitMaterialAndVariant(spool.filament.material)
            val materialData = MaterialDatabase.getMaterial(baseMaterial)
            val extruderTemp = spool.filament.settings_extruder_temp
            val bedTemp = spool.filament.settings_bed_temp

            val minTemp: Int?
            val maxTemp: Int?
            if (materialData != null && extruderTemp != null &&
                extruderTemp >= materialData.defaultMinTemp && extruderTemp <= materialData.defaultMaxTemp) {
                minTemp = materialData.defaultMinTemp
                maxTemp = materialData.defaultMaxTemp
            } else {
                minTemp = extruderTemp
                maxTemp = extruderTemp?.plus(20)
            }

            val bedMinTemp: Int?
            val bedMaxTemp: Int?
            if (materialData != null && bedTemp != null &&
                bedTemp >= materialData.defaultBedMinTemp && bedTemp <= materialData.defaultBedMaxTemp) {
                bedMinTemp = materialData.defaultBedMinTemp
                bedMaxTemp = materialData.defaultBedMaxTemp
            } else {
                bedMinTemp = bedTemp
                bedMaxTemp = bedTemp?.plus(10)
            }

            return FilamentSpool(
                id = spool.id,
                material = baseMaterial,
                variant = variant,
                brand = spool.filament.vendor?.name ?: "Unknown",
                colorHex = spool.filament.color_hex?.takeIf { it.isNotEmpty() },
                minTemp = minTemp,
                maxTemp = maxTemp,
                bedMinTemp = bedMinTemp,
                bedMaxTemp = bedMaxTemp,
                remainingWeight = spool.remaining_weight,
                usedWeight = spool.used_weight,
                location = spool.location,
                lotNr = spool.lot_nr,
                archived = spool.archived,
                spoolmanName = spool.filament.name,
                filamentId = spool.filament.id,
                comment = spool.comment
            )
        }

        fun fromOpenSpool(spool: OpenSpoolData): FilamentSpool {
            val material = MaterialDatabase.getMaterial(spool.type)
            return FilamentSpool(
                id = spool.spoolId?.toIntOrNull(),
                // material = spool.type,
                material = spool.type?.ifBlank { "PLA" } ?: "PLA",
                // variant = spool.subtype?.trim().orEmpty().ifBlank { "Basic" },
                variant = spool.subtype?.ifBlank { "Basic" } ?: "Basic",
                brand = spool.brand,
                location = null,
                colorHex = normalizeHexColor(spool.colorHex),
                minTemp = spool.minTemp.toIntOrNull() ?: material?.defaultMinTemp,
                maxTemp = spool.maxTemp.toIntOrNull() ?: material?.defaultMaxTemp,
                bedMinTemp = spool.bedMinTemp?.toIntOrNull() ?: material?.defaultBedMinTemp,
                bedMaxTemp = spool.bedMaxTemp?.toIntOrNull() ?: material?.defaultBedMaxTemp,
                lotNr = spool.lotNr,
                spoolmanName = "",
                filamentId = null
            )
        }
    }
}
