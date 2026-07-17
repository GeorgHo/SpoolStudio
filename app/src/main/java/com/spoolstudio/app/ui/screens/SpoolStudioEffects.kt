package com.spoolstudio.app.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.spoolstudio.app.domain.models.FilamentSpool
import com.spoolstudio.app.domain.models.OpenSpoolData
import com.spoolstudio.app.ui.SpoolMode
import com.spoolstudio.app.utils.suggestColorName
import kotlinx.coroutines.delay

@Composable
fun SpoolStudioFormEffects(
    form: SpoolFormState,
    readData: OpenSpoolData?,
    dataVersion: Int,
    selectedSpool: FilamentSpool?,
    spoolMode: SpoolMode,
    availableLocations: List<String>,
    suppressCreateReset: Boolean = false
) {
    LaunchedEffect(readData, dataVersion, selectedSpool, spoolMode, availableLocations, suppressCreateReset) {
        val sourceSpool = selectedSpool ?: readData?.let { FilamentSpool.fromOpenSpool(it) }

        if (sourceSpool != null) {
            form.applySpoolSource(
                sourceSpool = sourceSpool,
                spoolMode = spoolMode,
                availableLocations = availableLocations
            )
        } else if (spoolMode == SpoolMode.CREATE && !suppressCreateReset) {
            form.resetForNewSpool()
        }
    }

    LaunchedEffect(form.colorHex) {
        form.colorHexInput = form.colorHex ?: ""
        if (!form.colorNameWasManuallyEdited) {
            val suggested = suggestColorName(form.colorHex)
            if (suggested.isNotBlank()) {
                form.colorName = suggested
            } else if (form.colorHex.isNullOrBlank()) {
                form.colorName = ""
            }
        }
    }
}

@Composable
fun SnackbarAutoDismissEffect(
    showSnackbar: Boolean,
    snackbarMessage: String,
    autoDismiss: Boolean,
    onSnackbarDismiss: () -> Unit
) {
    LaunchedEffect(showSnackbar, snackbarMessage, autoDismiss) {
        if (autoDismiss && showSnackbar && snackbarMessage.isNotBlank()) {
            delay(2500)
            onSnackbarDismiss()
        }
    }
}

@Composable
fun BambuRfidDumpEffect(
    rawReadVersion: Int,
    rawReadText: String?,
    onBambuDumpDetected: (String) -> Unit
) {
    LaunchedEffect(rawReadVersion) {
        val raw = rawReadText ?: return@LaunchedEffect

        if (isBambuRfidDump(raw)) {
            onBambuDumpDetected(raw)
        }
    }
}

@Composable
fun PrinterMappingSelectionSyncEffect(
    toolhead1SpoolId: Int?,
    toolhead2SpoolId: Int?,
    toolhead3SpoolId: Int?,
    toolhead4SpoolId: Int?,
    activePrinterSpoolId: Int?,
    printerMappingLoadVersion: Int,
    printerMappingSelection: PrinterMappingSelection,
    onSelectionChange: (PrinterMappingSelection) -> Unit
) {
    LaunchedEffect(
        toolhead1SpoolId,
        toolhead2SpoolId,
        toolhead3SpoolId,
        toolhead4SpoolId,
        activePrinterSpoolId,
        printerMappingLoadVersion
    ) {
        onSelectionChange(printerMappingSelection)
    }
}
