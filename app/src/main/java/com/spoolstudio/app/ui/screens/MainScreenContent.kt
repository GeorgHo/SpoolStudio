package com.spoolstudio.app.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.spoolstudio.app.ui.MainViewModel
import androidx.compose.runtime.LaunchedEffect

@Composable
fun MainScreenContent(
    viewModel: MainViewModel,
    onWriteTag: (String) -> Unit,
    onReadTag: () -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadSpoolmanUrl(context)
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
            onBack = { viewModel.hideSettings() }
        )
    } else {
        SpoolStudioScreen(
            onWriteTag = onWriteTag,
            onReadTag = onReadTag,
            readData = viewModel.readData,
            rawReadText = viewModel.rawReadText,
            rawReadVersion = viewModel.rawReadVersion,
            dataVersion = viewModel.dataVersion,
            snackbarMessage = viewModel.snackbarMessage,
            showSnackbar = viewModel.showSnackbar,
            onClearRawReadData = { viewModel.clearRawReadData() },
            onSnackbarDismiss = { viewModel.dismissSnackbar() },
            onSettingsClick = { viewModel.showSettings() },
            onBambuDataApplied = { viewModel.showSnackbarMessage("Daten aus Bambu RFID übernommen") },
            spools = viewModel.spools,
            selectedSpool = viewModel.selectedSpool,
            isLoadingSpools = viewModel.isLoadingSpools,
            onSpoolSelected = { filament -> viewModel.handleFilamentSelection(filament) },
            onRefreshSpools = { viewModel.refreshSpools() },
            spoolmanUrl = viewModel.spoolmanUrl,
            currentSpoolId = viewModel.currentSpoolId,
            availableBrands = viewModel.availableBrands,
            availableMaterials = viewModel.availableMaterials,
            availableVariants = viewModel.availableVariants,
            availableLocations = viewModel.availableLocations,
            spoolMode = viewModel.spoolMode,
            onDuplicateSpool = { viewModel.duplicateCurrentSpool() },
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
            onBambuExistingSpoolFound = {
                viewModel.showSnackbarMessage("Identical spool found in Spoolman")
            },
            onCreateInSpoolman = { material, variant, brand, location, colorHex, colorName, minTemp, maxTemp, bedMinTemp, bedMaxTemp, lotNr, comment, existingSpoolId ->
                viewModel.saveToSpoolman(
                    material,
                    variant,
                    brand,
                    location,
                    colorHex,
                    colorName,
                    minTemp,
                    maxTemp,
                    bedMinTemp,
                    bedMaxTemp,
                    lotNr,
                    comment,
                    existingSpoolId
                )
            },
            onCreateNewSpool = {
                viewModel.createNewSpoolFromCurrent()
            },
            onCreateAndWriteTag = { material, variant, brand, location, colorHex, colorName, minTemp, maxTemp, bedMinTemp, bedMaxTemp, lotNr, comment, existingSpoolId ->
                viewModel.saveToSpoolmanAndWriteTag(
                    material,
                    variant,
                    brand,
                    location,
                    colorHex,
                    colorName,
                    minTemp,
                    maxTemp,
                    bedMinTemp,
                    bedMaxTemp,
                    lotNr,
                    comment,
                    existingSpoolId
                ) { ndefMessage ->
                    onWriteTag(ndefMessage)
                }
            }
        )
    }
}
