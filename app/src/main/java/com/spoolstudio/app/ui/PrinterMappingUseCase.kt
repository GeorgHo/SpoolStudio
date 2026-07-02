package com.spoolstudio.app.ui

sealed class PrinterMappingOperationResult {
    data class Loaded(
        val snapshot: PrinterMappingSnapshot,
        val message: String
    ) : PrinterMappingOperationResult()

    data class Saved(
        val snapshot: PrinterMappingSnapshot,
        val message: String
    ) : PrinterMappingOperationResult()

    data class Failed(val message: String) : PrinterMappingOperationResult()
}

class PrinterMappingUseCase(
    private val loadMapping: suspend (String) -> PrinterMappingLoadResult =
        { baseUrl -> PrinterMappingRepository().load(baseUrl) },
    private val saveMapping: suspend (String, Int?, Int?, Int?, Int?, Int?) -> PrinterMappingSnapshot =
        { baseUrl, toolhead1, toolhead2, toolhead3, toolhead4, activeSpoolId ->
            PrinterMappingRepository().save(
                baseUrl = baseUrl,
                toolhead1SpoolId = toolhead1,
                toolhead2SpoolId = toolhead2,
                toolhead3SpoolId = toolhead3,
                toolhead4SpoolId = toolhead4,
                activeSpoolId = activeSpoolId
            )
        }
) {
    suspend fun load(baseUrl: String): PrinterMappingOperationResult {
        return try {
            val result = loadMapping(baseUrl)
            PrinterMappingOperationResult.Loaded(
                snapshot = result.snapshot,
                message = if (result.activeSpoolAvailable) {
                    "Printer mapping loaded"
                } else {
                    "Printer mapping loaded (active spool not available)"
                }
            )
        } catch (error: Exception) {
            PrinterMappingOperationResult.Failed(printerMappingLoadErrorMessage(error))
        }
    }

    suspend fun save(
        baseUrl: String,
        toolhead1SpoolId: Int?,
        toolhead2SpoolId: Int?,
        toolhead3SpoolId: Int?,
        toolhead4SpoolId: Int?,
        activeSpoolId: Int?
    ): PrinterMappingOperationResult {
        return try {
            PrinterMappingOperationResult.Saved(
                snapshot = saveMapping(
                    baseUrl,
                    toolhead1SpoolId,
                    toolhead2SpoolId,
                    toolhead3SpoolId,
                    toolhead4SpoolId,
                    activeSpoolId
                ),
                message = "Mapping saved to printer"
            )
        } catch (error: Exception) {
            PrinterMappingOperationResult.Failed(printerMappingSaveErrorMessage(error))
        }
    }
}
