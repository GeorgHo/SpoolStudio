package com.spoolstudio.app.ui

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spoolstudio.app.data.remote.moonraker.MoonrakerService
import com.spoolstudio.app.data.remote.spoolman.SpoolmanService
import com.spoolstudio.app.domain.models.FilamentSpool
import com.spoolstudio.app.domain.models.OpenSpoolData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class SpoolMode {
    CREATE,
    UPDATE,
    DUPLICATE
}

class MainViewModel : ViewModel() {

    var readData by mutableStateOf<OpenSpoolData?>(null)
        private set
    var currentSpoolId by mutableStateOf<String?>(null)
        private set
    var dataVersion by mutableIntStateOf(0)
        private set
    var snackbarMessage by mutableStateOf("")
        private set
    var showSnackbar by mutableStateOf(false)
        private set
    var showSettings by mutableStateOf(false)
        private set
    var spoolmanUrl by mutableStateOf("")
        private set
    var moonrakerUrl by mutableStateOf("")
        private set
    var spools by mutableStateOf<List<FilamentSpool>>(emptyList())
        private set
    var selectedSpool by mutableStateOf<FilamentSpool?>(null)
        private set
    var isLoadingSpools by mutableStateOf(false)
        private set
    var spoolmanSortBy by mutableStateOf("")
        private set
    var showLotNumber by mutableStateOf(false)
        private set
    var availableBrands by mutableStateOf<List<String>>(emptyList())
        private set
    var availableMaterials by mutableStateOf<List<String>>(emptyList())
        private set
    var availableVariants by mutableStateOf<List<String>>(emptyList())
        private set
    var availableLocations by mutableStateOf<List<String>>(emptyList())
        private set
    var spoolMode by mutableStateOf(SpoolMode.CREATE)
        private set
    var isMoonrakerReachable by mutableStateOf(false)
        private set
    var printerTool1SpoolId by mutableStateOf<Int?>(null)
        private set
    var printerTool2SpoolId by mutableStateOf<Int?>(null)
        private set
    var printerTool3SpoolId by mutableStateOf<Int?>(null)
        private set
    var printerTool4SpoolId by mutableStateOf<Int?>(null)
        private set
    var activePrinterSpoolId by mutableStateOf<Int?>(null)
        private set
    var printerMappingSaveSuccessful by mutableStateOf<Boolean?>(null)
        private set
    var printerMappingLoadVersion by mutableIntStateOf(0)
        private set
    var isLoadingPrinterMapping by mutableStateOf(false)
        private set
    var printerMappingStatusMessage by mutableStateOf<String?>(null)
        private set
    var printerMappingOperation by mutableStateOf<String?>(null)
        private set

    var spoolmanStatus by mutableStateOf<String?>(null)
        private set
    var spoolmanError by mutableStateOf<String?>(null)
        private set
    var isTestingSpoolman by mutableStateOf(false)
        private set
    var moonrakerStatus by mutableStateOf<String?>(null)
        private set
    var moonrakerError by mutableStateOf<String?>(null)
        private set
    var isTestingMoonraker by mutableStateOf(false)
        private set

    //BAM
    var rawReadText by mutableStateOf<String?>(null)
        private set

    fun clearRawReadData() {
        rawReadText = null
    }
    var rawReadVersion by mutableStateOf(0)
        private set
    var bambuMasterKey by mutableStateOf("")
        private set
    var showCommentField by mutableStateOf(false)
        private set
    fun refreshSelectedSpool(spoolId: Int) {
        if (spoolmanUrl.isBlank()) return

        viewModelScope.launch {
            try {
                val service = SpoolmanService(spoolmanUrl)
                val refreshed = service.findFilamentBySpoolId(spoolId.toString())
                if (refreshed != null) {
                    selectedSpool = refreshed
                }
            } catch (e: Exception) {
                Log.e("SpoolStudio", "Refresh failed", e)
            }
        }
    }

