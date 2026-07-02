package com.spoolstudio.app.ui

sealed class ConnectionTestResult {
    data class Moonraker(
        val reachable: Boolean,
        val status: String?,
        val error: String?
    ) : ConnectionTestResult()

    data class Spoolman(val status: String) : ConnectionTestResult()
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
        }
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
                error = result.error
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
            ConnectionTestResult.Spoolman(status = "Spoolman erreichbar")
        } catch (error: Exception) {
            ConnectionTestResult.Failed(connectionErrorMessage(error))
        }
    }
}
