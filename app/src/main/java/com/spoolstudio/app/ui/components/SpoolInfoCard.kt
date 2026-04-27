package com.spoolstudio.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.spoolstudio.app.domain.models.FilamentSpool
import kotlinx.coroutines.delay

@Composable
fun SpoolInfoCard(
    spool: FilamentSpool,
    modifier: Modifier = Modifier,
    onOpenRefreshRequested: (() -> Unit)? = null
) {
    var showDialog by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(showDialog, isRefreshing) {
        if (showDialog && isRefreshing) {
            delay(800)
            isRefreshing = false
        }
    }

    IconButton(
        onClick = {
            isRefreshing = true
            onOpenRefreshRequested?.invoke()
            showDialog = true
        },
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = "Spool Info",
            tint = MaterialTheme.colorScheme.primary
        )
    }

    if (showDialog) {
        Dialog(onDismissRequest = { showDialog = false }) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                tonalElevation = 8.dp,
                shadowElevation = 12.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .width(340.dp)
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            spool.colorHex?.let { hex ->
                                runCatching {
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .background(
                                                Color(android.graphics.Color.parseColor("#$hex")),
                                                CircleShape
                                            )
                                            .border(
                                                1.dp,
                                                MaterialTheme.colorScheme.outline,
                                                CircleShape
                                            )
                                    )
                                }
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "Spool Info",
                                        style = MaterialTheme.typography.headlineSmall
                                    )

                                    if (isRefreshing) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp
                                        )
                                    }
                                }
                                Text(
                                    text = spool.spoolmanName ?: spool.material,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }

                        IconButton(onClick = { showDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    HorizontalDivider()

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            InfoRow("ID", spool.id?.toString() ?: "-")

                            HorizontalDivider(
                                thickness = 1.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                            )

                            InfoRow("Used", "${spool.usedWeight.toInt()} g")
                            InfoRow("Remaining", "${(spool.remainingWeight ?: 0f).toInt()} g")

                            val initialWeight = calculateInitialWeight(spool)
                            if (initialWeight != null) {
                                InfoRow("Initial", "${initialWeight.toInt()} g")
                            }

                            HorizontalDivider(
                                thickness = 1.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                            )

                            val used = spool.usedWeight.coerceAtLeast(0f)
                            val remaining = (spool.remainingWeight ?: 0f).coerceAtLeast(0f)
                            val total = (used + remaining).coerceAtLeast(1f)

                            val remainingFraction = remaining / total
                            val remainingPercent = ((remaining / total) * 100).toInt()

                            val filamentColor = runCatching {
                                spool.colorHex?.let { Color(android.graphics.Color.parseColor("#$it")) }
                            }.getOrNull() ?: MaterialTheme.colorScheme.primary

                            val isVeryLightFilament =
                                spool.colorHex?.uppercase() in setOf("FFFFFF", "FFFFF0", "FDF4E3", "F3F8FF")

                            val barFillColor = if (isVeryLightFilament) {
                                Color(0xFFF5F5F5)
                            } else {
                                filamentColor
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(14.dp)
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                                        RoundedCornerShape(999.dp)
                                    )
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(remainingFraction)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(999.dp))
                                        .background(barFillColor)
                                )
                            }

                            Text(
                                text = "$remainingPercent% remaining",
                                style = MaterialTheme.typography.bodyMedium
                            )

                            spool.location?.takeIf { it.isNotBlank() }?.let {
                                InfoRow("Location", it)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        val lotValue = spool.lotNr?.takeIf { it.isNotBlank() } ?: "-"
                        val firstUseValue = extractUsageDate(
                            spool,
                            "firstUsed",
                            "first_used",
                            "firstUse",
                            "first_use"
                        )?.let { formatUsageValue(it) } ?: "-"
                        val lastUseValue = extractUsageDate(
                            spool,
                            "lastUsed",
                            "last_used",
                            "lastUse",
                            "last_use"
                        )?.let { formatUsageValue(it) } ?: "-"
                        val commentValue = spool.comment?.takeIf { it.isNotBlank() } ?: "-"

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Additional Info",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )

                            InfoRow("Lot", lotValue)
                            InfoRow("First use", firstUseValue)
                            InfoRow("Last use", lastUseValue)
                            InfoRow("Comment", commentValue)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Medium
            ),
            color = if (value == "-") {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )
    }
}

private fun calculateInitialWeight(spool: FilamentSpool): Float? {
    val used = spool.usedWeight.coerceAtLeast(0f)
    val remaining = (spool.remainingWeight ?: 0f).coerceAtLeast(0f)
    val total = used + remaining
    return total.takeIf { it > 0f }
}

private fun extractUsageDate(spool: FilamentSpool, vararg candidates: String): String? {
    val clazz = spool.javaClass

    for (name in candidates) {
        // 1) Kotlin/Java Getter probieren
        val getterNames = listOf(
            "get${name.replaceFirstChar { it.uppercase() }}",
            name
        )

        for (getterName in getterNames) {
            val getterValue = runCatching {
                clazz.methods.firstOrNull { it.name == getterName && it.parameterCount == 0 }
                    ?.invoke(spool)
            }.getOrNull()

            val normalizedGetterValue = normalizeUsageValue(getterValue)
            if (!normalizedGetterValue.isNullOrBlank()) return normalizedGetterValue
        }

        // 2) Feld in Klasse oder Oberklasse suchen
        var currentClass: Class<*>? = clazz
        while (currentClass != null) {
            val fieldValue = runCatching {
                currentClass.getDeclaredField(name).apply {
                    isAccessible = true
                }.get(spool)
            }.getOrNull()

            val normalizedFieldValue = normalizeUsageValue(fieldValue)
            if (!normalizedFieldValue.isNullOrBlank()) return normalizedFieldValue

            currentClass = currentClass.superclass
        }
    }

    return null
}

private fun normalizeUsageValue(value: Any?): String? {
    val text = when (value) {
        null -> null
        is String -> value
        else -> value.toString()
    }?.trim()

    if (text.isNullOrBlank() || text.equals("null", ignoreCase = true)) return null

    return text
}

private fun formatUsageValue(value: String): String {
    return value
        .replace("T", " ")
        .removeSuffix("Z")
        .substringBefore(".")
        .trim()
}