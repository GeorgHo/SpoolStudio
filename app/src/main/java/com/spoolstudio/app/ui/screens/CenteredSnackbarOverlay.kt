package com.spoolstudio.app.ui.screens

import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.unit.dp

@Composable
fun CenteredSnackbarOverlay(
    message: String,
    visible: Boolean
) {
    if (!visible || message.isBlank()) return

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Text(
                text = message,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
