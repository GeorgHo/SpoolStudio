package com.spoolstudio.app.ui

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

data class MoonrakerConnectionResult(
    val reachable: Boolean,
    val status: String? = null,
    val error: String? = null,
    val firmwareVersion: String? = null,
    val moonrakerVersion: String? = null,
    val supportsSpoolLink: Boolean? = null,
    val hasSpoolmanComponent: Boolean? = null,
    val hasSpoolLinkComponent: Boolean? = null,
    val spoolmanIntegrationEnabled: Boolean? = null,
    val detectedModeLabel: String? = null
)

class MoonrakerConnectionRepository {
    suspend fun test(baseUrl: String): MoonrakerConnectionResult {
        val testUrl = "$baseUrl/printer/info"

        val connection = withContext(Dispatchers.IO) {
            (URL(testUrl).openConnection() as HttpURLConnection).apply {
                connectTimeout = 5000
                readTimeout = 5000
                requestMethod = "GET"
            }
        }

        try {
            val responseCode = withContext(Dispatchers.IO) { connection.responseCode }

            val responseText = withContext(Dispatchers.IO) {
                connection.inputStream.bufferedReader().use { it.readText() }
            }

            return if (responseCode in 200..299 && responseText.trim().startsWith("{")) {
                val printerInfo = runCatching {
                    com.spoolstudio.app.data.remote.moonraker.MoonrakerService(baseUrl).getPrinterInfo()
                }.getOrNull()
                val supportsSpoolLink = printerInfo?.supportsSpoolLink

                MoonrakerConnectionResult(
                    reachable = true,
                    status = "Moonraker reachable",
                    firmwareVersion = printerInfo?.displayVersion,
                    moonrakerVersion = printerInfo?.moonrakerVersion,
                    supportsSpoolLink = supportsSpoolLink,
                    hasSpoolmanComponent = printerInfo?.hasSpoolmanComponent,
                    hasSpoolLinkComponent = printerInfo?.hasSpoolLinkComponent,
                    spoolmanIntegrationEnabled = printerInfo?.spoolmanIntegrationEnabled,
                    detectedModeLabel = when (supportsSpoolLink) {
                        true -> "Firmware Spoolman integration"
                        false -> "Paxx12 1.5.0+ required"
                        null -> null
                    }
                )
            } else {
                MoonrakerConnectionResult(
                    reachable = false,
                    error = if (responseText.contains("<html", ignoreCase = true)) {
                        "Not a direct Moonraker endpoint"
                    } else {
                        "HTTP $responseCode"
                    }
                )
            }
        } finally {
            connection.disconnect()
        }
    }
}
