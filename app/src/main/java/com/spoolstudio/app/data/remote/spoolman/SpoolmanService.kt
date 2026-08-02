package com.spoolstudio.app.data.remote.spoolman

import android.util.Log
import com.google.gson.JsonElement
import com.spoolstudio.app.domain.models.CreateFilamentRequest
import com.spoolstudio.app.domain.models.CreateSpoolRequest
import com.spoolstudio.app.domain.models.CreateVendorRequest
import com.spoolstudio.app.domain.models.FilamentSpool
import com.spoolstudio.app.domain.models.SpoolmanExtraField
import com.spoolstudio.app.domain.models.SpoolmanExtraFieldRequest
import com.spoolstudio.app.domain.models.SpoolmanFilament
import com.spoolstudio.app.domain.models.SpoolmanSpool
import com.spoolstudio.app.domain.models.SpoolmanVendor
import com.spoolstudio.app.domain.models.SpoolStudioFilamentFields
import com.spoolstudio.app.domain.models.UpdateVendorRequest
import com.spoolstudio.app.domain.models.encodeSpoolmanExtraString
import com.spoolstudio.app.domain.models.formatCardUidsForSpoolman
import com.spoolstudio.app.domain.models.normalizeCardUid
import com.spoolstudio.app.domain.models.normalizeSpoolLinkFilamentFields
import com.spoolstudio.app.domain.models.parseCardUids
import com.spoolstudio.app.domain.models.spoolLinkSpoolmanFields
import com.spoolstudio.app.domain.models.splitLegacyMaterialAndVariant
import com.spoolstudio.app.domain.models.stringValue
import com.spoolstudio.app.domain.models.toRequestExtraMap
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
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
        @Query("sort") sort: String? = null,
        @Query("allow_archived") allowArchived: Boolean? = null
    ): Response<List<SpoolmanSpool>>

    @GET("api/v1/spool/{id}")
    suspend fun getSpool(@Path("id") id: Int): Response<SpoolmanSpool>

    @GET("api/v1/filament/{id}")
    suspend fun getFilament(@Path("id") id: Int): Response<SpoolmanFilament>

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

    @GET("api/v1/field/{entityType}")
    suspend fun getExtraFields(
        @Path("entityType") entityType: String
    ): Response<List<SpoolmanExtraField>>

    @POST("api/v1/field/{entityType}/{key}")
    suspend fun addOrUpdateExtraField(
        @Path("entityType") entityType: String,
        @Path("key") key: String,
        @Body request: SpoolmanExtraFieldRequest
    ): Response<ResponseBody>

    @POST("api/v1/vendor")
    suspend fun createVendor(@Body request: CreateVendorRequest): Response<SpoolmanVendor>

    @PATCH("api/v1/vendor/{id}")
    suspend fun updateVendor(
        @Path("id") id: Int,
        @Body request: UpdateVendorRequest
    ): Response<SpoolmanVendor>

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

    @DELETE("api/v1/spool/{id}")
    suspend fun deleteSpool(@Path("id") id: Int): Response<ResponseBody>
}

data class SpoolmanCatalog(
    val spools: List<FilamentSpool>,
    val vendorNames: List<String>,
    val materialNames: List<String>,
    val variantNames: List<String>,
    val locationNames: List<String>,
    val cardUidFieldSpoolCount: Int = 0,
    val cardUidFieldKeys: List<String> = emptyList(),
    val materialModifierFieldAvailable: Boolean = false
)

data class SpoolmanLegacyFilamentConversion(
    val filamentId: Int,
    val name: String,
    val vendorName: String,
    val spoolIds: List<Int>,
    val currentMaterial: String,
    val currentVariant: String,
    val currentMaterialModifier: String,
    val targetMaterial: String,
    val targetVariant: String,
    val targetMaterialModifier: String
) {
    val affectedSpoolCount: Int
        get() = spoolIds.size

    val currentLabel: String
        get() {
            val (_, embeddedVariant) = splitLegacyMaterialAndVariant(currentMaterial)
            return buildList {
                add(currentMaterial)
                currentMaterialModifier.takeIf { it.isNotBlank() }?.let { add(it) }
                currentVariant
                    .takeIf { it.isNotBlank() }
                    .takeUnless { it.equals("Basic", ignoreCase = true) }
                    .takeUnless { it.equals(embeddedVariant, ignoreCase = true) }
                    ?.let { add(it) }
            }.joinToString(" / ")
        }

    val targetLabel: String
        get() = buildList {
            add(targetMaterial)
            targetMaterialModifier.takeIf { it.isNotBlank() }?.let { add(it) }
            targetVariant.takeIf { it.isNotBlank() }?.let { add(it) }
        }.joinToString(" / ")
}

