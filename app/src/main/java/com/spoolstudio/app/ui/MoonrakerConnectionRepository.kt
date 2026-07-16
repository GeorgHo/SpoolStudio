package com.spoolstudio.app.ui

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

data class MoonrakerConnectionResult(
    val reachable: Boolean,
    val status: String? = null,
    val error: String? = null
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
                MoonrakerConnectionResult(
                    reachable = true,
                    status = "Moonraker reachable"
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
