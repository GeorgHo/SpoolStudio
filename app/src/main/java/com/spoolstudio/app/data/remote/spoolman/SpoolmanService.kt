package com.spoolstudio.app.data.remote.spoolman

import android.util.Log
import com.google.gson.Gson
import com.spoolstudio.app.domain.models.CreateFilamentRequest
import com.spoolstudio.app.domain.models.CreateSpoolRequest
import com.spoolstudio.app.domain.models.CreateVendorRequest
import com.spoolstudio.app.domain.models.FilamentSpool
import com.spoolstudio.app.domain.models.SpoolmanFilament
import com.spoolstudio.app.domain.models.SpoolmanSpool
import com.spoolstudio.app.domain.models.SpoolmanVendor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface SpoolmanApi {
    @GET("api/v1/spool")
    suspend fun getSpools(
        @Query("limit") limit: Int,
        @Query("offset") offset: Int = 0,
        @Query("sort") sort: String? = null
    ): Response<List<SpoolmanSpool>>

    @GET("api/v1/spool/{id}")
    suspend fun getSpool(@Path("id") id: Int): Response<SpoolmanSpool>

    @GET("api/v1/vendor")
    suspend fun getVendors(
        @Query("limit") limit: Int,
        @Query("offset") offset: Int = 0,
        @Query("sort") sort: String? = null
    ): Response<List<SpoolmanVendor>>

    @GET("api/v1/filament")
    suspend fun getFilaments(
        @Query("limit") limit: Int,
        @Query("offset") offset: Int = 0,
        @Query("sort") sort: String? = null
    ): Response<List<SpoolmanFilament>>

    @POST("api/v1/vendor")
    suspend fun createVendor(@Body request: CreateVendorRequest): Response<SpoolmanVendor>

    @POST("api/v1/filament")
    suspend fun createFilament(@Body request: CreateFilamentRequest): Response<SpoolmanFilament>

    @PATCH("api/v1/filament/{id}")
    suspend fun updateFilament(
        @Path("id") id: Int,
        @Body request: Map<String, @JvmSuppressWildcards Any?>
    ): Response<SpoolmanFilament>

    @POST("api/v1/spool")
    suspend fun createSpool(@Body request: CreateSpoolRequest): Response<SpoolmanSpool>

    @PATCH("api/v1/spool/{id}")
    suspend fun updateSpool(
        @Path("id") id: Int,
        @Body request: Map<String, @JvmSuppressWildcards Any?>
    ): Response<SpoolmanSpool>
}

data class SpoolmanCatalog(
    val spools: List<FilamentSpool>,
    val vendorNames: List<String>,
    val materialNames: List<String>,
    val variantNames: List<String>,
    val locationNames: List<String>
)

class SpoolmanService(private val baseUrl: String) {
    private var cachedCatalog: SpoolmanCatalog? = null
    private var lastFetchTime = 0L
    private val cacheValidityMs = 30_000L
    private val gson = Gson()

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
        .create(SpoolmanApi::class.java)

    companion object {
        private const val PAGE_SIZE = 100
    }

    private fun normalizeText(value: String?): String = value?.trim().orEmpty()
    private fun normalizeUpper(value: String?): String = normalizeText(value).uppercase()
    private fun normalizeHex(value: String?): String? =
        normalizeText(value).removePrefix("#").ifBlank { null }?.uppercase()

    suspend fun getCatalog(sortBy: String? = null, forceRefresh: Boolean = false): SpoolmanCatalog {
        val now = System.currentTimeMillis()
        if (!forceRefresh) {
            cachedCatalog?.let { cached ->
                if (now - lastFetchTime < cacheValidityMs) return cached
            }
        }

        val spools = fetchAllSpools(sortBy)
        val vendors = fetchAllVendors()
        val filaments = fetchAllFilaments()

        val vendorNames = (vendors.map { it.name } + spools.map { it.brand })
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()

        val materialNames = (filaments.map { FilamentSpool.splitMaterialAndVariant(it.material).first } + spools.map { it.material })
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()

        val variantNames = (filaments.mapNotNull { FilamentSpool.splitMaterialAndVariant(it.material).second } + spools.map { it.variant })
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()

        val locationNames = spools.mapNotNull { it.location }
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()

        return SpoolmanCatalog(
            spools = spools,
            vendorNames = vendorNames,
            materialNames = materialNames,
            variantNames = variantNames,
            locationNames = locationNames
        ).also {
            cachedCatalog = it
            lastFetchTime = now
        }
    }