    fun loadSpoolmanUrl(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        val savedSpoolmanUrl = prefs.getString(SPOOLMAN_URL_KEY, DEFAULT_URL) ?: DEFAULT_URL
        val savedSort = prefs.getString(SPOOLMAN_SORT_KEY, "") ?: ""
        val savedMoonrakerUrl =
            prefs.getString(MOONRAKER_URL_KEY, DEFAULT_MOONRAKER_URL) ?: DEFAULT_MOONRAKER_URL
        val savedShowLotNumber = prefs.getBoolean(SHOW_LOT_NUMBER_KEY, false)
        prefs.getBoolean(SHOW_COMMENT_FIELD, false)
        val savedShowComment = prefs.getBoolean(SHOW_COMMENT_FIELD, false)
        val savedBambuMasterKey = prefs.getString(BAMBU_MASTER_KEY, "") ?: ""

        showLotNumber = savedShowLotNumber
        showCommentField = savedShowComment
        spoolmanUrl = normalizeUrl(savedSpoolmanUrl)
        spoolmanSortBy = savedSort.ifBlank { "" }
        moonrakerUrl = normalizeUrl(savedMoonrakerUrl)
        bambuMasterKey = savedBambuMasterKey.trim().uppercase()

        if (spoolmanUrl.isNotBlank()) {
            loadSpoolmanFilaments()
        }
    }
    fun handleNfcTagDetected(data: String?) {
        Log.d("MainViewModel", "handleNfcTagDetected called with data: $data")

        rawReadText = data
        rawReadVersion++

        data?.let {
            OpenSpoolData.fromJson(it)?.let { openSpoolData ->
                readData = openSpoolData
                currentSpoolId = openSpoolData.spoolId
                selectedSpool = null
                spoolMode = if (openSpoolData.spoolId.isNullOrBlank()) {
                    SpoolMode.CREATE
                } else {
                    SpoolMode.UPDATE
                }
                dataVersion++
            } ?: Log.d("MainViewModel", "Raw RFID data is not OpenSpool JSON")
        }
    }
    fun showSnackbarMessage(message: String) {
        snackbarMessage = message
        showSnackbar = true
    }

    fun refreshSpools() {
        if (spoolmanUrl.isNotBlank()) {
            loadSpoolmanFilaments()
        }
    }

    fun dismissSnackbar() {
        showSnackbar = false
    }

    fun showSettings() {
        showSettings = true
    }

    fun hideSettings() {
        showSettings = false
    }

    fun handleSettingsSave(
        context: Context,
        newUrl: String,
        newMoonrakerUrl: String,
        newSort: String,
        newBambuMasterKey: String,
        newShowCommentField: Boolean
    ) {
        val normalizedUrl = normalizeUrl(newUrl)
        val normalizedMoonrakerUrl = normalizeUrl(newMoonrakerUrl)
        val normalizedSort = newSort.ifBlank { "" }
        val normalizedBambuMasterKey = newBambuMasterKey.trim().uppercase()

        spoolmanUrl = normalizedUrl
        moonrakerUrl = normalizedMoonrakerUrl
        spoolmanSortBy = normalizedSort
        bambuMasterKey = normalizedBambuMasterKey
        showCommentField = newShowCommentField

        saveSpoolmanUrl(context, normalizedUrl)
        saveMoonrakerUrl(context, normalizedMoonrakerUrl)
        saveSpoolmanSort(context, normalizedSort)
        saveBambuMasterKey(context, normalizedBambuMasterKey)
        saveShowCommentField(context, newShowCommentField)

        loadSpoolmanFilaments()
        showSettings = false
    }

    fun handleFilamentSelection(filament: FilamentSpool?) {
        selectedSpool = filament
        if (filament != null) {
            readData = OpenSpoolData.toOpenSpoolData(filament)
            currentSpoolId = filament.id?.toString()
            spoolMode = SpoolMode.UPDATE
        } else {
            readData = null
            currentSpoolId = null
            spoolMode = SpoolMode.CREATE
        }
        dataVersion++
    }

