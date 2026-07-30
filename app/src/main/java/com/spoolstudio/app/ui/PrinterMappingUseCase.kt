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
    private val loadMapping: suspend (String, PrinterIntegrationMode) -> PrinterMappingLoadResult =
        { baseUrl, printerIntegrationMode -> PrinterMappingRepository().load(baseUrl, printerIntegrationMode) },
    private val saveMapping: suspend (String, PrinterIntegrationMode, Int?, Int?, Int?, Int?, Int?) -> PrinterMappingSnapshot =
        { baseUrl, printerIntegrationMode, toolhead1, toolhead2, toolhead3, toolhead4, activeSpoolId ->
            PrinterMappingRepository().save(
                baseUrl = baseUrl,
                toolhead1SpoolId = toolhead1,
                toolhead2SpoolId = toolhead2,
                toolhead3SpoolId = toolhead3,
                toolhead4SpoolId = toolhead4,
                activeSpoolId = activeSpoolId,
                printerIntegrationMode = printerIntegrationMode
            )
        }
) {
    suspend fun load(
        baseUrl: String,
        printerIntegrationMode: PrinterIntegrationMode
    ): PrinterMappingOperationResult {
        return try {
            val result = loadMapping(baseUrl, printerIntegrationMode)
            PrinterMappingOperationResult.Loaded(
                snapshot = result.snapshot,
                message = printerMappingLoadedMessage(result)
            )
        } catch (error: Exception) {
            PrinterMappingOperationResult.Failed(printerMappingLoadErrorMessage(error))
        }
    }

    suspend fun save(
        baseUrl: String,
        printerIntegrationMode: PrinterIntegrationMode,
        toolhead1SpoolId: Int?,
        toolhead2SpoolId: Int?,
        toolhead3SpoolId: Int?,
        toolhead4SpoolId: Int?,
        activeSpoolId: Int?
    ): PrinterMappingOperationResult {
        return try {
            val snapshot = saveMapping(
                baseUrl,
                printerIntegrationMode,
                toolhead1SpoolId,
                toolhead2SpoolId,
                toolhead3SpoolId,
                toolhead4SpoolId,
                activeSpoolId
            )
            PrinterMappingOperationResult.Saved(
                snapshot = snapshot,
                message = "Toolhead status saved to printer (${snapshot.integrationMode.label})"
            )
        } catch (error: Exception) {
            PrinterMappingOperationResult.Failed(printerMappingSaveErrorMessage(error))
        }
    }
}

private fun printerMappingLoadedMessage(result: PrinterMappingLoadResult): String {
    return "Toolhead status loaded"
}
