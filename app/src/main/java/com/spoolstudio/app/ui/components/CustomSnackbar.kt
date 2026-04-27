package com.spoolstudio.app.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun CustomSnackbar(
    message: String,
    isVisible: Boolean,
    onDismiss: () -> Unit
) {
    LaunchedEffect(isVisible) {
        if (isVisible) {
            delay(3600) // Auto dismiss after 3.6 seconds von 3600 auf 20000 geändert
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = isVisible,
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
                shape = RoundedCornerShape(24.dp), // Much more rounded
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.inverseSurface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp) // Higher elevation
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp), // Even bigger padding
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "📱",
                        style = MaterialTheme.typography.headlineMedium // Bigger emoji
                    )
                    
                    Text(
                        text = message,
                        style = MaterialTheme.typography.titleMedium, // Bigger text
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.inverseOnSurface
                    )
                }
            }
        }
    }
}
