package com.spoolstudio.app.domain.models

import com.spoolstudio.app.data.local.MaterialDatabase
import org.json.JSONObject

data class OpenSpoolData(
    val protocol: String = "openspool",
    val version: String = "1.0",
    val type: String,
    val colorHex: String?,
    val brand: String,
    val minTemp: String,
    val maxTemp: String,
    val bedMinTemp: String? = null,
    val bedMaxTemp: String? = null,
    val subtype: String = "Basic",
    val spoolId: String? = null,
    val lotNr: String? = null,
    val cardUid: String? = null
) {
    fun toJson(): String {
        return JSONObject().apply {
            put("protocol", protocol)
            put("version", version)
            put("type", type)
            put("color_hex", colorHex ?: "")
            put("brand", brand)
            put("min_temp", minTemp)
            put("max_temp", maxTemp)
            bedMinTemp?.let { put("bed_min_temp", it) }
            bedMaxTemp?.let { put("bed_max_temp", it) }
            spoolId?.let { put("spool_id", it) }
            lotNr?.let { put("lot_nr", it) }
            cardUid?.let { put("card_uid", normalizeCardUid(it)) }
            // if (subtype.isNotEmpty()) put("subtype", subtype)
            put("subtype", subtype.ifBlank { "Basic" })
        }.toString()
    }

    companion object {
        fun fromJson(json: String): OpenSpoolData? {
            return try {
                val cleanJson = if (json.startsWith("{")) json else json.dropWhile { it != '{' }
                val jsonObj = JSONObject(cleanJson)
                if (jsonObj.optString("protocol") == "openspool") {
                    val type = jsonObj.optString("type", "Unknown")
                    val subtype = jsonObj.optString("subtype", "Basic")
                    val normalizedFields = normalizeSpoolLinkFilamentFields(type, subtype)
                    val material = MaterialDatabase.getMaterial(normalizedFields.material)
                    OpenSpoolData(
                        type = type,
                        colorHex = jsonObj.optString("color_hex", "").takeIf { it.isNotEmpty() },
                        brand = jsonObj.optString("brand", "Unknown"),
                        minTemp = jsonObj.optString("min_temp", material?.defaultMinTemp?.toString() ?: "200"),
                        maxTemp = jsonObj.optString("max_temp", material?.defaultMaxTemp?.toString() ?: "220"),
                        bedMinTemp = jsonObj.optString("bed_min_temp").takeIf { it.isNotEmpty() },
                        bedMaxTemp = jsonObj.optString("bed_max_temp").takeIf { it.isNotEmpty() },
                        subtype = subtype,
                        spoolId = jsonObj.optString("spool_id").takeIf { it.isNotEmpty() },
                        lotNr = jsonObj.optString("lot_nr").takeIf { it.isNotEmpty() },
                        cardUid = normalizeCardUid(jsonObj.optString("card_uid").takeIf { it.isNotEmpty() })
                            .takeIf { it.isNotBlank() }
                    )
                } else null
            } catch (e: Exception) {
                null
            }
        }

        /*
        fun toOpenSpoolData(spool: FilamentSpool): OpenSpoolData {
            return OpenSpoolData(
                type = spool.material,
                colorHex = spool.colorHex,
                brand = spool.brand,
                minTemp = spool.minTemp?.toString() ?: "200",
                maxTemp = spool.maxTemp?.toString() ?: "220",
                bedMinTemp = spool.bedMinTemp?.toString(),
                bedMaxTemp = spool.bedMaxTemp?.toString(),
                subtype = spool.variant.ifBlank { "Basic" },
                spoolId = spool.id?.toString(),
                lotNr = spool.lotNr
            )
        }
        */
        fun toOpenSpoolData(spool: FilamentSpool): OpenSpoolData {
            val fields = spoolLinkTagFields(spool.material, spool.variant, spool.materialModifier)

            return OpenSpoolData(
                type = fields.material,
                colorHex = spool.colorHex,
                brand = spool.brand,
                minTemp = spool.minTemp?.toString() ?: "200",
                maxTemp = spool.maxTemp?.toString() ?: "220",
                bedMinTemp = spool.bedMinTemp?.toString(),
                bedMaxTemp = spool.bedMaxTemp?.toString(),
                subtype = fields.variant.ifBlank { "Basic" },
                spoolId = spool.id?.toString(),
                lotNr = spool.lotNr,
                cardUid = spool.cardUids.firstOrNull()
            )
        }
    }
}
