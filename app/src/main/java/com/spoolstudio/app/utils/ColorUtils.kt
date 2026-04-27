package com.spoolstudio.app.utils

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

private fun collapseWhitespace(input: String): String {
    return input.replace(Regex("\\s+"), " ").trim()
}

private fun normalizedLookupKey(input: String): String {
    return collapseWhitespace(input)
        .lowercase()
        .replace("ß", "ss")
        .replace("ä", "ae")
        .replace("ö", "oe")
        .replace("ü", "ue")
        .replace("é", "e")
        .replace("è", "e")
        .replace("ê", "e")
        .replace("á", "a")
        .replace("à", "a")
        .replace("â", "a")
        .replace("ó", "o")
        .replace("ò", "o")
        .replace("ô", "o")
        .replace("í", "i")
        .replace("ì", "i")
        .replace("î", "i")
        .replace("ú", "u")
        .replace("ù", "u")
        .replace("û", "u")
        .replace(Regex("[^a-z0-9 ]"), "")
        .replace(Regex("\\s+"), " ")
        .trim()
}

fun normalizeColorName(input: String): String {
    return collapseWhitespace(input)
}

fun formatColorName(input: String): String {
    val normalized = normalizeColorName(input)
    if (normalized.isBlank()) return ""

    val exactMatch = knownColors.firstOrNull { color ->
        normalizedLookupKey(color.name) == normalizedLookupKey(normalized) ||
            color.aliases.any { alias -> normalizedLookupKey(alias) == normalizedLookupKey(normalized) }
    }
    if (exactMatch != null) return exactMatch.name

    return normalized
        .split(" ")
        .filter { it.isNotBlank() }
        .joinToString(" ") { word ->
            word.lowercase().replaceFirstChar { c -> c.uppercase() }
        }
}

fun suggestHexFromName(name: String): String? {
    val normalized = normalizedLookupKey(name)
    if (normalized.isBlank()) return null

    val exact = knownColors.firstOrNull { color ->
        normalizedLookupKey(color.name) == normalized ||
            color.aliases.any { alias -> normalizedLookupKey(alias) == normalized }
    }
    if (exact != null) return exact.hex

    val tokens = normalized.split(" ").filter { it.isNotBlank() }
    val contains = knownColors.firstOrNull { color ->
        val searchable = buildList {
            add(normalizedLookupKey(color.name))
            addAll(color.aliases.map(::normalizedLookupKey))
        }.distinct()

        searchable.any { candidate ->
            candidate.contains(normalized) ||
                normalized.contains(candidate) ||
                tokens.any { token -> token.length >= 4 && candidate.contains(token) }
        }
    }
    if (contains != null) return contains.hex

    return knownColors
        .map { color -> color to nameSimilarityScore(normalized, color) }
        .filter { (_, score) -> score >= 0.72 }
        .maxByOrNull { it.second }
        ?.first
        ?.hex
}

private fun nameSimilarityScore(query: String, color: NamedColor): Double {
    val q = query.split(" ").filter { it.isNotBlank() }.toSet()
    val variants = buildList {
        add(color.name)
        addAll(color.aliases)
    }.map(::normalizedLookupKey)

    return variants.maxOfOrNull { candidate ->
        val c = candidate.split(" ").filter { it.isNotBlank() }.toSet()
        if (candidate == query) {
            1.0
        } else {
            val overlap = if (q.isEmpty() || c.isEmpty()) 0.0 else q.intersect(c).size.toDouble() / max(q.size, c.size).toDouble()
            val containment = when {
                candidate.contains(query) || query.contains(candidate) -> 0.9
                else -> 0.0
            }
            max(overlap, containment)
        }
    } ?: 0.0
}

private fun rgb(hex: String): Triple<Int, Int, Int> =
    Triple(
        hex.substring(0, 2).toInt(16),
        hex.substring(2, 4).toInt(16),
        hex.substring(4, 6).toInt(16)
    )

private data class HslColor(val h: Float, val s: Float, val l: Float)

private fun toHsl(hex: String): HslColor {
    val (ri, gi, bi) = rgb(hex)
    val r = ri / 255f
    val g = gi / 255f
    val b = bi / 255f
    val maxV = max(r, max(g, b))
    val minV = min(r, min(g, b))
    val delta = maxV - minV
    val l = (maxV + minV) / 2f
    val s = if (delta == 0f) 0f else delta / (1f - abs(2f * l - 1f))
    val h = when {
        delta == 0f -> 0f
        maxV == r -> 60f * (((g - b) / delta) % 6f)
        maxV == g -> 60f * (((b - r) / delta) + 2f)
        else -> 60f * (((r - g) / delta) + 4f)
    }.let { if (it < 0f) it + 360f else it }
    return HslColor(h, s, l)
}

private data class LabColor(val l: Double, val a: Double, val b: Double)

private fun srgbToLinear(channel: Int): Double {
    val c = channel / 255.0
    return if (c <= 0.04045) c / 12.92 else Math.pow((c + 0.055) / 1.055, 2.4)
}