    fun duplicateCurrentSpool() {
        val sourceSpool = selectedSpool
        if (sourceSpool == null && readData == null) {
            showSnackbarMessage("No spool loaded to duplicate")
            return
        }

        currentSpoolId = null
        selectedSpool = null
        spoolMode = SpoolMode.DUPLICATE

        readData = when {
            sourceSpool != null -> OpenSpoolData.toOpenSpoolData(sourceSpool).copy(
                spoolId = null
            )
            readData != null -> readData!!.copy(
                spoolId = null
            )
            else -> null
        }

        dataVersion++
        showSnackbarMessage("Create New Spool mode enabled")
    }

    fun createNewSpoolFromCurrent() {
        val sourceSpool = selectedSpool

        if (sourceSpool == null && readData == null) {
            showSnackbarMessage("No spool loaded")
            return
        }

        currentSpoolId = null
        selectedSpool = null
        spoolMode = SpoolMode.CREATE

        readData = when {
            sourceSpool != null -> OpenSpoolData.toOpenSpoolData(sourceSpool).copy(
                spoolId = null
            )
            readData != null -> readData!!.copy(
                spoolId = null
            )
            else -> null
        }

        dataVersion++
        showSnackbarMessage("Create new spool mode enabled")
    }

    fun saveToSpoolman(request: SpoolmanSaveRequest) {
        saveSpoolmanInternal(request, false, null)
    }

    fun saveToSpoolmanAndWriteTag(
        request: SpoolmanSaveRequest,
        onWriteTag: (String) -> Unit
    ) {
        saveSpoolmanInternal(request, true, onWriteTag)
    }

    private fun buildMaterialWithVariant(material: String, variant: String): String {
        val cleanMaterial = material.trim().ifBlank { "Unknown" }
        val cleanVariant = variant.trim().ifBlank { "Basic" }
        return "$cleanMaterial - $cleanVariant"
    }

    private fun normalizedColorHex(colorHex: String?): String? =
        colorHex?.trim()?.removePrefix("#")?.uppercase()?.ifBlank { null }

    private fun normalizeUrl(url: String): String = url.trim().removeSuffix("/")

    private fun parseRemainingWeight(value: String): Float? =
        value.trim()
            .replace(",", ".")
            .takeIf { it.isNotBlank() }
            ?.toFloatOrNull()
            ?.takeIf { it >= 0f }

    private fun buildTagData(
        spool: FilamentSpool,
        request: SpoolmanSaveRequest,
        resolvedLotNr: String
    ): OpenSpoolData? {
        return try {
            OpenSpoolData.toOpenSpoolData(spool).copy(
                minTemp = request.minTemp,
                maxTemp = request.maxTemp,
                bedMinTemp = request.bedMinTemp.ifBlank { null },
                bedMaxTemp = request.bedMaxTemp.ifBlank { null },
                subtype = request.variant.ifBlank { "Basic" },
                lotNr = resolvedLotNr.ifBlank { null }
            )
        } catch (e: IllegalArgumentException) {
            Log.w("SpoolStudio", "Saved spool cannot be converted to OpenSpool tag data", e)
            null
        }
    }

    private fun validateBeforeSave(
        material: String,
        brand: String,
        colorName: String,
        colorHex: String?,
        minTemp: String,
        maxTemp: String,
        remainingWeight: String
    ): String? {
        if (!isValidSpoolmanUrl(spoolmanUrl)) return "Please configure a valid Spoolman URL first"
        if (material.trim().isBlank()) return "Material is required"
        if (brand.trim().isBlank()) return "Brand is required"
        if (colorName.trim().isBlank()) return "Color name is required"

        val cleanHex = normalizedColorHex(colorHex)
        if (colorHex != null && cleanHex == null) return "HEX color is invalid"

        if (minTemp.isNotBlank() && minTemp.toIntOrNull() == null) return "Min nozzle temperature is invalid"
        if (maxTemp.isNotBlank() && maxTemp.toIntOrNull() == null) return "Max nozzle temperature is invalid"
        if (remainingWeight.isNotBlank() && parseRemainingWeight(remainingWeight) == null) {
            return "Remaining filament weight is invalid"
        }

        return null
    }

