package com.spoolstudio.app.ui.screens

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import com.spoolstudio.app.domain.models.FilamentSpool
import com.spoolstudio.app.domain.models.OpenSpoolData
import com.spoolstudio.app.ui.SpoolMode

private val fallbackSpoolColor = Color(0xFF4A423D)

fun resolveSpoolColor(colorHex: String?): Color {
    val normalized = colorHex?.trim()?.removePrefix("#").orEmpty()

    return if (normalized.matches(Regex("^[A-Fa-f0-9]{6}$"))) {
        Color(android.graphics.Color.parseColor("#$normalized"))
    } else {
        fallbackSpoolColor
    }
}

fun spoolActionLabel(spoolMode: SpoolMode): String =
    when (spoolMode) {
        SpoolMode.CREATE -> "Write to Spoolman"
        SpoolMode.UPDATE -> "Update in Spoolman"
        SpoolMode.DUPLICATE -> "Duplicate in Spoolman"
    }

fun printerMappingBusyLabel(operation: String?): String? =
    when (operation) {
        "load" -> "Loading printer mapping..."
        "save" -> "Saving printer mapping..."
        else -> null
    }

fun hasPrinterMappingChanges(
    toolhead1SpoolId: Int?,
    toolhead2SpoolId: Int?,
    toolhead3SpoolId: Int?,
    toolhead4SpoolId: Int?,
    activeDialogSpoolId: Int?,
    printerTool1SpoolId: Int?,
    printerTool2SpoolId: Int?,
    printerTool3SpoolId: Int?,
    printerTool4SpoolId: Int?,
    activePrinterSpoolId: Int?
): Boolean =
    toolhead1SpoolId != printerTool1SpoolId ||
        toolhead2SpoolId != printerTool2SpoolId ||
        toolhead3SpoolId != printerTool3SpoolId ||
        toolhead4SpoolId != printerTool4SpoolId ||
        activeDialogSpoolId != activePrinterSpoolId

fun isActiveSpoolOutsideMapping(
    activePrinterSpoolId: Int?,
    toolhead1SpoolId: Int?,
    toolhead2SpoolId: Int?,
    toolhead3SpoolId: Int?,
    toolhead4SpoolId: Int?
): Boolean =
    activePrinterSpoolId != null &&
        activePrinterSpoolId !in listOf(
            toolhead1SpoolId,
            toolhead2SpoolId,
            toolhead3SpoolId,
            toolhead4SpoolId
        )

fun isWriteActionEnabled(form: SpoolFormState): Boolean =
    form.isValid() && !form.colorHex.isNullOrBlank()

fun hasSpoolmanChanges(
    form: SpoolFormState,
    spoolMode: SpoolMode,
    selectedSpool: FilamentSpool?
): Boolean =
    spoolMode != SpoolMode.UPDATE ||
        (selectedSpool != null &&
            hasSpoolmanSaveChanges(form.buildSaveRequest(spoolMode, selectedSpool), selectedSpool))

fun isSaveToSpoolmanEnabled(
    form: SpoolFormState,
    spoolMode: SpoolMode,
    selectedSpool: FilamentSpool?
): Boolean =
    isWriteActionEnabled(form) && hasSpoolmanChanges(form, spoolMode, selectedSpool)

fun isNewFromSelectedEnabled(
    spoolMode: SpoolMode,
    selectedSpool: FilamentSpool?,
    readData: OpenSpoolData?
): Boolean =
    spoolMode != SpoolMode.CREATE && (selectedSpool != null || readData != null)

fun printerMappingStatusText(
    isLoadingPrinterMapping: Boolean,
    printerMappingOperation: String?,
    printerMappingStatusMessage: String?
): String? =
    when {
        isLoadingPrinterMapping -> printerMappingBusyLabel(printerMappingOperation)
        !printerMappingStatusMessage.isNullOrBlank() -> printerMappingStatusMessage
        else -> null
    }

fun printerMappingStatusColor(
    colorScheme: ColorScheme,
    isLoadingPrinterMapping: Boolean,
    printerMappingSaveSuccessful: Boolean?
): Color =
    when {
        isLoadingPrinterMapping -> colorScheme.primary
        printerMappingSaveSuccessful == true -> colorScheme.primary
        printerMappingSaveSuccessful == false -> colorScheme.error
        else -> colorScheme.onSurfaceVariant
    }
