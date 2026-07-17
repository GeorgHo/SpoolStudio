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
            spoolCount = viewModel.spools.size,
            activeSpoolCount = viewModel.spools.count { !it.archived },
            archivedSpoolCount = viewModel.spools.count { it.archived },
            spoolmanBrandCount = viewModel.availableBrands.size,
            spoolmanMaterialCount = viewModel.availableMaterials.size,
            spoolmanLocationCount = viewModel.availableLocations.size,
            spoolmanColorCount = viewModel.spools.mapNotNull { it.colorHex?.takeIf(String::isNotBlank) }.distinct().size,
            onSnackbarDismiss = { viewModel.dismissSnackbar() },

            onTestMoonrakerConnection = { url ->
                viewModel.testMoonrakerConnection(url)
            },

            onTestSpoolmanConnection = { url ->
                viewModel.testSpoolmanConnection(url)
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
            onSave = { newUrl, newMoonrakerUrl, newSort, newBambuKey, newShowCommentField ->
                viewModel.handleSettingsSave(
                    context,
                    newUrl,
                    newMoonrakerUrl,
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
                viewModel.showSnackbarMessage("RFID write ready. Hold tag near the phone.")
                onWriteTag(data)
            },
            onReadTag = onReadTag,
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
            printerMappingLoadVersion = viewModel.printerMappingLoadVersion,
            isLoadingPrinterMapping = viewModel.isLoadingPrinterMapping,
            onLoadCurrentPrinterMapping = { viewModel.loadCurrentPrinterMapping() },
            onTestMoonrakerConnection = { viewModel.testMoonrakerConnection() },
            onRefreshSelectedSpool = { id -> viewModel.refreshSelectedSpool(id) },
            onSavePrinterMapping = { e0, e1, e2, e3, activeSpoolId ->
                viewModel.savePrinterMapping(e0, e1, e2, e3, activeSpoolId)
            },
            activePrinterSpoolId = viewModel.activePrinterSpoolId,
            printerMappingSaveSuccessful = viewModel.printerMappingSaveSuccessful,
            showLotNumber = viewModel.showLotNumber,
            showCommentField = viewModel.showCommentField,
            showEmptySpoolWeight = viewModel.showEmptySpoolWeight,
            onBambuExistingSpoolFound = {
                viewModel.showSnackbarMessage("Identical spool found in Spoolman")
            },
            onCreateInSpoolman = { request ->
                viewModel.saveToSpoolman(request)
            },
            onCreateNewSpool = {
                viewModel.duplicateCurrentSpool()
            },
            onCreateEmptySpool = {
                viewModel.createEmptySpool()
            },
            isDeletingSpool = viewModel.isDeletingSpool,
            onDeleteSelectedSpool = {
                viewModel.deleteSelectedSpool()
            }
        )
    }
}