    suspend fun getFilaments(sortBy: String? = null, forceRefresh: Boolean = false): List<FilamentSpool> {
        return getCatalog(sortBy, forceRefresh).spools
    }

    private suspend fun fetchAllSpools(sortBy: String? = null): List<FilamentSpool> {
        val allSpools = mutableListOf<FilamentSpool>()
        var offset = 0
        while (true) {
            Log.d("SpoolmanService", "Fetching spools: offset=$offset, limit=$PAGE_SIZE, sort=$sortBy")
            val response = api.getSpools(PAGE_SIZE, offset, sortBy)
            if (!response.isSuccessful) break
            val batch = response.body()?.map { FilamentSpool.fromSpoolman(it) } ?: emptyList()
            allSpools.addAll(batch)
            if (batch.size < PAGE_SIZE) break
            offset += PAGE_SIZE
        }
        return allSpools
    }

    private suspend fun fetchAllVendors(): List<SpoolmanVendor> {
        val vendors = mutableListOf<SpoolmanVendor>()
        var offset = 0
        while (true) {
            val response = api.getVendors(PAGE_SIZE, offset, "name:asc")
            if (!response.isSuccessful) break
            val batch = response.body().orEmpty()
            vendors.addAll(batch)
            if (batch.size < PAGE_SIZE) break
            offset += PAGE_SIZE
        }
        return vendors
    }

    private suspend fun fetchAllFilaments(): List<SpoolmanFilament> {
        val filaments = mutableListOf<SpoolmanFilament>()
        var offset = 0
        while (true) {
            val response = api.getFilaments(PAGE_SIZE, offset, "name:asc")
            if (!response.isSuccessful) break
            val batch = response.body().orEmpty()
            filaments.addAll(batch)
            if (batch.size < PAGE_SIZE) break
            offset += PAGE_SIZE
        }
        return filaments
    }

    suspend fun countSpoolsUsingFilament(filamentId: Int): Int {
        var count = 0
        var offset = 0
        while (true) {
            val response = api.getSpools(PAGE_SIZE, offset, null)
            if (!response.isSuccessful) break
            val batch = response.body().orEmpty()
            count += batch.count { it.filament.id == filamentId }
            if (batch.size < PAGE_SIZE) break
            offset += PAGE_SIZE
        }
        return count
    }

    suspend fun findFilamentBySpoolId(spoolId: String): FilamentSpool? {
        val id = spoolId.toIntOrNull() ?: return null
        cachedCatalog?.spools?.find { it.id == id }?.let { return it }
        return try {
            val response = api.getSpool(id)
            if (response.isSuccessful) response.body()?.let { FilamentSpool.fromSpoolman(it) } else null
        } catch (_: Exception) {
            null
        }
    }

    suspend fun findSpoolByLotNr(lotNr: String, forceRefresh: Boolean = false): FilamentSpool? {
        val normalizedLotNr = lotNr.trim()
        if (normalizedLotNr.isBlank()) return null

        return getCatalog(forceRefresh = forceRefresh).spools.firstOrNull { spool ->
            spool.lotNr?.trim()?.equals(normalizedLotNr, ignoreCase = false) == true
        }
    }

    suspend fun existsLotNr(lotNr: String, forceRefresh: Boolean = false): Boolean {
        return findSpoolByLotNr(lotNr, forceRefresh) != null
    }

    suspend fun createOrFindVendor(name: String): SpoolmanVendor {
        val normalizedName = normalizeText(name)
        val existing = fetchAllVendors().firstOrNull { it.name.equals(normalizedName, ignoreCase = true) }
        if (existing != null) return existing

        val response = api.createVendor(
            CreateVendorRequest(
                name = normalizedName,
                comment = "Spool Studio",
                empty_spool_weight = 180f
            )
        )
        if (!response.isSuccessful) {
            val errorText = response.errorBody()?.string()
            throw IllegalStateException("Vendor could not be created (${response.code()}): $errorText")
        }
        cachedCatalog = null
        return response.body() ?: throw IllegalStateException("Vendor response was empty")
    }

