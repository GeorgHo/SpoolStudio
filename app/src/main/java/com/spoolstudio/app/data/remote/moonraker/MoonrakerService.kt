package com.spoolstudio.app.data.remote.moonraker

import kotlinx.coroutines.delay
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import java.util.concurrent.TimeUnit

data class MoonrakerPrinterInfo(
    val firmwareVersion: String?,
    val moonrakerVersion: String?,
    val softwareVersion: String?,
    val supportsSpoolLink: Boolean,
    val hasSpoolmanComponent: Boolean?,
    val hasSpoolLinkComponent: Boolean?,
    val spoolmanIntegrationEnabled: Boolean?
) {
    val displayVersion: String?
        get() = firmwareVersion ?: softwareVersion
}

data class MoonrakerToolheadStatus(
    val toolheadIndex: Int,
    val spoolId: Int?,
    val isLoaded: Boolean,
    val material: String?,
    val variant: String?,
    val brand: String?,
    val filamentName: String?,
    val colorHex: String?
)

interface MoonrakerApi {
    @GET("server/info")
    suspend fun getServerInfo(): Response<Map<String, @JvmSuppressWildcards Any>>

    @GET("printer/info")
    suspend fun getPrinterInfo(): Response<Map<String, @JvmSuppressWildcards Any>>

    @GET("machine/system_info")
    suspend fun getMachineSystemInfo(): Response<Map<String, @JvmSuppressWildcards Any>>

    @POST("printer/objects/query")
    suspend fun queryPrinterObjects(
        @Body body: Map<String, @JvmSuppressWildcards Any>
    ): Response<Map<String, @JvmSuppressWildcards Any>>

    @POST("printer/gcode/script")
    suspend fun sendGcode(
        @Body body: Map<String, String>
    ): Response<Map<String, @JvmSuppressWildcards Any>>

    @GET("server/spoolman/status")
    suspend fun getSpoolmanStatus(): Response<Map<String, @JvmSuppressWildcards Any>>
}

