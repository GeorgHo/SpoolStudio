package com.spoolstudio.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun TemperatureCard(
    nozzleMin: String,
    nozzleMax: String,
    bedMin: String,
    bedMax: String,
    onNozzleMinChange: (String) -> Unit,
    onNozzleMaxChange: (String) -> Unit,
    onBedMinChange: (String) -> Unit,
    onBedMaxChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Temperature",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            // Nozzle Temperature
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Nozzle",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.width(60.dp)
                )
                
                TemperatureControl(
                    value = nozzleMin,
                    onValueChange = onNozzleMinChange,
                    modifier = Modifier.weight(1f)
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                TemperatureControl(
                    value = nozzleMax,
                    onValueChange = onNozzleMaxChange,
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Bed Temperature
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Bed",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.width(60.dp)
                )
                
                TemperatureControl(
                    value = bedMin,
                    onValueChange = onBedMinChange,
                    modifier = Modifier.weight(1f)
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                TemperatureControl(
                    value = bedMax,
                    onValueChange = onBedMaxChange,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun TemperatureControl(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "−",
            modifier = Modifier
                .size(32.dp)
                .clickable {
                    val current = value.toIntOrNull() ?: 0
                    if (current > 0) onValueChange((current - 5).toString())
                }
                .wrapContentSize(Alignment.Center),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )
        
        OutlinedTextField(
            value = value,
            onValueChange = { input ->
                val sanitized = input.filter { it.isDigit() }.take(3)
                val temp = sanitized.toIntOrNull()
                if (temp == null || temp <= 500) {
                    onValueChange(sanitized)
                }
            },
            modifier = Modifier.width(80.dp),
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.SemiBold
            ),
            singleLine = true,
            suffix = { 
                Text(
                    text = "°C",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
            ),
            shape = RoundedCornerShape(20.dp)
        )
        
        Text(
            text = "+",
            modifier = Modifier
                .size(32.dp)
                .clickable {
                    val current = value.toIntOrNull() ?: 0
                    if (current < 500) onValueChange((current + 5).toString())
                }
                .wrapContentSize(Alignment.Center),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