    private fun saveSpoolmanInternal(
        request: SpoolmanSaveRequest,
        writeTag: Boolean,
        onWriteTag: ((String) -> Unit)?
    ) {
        val validationError = validateBeforeSave(
            material = request.material,
            brand = request.brand,
            colorName = request.colorName,
            colorHex = request.colorHex,
            minTemp = request.minTemp,
            maxTemp = request.maxTemp,
            remainingWeight = request.remainingWeight
        )
        if (validationError != null) {
            showSnackbarMessage(validationError)
            return
        }

        viewModelScope.launch {
            try {
                val service = SpoolmanService(spoolmanUrl)
                val actionMode = spoolMode

                val resolvedLotNr = when (actionMode) {
                    SpoolMode.UPDATE -> request.lotNr.trim().ifBlank {
                        selectedSpool?.lotNr
                            ?: readData?.lotNr
                            ?: ""
                    }
                    SpoolMode.CREATE, SpoolMode.DUPLICATE -> request.lotNr.trim()
                }

                val vendor = service.createOrFindVendor(request.brand)
                val composedMaterial = buildMaterialWithVariant(request.material, request.variant)
                val cleanColorName = request.colorName.trim().ifBlank { "Unknown" }
                val cleanColorHex = normalizedColorHex(request.colorHex)
                val nozzleTemp = request.minTemp.toIntOrNull()
                val bedTemp = request.bedMinTemp.toIntOrNull()
                val cleanRemainingWeight = parseRemainingWeight(request.remainingWeight)

                val finalSpool = when (actionMode) {
                    SpoolMode.UPDATE -> {
                        val spoolId = request.existingSpoolId
                            ?: currentSpoolId?.toIntOrNull()
                            ?: throw IllegalStateException("No spool id available for update")

                        val currentFilamentId = selectedSpool?.filamentId
                            ?: throw IllegalStateException("Current filament id missing")

                        val usageCount = service.countSpoolsUsingFilament(currentFilamentId)

                        val targetFilamentId = if (usageCount <= 1) {
                            service.updateFilament(
                                id = currentFilamentId,
                                name = cleanColorName,
                                material = composedMaterial,
                                vendorId = vendor.id ?: throw IllegalStateException("Vendor id missing"),
                                colorHex = cleanColorHex,
                                nozzleTemp = nozzleTemp,
                                bedTemp = bedTemp
                            ).id
                        } else {
                            service.createOrFindFilament(
                                name = cleanColorName,
                                material = composedMaterial,
                                vendorId = vendor.id ?: throw IllegalStateException("Vendor id missing"),
                                colorHex = cleanColorHex,
                                nozzleTemp = nozzleTemp,
                                bedTemp = bedTemp
                            ).id
                        }

                        service.updateSpool(
                            id = spoolId,
                            filamentId = targetFilamentId,
                            lotNr = resolvedLotNr,
                            location = request.location.trim().ifBlank { null },
                            remainingWeight = cleanRemainingWeight ?: selectedSpool?.remainingWeight,
                            comment = request.comment.trim().ifBlank { null }
                        )

                        service.findFilamentBySpoolId(spoolId.toString())
                            ?: throw IllegalStateException("Updated spool could not be reloaded")
                    }

                    SpoolMode.CREATE, SpoolMode.DUPLICATE -> {
                        val filament = service.createOrFindFilament(
                            name = cleanColorName,
                            material = composedMaterial,
                            vendorId = vendor.id ?: throw IllegalStateException("Vendor id missing"),
                            colorHex = cleanColorHex,
                            nozzleTemp = nozzleTemp,
                            bedTemp = bedTemp
                        )

                        val createdSpool = service.createSpool(
                            filamentId = filament.id,
                            lotNr = resolvedLotNr,
                            location = request.location.trim().ifBlank { null },
                            remainingWeight = cleanRemainingWeight ?: selectedSpool?.remainingWeight ?: filament.weight,
                            comment = request.comment.trim().ifBlank { null }
                        )

                        service.findFilamentBySpoolId(createdSpool.id?.toString() ?: "")
                            ?: FilamentSpool.fromSpoolman(createdSpool.copy(filament = filament))
                    }
                }

                selectedSpool = finalSpool
                currentSpoolId = finalSpool.id?.toString()
                val tagData = buildTagData(finalSpool, request, resolvedLotNr)
                if (tagData != null) {
                    readData = tagData
                }
                spoolMode = SpoolMode.UPDATE
                dataVersion++
                loadSpoolmanFilaments()

                if (writeTag && onWriteTag != null) {
                    if (tagData != null) {
                        onWriteTag(tagData.toJson())
                        showSnackbarMessage(
                            if (actionMode == SpoolMode.UPDATE) {
                                "Spoolman updated. Hold NFC tag to write."
                            } else {
                                "Saved to Spoolman with unique lot number. Hold NFC tag to write."
                            }
                        )
                    } else {
                        showSnackbarMessage("Saved to Spoolman, but this material cannot be written to an OpenSpool tag")
                    }
                } else {
                    showSnackbarMessage(
                        if (actionMode == SpoolMode.UPDATE) {
                            "Updated in Spoolman successfully"
                        } else {
                            "Saved to Spoolman successfully"
                        }
                    )
                }
            } catch (e: Exception) {
                showSnackbarMessage("Spoolman action failed: ${e.message}")
            }
        }
    }

