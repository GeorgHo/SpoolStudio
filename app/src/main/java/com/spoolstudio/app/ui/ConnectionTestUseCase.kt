package com.spoolstudio.app.ui

sealed class ConnectionTestResult {
    data class Moonraker(
        val reachable: Boolean,
        val status: String?,
        val error: String?,
        val firmwareVersion: String?,
        val moonrakerVersion: String?,
        val supportsSpoolLink: Boolean?,
        val hasSpoolmanComponent: Boolean?,
        val hasSpoolLinkComponent: Boolean?,
        val spoolmanIntegrationEnabled: Boolean?,
        val assignmentCommandAvailable: Boolean?,
        val detectedModeLabel: String?
    ) : ConnectionTestResult()

    data class Spoolman(
        val status: String,
        val materialModifierFieldAvailable: Boolean
    ) : ConnectionTestResult()
    data class Failed(val error: String) : ConnectionTestResult()
}

class ConnectionTestUseCase(
    private val testMoonrakerConnection: suspend (String) -> MoonrakerConnectionResult =
        { url -> MoonrakerConnectionRepository().test(url) },
    private val testSpoolmanConnection: suspend (String, String) -> Unit =
        { url, sortBy ->
            SpoolmanCatalogRepository().load(
                baseUrl = url,
                sortBy = sortBy,
                forceRefresh = true
            )
        },
    private val testMaterialModifierField: suspend (String) -> Boolean =
        { url -> com.spoolstudio.app.data.remote.spoolman.SpoolmanService(url).hasFilamentMaterialModifierField() }
) {
    fun validationError(inputUrl: String): String? =
        httpUrlValidationError(normalizeConnectionUrl(inputUrl))

    suspend fun testMoonraker(inputUrl: String): ConnectionTestResult {
        val normalizedUrl = normalizeConnectionUrl(inputUrl)
        validationError(normalizedUrl)?.let { return ConnectionTestResult.Failed(it) }

        return try {
            val result = testMoonrakerConnection(normalizedUrl)
            ConnectionTestResult.Moonraker(
                reachable = result.reachable,
                status = result.status,
                error = result.error,
                firmwareVersion = result.firmwareVersion,
                moonrakerVersion = result.moonrakerVersion,
                supportsSpoolLink = result.supportsSpoolLink,
                hasSpoolmanComponent = result.hasSpoolmanComponent,
                hasSpoolLinkComponent = result.hasSpoolLinkComponent,
                spoolmanIntegrationEnabled = result.spoolmanIntegrationEnabled,
                assignmentCommandAvailable = result.assignmentCommandAvailable,
                detectedModeLabel = result.detectedModeLabel
            )
        } catch (error: Exception) {
            ConnectionTestResult.Failed(connectionErrorMessage(error))
        }
    }

    suspend fun testSpoolman(inputUrl: String, sortBy: String): ConnectionTestResult {
        val normalizedUrl = normalizeConnectionUrl(inputUrl)
        validationError(normalizedUrl)?.let { return ConnectionTestResult.Failed(it) }

        return try {
            testSpoolmanConnection(normalizedUrl, sortBy)
            val materialModifierFieldAvailable = testMaterialModifierField(normalizedUrl)
            ConnectionTestResult.Spoolman(
                status = "Spoolman reachable",
                materialModifierFieldAvailable = materialModifierFieldAvailable
            )
        } catch (error: Exception) {
            ConnectionTestResult.Failed(connectionErrorMessage(error))
        }
    }
}
