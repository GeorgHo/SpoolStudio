package com.spoolstudio.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun CustomSnackbar(
    message: String,
    isVisible: Boolean,
    onDismiss: () -> Unit
) {
    LaunchedEffect(isVisible, message) {
        if (isVisible && message.isNotBlank()) {
            delay(5500)
            onDismiss()
        }
    }

    val title = feedbackTitle(message)
    val accentColor = feedbackAccentColor(message)

    AnimatedVisibility(
        visible = isVisible && message.isNotBlank(),
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.inverseSurface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp, vertical = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
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
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.inverseOnSurface
                    )
                }
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
