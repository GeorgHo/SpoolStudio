package com.spoolstudio.app.domain.models

import com.spoolstudio.app.data.local.MaterialDatabase
import com.spoolstudio.app.domain.models.displayMaterialWithModifier

data class FilamentSpool(
    val id: Int? = null,
    val material: String,
    val variant: String = "",
    val materialModifier: String = "",
    val brand: String,
    val colorHex: String?,
    val minTemp: Int?,
    val maxTemp: Int?,
    val bedMinTemp: Int?,
    val bedMaxTemp: Int?,
    val remainingWeight: Float? = null,
    val usedWeight: Float = 0f,
    val emptySpoolWeight: Float? = null,
    val location: String? = null,
    val lotNr: String? = null,
    val archived: Boolean = false,
    val spoolmanName: String?,
    val filamentId: Int? = null,
    val comment: String? = null,
    val firstUsed: String? = null,
    val lastUsed: String? = null,
    val cardUids: List<String> = emptyList()
) {
    val displayName: String
        get() = listOf(
            displayMaterialWithModifier(material, materialModifier),
            variant.takeIf { it.isNotBlank() && !it.equals("Basic", ignoreCase = true) }
        ).filterNotNull()
            .joinToString(" ")

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
            return splitLegacyMaterialAndVariant(rawMaterial)
        }

        fun fromSpoolman(spool: SpoolmanSpool): FilamentSpool {
            val (legacyMaterial, legacyVariant) = splitMaterialAndVariant(spool.filament.material)
            val normalizedFields = normalizeSpoolLinkFilamentFields(
                material = legacyMaterial,
                variant = spool.filament.extra.stringValue("variant") ?: legacyVariant
            )
            val spoolStudioFields = spoolLinkSpoolmanFields(
                material = normalizedFields.material,
                variant = normalizedFields.variant,
                materialModifier = spool.filament.extra.stringValue("material_modifier")
            )
            val materialData = MaterialDatabase.getMaterial(spoolStudioFields.material)
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
                material = spoolStudioFields.material,
                variant = spoolStudioFields.variant,
                materialModifier = spoolStudioFields.materialModifier,
                brand = spool.filament.vendor?.name ?: "Unknown",
                colorHex = normalizeHexColor(spool.filament.color_hex),
                minTemp = minTemp,
                maxTemp = maxTemp,
                bedMinTemp = bedMinTemp,
                bedMaxTemp = bedMaxTemp,
                remainingWeight = spool.remaining_weight,
                usedWeight = spool.used_weight,
                emptySpoolWeight = spool.filament.spool_weight,
                location = spool.location,
                lotNr = spool.lot_nr,
                archived = spool.archived,
                spoolmanName = spool.filament.name,
                filamentId = spool.filament.id,
                comment = spool.comment,
                firstUsed = spool.first_used,
                lastUsed = spool.last_used,
                cardUids = parseCardUids(
                    spool.extra.stringValue("card_uids") ?: spool.extra.stringValue("card_uid")
                )
            )
        }

        fun fromOpenSpool(spool: OpenSpoolData): FilamentSpool {
            val fields = normalizeSpoolLinkFilamentFields(spool.type, spool.subtype)
            val spoolStudioFields = spoolLinkSpoolmanFields(fields.material, fields.variant)
            val material = MaterialDatabase.getMaterial(spoolStudioFields.material)
            return FilamentSpool(
                id = spool.spoolId?.toIntOrNull(),
                material = spoolStudioFields.material,
                variant = spoolStudioFields.variant,
                materialModifier = spoolStudioFields.materialModifier,
                brand = spool.brand,
                location = null,
                colorHex = normalizeHexColor(spool.colorHex),
                minTemp = spool.minTemp.toIntOrNull() ?: material?.defaultMinTemp,
                maxTemp = spool.maxTemp.toIntOrNull() ?: material?.defaultMaxTemp,
                bedMinTemp = spool.bedMinTemp?.toIntOrNull() ?: material?.defaultBedMinTemp,
                bedMaxTemp = spool.bedMaxTemp?.toIntOrNull() ?: material?.defaultBedMaxTemp,
                lotNr = spool.lotNr,
                cardUids = listOfNotNull(normalizeCardUid(spool.cardUid).takeIf { it.isNotBlank() }),
                spoolmanName = "",
                filamentId = null
            )
        }
    }
}
