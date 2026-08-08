package com.spoolstudio.app.ui.screens

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.spoolstudio.app.ui.MainViewModel
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun MainScreenContent(
    viewModel: MainViewModel,
    onWriteTag: (String) -> Unit,
    onReadTag: () -> Unit
) {
    val context = LocalContext.current
    var lastBackPressTime by remember { mutableStateOf(0L) }

    LaunchedEffect(Unit) {
        viewModel.loadSpoolmanUrl(context)
    }

    BackHandler {
        if (viewModel.showSettings) {
            viewModel.hideSettings()
        } else {
            val now = System.currentTimeMillis()
            if (now - lastBackPressTime < 2000L) {
                (context as? Activity)?.finishAndRemoveTask()
            } else {
                lastBackPressTime = now
                viewModel.showSnackbarMessage("Press back again to close Spool Studio")
            }
        }
    }

    if (viewModel.showSettings) {
        SettingsScreen(
            spoolmanUrl = viewModel.spoolmanUrl,
            moonrakerUrl = viewModel.moonrakerUrl,
            bambuMasterKey = viewModel.bambuMasterKey,
            spoolmanSortBy = viewModel.spoolmanSortBy,
            snackbarMessage = viewModel.snackbarMessage,
            showSnackbar = viewModel.showSnackbar,
            showCommentField = viewModel.showCommentField,
            showEmptySpoolWeight = viewModel.showEmptySpoolWeight,
            printerIntegrationMode = viewModel.printerIntegrationMode,
            spoolCount = viewModel.spools.size,
            activeSpoolCount = viewModel.spools.count { !it.archived },
            archivedSpoolCount = viewModel.spools.count { it.archived },
            spoolmanBrandCount = viewModel.availableBrands.size,
            spoolmanMaterialCount = viewModel.availableMaterials.size,
            spoolmanLocationCount = viewModel.availableLocations.size,
            spoolmanColorCount = viewModel.spools.mapNotNull { it.colorHex?.takeIf(String::isNotBlank) }.distinct().size,
            spoolmanCardUidFieldSpoolCount = viewModel.spoolmanCardUidFieldSpoolCount,
            spoolmanCardUidFieldKeys = viewModel.spoolmanCardUidFieldKeys,
            spoolmanMaterialModifierFieldAvailable = viewModel.spoolmanMaterialModifierFieldAvailable,
            materialModifierFieldDeclined = viewModel.materialModifierFieldDeclined,
            isCreatingMaterialModifierField = viewModel.isCreatingMaterialModifierField,
            moonrakerFirmwareVersion = viewModel.moonrakerFirmwareVersion,
            moonrakerVersion = viewModel.moonrakerVersion,
            moonrakerSupportsSpoolLink = viewModel.moonrakerSupportsSpoolLink,
            moonrakerHasSpoolmanComponent = viewModel.moonrakerHasSpoolmanComponent,
            moonrakerHasSpoolLinkComponent = viewModel.moonrakerHasSpoolLinkComponent,
            moonrakerSpoolmanIntegrationEnabled = viewModel.moonrakerSpoolmanIntegrationEnabled,
            moonrakerAssignmentCommandAvailable = viewModel.moonrakerAssignmentCommandAvailable,
            moonrakerDetectedModeLabel = viewModel.moonrakerDetectedModeLabel,
            legacyFilamentConversions = viewModel.legacyFilamentConversions,
            isScanningLegacyFilaments = viewModel.isScanningLegacyFilaments,
            isConvertingLegacyFilaments = viewModel.isConvertingLegacyFilaments,
            onSnackbarDismiss = { viewModel.dismissSnackbar() },

            onTestMoonrakerConnection = { url ->
                viewModel.testMoonrakerConnection(url)
            },

            onTestSpoolmanConnection = { url ->
                viewModel.testSpoolmanConnection(url)
            },
            onCreateMaterialModifierField = { url ->
                viewModel.createMaterialModifierField(context, url)
            },
            onScanLegacyFilamentConversions = { url ->
                viewModel.scanLegacyFilamentConversions(url)
            },
            onClearLegacyFilamentConversions = {
                viewModel.clearLegacyFilamentConversions()
            },
            onConvertLegacyFilaments = { url, ids ->
                viewModel.convertLegacyFilaments(ids, url)
            },
            onClearSpoolmanStatus = {
                viewModel.clearSpoolmanStatus()
            },
            onClearMoonrakerStatus = {
                viewModel.clearMoonrakerStatus()
            },
            spoolmanStatus = viewModel.spoolmanStatus,
            spoolmanError = viewModel.spoolmanError,
            isTestingSpoolman = viewModel.isTestingSpoolman,
            moonrakerStatus = viewModel.moonrakerStatus,
            moonrakerError = viewModel.moonrakerError,
            isTestingMoonraker = viewModel.isTestingMoonraker,
            onSave = { newUrl, newMoonrakerUrl, newPrinterIntegrationMode, newSort, newBambuKey, newShowCommentField ->
                viewModel.handleSettingsSave(
                    context,
                    newUrl,
                    newMoonrakerUrl,
                    newPrinterIntegrationMode,
                    newSort,
                    newBambuKey,
                    newShowCommentField
                )
            },
            showLotNumber = viewModel.showLotNumber,
            onShowLotNumberChanged = { value ->
                viewModel.setShowLotNumber(context, value)
            },
            onShowEmptySpoolWeightChanged = { value ->
                viewModel.setShowEmptySpoolWeight(context, value)
            },
            onBack = { viewModel.hideSettings() }
        )
    } else {
        SpoolStudioScreen(
            onWriteTag = { data ->
                viewModel.preparePendingTagWriteLink()
                viewModel.showSnackbarMessage("RFID write ready. Hold tag near the phone.")
                onWriteTag(data)
            },
            onReadTag = onReadTag,
            isPrinterSpoolmanReady = viewModel.moonrakerSupportsSpoolLink == true &&
                viewModel.moonrakerHasSpoolmanComponent == true &&
                viewModel.moonrakerHasSpoolLinkComponent == true &&
                viewModel.moonrakerSpoolmanIntegrationEnabled == true &&
                viewModel.moonrakerAssignmentCommandAvailable == true,
            readData = viewModel.readData,
            rawReadText = viewModel.rawReadText,
            rawReadVersion = viewModel.rawReadVersion,
            dataVersion = viewModel.dataVersion,
            snackbarMessage = viewModel.snackbarMessage,
            showSnackbar = viewModel.showSnackbar,
            snackbarAutoDismiss = viewModel.snackbarAutoDismiss,
            onClearRawReadData = { viewModel.clearRawReadData() },
            onSnackbarDismiss = { viewModel.dismissSnackbar() },
            onSettingsClick = { viewModel.showSettings() },
            onBambuDataApplied = { viewModel.showSnackbarMessage("Bambu RFID data applied") },
            spools = viewModel.spools,
            selectedSpool = viewModel.selectedSpool,
            isLoadingSpools = viewModel.isLoadingSpools,
            onSpoolSelected = { filament -> viewModel.handleFilamentSelection(filament) },
            onRefreshSpools = { viewModel.refreshSpools() },
            onRefreshSpoolmanCatalogIfStale = { viewModel.refreshSpoolmanCatalogIfStale() },
            spoolmanUrl = viewModel.spoolmanUrl,
            currentSpoolId = viewModel.currentSpoolId,
            availableBrands = viewModel.availableBrands,
            availableMaterials = viewModel.availableMaterials,
            availableVariants = viewModel.availableVariants,
            availableLocations = viewModel.availableLocations,
            spoolMode = viewModel.spoolMode,
            isMoonrakerReachable = viewModel.isMoonrakerReachable,
            printerTool1SpoolId = viewModel.printerTool1SpoolId,
            printerTool2SpoolId = viewModel.printerTool2SpoolId,
            printerTool3SpoolId = viewModel.printerTool3SpoolId,
            printerTool4SpoolId = viewModel.printerTool4SpoolId,
            isLoadingPrinterMapping = viewModel.isLoadingPrinterMapping,
            onLoadCurrentPrinterMapping = { viewModel.loadCurrentPrinterMapping() },
            onAssignPrinterToolhead = { toolheadIndex, spoolId ->
                viewModel.assignPrinterToolhead(toolheadIndex, spoolId)
            },
            onTestMoonrakerConnection = { viewModel.testMoonrakerConnection() },
            onRefreshSelectedSpool = { id -> viewModel.refreshSelectedSpool(id) },
            printerIntegrationMode = viewModel.printerIntegrationMode,
            resolvedPrinterIntegrationMode = viewModel.resolvedPrinterIntegrationMode,
            printerMappingSaveSuccessful = viewModel.printerMappingSaveSuccessful,
            showLotNumber = viewModel.showLotNumber,
            showCommentField = viewModel.showCommentField,
            showEmptySpoolWeight = viewModel.showEmptySpoolWeight,
            materialModifierFieldEnabled = viewModel.materialModifierFieldEnabled,
            showMaterialModifierFieldPrompt = viewModel.showMaterialModifierFieldPrompt,
            isCreatingMaterialModifierField = viewModel.isCreatingMaterialModifierField,
            onConfirmMaterialModifierField = { viewModel.confirmMaterialModifierFieldCreation(context) },
            onDeclineMaterialModifierField = { viewModel.declineMaterialModifierFieldCreation(context) },
            onBambuExistingSpoolFound = {
                viewModel.showSnackbarMessage("Identical spool found in Spoolman")
            },
            onCreateInSpoolman = { request ->
                viewModel.saveToSpoolman(context, request)
            },
            onCreateNewSpool = {
                viewModel.duplicateCurrentSpool()
            },
            onCreateEmptySpool = {
                viewModel.createEmptySpool()
            },
            isDeletingSpool = viewModel.isDeletingSpool,
            isArchivingSpool = viewModel.isArchivingSpool,
            onDeleteSelectedSpool = {
                viewModel.deleteSelectedSpool()
            },
            onArchiveSelectedSpool = {
                viewModel.archiveSelectedSpool()
            },
            pendingTagConversion = viewModel.pendingTagConversion,
            isConvertingTag = viewModel.isConvertingTag,
            onConfirmTagConversion = { viewModel.confirmTagConversion(context) },
            onDeclineTagConversion = { viewModel.declineTagConversion() }
        )
    }
}
