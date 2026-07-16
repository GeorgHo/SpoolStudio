package com.spoolstudio.app.ui

fun normalizeConnectionUrl(url: String): String = url.trim().removeSuffix("/")

fun httpUrlValidationError(url: String): String? = when {
    url.isBlank() -> "URL is missing"
    !url.startsWith("http://") && !url.startsWith("https://") -> "URL must start with http:// or https://"
    else -> null
}

fun connectionErrorMessage(error: Exception): String = when {
    error.message?.contains("timeout", true) == true -> "Timeout"
    error.message?.contains("Unable to resolve host", true) == true -> "Host not reachable"
    else -> "Error: ${error.message ?: "Unknown"}"
}