private fun toLab(hex: String): LabColor {
    val (r8, g8, b8) = rgb(hex)
    val r = srgbToLinear(r8)
    val g = srgbToLinear(g8)
    val b = srgbToLinear(b8)

    val x = (r * 0.4124 + g * 0.3576 + b * 0.1805) / 0.95047
    val y = (r * 0.2126 + g * 0.7152 + b * 0.0722) / 1.00000
    val z = (r * 0.0193 + g * 0.1192 + b * 0.9505) / 1.08883

    fun f(t: Double): Double =
        if (t > 0.008856) Math.cbrt(t) else (7.787 * t) + (16.0 / 116.0)

    val fx = f(x)
    val fy = f(y)
    val fz = f(z)

    return LabColor(
        l = (116.0 * fy) - 16.0,
        a = 500.0 * (fx - fy),
        b = 200.0 * (fy - fz)
    )
}

private fun deltaE(a: LabColor, b: LabColor): Double {
    val dl = a.l - b.l
    val da = a.a - b.a
    val db = a.b - b.b
    return sqrt(dl * dl + da * da + db * db)
}

private data class ColorMetrics(
    val color: NamedColor,
    val distance: Double,
    val targetHsl: HslColor,
    val candidateHsl: HslColor,
    val deltaE: Double,
    val hueDiff: Float,
    val satDiff: Float,
    val lightDiff: Float,
    val familyPenalty: Double
)

data class ColorSuggestionDebugEntry(
    val name: String,
    val hex: String,
    val score: Double,
    val deltaE: Double,
    val hueDiff: Float,
    val saturationDiff: Float,
    val lightnessDiff: Float,
    val familyPenalty: Double
)

data class ColorSuggestionDebug(
    val inputHex: String,
    val suggestedName: String,
    val reason: String,
    val targetHue: Float,
    val targetSaturation: Float,
    val targetLightness: Float,
    val nearest: List<ColorSuggestionDebugEntry>
)

private fun hueDistance(a: Float, b: Float): Float {
    val diff = abs(a - b)
    return min(diff, 360f - diff)
}

private fun familyBias(target: HslColor, candidate: NamedColor): Double {
    val nameKey = normalizedLookupKey(candidate.name)
    val warmNeutral = target.h in 18f..42f && target.s in 0.08f..0.45f && target.l in 0.48f..0.85f
    val orangeLike = target.h in 18f..42f && target.s > 0.45f
    val veryNeutral = target.s < 0.10f

    var bias = 0.0

    if (warmNeutral) {
        if (nameKey.contains("orange") || nameKey.contains("amber")) bias += 12.0
        if (
            nameKey.contains("beige") ||
            nameKey.contains("taupe") ||
            nameKey.contains("latte") ||
            nameKey.contains("sand") ||
            nameKey.contains("tan") ||
            nameKey.contains("camel") ||
            nameKey.contains("nude") ||
            nameKey.contains("greige") ||
            nameKey.contains("stone") ||
            nameKey.contains("cappuccino") ||
            nameKey.contains("mocha") ||
            nameKey.contains("brown") ||
            nameKey.contains("caramel") ||
            nameKey.contains("champagne") ||
            nameKey.contains("cream") ||
            nameKey.contains("ivory")
        ) bias -= 9.0
    }

    if (orangeLike) {
        if (nameKey.contains("orange") || nameKey.contains("amber") || nameKey.contains("coral") || nameKey.contains("apricot")) {
            bias -= 5.0
        }
        if (nameKey.contains("gray") || nameKey.contains("grey")) bias += 4.0
    }

    if (veryNeutral) {
        if (nameKey.contains("gray") || nameKey.contains("grey") || nameKey.contains("white") || nameKey.contains("black") || nameKey.contains("silver")) {
            bias -= 6.0
        }
        if (nameKey.contains("orange") || nameKey.contains("pink") || nameKey.contains("purple")) bias += 8.0
    }

    return bias
}

private fun scoreCatalogColors(hex: String): List<ColorMetrics> {
    val targetHsl = toHsl(hex)
    val targetLab = toLab(hex)

    return knownColors.map { candidate ->
        val candidateHsl = toHsl(candidate.hex)
        val candidateLab = toLab(candidate.hex)
        val dE = deltaE(targetLab, candidateLab)
        val hDiff = hueDistance(targetHsl.h, candidateHsl.h)
        val sDiff = abs(targetHsl.s - candidateHsl.s)
        val lDiff = abs(targetHsl.l - candidateHsl.l)
        val penalty = familyBias(targetHsl, candidate)

        var distance = dE
        distance += hDiff * 0.06
        distance += sDiff * 14.0
        distance += lDiff * 18.0
        distance += penalty

        ColorMetrics(
            color = candidate,
            distance = distance,
            targetHsl = targetHsl,
            candidateHsl = candidateHsl,
            deltaE = dE,
            hueDiff = hDiff,
            satDiff = sDiff,
            lightDiff = lDiff,
            familyPenalty = penalty
        )
    }.sortedBy { it.distance }
}

private fun nearestCatalogColor(hex: String): NamedColor = scoreCatalogColors(hex).first().color