    private fun saveSpoolmanUrl(context: Context, url: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(SPOOLMAN_URL_KEY, normalizeUrl(url)).apply()
    }

    private fun saveMoonrakerUrl(context: Context, url: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(MOONRAKER_URL_KEY, url.trim().removeSuffix("/")).apply()
    }

    private fun saveSpoolmanSort(context: Context, sort: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(SPOOLMAN_SORT_KEY, sort).apply()
    }

    private fun saveBambuMasterKey(context: Context, key: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(BAMBU_MASTER_KEY, key.trim().uppercase()).apply()
    }

    private fun saveShowLotNumber(context: Context, value: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(SHOW_LOT_NUMBER_KEY, value).apply()
    }
    private fun saveShowCommentField(context: Context, value: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(SHOW_COMMENT_FIELD, value).apply()
    }

    private fun isValidSpoolmanUrl(url: String): Boolean {
        val normalized = normalizeUrl(url)
        return normalized.isNotEmpty() &&
            (normalized.startsWith("http://") || normalized.startsWith("https://"))
    }

    fun testMoonrakerConnection(inputUrl: String) {
        val normalizedUrl = normalizeUrl(inputUrl)

        moonrakerStatus = null
        moonrakerError = null

        if (normalizedUrl.isBlank()) {
            isMoonrakerReachable = false
            moonrakerError = "URL fehlt"
            return
        }

        if (!normalizedUrl.startsWith("http://") && !normalizedUrl.startsWith("https://")) {
            isMoonrakerReachable = false
            moonrakerError = "URL muss mit http:// oder https:// beginnen"
            return
        }

        isTestingMoonraker = true

        viewModelScope.launch {
            try {
                val testUrl = "$normalizedUrl/printer/info"

                val connection = withContext(Dispatchers.IO) {
                    (java.net.URL(testUrl).openConnection() as java.net.HttpURLConnection).apply {
                        connectTimeout = 5000
                        readTimeout = 5000
                        requestMethod = "GET"
                    }
                }

                try {
                    val responseCode = withContext(Dispatchers.IO) { connection.responseCode }

                    val responseText = withContext(Dispatchers.IO) {
                        connection.inputStream.bufferedReader().use { it.readText() }
                    }

                    if (responseCode in 200..299 && responseText.trim().startsWith("{")) {
                        isMoonrakerReachable = true
                        moonrakerStatus = "Moonraker erreichbar"
                    } else {
                        isMoonrakerReachable = false
                        moonrakerError = if (responseText.contains("<html", ignoreCase = true)) {
                            "Kein direkter Moonraker-Endpunkt"
                        } else {
                            "HTTP $responseCode"
                        }
                    }
                } finally {
                    connection.disconnect()
                }

            } catch (e: Exception) {
                isMoonrakerReachable = false
                moonrakerError = when {
                    e.message?.contains("timeout", true) == true -> "Timeout"
                    e.message?.contains("Unable to resolve host", true) == true -> "Host nicht erreichbar"
                    else -> "Fehler: ${e.message ?: "Unbekannt"}"
                }

                Log.e("MoonrakerTest", "Connection failed", e)
            } finally {
                isTestingMoonraker = false
            }
        }
    }