class SpoolmanService(private val baseUrl: String) {
    private var cachedCatalog: SpoolmanCatalog? = null
    private var lastFetchTime = 0L
    private val cacheValidityMs = 30_000L
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
        private const val SPOOL_ENTITY_TYPE = "spool"
        private const val FILAMENT_ENTITY_TYPE = "filament"
        private const val CARD_UIDS_FIELD_KEY = "card_uids"
        private const val VARIANT_FIELD_KEY = "variant"
        const val MATERIAL_MODIFIER_FIELD_KEY = "material_modifier"
    }

    private fun normalizeText(value: String?): String = value?.trim().orEmpty()
    private fun normalizeUpper(value: String?): String = normalizeText(value).uppercase()
    private fun normalizeHex(value: String?): String? =
        normalizeText(value).removePrefix("#").ifBlank { null }?.uppercase()

    private fun weightsEqual(first: Float?, second: Float?): Boolean =
        (first == null && second == null) ||
            (first != null && second != null && kotlin.math.abs(first - second) <= 0.01f)

    suspend fun getCatalog(sortBy: String? = null, forceRefresh: Boolean = false): SpoolmanCatalog {
        val now = System.currentTimeMillis()
        if (!forceRefresh) {
            cachedCatalog?.let { cached ->
                if (now - lastFetchTime < cacheValidityMs) return cached
            }
        }

        val spoolFetchResult = fetchAllSpools(sortBy)
        val spools = spoolFetchResult.spools
        val vendors = fetchAllVendors()
        val filaments = fetchAllFilaments()
        val filamentExtraFields = fetchExtraFields(FILAMENT_ENTITY_TYPE)
        val cardUidFieldKeys = spoolFetchResult.rawSpools
            .flatMap { it.extra.cardUidKeys() }
            .distinct()
            .sorted()

        val vendorNames = (vendors.map { it.name } + spools.map { it.brand })
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()

        val materialNames = (filaments.map { spoolLinkFields(it).material } + spools.map { it.material })
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()

        val variantNames = (filaments.map { spoolLinkFields(it).variant } + spools.map { it.variant })
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
            locationNames = locationNames,
            cardUidFieldSpoolCount = spoolFetchResult.rawSpools.count { it.extra.cardUidKeys().isNotEmpty() },
            cardUidFieldKeys = cardUidFieldKeys,
            materialModifierFieldAvailable = hasMaterialModifierField(filamentExtraFields)
        ).also {
            cachedCatalog = it
            lastFetchTime = now
        }
    }

    suspend fun hasFilamentMaterialModifierField(): Boolean =
        hasMaterialModifierField(fetchExtraFields(FILAMENT_ENTITY_TYPE))

    suspend fun createFilamentMaterialModifierField(): Boolean {
        ensureExtraTextField(
            entityType = FILAMENT_ENTITY_TYPE,
            key = MATERIAL_MODIFIER_FIELD_KEY,
            name = "Material modifier",
            order = 20,
            errorContext = "Material modifier field"
        )
        cachedCatalog = null
        return hasFilamentMaterialModifierField()
    }

    suspend fun ensurePaxx12SpoolLinkFields() {
        ensureExtraTextField(
            entityType = SPOOL_ENTITY_TYPE,
            key = CARD_UIDS_FIELD_KEY,
            name = "Card UIDs",
            order = 1,
            defaultValue = encodeSpoolmanExtraString(""),
            errorContext = "Paxx12 Card UIDs field"
        )
        ensureExtraTextField(
            entityType = FILAMENT_ENTITY_TYPE,
            key = VARIANT_FIELD_KEY,
            name = "Variant",
            order = 1,
            defaultValue = encodeSpoolmanExtraString(""),
            errorContext = "Paxx12 Variant field"
        )
    }

    suspend fun findLegacyFilamentConversions(): List<SpoolmanLegacyFilamentConversion> {
        val rawSpools = fetchAllRawSpools(includeArchived = true)
        return rawSpools
            .groupBy { it.filament.id }
            .mapNotNull { (_, spools) ->
                val filament = spools.firstOrNull()?.filament ?: return@mapNotNull null
                buildLegacyFilamentConversion(
                    filament = filament,
                    spoolIds = spools.mapNotNull { it.id }.distinct().sorted()
                )
            }
            .sortedWith(compareBy<SpoolmanLegacyFilamentConversion> { it.vendorName.lowercase() }
                .thenBy { it.name.lowercase() }
                .thenBy { it.filamentId })
    }

    suspend fun convertLegacyFilaments(filamentIds: Set<Int>): Int {
        if (filamentIds.isEmpty()) return 0
        val candidates = findLegacyFilamentConversions()
            .filter { it.filamentId in filamentIds }
        if (candidates.isEmpty()) return 0

        ensurePaxx12SpoolLinkFields()
        if (candidates.any { it.targetMaterialModifier.isNotBlank() }) {
            createFilamentMaterialModifierField()
        }

        candidates.forEach { candidate ->
            val response = api.getFilament(candidate.filamentId)
            if (!response.isSuccessful) {
                val errorText = response.errorBody()?.string()
                throw IllegalStateException(
                    "Filament ${candidate.filamentId} could not be loaded for conversion (${response.code()}): $errorText"
                )
            }
            val filament = response.body()
                ?: throw IllegalStateException("Filament ${candidate.filamentId} conversion response was empty")
            val extra = filament.extra.toRequestExtraMap()
            extra[VARIANT_FIELD_KEY] = encodeSpoolmanExtraString(candidate.targetVariant.ifBlank { "Basic" })
            if (candidate.targetMaterialModifier.isBlank()) {
                extra.remove(MATERIAL_MODIFIER_FIELD_KEY)
            } else {
                extra[MATERIAL_MODIFIER_FIELD_KEY] = encodeSpoolmanExtraString(candidate.targetMaterialModifier)
            }

            val updateResponse = api.updateFilament(
                candidate.filamentId,
                mapOf(
                    "material" to candidate.targetMaterial,
                    "extra" to extra
                )
            )
            if (!updateResponse.isSuccessful) {
                val errorText = updateResponse.errorBody()?.string()
                throw IllegalStateException(
                    "Filament ${candidate.filamentId} could not be converted (${updateResponse.code()}): $errorText"
                )
            }
        }

        cachedCatalog = null
        return candidates.size
    }

    private fun buildLegacyFilamentConversion(
        filament: SpoolmanFilament,
        spoolIds: List<Int>
    ): SpoolmanLegacyFilamentConversion? {
        val rawMaterial = normalizeText(filament.material)
        if (rawMaterial.isBlank()) return null

        val (legacyMaterial, legacyVariant) = splitLegacyMaterialAndVariant(rawMaterial)
        val currentVariant = filament.extra.stringValue(VARIANT_FIELD_KEY)
            ?: legacyVariant.ifBlank { "Basic" }
        val currentModifier = filament.extra.stringValue(MATERIAL_MODIFIER_FIELD_KEY).orEmpty()
        val fields = spoolLinkSpoolmanFields(
            material = legacyMaterial,
            variant = currentVariant,
            materialModifier = currentModifier,
            allowMaterialModifier = true
        )

        val needsConversion =
            !rawMaterial.equals(fields.material, ignoreCase = true) ||
                !currentVariant.equals(fields.variant, ignoreCase = true) ||
                !currentModifier.equals(fields.materialModifier, ignoreCase = true)

        if (!needsConversion) return null

        return SpoolmanLegacyFilamentConversion(
            filamentId = filament.id,
            name = filament.name.ifBlank { "Filament #${filament.id}" },
            vendorName = filament.vendor?.name.orEmpty().ifBlank { "Unknown brand" },
            spoolIds = spoolIds,
            currentMaterial = rawMaterial,
            currentVariant = currentVariant,
            currentMaterialModifier = currentModifier,
            targetMaterial = fields.material,
            targetVariant = fields.variant.ifBlank { "Basic" },
            targetMaterialModifier = fields.materialModifier
        )
    }

    private suspend fun fetchExtraFields(entityType: String): List<SpoolmanExtraField> {
        return try {
            val response = api.getExtraFields(entityType)
            if (response.isSuccessful) response.body().orEmpty() else emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    private suspend fun ensureExtraTextField(
        entityType: String,
        key: String,
        name: String,
        order: Int,
        defaultValue: String? = null,
        errorContext: String
    ) {
        val fields = fetchExtraFields(entityType)
        if (fields.any { field ->
                field.key.equals(key, ignoreCase = true) &&
                    field.field_type.equals("text", ignoreCase = true)
            }
        ) {
            return
        }

        val response = api.addOrUpdateExtraField(
            entityType = entityType,
            key = key,
            request = SpoolmanExtraFieldRequest(
                name = name,
                order = order,
                field_type = "text",
                default_value = defaultValue
            )
        )
        if (!response.isSuccessful) {
            val errorText = response.errorBody()?.string()
            throw IllegalStateException("$errorContext could not be created (${response.code()}): $errorText")
        }
        cachedCatalog = null
    }

    private fun hasMaterialModifierField(fields: List<SpoolmanExtraField>): Boolean =
        fields.any { field ->
            field.key.equals(MATERIAL_MODIFIER_FIELD_KEY, ignoreCase = true) &&
                field.field_type.equals("text", ignoreCase = true)
        }

    suspend fun getFilaments(sortBy: String? = null, forceRefresh: Boolean = false): List<FilamentSpool> {
        return getCatalog(sortBy, forceRefresh).spools
    }

    private data class SpoolFetchResult(
        val rawSpools: List<SpoolmanSpool>,
        val spools: List<FilamentSpool>
    )

    private suspend fun fetchAllSpools(sortBy: String? = null): SpoolFetchResult {
        val rawSpools = mutableListOf<SpoolmanSpool>()
        val allSpools = mutableListOf<FilamentSpool>()
        var offset = 0
        while (true) {
            Log.d("SpoolmanService", "Fetching spools: offset=$offset, limit=$PAGE_SIZE, sort=$sortBy")
            val response = api.getSpools(PAGE_SIZE, offset, sortBy)
            if (!response.isSuccessful) break
            val rawBatch = response.body().orEmpty()
            val batch = rawBatch.map { FilamentSpool.fromSpoolman(it) }
            rawSpools.addAll(rawBatch)
            allSpools.addAll(batch)
            if (batch.size < PAGE_SIZE) break
            offset += PAGE_SIZE
        }
        return SpoolFetchResult(rawSpools = rawSpools, spools = allSpools)
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

    private fun spoolLinkFields(filament: SpoolmanFilament): SpoolStudioFilamentFields {
        val normalized = normalizeSpoolLinkFilamentFields(
            material = filament.material,
            variant = filament.extra.stringValue("variant")
        )
        return spoolLinkSpoolmanFields(
            material = normalized.material,
            variant = normalized.variant,
            materialModifier = filament.extra.stringValue("material_modifier")
        )
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

    suspend fun createOrFindVendor(name: String, emptySpoolWeight: Float? = null): SpoolmanVendor {
        val normalizedName = normalizeText(name)
        val existing = fetchAllVendors().firstOrNull { it.name.equals(normalizedName, ignoreCase = true) }
        if (existing != null) {
            val id = existing.id
            return if (
                id != null &&
                emptySpoolWeight != null &&
                !weightsEqual(existing.empty_spool_weight, emptySpoolWeight)
            ) {
                updateVendorEmptySpoolWeight(existing, emptySpoolWeight)
            } else {
                existing
            }
        }

        val response = api.createVendor(
            CreateVendorRequest(
                name = normalizedName,
                comment = "Spool Studio",
                empty_spool_weight = emptySpoolWeight ?: 180f
            )
        )
        if (!response.isSuccessful) {
            val errorText = response.errorBody()?.string()
            throw IllegalStateException("Vendor could not be created (${response.code()}): $errorText")
        }
        cachedCatalog = null
        return response.body() ?: throw IllegalStateException("Vendor response was empty")
    }

    private suspend fun updateVendorEmptySpoolWeight(
        vendor: SpoolmanVendor,
        emptySpoolWeight: Float
    ): SpoolmanVendor {
        val id = vendor.id ?: return vendor
        val response = api.updateVendor(
            id,
            UpdateVendorRequest(
                name = vendor.name,
                comment = vendor.comment,
                empty_spool_weight = emptySpoolWeight
            )
        )
        if (!response.isSuccessful) {
            Log.w("SpoolStudio", "Vendor empty spool weight update failed (${response.code()})")
            return vendor
        }
        cachedCatalog = null
        return response.body() ?: vendor.copy(empty_spool_weight = emptySpoolWeight)
    }

    suspend fun createOrFindFilament(
        name: String,
        material: String,
        variant: String,
        materialModifier: String = "",
        allowMaterialModifier: Boolean = true,
        vendorId: Int,
        colorHex: String?,
        nozzleTemp: Int?,
        bedTemp: Int?,
        spoolWeight: Float? = null
    ): SpoolmanFilament {
        val normalizedName = normalizeText(name)
        val fields = spoolLinkSpoolmanFields(
            material = material,
            variant = variant,
            materialModifier = materialModifier,
            allowMaterialModifier = allowMaterialModifier
        )
        val normalizedMaterial = fields.material
        val normalizedVariant = fields.variant
        val normalizedMaterialModifier = fields.materialModifier
        val normalizedColorHex = normalizeHex(colorHex)

        val existing = fetchAllFilaments().firstOrNull { filament ->
            val existingFields = spoolLinkFields(filament)
            filament.vendor?.id == vendorId &&
                existingFields.material.equals(normalizedMaterial, ignoreCase = true) &&
                existingFields.variant.equals(normalizedVariant, ignoreCase = true) &&
                existingFields.materialModifier.equals(normalizedMaterialModifier, ignoreCase = true) &&
                normalizeHex(filament.color_hex) == normalizedColorHex &&
                normalizeText(filament.name).equals(normalizedName, ignoreCase = true)
        }
        if (existing != null) {
            return if (spoolWeight == null || weightsEqual(existing.spool_weight, spoolWeight)) {
                existing
            } else {
                updateFilament(
                    id = existing.id,
                    name = normalizedName,
                    material = normalizedMaterial,
                    variant = normalizedVariant,
                    materialModifier = normalizedMaterialModifier,
                    allowMaterialModifier = allowMaterialModifier,
                    vendorId = vendorId,
                    colorHex = normalizedColorHex,
                    nozzleTemp = nozzleTemp,
                    bedTemp = bedTemp,
                    spoolWeight = spoolWeight
                )
            }
        }

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
            spool_weight = spoolWeight,
            price = 0.0f,
            comment = "Spool Studio",
            extra = buildFilamentExtra(normalizedVariant, normalizedMaterialModifier)
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
        variant: String,
        materialModifier: String = "",
        allowMaterialModifier: Boolean = true,
        vendorId: Int,
        colorHex: String?,
        nozzleTemp: Int?,
        bedTemp: Int?,
        spoolWeight: Float? = null
    ): SpoolmanFilament {
        val request = mutableMapOf<String, Any?>()
        val fields = spoolLinkSpoolmanFields(
            material = material,
            variant = variant,
            materialModifier = materialModifier,
            allowMaterialModifier = allowMaterialModifier
        )
        val existingExtra = try {
            val existingResponse = api.getFilament(id)
            if (existingResponse.isSuccessful) {
                existingResponse.body()?.extra.toRequestExtraMap()
            } else {
                mutableMapOf()
            }
        } catch (_: Exception) {
            mutableMapOf()
        }
        request["name"] = normalizeText(name).ifBlank { "Unknown" }
        request["material"] = fields.material
        request["vendor_id"] = vendorId
        request["color_hex"] = normalizeHex(colorHex)
        request["settings_extruder_temp"] = nozzleTemp
        request["settings_bed_temp"] = bedTemp
        spoolWeight?.let { request["spool_weight"] = it }
        request["comment"] = "Spool Studio"
        existingExtra["variant"] = encodeSpoolmanExtraString(fields.variant)
        if (fields.materialModifier.isBlank()) {
            existingExtra.remove("material_modifier")
        } else {
            existingExtra["material_modifier"] = encodeSpoolmanExtraString(fields.materialModifier)
        }
        request["extra"] = existingExtra

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

    suspend fun findSpoolByCardUid(cardUid: String, forceRefresh: Boolean = false): FilamentSpool? {
        val normalizedUid = normalizeCardUid(cardUid)
        if (normalizedUid.isBlank()) return null

        return getCatalog(forceRefresh = forceRefresh).spools.firstOrNull { spool ->
            spool.cardUids.any { it.equals(normalizedUid, ignoreCase = true) }
        }
    }

    suspend fun assignCardUidToSpool(spoolId: Int, cardUid: String) {
        val normalizedUid = normalizeCardUid(cardUid)
        if (normalizedUid.isBlank()) return
        ensurePaxx12SpoolLinkFields()

        val targetResponse = api.getSpool(spoolId)
        if (!targetResponse.isSuccessful) {
            val errorText = targetResponse.errorBody()?.string()
            throw IllegalStateException("Spool could not be loaded for card UID update (${targetResponse.code()}): $errorText")
        }

        var changed = false
        fetchAllRawSpools(includeArchived = true).forEach { spool ->
            val id = spool.id ?: return@forEach
            val currentUids = parseCardUids(spool.extra.stringValue("card_uids") ?: spool.extra.stringValue("card_uid"))
            val nextUids = if (id == spoolId) {
                (currentUids + normalizedUid).distinct()
            } else {
                currentUids.filterNot { it.equals(normalizedUid, ignoreCase = true) }
            }

            if (nextUids != currentUids) {
                val extra = spool.extra.toRequestExtraMap()
                extra.remove("card_uid")
                extra["card_uids"] = encodeSpoolmanExtraString(formatCardUidsForSpoolman(nextUids))

                val response = api.updateSpool(id, mapOf("extra" to extra))
                if (!response.isSuccessful) {
                    val errorText = response.errorBody()?.string()
                    throw IllegalStateException("Card UID could not be assigned (${response.code()}): $errorText")
                }
                changed = true
            }
        }

        if (changed) cachedCatalog = null
    }

    private suspend fun fetchAllRawSpools(includeArchived: Boolean = false): List<SpoolmanSpool> {
        val spools = mutableListOf<SpoolmanSpool>()
        var offset = 0
        while (true) {
            val response = api.getSpools(
                limit = PAGE_SIZE,
                offset = offset,
                sort = null,
                allowArchived = includeArchived.takeIf { it }
            )
            if (!response.isSuccessful) break
            val batch = response.body().orEmpty()
            spools.addAll(batch)
            if (batch.size < PAGE_SIZE) break
            offset += PAGE_SIZE
        }
        return spools
    }

    suspend fun deleteSpool(id: Int) {
        val response = api.deleteSpool(id)
        if (!response.isSuccessful) {
            val errorText = response.errorBody()?.string()
            throw IllegalStateException("Spool could not be deleted (${response.code()}): $errorText")
        }
        cachedCatalog = null
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

    private fun buildFilamentExtra(variant: String, materialModifier: String): Map<String, String> =
        buildMap {
            put("variant", encodeSpoolmanExtraString(variant))
            if (materialModifier.isNotBlank()) {
                put("material_modifier", encodeSpoolmanExtraString(materialModifier))
            }
        }
}

private fun Map<String, JsonElement>?.cardUidKeys(): List<String> {
    if (this == null) return emptyList()
    return keys.filter { key ->
        key.equals("card_uid", ignoreCase = true) ||
            key.equals("card_uids", ignoreCase = true)
    }
}
