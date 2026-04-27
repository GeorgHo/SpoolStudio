package com.spoolstudio.app.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily

@Composable
fun NfcDumpViewer(
    text: String,
    modifier: Modifier = Modifier
) {
    val verticalScroll = rememberScrollState()
    val horizontalScroll = rememberScrollState()

    Text(
        text = text,
        modifier = modifier
            .verticalScroll(verticalScroll)
            .horizontalScroll(horizontalScroll),
        fontFamily = FontFamily.Monospace,
        style = MaterialTheme.typography.bodySmall
    )
}