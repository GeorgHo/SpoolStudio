package com.spoolstudio.app.ui

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spoolstudio.app.data.remote.spoolman.SpoolmanService
import com.spoolstudio.app.domain.models.FilamentSpool
import com.spoolstudio.app.domain.models.OpenSpoolData
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val spoolmanCatalogRepository = SpoolmanCatalogRepository()
    private val saveOrUpdateSpoolmanSpoolUseCase = SaveOrUpdateSpoolmanSpoolUseCase()
    private val connectionTestUseCase = ConnectionTestUseCase()
    private val printerMappingUseCase = PrinterMappingUseCase()

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
    var snackbarAutoDismiss by mutableStateOf(true)
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
    var isDeletingSpool by mutableStateOf(false)
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
    var showEmptySpoolWeight by mutableStateOf(false)
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
        applySettingsLoadState(buildSettingsLoadState(AppSettingsStore.load(context)))

        if (spoolmanUrl.isNotBlank()) {
            loadSpoolmanFilaments()
        }
    }
    fun handleNfcTagDetected(data: String?) {
        Log.d("MainViewModel", "handleNfcTagDetected called with data: $data")

        val tagData = parseNfcTagData(data)
        val stateUpdate = buildNfcTagReadStateUpdate(
            rawData = data,
            currentRawReadVersion = rawReadVersion,
            parsedTagData = tagData
        )

        rawReadText = stateUpdate.rawReadText
        rawReadVersion = stateUpdate.rawReadVersion

        if (tagData != null) {
            readData = stateUpdate.readData
            currentSpoolId = stateUpdate.currentSpoolId
            if (stateUpdate.clearSelectedSpool) {
                selectedSpool = null
            }
            stateUpdate.spoolMode?.let { spoolMode = it }
            if (stateUpdate.incrementDataVersion) {
                dataVersion++
            }
        } else {
            Log.d("MainViewModel", "Raw RFID data is not OpenSpool JSON")
        }
    }
    fun showSnackbarMessage(message: String, autoDismiss: Boolean = true) {
        snackbarMessage = message
        snackbarAutoDismiss = autoDismiss
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
        val settings = buildSettingsSaveState(
            SettingsSaveInput(
                spoolmanUrl = newUrl,
                moonrakerUrl = newMoonrakerUrl,
                spoolmanSortBy = newSort,
                bambuMasterKey = newBambuMasterKey,
                showCommentField = newShowCommentField
            )
        )

        spoolmanUrl = settings.spoolmanUrl
        moonrakerUrl = settings.moonrakerUrl
        spoolmanSortBy = settings.spoolmanSortBy
        bambuMasterKey = settings.bambuMasterKey
        showCommentField = settings.showCommentField

        AppSettingsStore.saveConnectionSettings(
            context = context,
            spoolmanUrl = settings.spoolmanUrl,
            moonrakerUrl = settings.moonrakerUrl,
            spoolmanSortBy = settings.spoolmanSortBy,
            bambuMasterKey = settings.bambuMasterKey,
            showCommentField = settings.showCommentField
        )

        loadSpoolmanFilaments()
        showSettings = false
    }

    private fun applySettingsLoadState(state: SettingsLoadState) {
        showLotNumber = state.showLotNumber
        showCommentField = state.showCommentField
        showEmptySpoolWeight = state.showEmptySpoolWeight
        spoolmanUrl = state.spoolmanUrl
        spoolmanSortBy = state.spoolmanSortBy
        moonrakerUrl = state.moonrakerUrl
        bambuMasterKey = state.bambuMasterKey
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

    fun createEmptySpool() {
        currentSpoolId = null
        selectedSpool = null
        spoolMode = SpoolMode.CREATE
        readData = null

        dataVersion++
        showSnackbarMessage("New spool mode enabled")
    }

    fun saveToSpoolman(request: SpoolmanSaveRequest) {
        showSnackbarMessage(
            if (spoolMode == SpoolMode.UPDATE) {
                "Updating spool in Spoolman..."
            } else {
                "Saving spool to Spoolman..."
            }
        )

        viewModelScope.launch {
            try {
                when (val saveResult = saveOrUpdateSpoolmanSpoolUseCase.execute(
                    SpoolmanSaveInput(
                        baseUrl = spoolmanUrl,
                        request = request,
                        mode = spoolMode,
                        selectedSpool = selectedSpool,
                        readData = readData,
                        currentSpoolId = currentSpoolId
                    )
                )) {
                    is SaveOrUpdateSpoolmanSpoolResult.ValidationFailed -> {
                        showSnackbarMessage(saveResult.message)
                    }

                    is SaveOrUpdateSpoolmanSpoolResult.Saved -> {
                        val result = saveResult.result
                        selectedSpool = result.finalSpool
                        currentSpoolId = result.finalSpool.id?.toString()
                        val tagData = result.tagData
                        if (tagData != null) {
                            readData = tagData
                        }
                        spoolMode = SpoolMode.UPDATE
                        dataVersion++
                        loadSpoolmanFilaments()

                        showSnackbarMessage(saveResult.successMessage)
                    }
                }
            } catch (e: Exception) {
                showSnackbarMessage("Spoolman action failed: ${e.message ?: "Unknown error"}")
            }
        }
    }

    fun deleteSelectedSpool() {
        val spool = selectedSpool
        val spoolId = spool?.id
        when {
            spoolmanUrl.isBlank() -> {
                showSnackbarMessage("Please configure a Spoolman URL first")
                return
            }
            spool == null || spoolId == null -> {
                showSnackbarMessage("No Spoolman spool selected")
                return
            }
            isDeletingSpool -> return
        }

        showSnackbarMessage("Deleting spool ID #$spoolId from Spoolman...")
        viewModelScope.launch {
            isDeletingSpool = true
            try {
                SpoolmanService(spoolmanUrl).deleteSpool(spoolId)
                selectedSpool = null
                currentSpoolId = null
                readData = null
                spoolMode = SpoolMode.CREATE
                dataVersion++
                loadSpoolmanFilaments()
                showSnackbarMessage("Spool ID #$spoolId deleted from Spoolman")
            } catch (e: Exception) {
                showSnackbarMessage("Delete failed: ${e.message ?: "Unknown error"}")
            } finally {
                isDeletingSpool = false
            }
        }
    }

    fun testMoonrakerConnection(inputUrl: String) {
        moonrakerStatus = null
        moonrakerError = null

        val validationError = connectionTestUseCase.validationError(inputUrl)
        if (validationError != null) {
            isMoonrakerReachable = false
            moonrakerError = validationError
            return
        }

        isTestingMoonraker = true

        viewModelScope.launch {
            try {
                when (val result = connectionTestUseCase.testMoonraker(inputUrl)) {
                    is ConnectionTestResult.Moonraker -> {
                        isMoonrakerReachable = result.reachable
                        moonrakerStatus = result.status
                        moonrakerError = result.error
                    }

                    is ConnectionTestResult.Failed -> {
                        isMoonrakerReachable = false
                        moonrakerError = result.error
                    }

                    is ConnectionTestResult.Spoolman -> Unit
                }
            } finally {
                isTestingMoonraker = false
            }
        }
    }

    fun testMoonrakerConnection() {
        testMoonrakerConnection(moonrakerUrl)
    }

    fun testSpoolmanConnection(inputUrl: String) {
        spoolmanStatus = null
        spoolmanError = null

        val validationError = connectionTestUseCase.validationError(inputUrl)
        if (validationError != null) {
            spoolmanError = validationError
            return
        }

        isTestingSpoolman = true

        viewModelScope.launch {
            try {
                when (val result = connectionTestUseCase.testSpoolman(inputUrl, spoolmanSortBy)) {
                    is ConnectionTestResult.Spoolman -> {
                        spoolmanStatus = result.status
                    }

                    is ConnectionTestResult.Failed -> {
                        spoolmanError = result.error
                    }

                    is ConnectionTestResult.Moonraker -> Unit
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
                val catalog = spoolmanCatalogRepository.load(
                    baseUrl = spoolmanUrl,
                    sortBy = spoolmanSortBy,
                    forceRefresh = true
                )
                applySpoolmanCatalogState(buildSpoolmanCatalogState(catalog))
            } catch (_: Exception) {
                applySpoolmanCatalogState(emptySpoolmanCatalogState())
            } finally {
                isLoadingSpools = false
            }
        }
    }

    private fun applySpoolmanCatalogState(state: SpoolmanCatalogState) {
        spools = state.spools
        availableBrands = state.availableBrands
        availableMaterials = state.availableMaterials
        availableVariants = state.availableVariants
        availableLocations = state.availableLocations
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
                when (val result = printerMappingUseCase.load(normalizeConnectionUrl(url))) {
                    is PrinterMappingOperationResult.Loaded -> {
                        applyPrinterMappingSnapshot(result.snapshot)
                        printerMappingLoadVersion++
                        printerMappingSaveSuccessful = null
                        printerMappingStatusMessage = result.message
                        showSnackbarMessage(result.message)
                    }

                    is PrinterMappingOperationResult.Failed -> {
                        printerMappingSaveSuccessful = false
                        printerMappingStatusMessage = result.message
                        showSnackbarMessage(result.message)
                    }

                    is PrinterMappingOperationResult.Saved -> Unit
                }
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

    fun setShowEmptySpoolWeight(context: Context, value: Boolean) {
        showEmptySpoolWeight = value
        AppSettingsStore.saveShowEmptySpoolWeight(context, value)
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
                when (val result = printerMappingUseCase.save(
                    baseUrl = normalizeConnectionUrl(url),
                    toolhead1SpoolId = e0,
                    toolhead2SpoolId = e1,
                    toolhead3SpoolId = e2,
                    toolhead4SpoolId = e3,
                    activeSpoolId = activeSpoolId
                )) {
                    is PrinterMappingOperationResult.Saved -> {
                        applyPrinterMappingSnapshot(result.snapshot)
                        printerMappingLoadVersion++
                        printerMappingSaveSuccessful = true
                        printerMappingStatusMessage = result.message
                        showSnackbarMessage(result.message)
                    }

                    is PrinterMappingOperationResult.Failed -> {
                        printerMappingSaveSuccessful = false
                        printerMappingStatusMessage = result.message
                        showSnackbarMessage(result.message)
                    }

                    is PrinterMappingOperationResult.Loaded -> Unit
                }
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