    fun testMoonrakerConnection() {
        testMoonrakerConnection(moonrakerUrl)
    }

    fun testSpoolmanConnection(inputUrl: String) {
        val normalizedUrl = normalizeUrl(inputUrl)

        spoolmanStatus = null
        spoolmanError = null

        if (normalizedUrl.isBlank()) {
            spoolmanError = "URL fehlt"
            return
        }

        if (!normalizedUrl.startsWith("http://") && !normalizedUrl.startsWith("https://")) {
            spoolmanError = "URL muss mit http:// oder https:// beginnen"
            return
        }

        isTestingSpoolman = true

        viewModelScope.launch {
            try {
                val service = SpoolmanService(normalizedUrl)
                service.getCatalog(spoolmanSortBy.ifEmpty { null }, forceRefresh = true)
                spoolmanStatus = "Spoolman erreichbar"
            } catch (e: Exception) {
                spoolmanError = when {
                    e.message?.contains("timeout", true) == true -> "Timeout"
                    e.message?.contains("Unable to resolve host", true) == true -> "Host nicht erreichbar"
                    else -> "Fehler: ${e.message ?: "Unbekannt"}"
                }
            } finally {
                isTestingSpoolman = false
            }
        }
    }

    fun testSpoolmanConnection() {
        testSpoolmanConnection(spoolmanUrl)
    }

    fun clearSpoolmanStatus() {
        spoolmanStatus = null
        spoolmanError = null
    }

    fun clearMoonrakerStatus() {
        moonrakerStatus = null
        moonrakerError = null
    }

    fun beginPrinterMappingDialogSession() {
        printerMappingSaveSuccessful = null
        printerMappingStatusMessage = null
        printerMappingOperation = null
    }

    fun clearPrinterMappingDialogFeedback() {
        printerMappingSaveSuccessful = null
        printerMappingStatusMessage = null
        printerMappingOperation = null
    }

    private fun loadSpoolmanFilaments() {
        isLoadingSpools = true
        viewModelScope.launch {
            try {
                val service = SpoolmanService(spoolmanUrl)
                val catalog = service.getCatalog(spoolmanSortBy.ifEmpty { null }, forceRefresh = true)
                spools = catalog.spools
                availableBrands = catalog.vendorNames
                availableMaterials = catalog.materialNames
                availableVariants = catalog.variantNames
                availableLocations = catalog.locationNames
            } catch (_: Exception) {
                spools = emptyList()
                availableBrands = emptyList()
                availableMaterials = emptyList()
                availableVariants = emptyList()
                availableLocations = emptyList()
            } finally {
                isLoadingSpools = false
            }
        }
    }

    companion object {
        private const val PREFS_NAME = "spoolstudio_prefs"
        private const val SPOOLMAN_URL_KEY = "spoolman_url"
        private const val SPOOLMAN_SORT_KEY = "spoolman_sort"
        private const val DEFAULT_URL = ""
        private const val MOONRAKER_URL_KEY = "moonraker_url"
        private const val SHOW_LOT_NUMBER_KEY = "show_lot_number"
        private const val SHOW_COMMENT_FIELD = "show_comment_field"
        private const val DEFAULT_MOONRAKER_URL = ""
        private const val BAMBU_MASTER_KEY = "bambu_master_key"
    }

