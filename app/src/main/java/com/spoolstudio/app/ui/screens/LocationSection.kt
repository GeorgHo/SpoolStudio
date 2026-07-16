package com.spoolstudio.app.ui.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationSection(
    location: String,
    customLocation: String,
    availableLocations: List<String>,
    onLocationChange: (String) -> Unit,
    onCustomLocationChange: (String) -> Unit
) {
    var locationExpanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val filteredLocations = availableLocations.filter {
        searchQuery.isBlank() || it.contains(searchQuery, ignoreCase = true)
    }

    ExposedDropdownMenuBox(
        expanded = locationExpanded,
        onExpandedChange = { locationExpanded = !locationExpanded }
    ) {
        OutlinedTextField(
            value = when {
                location == "Other" -> customLocation
                location.isNotBlank() -> location
                else -> "No Location"
            },
            onValueChange = {},
            readOnly = true,
            label = { Text("Location") },
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = if (
                    (location == "Other" && customLocation.isNotBlank()) ||
                    (location.isNotBlank() && location != "Other")
                ) FontWeight.Bold else FontWeight.Normal
            ),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = locationExpanded) },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(16.dp)
        )

        DropdownMenu(
            expanded = locationExpanded,
            onDismissRequest = {
                locationExpanded = false
                searchQuery = ""
            },
            modifier = Modifier.height(320.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it.take(60) },
                label = { Text("Search") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium,
                shape = RoundedCornerShape(16.dp)
            )

            filteredLocations.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item) },
                    onClick = {
                        onLocationChange(item)
                        onCustomLocationChange("")
                        locationExpanded = false
                        searchQuery = ""
                    }
                )
            }
            HorizontalDivider()

            DropdownMenuItem(
                text = { Text("Other") },
                onClick = {
                    onLocationChange("Other")
                    locationExpanded = false
                    searchQuery = ""
                }
            )
        }
    }

    if (location == "Other") {
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = customLocation,
            onValueChange = { input ->
                if (input.length <= 60) {
                    onCustomLocationChange(input)
                }
            },
            label = { Text("Custom Location") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(16.dp)
        )
    }
}