class MoonrakerService(private val baseUrl: String) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    private val api = Retrofit.Builder()
        .baseUrl(if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(MoonrakerApi::class.java)

    suspend fun getPrinterInfo(): MoonrakerPrinterInfo {
        val printerInfoResult = runCatching {
            api.getPrinterInfo().resultOrThrow("Moonraker printer info query failed")
        }.getOrNull()

        val systemInfoResult = runCatching {
            api.getMachineSystemInfo().resultOrThrow("Moonraker system info query failed")
        }.getOrNull()

        val serverInfoResult = runCatching {
            api.getServerInfo().resultOrThrow("Moonraker server info query failed")
        }.getOrNull()

        val productInfo = (systemInfoResult?.get("system_info") as? Map<*, *>)
            ?.get("product_info") as? Map<*, *>

        val firmwareVersion = productInfo.stringValue("firmware_version")
        val softwareVersion = printerInfoResult.stringValue("software_version")
            ?: productInfo.stringValue("software_version")
        val versionForFeatureGate = firmwareVersion ?: softwareVersion
        val components = moonrakerComponentState(serverInfoResult?.listValue("components"))
        return MoonrakerPrinterInfo(
            firmwareVersion = firmwareVersion,
            moonrakerVersion = serverInfoResult.stringValue("moonraker_version"),
            softwareVersion = softwareVersion,
            supportsSpoolLink = moonrakerVersionAtLeast(versionForFeatureGate, 1, 5, 0),
            hasSpoolmanComponent = components?.hasSpoolman,
            hasSpoolLinkComponent = components?.hasSpoolLink,
            spoolmanIntegrationEnabled = components?.integrationEnabled
        )
    }

    suspend fun getLegacyToolMapping(): Map<String, Int?> {
        val response = api.queryPrinterObjects(
            mapOf(
                "objects" to mapOf(
                    "gcode_macro T0" to listOf("spool_id"),
                    "gcode_macro T1" to listOf("spool_id"),
                    "gcode_macro T2" to listOf("spool_id"),
                    "gcode_macro T3" to listOf("spool_id")
                )
            )
        )

        val status = response.statusOrThrow("Moonraker legacy mapping query failed")

        return mapOf(
            "T0" to extractSpoolId(status["gcode_macro T0"]),
            "T1" to extractSpoolId(status["gcode_macro T1"]),
            "T2" to extractSpoolId(status["gcode_macro T2"]),
            "T3" to extractSpoolId(status["gcode_macro T3"])
        )
    }

    suspend fun getSpoolLinkToolMapping(): Map<String, Int?> {
        val response = api.queryPrinterObjects(
            mapOf(
                "objects" to mapOf(
                    "print_task_config" to listOf("filament_spool_id")
                )
            )
        )

        val status = response.statusOrThrow("Moonraker SpoolLink mapping query failed")
        val config = status["print_task_config"] as? Map<*, *>
            ?: throw IllegalStateException("Moonraker response did not contain print_task_config")
        val spoolIds = when (val raw = config["filament_spool_id"]) {
            null -> emptyList<Any?>()
            is List<*> -> raw
            else -> throw IllegalStateException("Moonraker filament_spool_id had unexpected type")
        }

        return mapOf(
            "T0" to spoolIds.getOrNull(0).toPositiveSpoolIdOrNull(),
            "T1" to spoolIds.getOrNull(1).toPositiveSpoolIdOrNull(),
            "T2" to spoolIds.getOrNull(2).toPositiveSpoolIdOrNull(),
            "T3" to spoolIds.getOrNull(3).toPositiveSpoolIdOrNull()
        )
    }

    suspend fun getSpoolLinkToolheadStatuses(): List<MoonrakerToolheadStatus> {
        val response = api.queryPrinterObjects(
            mapOf(
                "objects" to mapOf(
                    "print_task_config" to listOf(
                        "filament_spool_id",
                        "filament_exist",
                        "filament_vendor",
                        "filament_type",
                        "filament_sub_type",
                        "filament_color_rgba"
                    ),
                    "AFC_lane E0" to listOf(
                        "load",
                        "prep",
                        "tool_loaded",
                        "material",
                        "filament_name",
                        "spool_id",
                        "color",
                        "status"
                    ),
                    "AFC_lane E1" to listOf(
                        "load",
                        "prep",
                        "tool_loaded",
                        "material",
                        "filament_name",
                        "spool_id",
                        "color",
                        "status"
                    ),
                    "AFC_lane E2" to listOf(
                        "load",
                        "prep",
                        "tool_loaded",
                        "material",
                        "filament_name",
                        "spool_id",
                        "color",
                        "status"
                    ),
                    "AFC_lane E3" to listOf(
                        "load",
                        "prep",
                        "tool_loaded",
                        "material",
                        "filament_name",
                        "spool_id",
                        "color",
                        "status"
                    )
                )
            )
        )

        val status = response.statusOrThrow("Moonraker SpoolLink toolhead status query failed")
        val config = status["print_task_config"] as? Map<*, *>

        return (0..3).map { index ->
            val lane = status["AFC_lane E$index"] as? Map<*, *>
            val spoolId = config.listItem("filament_spool_id", index).toPositiveSpoolIdOrNull()
                ?: lane.value("spool_id").toPositiveSpoolIdOrNull()
            val material = config.listItem("filament_type", index).cleanPrinterText()
                ?: lane.value("material").cleanPrinterText()
            val variant = config.listItem("filament_sub_type", index).cleanPrinterText()
            val brand = config.listItem("filament_vendor", index).cleanPrinterText()
            val filamentName = lane.value("filament_name").cleanPrinterText()
            val colorHex = config.listItem("filament_color_rgba", index).toPrinterColorHex()
                ?: lane.value("color").toPrinterColorHex()
            val isLoaded = config.listItem("filament_exist", index).toBooleanLike()
                || lane.value("load").toBooleanLike()
                || lane.value("prep").toBooleanLike()
                || lane.value("tool_loaded").toBooleanLike()
                || material != null
                || filamentName != null
                || spoolId != null

            MoonrakerToolheadStatus(
                toolheadIndex = index,
                spoolId = spoolId,
                isLoaded = isLoaded,
                material = material,
                variant = variant,
                brand = brand,
                filamentName = filamentName,
                colorHex = colorHex
            )
        }
    }

    suspend fun setLegacyToolSpool(tool: String, spoolId: Int?) {
        val value = spoolId ?: 0
        val variableName = "${tool.lowercase()}__spool_id"

        val script = """
            SET_GCODE_VARIABLE MACRO=$tool VARIABLE=spool_id VALUE=$value
            SAVE_VARIABLE VARIABLE=$variableName VALUE=$value
        """.trimIndent()

        val response = api.sendGcode(
            mapOf("script" to script)
        )

        if (!response.isSuccessful) {
            val errorText = response.errorBody()?.string()
            throw IllegalStateException("Moonraker legacy mapping write failed (${response.code()}): $errorText")
        }
    }

    suspend fun setSpoolLinkToolSpool(toolheadIndex: Int, spoolId: Int?): Map<String, Int?> {
        require(toolheadIndex in 0..3) { "Toolhead index must be between 0 and 3" }
        val value = spoolId ?: 0
        sendSpoolLinkAssignmentCommand(toolheadIndex, value)

        val expected = value.takeIf { it > 0 }
        var latestMapping = emptyMap<String, Int?>()
        repeat(8) { attempt ->
            delay(if (attempt == 0) 500 else 850)
            latestMapping = getSpoolLinkToolMapping()
            if (latestMapping["T$toolheadIndex"] == expected) {
                return latestMapping
            }
        }

        if (latestMapping["T$toolheadIndex"] != expected) {
            val actual = latestMapping["T$toolheadIndex"]?.toString() ?: "empty"
            val wanted = expected?.toString() ?: "empty"
            throw IllegalStateException(
                "Toolhead ${toolheadIndex + 1} assignment was not applied. Expected spool $wanted, printer still reports $actual."
            )
        }

        return latestMapping
    }

    private suspend fun sendSpoolLinkAssignmentCommand(toolheadIndex: Int, spoolId: Int) {
        val scripts = spoolLinkAssignmentScripts(toolheadIndex, spoolId)
        val errors = mutableListOf<String>()

        for (script in scripts) {
            val response = try {
                api.sendGcode(mapOf("script" to script))
            } catch (error: Exception) {
                errors += "${script.substringBefore(' ')}: ${error.message ?: error::class.java.simpleName}"
                continue
            }

            if (response.isSuccessful) {
                return
            }

            val errorText = response.errorBody()?.string()?.trim()
            errors += buildString {
                append(script.substringBefore(' '))
                append(" HTTP ")
                append(response.code())
                if (!errorText.isNullOrBlank()) {
                    append(": ")
                    append(errorText.take(180))
                }
            }
        }

        throw IllegalStateException(
            "Moonraker SpoolLink assignment command failed. ${errors.joinToString(" | ")}"
        )
    }

    private fun extractSpoolId(raw: Any?): Int? {
        val map = raw as? Map<*, *> ?: return null
        val value = map["spool_id"] ?: return null

        return value.toPositiveSpoolIdOrNull()
    }

    suspend fun getActiveSpoolId(): Int? {
        val response = api.getSpoolmanStatus()

        if (!response.isSuccessful) {
            val errorText = response.errorBody()?.string()
            throw IllegalStateException(
                "Moonraker active spool query failed (${response.code()}): $errorText"
            )
        }

        val body = response.body()
            ?: throw IllegalStateException("Moonraker active spool response was empty")

        // Robust gegen beide Varianten:
        // 1) { "spool_id": 5, ... }
        // 2) { "result": { "spool_id": 5, ... } }
        val resultMap = body["result"] as? Map<*, *>
        val raw = resultMap?.get("spool_id") ?: body["spool_id"]

        return when (raw) {
            is Number -> raw.toInt()
            is String -> raw.toIntOrNull()
            else -> null
        }?.takeIf { it > 0 }
    }

    suspend fun setActiveSpoolId(spoolId: Int?) {
        val script = if (spoolId != null && spoolId > 0) {
            "SET_ACTIVE_SPOOL ID=$spoolId"
        } else {
            "CLEAR_ACTIVE_SPOOL"
        }

        val response = api.sendGcode(
            mapOf("script" to script)
        )

        if (!response.isSuccessful) {
            val errorText = response.errorBody()?.string()
            throw IllegalStateException("Moonraker active spool write failed (${response.code()}): $errorText")
        }
    }

    private fun Response<Map<String, @JvmSuppressWildcards Any>>.statusOrThrow(
        context: String
    ): Map<*, *> {
        val result = resultOrThrow(context)

        return result["status"] as? Map<*, *>
            ?: throw IllegalStateException("$context: Moonraker response did not contain status")
    }

    private fun Response<Map<String, @JvmSuppressWildcards Any>>.resultOrThrow(
        context: String
    ): Map<*, *> {
        if (!isSuccessful) {
            val errorText = errorBody()?.string()
            throw IllegalStateException("$context (${code()}): $errorText")
        }

        val body = body()
            ?: throw IllegalStateException("$context: Moonraker response was empty")

        val result = body["result"] as? Map<*, *>
            ?: throw IllegalStateException("$context: Moonraker response did not contain result")

        return result
    }

    private fun Any?.toPositiveSpoolIdOrNull(): Int? =
        when (this) {
            is Number -> toInt()
            is String -> toIntOrNull()
            else -> null
        }?.takeIf { it > 0 }

    private fun Map<*, *>?.stringValue(key: String): String? =
        this?.get(key)?.toString()?.trim()?.takeIf { it.isNotBlank() }

    private fun Map<*, *>?.listValue(key: String): List<*>? =
        this?.get(key) as? List<*>

    private fun Map<*, *>?.listItem(key: String, index: Int): Any? =
        (this?.get(key) as? List<*>)?.getOrNull(index)

    private fun Map<*, *>?.value(key: String): Any? =
        this?.get(key)

    private fun Any?.cleanPrinterText(): String? {
        val value = this?.toString()?.trim() ?: return null
        return value.takeIf {
            it.isNotBlank() &&
                !it.equals("NONE", ignoreCase = true) &&
                it != "-"
        }
    }

    private fun Any?.toBooleanLike(): Boolean =
        when (this) {
            is Boolean -> this
            is Number -> toInt() != 0
            is String -> equals("true", ignoreCase = true) || this == "1"
            else -> false
        }

    private fun Any?.toPrinterColorHex(): String? {
        val raw = this?.toString()
            ?: return null
        val normalized = raw
            .trim()
            .removePrefix("#")
            .uppercase()
        val hex = when (normalized.length) {
            8 -> normalized.substring(0, 6)
            6 -> normalized
            else -> return null
        }
        return hex.takeUnless { it == "FFFFFF" }
    }

}

