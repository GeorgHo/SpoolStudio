package com.spoolstudio.app.domain.models

import com.google.gson.JsonElement
import com.google.gson.JsonParser

data class SpoolmanSpool(
    val id: Int? = null,
    val filament: SpoolmanFilament,
    val remaining_weight: Float? = null,
    val used_weight: Float = 0f,
    val location: String? = null,
    val lot_nr: String? = null,
    val archived: Boolean = false,
    val extra: Map<String, JsonElement>? = null,
    val comment: String? = null
)

data class SpoolmanFilament(
    val id: Int,
    val name: String,
    val material: String,
    val vendor: SpoolmanVendor?,
    val color_hex: String?,
    val settings_extruder_temp: Int?,
    val settings_bed_temp: Int?,
    val extra: Map<String, JsonElement>? = null,
    val density: Float? = null,
    val diameter: Float? = null,
    val spool_weight: Float? = null,
    val weight: Float? = null
)

data class SpoolmanVendor(
    val id: Int? = null,
    val name: String,
    val comment: String? = null,
    val empty_spool_weight: Float? = null
)

data class SpoolmanResponse<T>(
    val items: List<T>
)

data class CreateVendorRequest(
    val name: String,
    val comment: String? = null,
    val empty_spool_weight: Float? = null
)

data class CreateFilamentRequest(
    val name: String,
    val material: String,
    val vendor_id: Int,
    val color_hex: String? = null,
    val settings_extruder_temp: Int? = null,
    val settings_bed_temp: Int? = null,
    val density: Float,
    val diameter: Float,
    val weight: Float,
    val spool_weight: Float? = null,
    val price: Float? = null,
    val comment: String? = null,
    val extra: Map<String, Any?>? = null
)

data class CreateSpoolRequest(
    val filament_id: Int,
    val lot_nr: String? = null,
    val location: String? = null,
    val remaining_weight: Float? = null,
    val comment: String? = null,
    val extra: Map<String, Any?>? = null
)

private fun decodePossiblyJsonString(raw: String): String {
    val trimmed = raw.trim()
    return try {
        // val parsed = JsonParser.parseString(trimmed)
        val parsed = JsonParser().parse(trimmed)
        if (parsed.isJsonPrimitive && parsed.asJsonPrimitive.isString) {
            parsed.asString
        } else {
            trimmed
        }
    } catch (_: Exception) {
        trimmed.removePrefix("\"").removeSuffix("\"")
    }
}

fun Map<String, JsonElement>?.stringValue(key: String): String? {
    return this?.get(key)?.takeUnless { it.isJsonNull }?.let { element ->
        when {
            element.isJsonPrimitive && element.asJsonPrimitive.isString -> {
                decodePossiblyJsonString(element.asString)
            }
            element.isJsonPrimitive -> element.asJsonPrimitive.asString
            else -> decodePossiblyJsonString(element.toString())
        }
    }?.takeIf { it.isNotBlank() }
}

fun Map<String, JsonElement>?.floatValue(key: String): Float? {
    return stringValue(key)?.toFloatOrNull()
}
