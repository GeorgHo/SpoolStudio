package com.spoolstudio.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.spoolstudio.app.ui.components.TemperatureControl

@Composable
fun TemperatureSection(
    nozzleMin: String,
    nozzleMax: String,
    bedMin: String,
    bedMax: String,
    onNozzleMinChange: (String) -> Unit,
    onNozzleMaxChange: (String) -> Unit,
    onBedMinChange: (String) -> Unit,
    onBedMaxChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Temperature", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Nozzle", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.width(60.dp))
            TemperatureControl(value = nozzleMin, onValueChange = onNozzleMinChange, modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(16.dp))
            TemperatureControl(value = nozzleMax, onValueChange = onNozzleMaxChange, modifier = Modifier.weight(1f))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Bed", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.width(60.dp))
            TemperatureControl(value = bedMin, onValueChange = onBedMinChange, modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(16.dp))
            TemperatureControl(value = bedMax, onValueChange = onBedMaxChange, modifier = Modifier.weight(1f))
        }
    }
}