private fun baseHueName(h: Float): String = when {
    h < 15f || h >= 345f -> "Red"
    h < 40f -> "Orange"
    h < 65f -> "Yellow"
    h < 90f -> "Lime"
    h < 150f -> "Green"
    h < 190f -> "Cyan"
    h < 255f -> "Blue"
    h < 290f -> "Purple"
    h < 330f -> "Pink"
    else -> "Rose"
}

fun suggestColorName(hex: String?): String {
    return buildColorSuggestionDebug(hex).suggestedName
}

fun buildColorSuggestionDebug(hex: String?): ColorSuggestionDebug {
    val value = hex ?: return ColorSuggestionDebug(
        inputHex = "000000",
        suggestedName = "Unknown",
        reason = "No hex value provided",
        targetHue = 0f,
        targetSaturation = 0f,
        targetLightness = 0f,
        nearest = emptyList()
    )

    val upper = value.removePrefix("#").uppercase().padStart(6, '0').take(6)
    val hsl = toHsl(upper)
    val scored = scoreCatalogColors(upper)
    val nearest = scored.first()

    knownColors.firstOrNull { it.hex.equals(upper, ignoreCase = true) }?.let { exact ->
        return ColorSuggestionDebug(
            inputHex = upper,
            suggestedName = exact.name,
            reason = "Exact catalog match",
            targetHue = hsl.h,
            targetSaturation = hsl.s,
            targetLightness = hsl.l,
            nearest = scored.take(5).map { it.toDebugEntry() }
        )
    }

    if (
        nearest.deltaE <= 18.0 &&
        nearest.hueDiff < 25f &&
        nearest.satDiff < 0.25f &&
        nearest.lightDiff < 0.25f
    ) {
        return ColorSuggestionDebug(
            inputHex = upper,
            suggestedName = nearest.color.name,
            reason = "Nearest catalog color (LAB + HSL close enough)",
            targetHue = hsl.h,
            targetSaturation = hsl.s,
            targetLightness = hsl.l,
            nearest = scored.take(5).map { it.toDebugEntry() }
        )
    }

    if (hsl.s < 0.10f) {
        val neutralName = when {
            hsl.l < 0.12f -> "Black"
            hsl.l < 0.28f -> "Dark Gray"
            hsl.l < 0.72f -> "Gray"
            hsl.l < 0.90f -> "Light Gray"
            else -> "White"
        }
        return ColorSuggestionDebug(
            inputHex = upper,
            suggestedName = neutralName,
            reason = "Very low saturation fallback",
            targetHue = hsl.h,
            targetSaturation = hsl.s,
            targetLightness = hsl.l,
            nearest = scored.take(5).map { it.toDebugEntry() }
        )
    }

    if (hsl.h in 18f..42f && hsl.s in 0.08f..0.45f && hsl.l in 0.48f..0.85f) {
        val warmNeutralName = when {
            hsl.l > 0.75f && hsl.s < 0.18f -> "Warm Beige"
            hsl.s < 0.16f -> "Taupe"
            hsl.s < 0.28f -> "Latte Brown"
            else -> "Caramel"
        }
        return ColorSuggestionDebug(
            inputHex = upper,
            suggestedName = warmNeutralName,
            reason = "Warm neutral fallback",
            targetHue = hsl.h,
            targetSaturation = hsl.s,
            targetLightness = hsl.l,
            nearest = scored.take(5).map { it.toDebugEntry() }
        )
    }

    if (hsl.h in 20f..50f && hsl.l < 0.60f && hsl.s > 0.25f) {
        val brownName = if (hsl.l < 0.35f) "Dark Brown" else "Brown"
        return ColorSuggestionDebug(
            inputHex = upper,
            suggestedName = brownName,
            reason = "Brown fallback",
            targetHue = hsl.h,
            targetSaturation = hsl.s,
            targetLightness = hsl.l,
            nearest = scored.take(5).map { it.toDebugEntry() }
        )
    }

    val base = baseHueName(hsl.h)
    val prefix = when {
        hsl.l < 0.18f -> "Very Dark"
        hsl.l < 0.35f -> "Dark"
        hsl.l > 0.88f -> "Very Light"
        hsl.l > 0.72f -> "Light"
        hsl.s < 0.20f -> "Muted"
        else -> ""
    }
    val fallbackName = if (prefix.isBlank()) base else "$prefix $base"

    return ColorSuggestionDebug(
        inputHex = upper,
        suggestedName = fallbackName,
        reason = "Generic HSL fallback",
        targetHue = hsl.h,
        targetSaturation = hsl.s,
        targetLightness = hsl.l,
        nearest = scored.take(5).map { it.toDebugEntry() }
    )
}

private fun ColorMetrics.toDebugEntry(): ColorSuggestionDebugEntry = ColorSuggestionDebugEntry(
    name = color.name,
    hex = color.hex,
    score = distance,
    deltaE = deltaE,
    hueDiff = hueDiff,
    saturationDiff = satDiff,
    lightnessDiff = lightDiff,
    familyPenalty = familyPenalty
)