    fun loadCurrentPrinterMapping() {
        val url = moonrakerUrl
        if (url.isBlank()) {
            printerMappingSaveSuccessful = false
            printerMappingStatusMessage = "Please configure a Moonraker URL first"
            showSnackbarMessage("Please configure a Moonraker URL first")
            return
        }

        if (isLoadingPrinterMapping) return

        viewModelScope.launch {
            isLoadingPrinterMapping = true
            printerMappingOperation = "load"
            printerMappingSaveSuccessful = null
            printerMappingStatusMessage = null

            try {
                val service = MoonrakerService(normalizeUrl(url))
                val mapping = service.getToolMapping()

                val activeSpoolId = try {
                    service.getActiveSpoolId()
                } catch (e: Exception) {
                    if (e.message?.contains("404") == true) {
                        Log.w("MainViewModel", "Spoolman not active on printer")
                    } else {
                        Log.w("MainViewModel", "Active spool error: ${e.message}")
                    }
                    null
                    null
                }

                printerTool1SpoolId = mapping["T0"]?.takeIf { it > 0 }
                printerTool2SpoolId = mapping["T1"]?.takeIf { it > 0 }
                printerTool3SpoolId = mapping["T2"]?.takeIf { it > 0 }
                printerTool4SpoolId = mapping["T3"]?.takeIf { it > 0 }
                activePrinterSpoolId = activeSpoolId

                printerMappingLoadVersion++
                printerMappingSaveSuccessful = null

                val message = if (activeSpoolId == null) {
                    "Printer mapping loaded (active spool not available)"
                } else {
                    "Printer mapping loaded"
                }

                printerMappingStatusMessage = message
                showSnackbarMessage(message)
            } catch (e: Exception) {
                printerMappingSaveSuccessful = false
                printerMappingStatusMessage = "Loading printer mapping failed: ${e.message ?: "Unknown error"}"
                showSnackbarMessage("Loading printer mapping failed: ${e.message ?: "Unknown error"}")
            } finally {
                printerMappingOperation = null
                isLoadingPrinterMapping = false
            }
        }
    }

    fun setShowLotNumber(context: Context, value: Boolean) {
        showLotNumber = value
        saveShowLotNumber(context, value)
    }

    fun savePrinterMapping(
        e0: Int?,
        e1: Int?,
        e2: Int?,
        e3: Int?,
        activeSpoolId: Int?
    ) {
        val url = moonrakerUrl
        if (url.isBlank()) {
            printerMappingSaveSuccessful = false
            printerMappingStatusMessage = "Moonraker URL missing"
            showSnackbarMessage("Moonraker URL missing")
            return
        }

        if (isLoadingPrinterMapping) return

        viewModelScope.launch {
            isLoadingPrinterMapping = true
            printerMappingOperation = "save"
            printerMappingSaveSuccessful = null
            printerMappingStatusMessage = null
            try {
                val service = MoonrakerService(normalizeUrl(url))

                service.setToolSpool("T0", e0)
                service.setToolSpool("T1", e1)
                service.setToolSpool("T2", e2)
                service.setToolSpool("T3", e3)
                service.setActiveSpoolId(activeSpoolId)

                val mapping = service.getToolMapping()
                val refreshedActiveSpoolId = service.getActiveSpoolId()

                printerTool1SpoolId = mapping["T0"]?.takeIf { it > 0 }
                printerTool2SpoolId = mapping["T1"]?.takeIf { it > 0 }
                printerTool3SpoolId = mapping["T2"]?.takeIf { it > 0 }
                printerTool4SpoolId = mapping["T3"]?.takeIf { it > 0 }
                activePrinterSpoolId = refreshedActiveSpoolId

                printerMappingLoadVersion++
                printerMappingSaveSuccessful = true
                printerMappingStatusMessage = "Mapping saved to printer"
                showSnackbarMessage("Mapping saved to printer")
            } catch (e: Exception) {
                printerMappingSaveSuccessful = false
                printerMappingStatusMessage = "Save failed: ${e.message ?: "Unknown error"}"
                showSnackbarMessage("Save failed: ${e.message ?: "Unknown error"}")
            } finally {
                printerMappingOperation = null
                isLoadingPrinterMapping = false
            }
        }
    }
}
