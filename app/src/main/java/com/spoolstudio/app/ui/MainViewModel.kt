package com.spoolstudio.app.ui

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spoolstudio.app.domain.models.FilamentSpool
import com.spoolstudio.app.domain.models.OpenSpoolData
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val spoolmanCatalogRepository = SpoolmanCatalogRepository()
    private val spoolmanSaveRepository = SpoolmanSaveRepository()
    private val moonrakerConnectionRepository = MoonrakerConnectionRepository()
    private val printerMappingRepository = PrinterMappingRepository()

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
                val refreshed = spoolmanCatalogRepository.findBySpoolId(spoolmanUrl, spoolId)
                if (refreshed != null) {
                    selectedSpool = refreshed
                }
            } catch (e: Exception) {
                Log.e("SpoolStudio", "Refresh failed", e)
            }
        }
    }

    fun loadSpoolmanUrl(context: Context) {
        val settings = AppSettingsStore.load(context)

        showLotNumber = settings.showLotNumber
        showCommentField = settings.showCommentField
        spoolmanUrl = settings.spoolmanUrl
        spoolmanSortBy = settings.spoolmanSortBy
        moonrakerUrl = settings.moonrakerUrl
        bambuMasterKey = settings.bambuMasterKey

        if (spoolmanUrl.isNotBlank()) {
            loadSpoolmanFilaments()
        }
    }
    fun handleNfcTagDetected(data: String?) {
        Log.d("MainViewModel", "handleNfcTagDetected called with data: $data")

        rawReadText = data
        rawReadVersion++

        val tagData = parseNfcTagData(data)
        if (tagData != null) {
            readData = tagData.readData
            currentSpoolId = tagData.currentSpoolId
            selectedSpool = null
            spoolMode = tagData.spoolMode
            dataVersion++
        } else {
            Log.d("MainViewModel", "Raw RFID data is not OpenSpool JSON")
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
        val normalizedUrl = normalizeConnectionUrl(newUrl)
        val normalizedMoonrakerUrl = normalizeConnectionUrl(newMoonrakerUrl)
        val normalizedSort = newSort.ifBlank { "" }
        val normalizedBambuMasterKey = newBambuMasterKey.trim().uppercase()

        spoolmanUrl = normalizedUrl
        moonrakerUrl = normalizedMoonrakerUrl
        spoolmanSortBy = normalizedSort
        bambuMasterKey = normalizedBambuMasterKey
        showCommentField = newShowCommentField

        AppSettingsStore.saveConnectionSettings(
            context = context,
            spoolmanUrl = normalizedUrl,
            moonrakerUrl = normalizedMoonrakerUrl,
            spoolmanSortBy = normalizedSort,
            bambuMasterKey = normalizedBambuMasterKey,
            showCommentField = newShowCommentField
        )

        loadSpoolmanFilaments()
        showSettings = false
    }

    fun handleFilamentSelection(filament: FilamentSpool?) {
        selectedSpool = filament
        val selection = buildSpoolSelectionResult(filament)
        readData = selection.readData
        currentSpoolId = selection.currentSpoolId
        spoolMode = selection.spoolMode
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
        readData = buildSpoolModeSourceData(sourceSpool, readData)

        dataVersion++
        showSnackbarMessage("New from selected mode enabled")
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
        readData = buildSpoolModeSourceData(sourceSpool, readData)

        dataVersion++
        showSnackbarMessage("New from selected mode enabled")
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

    private fun saveSpoolmanInternal(
        request: SpoolmanSaveRequest,
        writeTag: Boolean,
        onWriteTag: ((String) -> Unit)?
    ) {
        val validationError = validateBeforeSave(
            spoolmanUrl = spoolmanUrl,
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

        showSnackbarMessage(
            if (spoolMode == SpoolMode.UPDATE) {
                "Updating spool in Spoolman..."
            } else {
                "Saving spool to Spoolman..."
            }
        )

        viewModelScope.launch {
            try {
                val result = spoolmanSaveRepository.save(
                    SpoolmanSaveInput(
                        baseUrl = spoolmanUrl,
                        request = request,
                        mode = spoolMode,
                        selectedSpool = selectedSpool,
                        readData = readData,
                        currentSpoolId = currentSpoolId
                    )
                )

                selectedSpool = result.finalSpool
                currentSpoolId = result.finalSpool.id?.toString()
                val tagData = result.tagData
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
                            if (result.actionMode == SpoolMode.UPDATE) {
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
                        if (result.actionMode == SpoolMode.UPDATE) {
                            "Spoolman update complete"
                        } else {
                            "Spool saved to Spoolman"
                        }
                    )
                }
            } catch (e: Exception) {
                showSnackbarMessage("Spoolman action failed: ${e.message ?: "Unknown error"}")
            }
        }
    }

    fun testMoonrakerConnection(inputUrl: String) {
        val normalizedUrl = normalizeConnectionUrl(inputUrl)

        moonrakerStatus = null
        moonrakerError = null

        val validationError = httpUrlValidationError(normalizedUrl)
        if (validationError != null) {
            isMoonrakerReachable = false
            moonrakerError = validationError
            return
        }

        isTestingMoonraker = true

        viewModelScope.launch {
            try {
                val result = moonrakerConnectionRepository.test(normalizedUrl)
                isMoonrakerReachable = result.reachable
                moonrakerStatus = result.status
                moonrakerError = result.error
            } catch (e: Exception) {
                isMoonrakerReachable = false
                moonrakerError = connectionErrorMessage(e)

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
        val normalizedUrl = normalizeConnectionUrl(inputUrl)

        spoolmanStatus = null
        spoolmanError = null

        val validationError = httpUrlValidationError(normalizedUrl)
        if (validationError != null) {
            spoolmanError = validationError
            return
        }

        isTestingSpoolman = true

        viewModelScope.launch {
            try {
                spoolmanCatalogRepository.load(
                    baseUrl = normalizedUrl,
                    sortBy = spoolmanSortBy,
                    forceRefresh = true
                )
                spoolmanStatus = "Spoolman erreichbar"
            } catch (e: Exception) {
                spoolmanError = connectionErrorMessage(e)
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
                val catalog = spoolmanCatalogRepository.load(
                    baseUrl = spoolmanUrl,
                    sortBy = spoolmanSortBy,
                    forceRefresh = true
                )
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
                val result = printerMappingRepository.load(normalizeConnectionUrl(url))
                applyPrinterMappingSnapshot(result.snapshot)

                printerMappingLoadVersion++
                printerMappingSaveSuccessful = null

                val message = if (!result.activeSpoolAvailable) {
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
        AppSettingsStore.saveShowLotNumber(context, value)
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
                val snapshot = printerMappingRepository.save(
                    baseUrl = normalizeConnectionUrl(url),
                    toolhead1SpoolId = e0,
                    toolhead2SpoolId = e1,
                    toolhead3SpoolId = e2,
                    toolhead4SpoolId = e3,
                    activeSpoolId = activeSpoolId
                )
                applyPrinterMappingSnapshot(snapshot)

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

    private fun applyPrinterMappingSnapshot(snapshot: PrinterMappingSnapshot) {
        printerTool1SpoolId = snapshot.toolhead1SpoolId
        printerTool2SpoolId = snapshot.toolhead2SpoolId
        printerTool3SpoolId = snapshot.toolhead3SpoolId
        printerTool4SpoolId = snapshot.toolhead4SpoolId
        activePrinterSpoolId = snapshot.activeSpoolId
    }
}