internal data class MoonrakerComponentState(
    val hasSpoolman: Boolean,
    val hasSpoolLink: Boolean
) {
    val integrationEnabled: Boolean
        get() = hasSpoolman && hasSpoolLink
}

internal fun moonrakerComponentState(components: List<*>?): MoonrakerComponentState? {
    val normalized = components
        ?.mapNotNull { it?.toString()?.trim()?.lowercase() }
        ?.toSet()
        ?: return null

    return MoonrakerComponentState(
        hasSpoolman = "spoolman" in normalized,
        hasSpoolLink = "spoollink" in normalized
    )
}

internal fun moonrakerComponentsHaveSpoolmanIntegration(components: List<*>?): Boolean? {
    return moonrakerComponentState(components)?.integrationEnabled
}

internal fun moonrakerVersionAtLeast(
    version: String?,
    minimumMajor: Int,
    minimumMinor: Int,
    minimumPatch: Int
): Boolean {
    val parts = Regex("""(\d+)(?:\.(\d+))?(?:\.(\d+))?""")
        .find(version ?: return false)
        ?.groupValues
        ?.drop(1)
        ?.map { it.toIntOrNull() ?: 0 }
        ?: return false

    val major = parts.getOrElse(0) { 0 }
    val minor = parts.getOrElse(1) { 0 }
    val patch = parts.getOrElse(2) { 0 }

    return when {
        major != minimumMajor -> major > minimumMajor
        minor != minimumMinor -> minor > minimumMinor
        else -> patch >= minimumPatch
    }
}
