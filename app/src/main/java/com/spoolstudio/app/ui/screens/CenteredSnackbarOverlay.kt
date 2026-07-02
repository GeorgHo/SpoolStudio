package com.spoolstudio.app.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun CenteredSnackbarOverlay(
    message: String,
    visible: Boolean
) {
    if (!visible || message.isBlank()) return

    val title = feedbackTitle(message)
    val accentColor = feedbackAccentColor(message)

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.inverseSurface
            )
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 18.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.inverseOnSurface
                )
            }
        }
    }
}

@Composable
private fun feedbackAccentColor(message: String): Color {
    val normalized = message.lowercase()
    return when {
        normalized.contains("failed") ||
            normalized.contains("error") ||
            normalized.contains("invalid") ||
            normalized.contains("missing") -> MaterialTheme.colorScheme.error
        normalized.contains("rfid") ||
            normalized.contains("nfc") ||
            normalized.contains("tag") -> MaterialTheme.colorScheme.secondary
        normalized.contains("spoolman") -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.tertiary
    }
}

private fun feedbackTitle(message: String): String {
    val normalized = message.lowercase()
    return when {
        normalized.contains("failed") ||
            normalized.contains("error") ||
            normalized.contains("invalid") ||
            normalized.contains("missing") -> "Action needed"
        normalized.contains("rfid") ||
            normalized.contains("nfc") ||
            normalized.contains("tag") -> "RFID status"
        normalized.contains("spoolman") -> "Spoolman status"
        else -> "Status"
    }
}
