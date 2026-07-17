package com.spoolstudio.app.ui.components

import com.spoolstudio.app.utils.normalizeHexColor
import com.spoolstudio.app.utils.suggestColorName

val commonColorPresets: Map<String, String> = mapOf(
    "White" to "FFFFFF",
    "Transparent" to "E8EEF2",
    "Red" to "FF0000",
    "Blue" to "0000FF",
    "Green" to "00FF00",
    "Yellow" to "FFFF00",
    "Orange" to "FFA500",
    "Pink" to "FFC0CB",
    "Black" to "000000"
)

fun presetNameForColor(color: String?): String? {
    val normalized = normalizeHexColor(color) ?: return null
    return commonColorPresets.entries
        .find { it.value.equals(normalized, ignoreCase = true) }
        ?.key
}

fun displayNameForSelectedColor(
    selectedColor: String?,
    colorName: String
): String {
    val normalizedColor = normalizeHexColor(selectedColor)
    val presetName = presetNameForColor(normalizedColor)

    return when {
        selectedColor == null -> "No Color"
        presetName != null -> presetName
        colorName.isNotBlank() -> colorName
        else -> suggestColorName(normalizedColor ?: "FFFFFF")
    }
}

fun sanitizeHexColorInput(input: String): String? {
    val sanitized = input
        .trim()
        .removePrefix("#")
        .uppercase()
        .filter { it in "0123456789ABCDEF" }
        .take(6)

    return sanitized.ifBlank { null }
}

fun colorNameForSelectedColor(color: String?): String {
    if (color.isNullOrBlank()) return ""

    return presetNameForColor(color) ?: suggestColorName(color)
}
