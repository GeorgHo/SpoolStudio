package com.spoolstudio.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.spoolstudio.app.data.local.MaterialDatabase
import com.spoolstudio.app.domain.models.FilamentSpool
import com.spoolstudio.app.domain.models.OpenSpoolData
import com.spoolstudio.app.ui.SpoolMode
import com.spoolstudio.app.ui.SpoolmanSaveRequest
import com.spoolstudio.app.ui.theme.SpoolStudioColors
import com.spoolstudio.app.ui.theme.spoolStudioBackground
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
    snackbarAutoDismiss: Boolean = true,
    onSnackbarDismiss: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onBambuDataApplied: () -> Unit = {},
    onBambuExistingSpoolFound: () -> Unit = {},
    spools: List<FilamentSpool> = emptyList(),
    selectedSpool: FilamentSpool? = null,
    isLoadingSpools: Boolean = false,
    onSpoolSelected: (FilamentSpool?) -> Unit = {},
    onRefreshSpools: () -> Unit = {},
    onRefreshSpoolmanCatalogIfStale: () -> Unit = {},
    onRefreshSelectedSpool: (Int) -> Unit = {},
    spoolmanUrl: String = "",
    currentSpoolId: String? = null,
    availableBrands: List<String> = emptyList(),
    availableMaterials: List<String> = emptyList(),
    availableVariants: List<String> = emptyList(),
    availableLocations: List<String> = emptyList(),
    spoolMode: SpoolMode = SpoolMode.CREATE,
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
    onClearPrinterMappingDialogFeedback: () -> Unit = {},
    onLoadCurrentPrinterMapping: () -> Unit = {},
    onSavePrinterMapping: (Int?, Int?, Int?, Int?, Int?) -> Unit = { _, _, _, _, _ -> },
    showLotNumber: Boolean = false,
    showCommentField: Boolean = false,
    showEmptySpoolWeight: Boolean = false,
    onCreateNewSpool: () -> Unit = {},
    onCreateEmptySpool: () -> Unit = {},
    onCreateInSpoolman: (SpoolmanSaveRequest) -> Unit = {},
    isDeletingSpool: Boolean = false,
    onDeleteSelectedSpool: () -> Unit = {},
) {
    var showBambuDialog by remember { mutableStateOf(false) }
    var bambuDialogText by remember { mutableStateOf("") }
    var showBambuDiffDialog by remember { mutableStateOf(false) }
    var bambuDiffDialogText by remember { mutableStateOf("") }
    var pendingBambuApply by remember { mutableStateOf<(() -> Unit)?>(null) }
    var preserveManualCreateForm by remember { mutableStateOf(false) }
    val defaultMaterial = MaterialDatabase.getMaterial("PLA") ?: MaterialDatabase.materials.first()
    val form = remember { SpoolFormState(defaultMaterial) }
    val writeOpenSpoolTagUseCase = remember { WriteOpenSpoolTagUseCase() }
    var showPrinterMappingDialog by remember { mutableStateOf(false) }
    var showDeleteSpoolDialog by remember { mutableStateOf(false) }
    var printerMappingDialogSelection by remember { mutableStateOf(PrinterMappingSelection()) }
    val printerMappingSelection = printerMappingSelection(
        toolhead1SpoolId = printerTool1SpoolId,
        toolhead2SpoolId = printerTool2SpoolId,
        toolhead3SpoolId = printerTool3SpoolId,
        toolhead4SpoolId = printerTool4SpoolId,
        activeSpoolId = activePrinterSpoolId
    )

    val hasPrinterMappingChanges = hasPrinterMappingChanges(
        dialogSelection = printerMappingDialogSelection,
        printerSelection = printerMappingSelection
    )

    val activeSpoolOutsideMapping = isActiveSpoolOutsideMapping(
        activePrinterSpoolId = activePrinterSpoolId,
        dialogSelection = printerMappingDialogSelection
    )

    val isWriteActionEnabled = isWriteActionEnabled(form)
    val isSaveToSpoolmanEnabled = isSaveToSpoolmanEnabled(form, spoolMode, selectedSpool)
    val isNewFromSelectedEnabled = isNewFromSelectedEnabled(spoolMode, selectedSpool, readData)
    val inlinePrinterMappingStatusColor = printerMappingStatusColor(
        colorScheme = MaterialTheme.colorScheme,
        isLoadingPrinterMapping = isLoadingPrinterMapping,
        printerMappingSaveSuccessful = printerMappingSaveSuccessful
    )
    val inlinePrinterMappingStatusText = printerMappingStatusText(
        isLoadingPrinterMapping = isLoadingPrinterMapping,
        printerMappingOperation = printerMappingOperation,
        printerMappingStatusMessage = printerMappingStatusMessage
    )

    fun isRemainingWeightValid(): Boolean = form.isRemainingWeightValid()
    fun validationMessage(): String? = form.validationMessage()
    fun buildSaveRequest(): SpoolmanSaveRequest =
        form.buildSaveRequest(spoolMode, selectedSpool)

    fun handleSpoolSelected(spool: FilamentSpool?) {
        preserveManualCreateForm = false
        onSpoolSelected(spool)
    }

    fun clearAllSpoolmanFields() {
        preserveManualCreateForm = false
        onSpoolSelected(null)
        form.resetForNewSpool()
    }

    fun applyBambuRfidText(text: String) {
        val decision = resolveBambuRfidApplyDecision(
            text = text,
            fallbackMaterial = form.filamentType,
            spools = spools
        )

        fun applyIntoForm(bambuData: BambuRfidFormData) {
            preserveManualCreateForm = true
            onSpoolSelected(null)
            form.applyBambuRfidData(
                data = bambuData,
                suggestedColorName = bambuData.colorHex?.let(::suggestColorName).orEmpty()
            )

            showBambuDialog = false
            showBambuDiffDialog = false
            onBambuDataApplied()
        }

        when (decision) {
            is BambuRfidApplyDecision.ApplyNewData -> {
                applyIntoForm(decision.data)
            }

            is BambuRfidApplyDecision.UseExistingSpool -> {
                preserveManualCreateForm = false
                onSpoolSelected(decision.spool)
                showBambuDialog = false
                onBambuExistingSpoolFound()
            }

            is BambuRfidApplyDecision.ShowDifference -> {
                bambuDiffDialogText = decision.diffText
                pendingBambuApply = { applyIntoForm(decision.data) }
                showBambuDialog = false
                showBambuDiffDialog = true
            }
        }
        onClearRawReadData()
    }

    fun applyBambuDialogData() {
        applyBambuRfidText(bambuDialogText)
    }

    SpoolStudioFormEffects(
        form = form,
        readData = readData,
        dataVersion = dataVersion,
        selectedSpool = selectedSpool,
        spoolMode = spoolMode,
        availableLocations = availableLocations,
        suppressCreateReset = preserveManualCreateForm
    )

    SnackbarAutoDismissEffect(
        showSnackbar = showSnackbar,
        snackbarMessage = snackbarMessage,
        autoDismiss = snackbarAutoDismiss,
        onSnackbarDismiss = onSnackbarDismiss
    )

    BambuRfidDumpEffect(
        rawReadVersion = rawReadVersion,
        rawReadText = rawReadText,
        onBambuDumpDetected = { applyBambuRfidText(it) }
    )

    PrinterMappingSelectionSyncEffect(
        toolhead1SpoolId = printerTool1SpoolId,
        toolhead2SpoolId = printerTool2SpoolId,
        toolhead3SpoolId = printerTool3SpoolId,
        toolhead4SpoolId = printerTool4SpoolId,
        activePrinterSpoolId = activePrinterSpoolId,
        printerMappingLoadVersion = printerMappingLoadVersion,
        printerMappingSelection = printerMappingSelection,
        onSelectionChange = { printerMappingDialogSelection = it }
    )

    val primaryActionLabel = spoolActionLabel(spoolMode)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = SpoolStudioColors.ScreenBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .spoolStudioBackground()
                .statusBarsPadding()
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.Top
        ) {
            SpoolStudioHeader(
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
                        spoolMode = spoolMode,
                        spoolmanUrl = spoolmanUrl,
                        currentSpoolId = currentSpoolId,
                        isLoadingSpools = isLoadingSpools,
                        availableMaterials = availableMaterials,
                        availableBrands = availableBrands,
                        availableVariants = availableVariants,
                        availableLocations = availableLocations,
                        showLotNumber = showLotNumber,
                        showCommentField = showCommentField,
                        showEmptySpoolWeight = showEmptySpoolWeight,
                        isRemainingWeightValid = isRemainingWeightValid(),
                        onSpoolSelected = { handleSpoolSelected(it) },
                        onClearAllSpoolFields = { clearAllSpoolmanFields() },
                        onRefreshSelectedSpool = onRefreshSelectedSpool,
                        onRefreshSpoolmanCatalogIfStale = onRefreshSpoolmanCatalogIfStale
                    )

                    validationMessage()?.let { message ->
                        ValidationMessageCard(message = message)
                    }

                    SpoolActionSection(
                        primaryActionLabel = primaryActionLabel,
                        isCreateMode = spoolMode != SpoolMode.UPDATE,
                        isSaveToSpoolmanEnabled = isSaveToSpoolmanEnabled,
                        isWriteTagEnabled = isWriteActionEnabled,
                        onReadTag = onReadTag,
                        onSaveToSpoolman = {
                            onCreateInSpoolman(buildSaveRequest())
                        },
                        onWriteTag = {
                            writeOpenSpoolTagUseCase.buildPayload(form, spoolMode, selectedSpool)?.let(onWriteTag)
                        },
                        isNewFromSelectedEnabled = isNewFromSelectedEnabled,
                        onCreateNewSpool = {
                            preserveManualCreateForm = false
                            onCreateNewSpool()
                        },
                        onCreateEmptySpool = {
                            preserveManualCreateForm = false
                            onCreateEmptySpool()
                        },
                        onOpenPrinterMapping = {
                            printerMappingDialogSelection = printerMappingSelection

                            showPrinterMappingDialog = true
                            onTestMoonrakerConnection()
                        },
                        isDeleteSpoolEnabled = selectedSpool?.id != null && !isDeletingSpool,
                        onDeleteSelectedSpool = {
                            showDeleteSpoolDialog = true
                        }
                    )
                }
            }
        }

        PrinterMappingDialogHost(
            visible = showPrinterMappingDialog,
            spools = spools,
            isMoonrakerReachable = isMoonrakerReachable,
            isLoadingPrinterMapping = isLoadingPrinterMapping,
            activeSpoolOutsideMapping = activeSpoolOutsideMapping,
            activePrinterSpoolId = activePrinterSpoolId,
            inlineStatusText = inlinePrinterMappingStatusText,
            inlineStatusColor = inlinePrinterMappingStatusColor,
            hasPrinterMappingChanges = hasPrinterMappingChanges,
            toolhead1SpoolId = printerMappingDialogSelection.toolhead1SpoolId,
            toolhead2SpoolId = printerMappingDialogSelection.toolhead2SpoolId,
            toolhead3SpoolId = printerMappingDialogSelection.toolhead3SpoolId,
            toolhead4SpoolId = printerMappingDialogSelection.toolhead4SpoolId,
            activeDialogSpoolId = printerMappingDialogSelection.activeSpoolId,
            onToolhead1SpoolIdChange = {
                printerMappingDialogSelection = printerMappingDialogSelection.withToolhead1(it)
            },
            onToolhead2SpoolIdChange = {
                printerMappingDialogSelection = printerMappingDialogSelection.withToolhead2(it)
            },
            onToolhead3SpoolIdChange = {
                printerMappingDialogSelection = printerMappingDialogSelection.withToolhead3(it)
            },
            onToolhead4SpoolIdChange = {
                printerMappingDialogSelection = printerMappingDialogSelection.withToolhead4(it)
            },
            onActiveDialogSpoolIdChange = {
                printerMappingDialogSelection = printerMappingDialogSelection.withActiveSpool(it, it != null)
            },
            onCancel = {
                onClearPrinterMappingDialogFeedback()
                showPrinterMappingDialog = false
            },
            onLoadCurrentPrinterMapping = onLoadCurrentPrinterMapping,
            onSavePrinterMapping = onSavePrinterMapping
        )

        BambuRfidDialogHost(
            showDumpDialog = showBambuDialog,
            dumpText = bambuDialogText,
            showDiffDialog = showBambuDiffDialog,
            diffText = bambuDiffDialogText,
            onDismissDump = { showBambuDialog = false },
            onApplyDump = { applyBambuDialogData() },
            onDismissDiff = {
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

        val spoolToDelete = selectedSpool
        if (showDeleteSpoolDialog && spoolToDelete?.id != null) {
            AlertDialog(
                onDismissRequest = { showDeleteSpoolDialog = false },
                containerColor = SpoolStudioColors.Surface,
                title = {
                    Text(
                        text = "Delete spool ID #${spoolToDelete.id}?",
                        color = SpoolStudioColors.Ink
                    )
                },
                text = {
                    Text(
                        text = "${spoolToDelete.brand} - ${spoolToDelete.spoolmanName ?: spoolToDelete.displayName} - ${spoolToDelete.displayName}\n\nThis deletes the spool from Spoolman only. RFID tags are not changed.",
                        color = SpoolStudioColors.Ink
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDeleteSpoolDialog = false
                            onDeleteSelectedSpool()
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = SpoolStudioColors.Error)
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteSpoolDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

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
