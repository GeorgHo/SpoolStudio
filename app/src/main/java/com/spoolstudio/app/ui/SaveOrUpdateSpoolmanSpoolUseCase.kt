package com.spoolstudio.app.ui

sealed class SaveOrUpdateSpoolmanSpoolResult {
    data class ValidationFailed(val message: String) : SaveOrUpdateSpoolmanSpoolResult()
    data class Saved(
        val result: SpoolmanSaveResult,
        val successMessage: String
    ) : SaveOrUpdateSpoolmanSpoolResult()
}

class SaveOrUpdateSpoolmanSpoolUseCase(
    private val saveSpool: suspend (SpoolmanSaveInput) -> SpoolmanSaveResult =
        { input -> SpoolmanSaveRepository().save(input) }
) {
    suspend fun execute(input: SpoolmanSaveInput): SaveOrUpdateSpoolmanSpoolResult {
        val request = input.request
        val validationError = validateBeforeSave(
            spoolmanUrl = input.baseUrl,
            material = request.material,
            brand = request.brand,
            colorName = request.colorName,
            colorHex = request.colorHex,
            minTemp = request.minTemp,
            maxTemp = request.maxTemp,
            remainingWeight = request.remainingWeight
        )

        if (validationError != null) {
            return SaveOrUpdateSpoolmanSpoolResult.ValidationFailed(validationError)
        }

        val result = saveSpool(input)
        return SaveOrUpdateSpoolmanSpoolResult.Saved(
            result = result,
            successMessage = if (result.actionMode == SpoolMode.UPDATE) {
                "Spoolman update complete"
            } else {
                "Spool saved to Spoolman"
            }
        )
    }
}
