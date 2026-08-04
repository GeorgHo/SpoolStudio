package com.spoolstudio.app.ui

private const val unknownPrinterMappingError = "Unknown printer error"

fun printerMappingLoadErrorMessage(error: Exception): String =
    "Loading toolhead status failed: ${friendlyPrinterMappingError(error, PrinterMappingOperation.Load)}"

fun printerMappingSaveErrorMessage(error: Exception): String =
    "Save failed: ${friendlyPrinterMappingError(error, PrinterMappingOperation.Save)}"

private enum class PrinterMappingOperation {
    Load,
    Save
}

fun friendlyPrinterMappingError(error: Exception): String =
    friendlyPrinterMappingError(error, PrinterMappingOperation.Save)

private fun friendlyPrinterMappingError(
    error: Exception,
    operation: PrinterMappingOperation
): String {
    val rawMessage = error.message.orEmpty()

    return when {
        rawMessage.isBlank() -> unknownPrinterMappingError
        rawMessage.contains("timeout", ignoreCase = true) -> {
            if (operation == PrinterMappingOperation.Save) {
                "Printer script did not finish in time. Check the printer macro or active spool script."
            } else {
                "Printer did not respond in time. Check the Moonraker connection."
            }
        }
        rawMessage.contains("Unable to resolve host", ignoreCase = true) -> {
            "Printer host is not reachable. Check the Moonraker URL."
        }
        rawMessage.contains("active spool write failed", ignoreCase = true) -> {
            "Active spool could not be written. Check the printer Spoolman macro or script."
        }
        rawMessage.contains("mapping write failed", ignoreCase = true) -> {
            "Toolhead mapping could not be written. Check the printer variable macros."
        }
        rawMessage.contains("mapping query failed", ignoreCase = true) -> {
            "Toolhead mapping could not be read. Check the printer variable macros."
        }
        rawMessage.contains("active spool query failed", ignoreCase = true) -> {
            "Active spool could not be read. Check the printer Spoolman integration."
        }
        rawMessage.contains("requires firmware 1.5.0", ignoreCase = true) -> {
            rawMessage
        }
        rawMessage.contains("Spoolman integration is not ready", ignoreCase = true) -> {
            rawMessage
        }
        rawMessage.contains("SET_PRINT_FILAMENT_CONFIG is not available", ignoreCase = true) -> {
            "Paxx12 SET_PRINT_FILAMENT_CONFIG is not available on the printer. Enable the printer Spoolman integration, restart Klipper/Moonraker, then try again."
        }
        rawMessage.contains("No Paxx12 toolhead assignment command is available", ignoreCase = true) -> {
            "No Paxx12 toolhead assignment command is available on the printer. Enable the printer Spoolman integration, restart Klipper/Moonraker, then try again."
        }
        else -> compactPrinterError(rawMessage)
    }
}

private fun compactPrinterError(rawMessage: String): String {
    val httpCode = Regex("""\((\d{3})\)""")
        .find(rawMessage)
        ?.groupValues
        ?.getOrNull(1)

    val moonrakerMessage = Regex(""""message"\s*:\s*"([^"]+)"""")
        .find(rawMessage)
        ?.groupValues
        ?.getOrNull(1)
        ?.replace("\\n", " ")
        ?.replace("\\\"", "\"")
        ?.trim()

    val readableDetail = moonrakerMessage
        ?: rawMessage.lineSequence()
            .map { it.trim() }
            .firstOrNull { line ->
                line.isNotBlank() &&
                    !line.startsWith("{") &&
                    !line.startsWith("[") &&
                    !line.contains("Traceback", ignoreCase = true)
            }

    return buildString {
        append("Printer returned an error")
        if (httpCode != null) {
            append(" (HTTP ")
            append(httpCode)
            append(")")
        }
        if (!readableDetail.isNullOrBlank()) {
            append(": ")
            append(readableDetail.take(120))
        }
    }
}
