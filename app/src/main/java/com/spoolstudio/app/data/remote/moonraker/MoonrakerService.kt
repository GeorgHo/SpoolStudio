package com.spoolstudio.app.data.remote.moonraker

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import java.util.concurrent.TimeUnit

interface MoonrakerApi {

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

    suspend fun getToolMapping(): Map<String, Int?> {
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

        if (!response.isSuccessful) {
            val errorText = response.errorBody()?.string()
            throw IllegalStateException("Moonraker mapping query failed (${response.code()}): $errorText")
        }

        val body = response.body()
            ?: throw IllegalStateException("Moonraker mapping response was empty")

        val result = body["result"] as? Map<*, *>
            ?: throw IllegalStateException("Moonraker response did not contain result")

        val status = result["status"] as? Map<*, *>
            ?: throw IllegalStateException("Moonraker response did not contain status")

        return mapOf(
            "T0" to extractSpoolId(status["gcode_macro T0"]),
            "T1" to extractSpoolId(status["gcode_macro T1"]),
            "T2" to extractSpoolId(status["gcode_macro T2"]),
            "T3" to extractSpoolId(status["gcode_macro T3"])
        )
    }

    suspend fun setToolSpool(tool: String, spoolId: Int?) {
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
            throw IllegalStateException("Moonraker mapping write failed (${response.code()}): $errorText")
        }
    }

    private fun extractSpoolId(raw: Any?): Int? {
        val map = raw as? Map<*, *> ?: return null
        val value = map["spool_id"] ?: return null

        return when (value) {
            is Number -> value.toInt()
            is String -> value.toIntOrNull()
            else -> null
        }
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
}