    suspend fun createOrFindFilament(
        name: String,
        material: String,
        vendorId: Int,
        colorHex: String?,
        nozzleTemp: Int?,
        bedTemp: Int?
    ): SpoolmanFilament {
        val normalizedName = normalizeText(name)
        val normalizedMaterial = normalizeText(material)
        val normalizedColorHex = normalizeHex(colorHex)

        val existing = fetchAllFilaments().firstOrNull { filament ->
            filament.vendor?.id == vendorId &&
                normalizeText(filament.material).equals(normalizedMaterial, ignoreCase = true) &&
                normalizeHex(filament.color_hex) == normalizedColorHex &&
                normalizeText(filament.name).equals(normalizedName, ignoreCase = true)
        }
        if (existing != null) return existing

        val request = CreateFilamentRequest(
            name = normalizedName.ifBlank { normalizedColorHex ?: "Unknown" },
            material = normalizedMaterial,
            vendor_id = vendorId,
            color_hex = normalizedColorHex,
            settings_extruder_temp = nozzleTemp,
            settings_bed_temp = bedTemp,
            density = defaultDensity(FilamentSpool.splitMaterialAndVariant(normalizedMaterial).first),
            diameter = 1.75f,
            weight = 1000f,
            price = 0.0f,
            comment = "Spool Studio",
            extra = null
        )
        val response = api.createFilament(request)
        if (!response.isSuccessful) {
            val errorText = response.errorBody()?.string()
            throw IllegalStateException("Filament could not be created (${response.code()}): $errorText")
        }
        cachedCatalog = null
        return response.body() ?: throw IllegalStateException("Filament response was empty")
    }

    suspend fun updateFilament(
        id: Int,
        name: String,
        material: String,
        vendorId: Int,
        colorHex: String?,
        nozzleTemp: Int?,
        bedTemp: Int?
    ): SpoolmanFilament {
        val request = mutableMapOf<String, Any?>()
        request["name"] = normalizeText(name).ifBlank { "Unknown" }
        request["material"] = normalizeText(material)
        request["vendor_id"] = vendorId
        request["color_hex"] = normalizeHex(colorHex)
        request["settings_extruder_temp"] = nozzleTemp
        request["settings_bed_temp"] = bedTemp
        request["comment"] = "Spool Studio"

        val response = api.updateFilament(id, request)
        if (!response.isSuccessful) {
            val errorText = response.errorBody()?.string()
            throw IllegalStateException("Filament could not be updated (${response.code()}): $errorText")
        }
        cachedCatalog = null
        return response.body() ?: throw IllegalStateException("Filament update response was empty")
    }

    suspend fun createSpool(
        filamentId: Int,
        lotNr: String?,
        location: String? = null,
        remainingWeight: Float? = null,
        comment: String? = null
    ): SpoolmanSpool {
        val response = api.createSpool(
            CreateSpoolRequest(
                filament_id = filamentId,
                lot_nr = lotNr,
                location = location,
                remaining_weight = remainingWeight ?: 1000f,
                comment = comment ?: "Created by Spool Studio",
                extra = null
            )
        )
        if (!response.isSuccessful) {
            val errorText = response.errorBody()?.string()
            throw IllegalStateException("Spool could not be created (${response.code()}): $errorText")
        }
        cachedCatalog = null
        return response.body() ?: throw IllegalStateException("Spool response was empty")
    }

    suspend fun updateSpool(
        id: Int,
        filamentId: Int? = null,
        lotNr: String? = null,
        location: String? = null,
        remainingWeight: Float? = null,
        comment: String? = null
    ): SpoolmanSpool {
        val request = mutableMapOf<String, Any?>()
        filamentId?.let { request["filament_id"] = it }
        request["lot_nr"] = lotNr
        location?.let { request["location"] = it }
        remainingWeight?.let { request["remaining_weight"] = it }
        request["comment"] = comment ?: "Created by Spool Studio"

        val response = api.updateSpool(id, request)
        if (!response.isSuccessful) {
            val errorText = response.errorBody()?.string()
            throw IllegalStateException("Spool could not be updated (${response.code()}): $errorText")
        }
        cachedCatalog = null
        return response.body() ?: throw IllegalStateException("Spool update response was empty")
    }

    private fun defaultDensity(material: String): Float {
        return when (material.uppercase()) {
            "PLA" -> 1.24f
            "ABS" -> 1.04f
            "PETG" -> 1.27f
            "TPU" -> 1.21f
            "ASA" -> 1.05f
            "PC" -> 1.20f
            "NYLON" -> 1.14f
            "PVA" -> 1.23f
            "HIPS" -> 1.03f
            else -> 1.24f
        }
    }
}
