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
import com.spoolstudio.app.ui.PendingTagConversion
import com.spoolstudio.app.ui.PrinterIntegrationMode
import com.spoolstudio.app.ui.ResolvedPrinterIntegrationMode
import com.spoolstudio.app.ui.SpoolMode
import com.spoolstudio.app.ui.SpoolmanSaveRequest
import com.spoolstudio.app.ui.theme.SpoolStudioColors
import com.spoolstudio.app.ui.theme.spoolStudioBackground
import com.spoolstudio.app.utils.*

@Composable
fun SpoolStudioScreen(
    onWriteTag: (String) -> Unit,
    onReadTag: () -> Unit,
    isPrinterSpoolmanReady: Boolean = false,
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
    printerIntegrationMode: PrinterIntegrationMode = PrinterIntegrationMode.PAXX12_SPOOL_LINK,
    resolvedPrinterIntegrationMode: ResolvedPrinterIntegrationMode? = null,
    isLoadingPrinterMapping: Boolean = false,
    printerMappingSaveSuccessful: Boolean? = null,
    printerMappingStatusMessage: String? = null,
    printerMappingOperation: String? = null,
    onClearPrinterMappingDialogFeedback: () -> Unit = {},
    onLoadCurrentPrinterMapping: () -> Unit = {},
    onAssignPrinterToolhead: (Int, Int?) -> Unit = { _, _ -> },
    showLotNumber: Boolean = false,
    showCommentField: Boolean = false,
    showEmptySpoolWeight: Boolean = false,
    materialModifierFieldEnabled: Boolean = false,
    showMaterialModifierFieldPrompt: Boolean = false,
    isCreatingMaterialModifierField: Boolean = false,
    onConfirmMaterialModifierField: () -> Unit = {},
    onDeclineMaterialModifierField: () -> Unit = {},
    onCreateNewSpool: () -> Unit = {},
    onCreateEmptySpool: () -> Unit = {},
    onCreateInSpoolman: (SpoolmanSaveRequest) -> Unit = {},
    isDeletingSpool: Boolean = false,
    isArchivingSpool: Boolean = false,
    onDeleteSelectedSpool: () -> Unit = {},
    onArchiveSelectedSpool: () -> Unit = {},
    pendingTagConversion: PendingTagConversion? = null,
    isConvertingTag: Boolean = false,
    onConfirmTagConversion: () -> Unit = {},
    onDeclineTagConversion: () -> Unit = {},
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
    var showArchiveSpoolDialog by remember { mutableStateOf(false) }
    var pendingTagWritePayload by remember { mutableStateOf<String?>(null) }

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
                        materialModifierFieldEnabled = materialModifierFieldEnabled,
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
                            writeOpenSpoolTagUseCase.buildPayload(form, spoolMode, selectedSpool)?.let { payload ->
                                if (isPrinterSpoolmanReady) {
                                    onWriteTag(payload)
                                } else {
                                    pendingTagWritePayload = payload
                                }
                            }
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
                            onClearPrinterMappingDialogFeedback()
                            showPrinterMappingDialog = true
                            onTestMoonrakerConnection()
                            onLoadCurrentPrinterMapping()
                        },
                        isDeleteSpoolEnabled = selectedSpool?.id != null && !isDeletingSpool && !isArchivingSpool,
                        onDeleteSelectedSpool = {
                            showDeleteSpoolDialog = true
                        },
                        isArchiveSpoolEnabled = selectedSpool?.id != null && !isDeletingSpool && !isArchivingSpool,
                        onArchiveSelectedSpool = {
                            showArchiveSpoolDialog = true
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
            printerIntegrationModeLabel = resolvedPrinterIntegrationMode?.label ?: printerIntegrationMode.label,
            inlineStatusText = inlinePrinterMappingStatusText,
            inlineStatusColor = inlinePrinterMappingStatusColor,
            toolhead1SpoolId = printerTool1SpoolId,
            toolhead2SpoolId = printerTool2SpoolId,
            toolhead3SpoolId = printerTool3SpoolId,
            toolhead4SpoolId = printerTool4SpoolId,
            onCancel = {
                onClearPrinterMappingDialogFeedback()
                showPrinterMappingDialog = false
            },
            onLoadCurrentPrinterMapping = onLoadCurrentPrinterMapping,
            onAssignPrinterToolhead = onAssignPrinterToolhead
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

        pendingTagWritePayload?.let { payload ->
            AlertDialog(
                onDismissRequest = { pendingTagWritePayload = null },
                containerColor = SpoolStudioColors.Graphite,
                title = {
                    Text(
                        text = "Printer Spoolman integration not ready",
                        color = SpoolStudioColors.OnGraphite
                    )
                },
                text = {
                    Text(
                        text = "At least one printer readiness check is not positive. The tag can still be written, but the printer may not auto-detect this spool until the firmware Spoolman integration is enabled and tested.",
                        color = SpoolStudioColors.OnGraphiteMuted
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            pendingTagWritePayload = null
                            onWriteTag(payload)
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = SpoolStudioColors.AccentCyan)
                    ) {
                        Text("Write anyway")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { pendingTagWritePayload = null },
                        colors = ButtonDefaults.textButtonColors(contentColor = SpoolStudioColors.GoldSoft)
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }

        if (showMaterialModifierFieldPrompt) {
            AlertDialog(
                onDismissRequest = {
                    if (!isCreatingMaterialModifierField) {
                        onDeclineMaterialModifierField()
                    }
                },
                containerColor = SpoolStudioColors.Surface,
                title = {
                    Text(
                        text = "Create Spoolman field?",
                        color = SpoolStudioColors.Ink
                    )
                },
                text = {
                    Text(
                        text = "This spool uses a material modifier such as Plus or HS. Spool Studio can create the official Spoolman extra field \"material_modifier\" and store the modifier there. If you decline, the app will save without these modifiers.",
                        color = SpoolStudioColors.Ink
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = onConfirmMaterialModifierField,
                        enabled = !isCreatingMaterialModifierField,
                        colors = ButtonDefaults.textButtonColors(contentColor = SpoolStudioColors.AccentCyan)
                    ) {
                        Text(if (isCreatingMaterialModifierField) "Creating..." else "Create field")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = onDeclineMaterialModifierField,
                        enabled = !isCreatingMaterialModifierField,
                        colors = ButtonDefaults.textButtonColors(contentColor = SpoolStudioColors.Gold)
                    ) {
                        Text("Use without modifier")
                    }
                }
            )
        }

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

        val spoolToArchive = selectedSpool
        if (showArchiveSpoolDialog && spoolToArchive?.id != null) {
            AlertDialog(
                onDismissRequest = { showArchiveSpoolDialog = false },
                containerColor = SpoolStudioColors.Surface,
                title = {
                    Text(
                        text = "Archive spool ID #${spoolToArchive.id}?",
                        color = SpoolStudioColors.Ink
                    )
                },
                text = {
                    Text(
                        text = "${spoolToArchive.brand} - ${spoolToArchive.spoolmanName ?: spoolToArchive.displayName} - ${spoolToArchive.displayName}\n\nThis archives the spool in Spoolman. RFID tags are not changed.",
                        color = SpoolStudioColors.Ink
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showArchiveSpoolDialog = false
                            onArchiveSelectedSpool()
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = SpoolStudioColors.Error)
                    ) {
                        Text("Archive")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showArchiveSpoolDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        pendingTagConversion?.let { conversion ->
            AlertDialog(
                onDismissRequest = {
                    if (!isConvertingTag) {
                        onDeclineTagConversion()
                    }
                },
                containerColor = SpoolStudioColors.Surface,
                title = {
                    Text(
                        text = "Convert old tag?",
                        color = SpoolStudioColors.Ink
                    )
                },
                text = {
                    Text(
                        text = "This tag uses the old spool_id mapping for spool #${conversion.spoolId} (${conversion.spoolName}). Spool Studio v3 writes only Paxx12 SpoolLink tags.\n\nConvert it by storing card UID ${conversion.cardUid} in Spoolman?",
                        color = SpoolStudioColors.Ink
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = onConfirmTagConversion,
                        enabled = !isConvertingTag,
                        colors = ButtonDefaults.textButtonColors(contentColor = SpoolStudioColors.AccentCyan)
                    ) {
                        Text(if (isConvertingTag) "Converting..." else "Convert")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = onDeclineTagConversion,
                        enabled = !isConvertingTag,
                        colors = ButtonDefaults.textButtonColors(contentColor = SpoolStudioColors.Gold)
                    ) {
                        Text("Use v2")
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
