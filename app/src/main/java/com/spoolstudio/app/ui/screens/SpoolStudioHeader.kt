package com.spoolstudio.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.spoolstudio.app.ui.theme.SpoolStudioColors

@Composable
fun SpoolStudioHeader(
    onSettingsClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(SpoolStudioColors.ScreenBackground)
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Text(
            text = "Spool Studio",
            style = MaterialTheme.typography.titleLarge,
            color = SpoolStudioColors.OnGraphite,
            modifier = Modifier.align(Alignment.Center)
        )

        IconButton(
            onClick = onSettingsClick,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(42.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = SpoolStudioColors.OnGraphite,
                modifier = Modifier.size(25.dp)
            )
        }
    }
}
