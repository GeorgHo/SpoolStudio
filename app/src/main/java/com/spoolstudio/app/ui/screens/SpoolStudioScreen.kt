package com.spoolstudio.app.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.spoolstudio.app.data.local.MaterialDatabase
import com.spoolstudio.app.domain.models.FilamentSpool
import com.spoolstudio.app.domain.models.OpenSpoolData
import com.spoolstudio.app.ui.SpoolMode
import com.spoolstudio.app.ui.SpoolmanSaveRequest
import com.spoolstudio.app.utils.*

@Composable
fun SpoolStudioScreen(
    onWriteTag: (String) -> Unit,
    onReadTag: () -> Unit,
    readData: OpenSpoolData? = null,
    // Bam
    rawReadText: String? = null,
    rawReadVersion: Int = 0,
    onClearRawReadData: () -> Unit = {},
    //
    dataVersion: Int = 0,
    snackbarMessage: String = "",
    showSnackbar: Boolean = false,
    onSnackbarDismiss: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onBambuDataApplied: () -> Unit = {},
    onBambuExistingSpoolFound: () -> Unit = {},
    spools: List<FilamentSpool> = emptyList(),
    selectedSpool: FilamentSpool? = null,
    isLoadingSpools: Boolean = false,
    onSpoolSelected: (FilamentSpool?) -> Unit = {},
    onRefreshSpools: () -> Unit = {},
    onRefreshSelectedSpool: (Int) -> Unit = {},
    spoolmanUrl: String = "",
    currentSpoolId: String? = null,
    availableBrands: List<String> = emptyList(),
    availableMaterials: List<String> = emptyList(),
    availableVariants: List<String> = emptyList(),
    availableLocations: List<String> = emptyList(),
    spoolMode: SpoolMode = SpoolMode.CREATE,
    onDuplicateSpool: () -> Unit = {},
    isMoonrakerReachable: Boolean = false,
    onTestMoonrakerConnection: () -> Unit = {},
    printerTool1SpoolId: Int? = null,
    printerTool2SpoolId: Int? = null,
    printerTool3SpoolId: Int? = null,
    printerTool4SpoolId: Int? = null,
    activePrinterSpoolId: Int? = null,
    printerMappingLoadVersion: Int = 0,
    isLoadingPrinterMapping: Boolean = false,
    printerMappingSaveSuccessful: Boolean? = null,
    printerMappingStatusMessage: String? = null,
    printerMappingOperation: String? = null,
    onBeginPrinterMappingDialogSession: () -> Unit = {},
    onClearPrinterMappingDialogFeedback: () -> Unit = {},
    onLoadCurrentPrinterMapping: () -> Unit = {},
    onSavePrinterMapping: (Int?, Int?, Int?, Int?, Int?) -> Unit = { _, _, _, _, _ -> },
    onSaveSuccess: () -> Unit = {},
    showLotNumber: Boolean = false,
    showCommentField: Boolean = false,
    onCreateNewSpool: () -> Unit = {},
    onCreateInSpoolman: (SpoolmanSaveRequest) -> Unit = {},
    onCreateAndWriteTag: (SpoolmanSaveRequest) -> Unit = {},
) {
    var showBambuDialog by remember { mutableStateOf(false) }
    var bambuDialogText by remember { mutableStateOf("") }
    var showBambuDiffDialog by remember { mutableStateOf(false) }
    var bambuDiffDialogText by remember { mutableStateOf("") }
    var pendingBambuApply by remember { mutableStateOf<(() -> Unit)?>(null) }
    val defaultMaterial = MaterialDatabase.getMaterial("PLA")!!
    val form = remember { SpoolFormState(defaultMaterial) }
    var showPrinterMappingDialog by remember { mutableStateOf(false) }
    var toolhead1SpoolId by remember { mutableStateOf<Int?>(null) }
    var toolhead2SpoolId by remember { mutableStateOf<Int?>(null) }
    var toolhead3SpoolId by remember { mutableStateOf<Int?>(null) }
    var toolhead4SpoolId by remember { mutableStateOf<Int?>(null) }
    var activeDialogSpoolId by remember { mutableStateOf<Int?>(null) }

    val hasPrinterMappingChanges =
        toolhead1SpoolId != printerTool1SpoolId ||
                toolhead2SpoolId != printerTool2SpoolId ||
                toolhead3SpoolId != printerTool3SpoolId ||
                toolhead4SpoolId != printerTool4SpoolId ||
                activeDialogSpoolId != activePrinterSpoolId

    val activeSpoolOutsideMapping =
        activePrinterSpoolId != null &&
                activePrinterSpoolId !in listOf(
            toolhead1SpoolId,
            toolhead2SpoolId,
            toolhead3SpoolId,
            toolhead4SpoolId
        )

    val spoolColor = form.colorHex?.let { hex ->

        val normalized = hex.trim().removePrefix("#")

        if (normalized.matches(Regex("^[A-Fa-f0-9]{6}$"))) {
            Color(android.graphics.Color.parseColor("#$normalized"))
        } else {
            Color(0xFF4A423D)
        }

    } ?: Color(0xFF4A423D)

    val printerMappingBusyLabel = when (printerMappingOperation) {
        "load" -> "Loading printer mapping..."
        "save" -> "Saving printer mapping..."
        else -> null
    }

    val inlinePrinterMappingStatusColor = when {
        isLoadingPrinterMapping -> MaterialTheme.colorScheme.primary
        printerMappingSaveSuccessful == true -> MaterialTheme.colorScheme.primary
        printerMappingSaveSuccessful == false -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val inlinePrinterMappingStatusText = when {
        isLoadingPrinterMapping -> printerMappingBusyLabel
        !printerMappingStatusMessage.isNullOrBlank() -> printerMappingStatusMessage
        else -> null
    }

    LaunchedEffect(readData, dataVersion, selectedSpool, spoolMode, availableLocations) {
        Log.d("SpoolStudioScreen", "LaunchedEffect triggered - readData: $readData, dataVersion: $dataVersion, selectedSpool: $selectedSpool, spoolMode: $spoolMode")

        val sourceSpool = selectedSpool ?: readData?.let { FilamentSpool.fromOpenSpool(it) }

        if (sourceSpool != null) {
            form.filamentType = sourceSpool.material
            form.variant = sourceSpool.variant.ifBlank { "Basic" }
            form.colorHex = sourceSpool.colorHex
            form.colorName = formatColorName(
                sourceSpool.spoolmanName?.takeIf { it.isNotBlank() }
                    ?: sourceSpool.colorHex
                    ?: form.colorName
            )
            form.brand = sourceSpool.brand

            val loadedLocation = sourceSpool.location.orEmpty().trim()
            if (loadedLocation.isBlank()) {
                form.clearLocation()
            } else if (loadedLocation in availableLocations) {
                form.location = loadedLocation
                form.customLocation = ""
            } else {
                form.location = "Other"
                form.customLocation = loadedLocation
            }

            form.minTemp = sourceSpool.minTemp?.toString() ?: form.minTemp
            form.maxTemp = sourceSpool.maxTemp?.toString() ?: form.maxTemp
            form.bedMinTemp = sourceSpool.bedMinTemp?.toString() ?: form.bedMinTemp
            form.bedMaxTemp = sourceSpool.bedMaxTemp?.toString() ?: form.bedMaxTemp
            form.lotNr = sourceSpool.lotNr ?: OpenSpoolData.generateLotNr()
            form.comment = sourceSpool.comment ?: ""
            form.remainingWeight = sourceSpool.remainingWeight
                ?.takeIf { it >= 0f }
                ?.let { weight ->
                    if (weight % 1f == 0f) weight.toInt().toString() else weight.toString()
                }
                ?: ""
            form.colorHexInput = form.colorHex ?: ""
            form.colorNameWasManuallyEdited = false
        } else if (spoolMode == SpoolMode.CREATE) {
            form.clearLocation()
        }
    }

    LaunchedEffect(form.colorHex) {
        form.colorHexInput = form.colorHex ?: ""
        if (!form.colorNameWasManuallyEdited) {
            val suggested = suggestColorName(form.colorHex)
            if (suggested.isNotBlank()) {
                form.colorName = suggested
            }
        }
    }

    LaunchedEffect(showSnackbar, snackbarMessage) {
        if (showSnackbar && snackbarMessage.isNotBlank()) {
            kotlinx.coroutines.delay(2500)
            onSnackbarDismiss()
        }
    }

    //BAM
    LaunchedEffect(rawReadVersion) {
        val raw = rawReadText ?: return@LaunchedEffect

        val isBambuDump =
            raw.contains("Bambu RFID Parsed") ||
                    raw.contains("=== Sektor") ||
                    raw.contains("Block 0 (abs")

        if (isBambuDump) {
            bambuDialogText = raw
            showBambuDialog = true
        }
    }
    //

    LaunchedEffect(
        printerTool1SpoolId,
        printerTool2SpoolId,
        printerTool3SpoolId,
        printerTool4SpoolId,
        activePrinterSpoolId,
        printerMappingLoadVersion
    ) {
        toolhead1SpoolId = printerTool1SpoolId
        toolhead2SpoolId = printerTool2SpoolId
        toolhead3SpoolId = printerTool3SpoolId
        toolhead4SpoolId = printerTool4SpoolId
        activeDialogSpoolId = activePrinterSpoolId
    }

    fun isRemainingWeightValid(): Boolean = form.isRemainingWeightValid()
    fun isFormValid(): Boolean = form.isValid()
    fun validationMessage(): String? = form.validationMessage()
    fun buildSaveRequest(): SpoolmanSaveRequest =
        form.buildSaveRequest(spoolMode, selectedSpool)

    fun applyBambuDialogData() {
        val bambuData = parseBambuRfidFormData(
            text = bambuDialogText,
            fallbackMaterial = form.filamentType
        )

        val applyIntoForm = {
            onSpoolSelected(null)
            form.applyBambuRfidData(
                data = bambuData,
                suggestedColorName = bambuData.colorHex?.let(::suggestColorName).orEmpty()
            )

            showBambuDialog = false
            onBambuDataApplied()
        }

        val matchingSpool = findMatchingSpoolByLotNr(spools, bambuData.uid)

        when {
            matchingSpool == null -> {
                applyIntoForm()
            }

            isSameBambuData(
                spool = matchingSpool,
                material = bambuData.material,
                normalizedVariant = bambuData.normalizedVariant,
                colorHexValue = bambuData.colorHex
            ) -> {
                onSpoolSelected(matchingSpool)
                showBambuDialog = false
                onBambuExistingSpoolFound()
            }

            else -> {
                bambuDiffDialogText = buildBambuDiffText(
                    spool = matchingSpool,
                    material = bambuData.material,
                    normalizedVariant = bambuData.normalizedVariant,
                    colorHexValue = bambuData.colorHex
                )
                pendingBambuApply = applyIntoForm
                showBambuDialog = false
                showBambuDiffDialog = true
            }
        }
        onClearRawReadData()
    }

    val primaryActionLabel = when (spoolMode) {
        SpoolMode.CREATE -> "Write to Spoolman"
        SpoolMode.UPDATE -> "Update in Spoolman"
        SpoolMode.DUPLICATE -> "Duplicate in Spoolman"
    }

    val combinedActionLabel = when (spoolMode) {
        SpoolMode.CREATE -> "Write to Spoolman + RFID"
        SpoolMode.UPDATE -> "Update in Spoolman + RFID"
        SpoolMode.DUPLICATE -> "Duplicate in Spoolman + RFID"
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF3E7DE)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding(),
            verticalArrangement = Arrangement.Top
        ) {
            SpoolStudioHeader(
                spoolColor = spoolColor,
                onSettingsClick = onSettingsClick
            )

            Spacer(modifier = Modifier.height(0.dp))

            @OptIn(ExperimentalMaterial3Api::class)
            PullToRefreshBox(
                isRefreshing = isLoadingSpools,
                onRefresh = onRefreshSpools,
                indicator = {},
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).navigationBarsPadding().padding(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SpoolFormCard(
                        form = form,
                        spools = spools,
                        selectedSpool = selectedSpool,
                        spoolmanUrl = spoolmanUrl,
                        currentSpoolId = currentSpoolId,
                        isLoadingSpools = isLoadingSpools,
                        availableMaterials = availableMaterials,
                        availableBrands = availableBrands,
                        availableVariants = availableVariants,
                        availableLocations = availableLocations,
                        showLotNumber = showLotNumber,
                        showCommentField = showCommentField,
                        isRemainingWeightValid = isRemainingWeightValid(),
                        onSpoolSelected = onSpoolSelected,
                        onRefreshSelectedSpool = onRefreshSelectedSpool
                    )

                    validationMessage()?.let { message ->
                        ValidationMessageCard(message = message)
                    }

                    SpoolActionSection(
                        primaryActionLabel = primaryActionLabel,
                        combinedActionLabel = combinedActionLabel,
                        isFormValid = isFormValid(),
                        onReadTag = onReadTag,
                        onSaveToSpoolman = {
                            onCreateInSpoolman(buildSaveRequest())
                        },
                        onWriteTag = {
                            form.buildOpenSpoolTagData(spoolMode, selectedSpool)?.let { tagData ->
                                onWriteTag(tagData.toJson())
                            }
                        },
                        onSaveAndWriteTag = {
                            onCreateAndWriteTag(buildSaveRequest())
                        },
                        onCreateNewSpool = onCreateNewSpool,
                        onOpenPrinterMapping = {
                            toolhead1SpoolId = printerTool1SpoolId
                            toolhead2SpoolId = printerTool2SpoolId
                            toolhead3SpoolId = printerTool3SpoolId
                            toolhead4SpoolId = printerTool4SpoolId
                            activeDialogSpoolId = activePrinterSpoolId

                            showPrinterMappingDialog = true
                            onTestMoonrakerConnection()
                        }
                    )
                }
            }
        }

        if (showPrinterMappingDialog) {
            PrinterMappingDialog(
                spools = spools,
                isMoonrakerReachable = isMoonrakerReachable,
                isLoadingPrinterMapping = isLoadingPrinterMapping,
                activeSpoolOutsideMapping = activeSpoolOutsideMapping,
                activePrinterSpoolId = activePrinterSpoolId,
                inlineStatusText = inlinePrinterMappingStatusText,
                inlineStatusColor = inlinePrinterMappingStatusColor,
                hasPrinterMappingChanges = hasPrinterMappingChanges,
                toolhead1SpoolId = toolhead1SpoolId,
                toolhead2SpoolId = toolhead2SpoolId,
                toolhead3SpoolId = toolhead3SpoolId,
                toolhead4SpoolId = toolhead4SpoolId,
                activeDialogSpoolId = activeDialogSpoolId,
                onToolhead1SpoolIdChange = { toolhead1SpoolId = it },
                onToolhead2SpoolIdChange = { toolhead2SpoolId = it },
                onToolhead3SpoolIdChange = { toolhead3SpoolId = it },
                onToolhead4SpoolIdChange = { toolhead4SpoolId = it },
                onActiveDialogSpoolIdChange = { activeDialogSpoolId = it },
                onCancel = {
                    onClearPrinterMappingDialogFeedback()
                    showPrinterMappingDialog = false
                },
                onLoadCurrentPrinterMapping = onLoadCurrentPrinterMapping,
                onSavePrinterMapping = onSavePrinterMapping
            )
        }

        BambuRfidDumpDialog(
            visible = showBambuDialog,
            text = bambuDialogText,
            onDismiss = { showBambuDialog = false },
            onApply = { applyBambuDialogData() }
        )

        BambuRfidDiffDialog(
            visible = showBambuDiffDialog,
            text = bambuDiffDialogText,
            onDismiss = {
                showBambuDiffDialog = false
                pendingBambuApply = null
            },
            onUseExisting = {
                showBambuDiffDialog = false
                pendingBambuApply = null
            },
            onApplyBambuData = {
                showBambuDiffDialog = false
                pendingBambuApply?.invoke()
                pendingBambuApply = null
            }
        )

        CenteredSnackbarOverlay(
            message = snackbarMessage,
            visible = showSnackbar
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SpoolStudioScreenPreview() {
    MaterialTheme { SpoolStudioScreen(onWriteTag = { }, onReadTag = { }, dataVersion = 0) }
}
