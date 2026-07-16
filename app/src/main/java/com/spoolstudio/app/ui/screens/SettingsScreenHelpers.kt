package com.spoolstudio.app.ui.screens

fun normalizeSettingsUrl(url: String): String {
    var result = url.trim()

    if (result.isBlank()) return ""

    if (!result.startsWith("http://") && !result.startsWith("https://")) {
        result = "http://$result"
    }

    result = result.replace(Regex("/(\\d{2,5})(/|$)")) { match ->
        ":${match.groupValues[1]}/"
    }

    return result.removeSuffix("/")
}

fun normalizeMoonrakerSettingsUrl(url: String): String {
    var result = url.trim()

    if (result.isBlank()) return ""

    if (!result.startsWith("http://") && !result.startsWith("https://")) {
        result = "http://$result"
    }

    result = result.replace(Regex("/(\\d{2,5})(/|$)")) { match ->
        val port = match.groupValues[1]
        ":$port/"
    }

    return result.removeSuffix("/")
}

fun normalizeSettingsSort(sort: String): String {
    return sort.ifBlank { "" }
}

fun hasSettingsChanges(
    tempSpoolmanUrl: String,
    savedSpoolmanUrl: String,
    tempMoonrakerUrl: String,
    savedMoonrakerUrl: String,
    tempSort: String,
    savedSort: String,
    tempBambuKey: String,
    savedBambuKey: String,
    tempShowCommentField: Boolean,
    savedShowCommentField: Boolean
): Boolean {
    return normalizeSettingsUrl(tempSpoolmanUrl) != normalizeSettingsUrl(savedSpoolmanUrl) ||
        normalizeMoonrakerSettingsUrl(tempMoonrakerUrl) != normalizeMoonrakerSettingsUrl(savedMoonrakerUrl) ||
        normalizeSettingsSort(tempSort) != normalizeSettingsSort(savedSort) ||
        tempBambuKey.trim().uppercase() != savedBambuKey.trim().uppercase() ||
        tempShowCommentField != savedShowCommentField
}

fun runUrlRetestIfNeeded(
    currentValue: String,
    lastTestedValue: String,
    isTesting: Boolean,
    triggeredManually: Boolean,
    onTest: (String) -> Unit,
    onLastTestedChange: (String) -> Unit
) {
    if (isTesting || triggeredManually) return

    val normalized = normalizeSettingsUrl(currentValue)
    if (normalized.isNotBlank() && normalized != lastTestedValue) {
        onTest(normalized)
        onLastTestedChange(normalized)
    }
}
