package com.spoolstudio.app.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.spoolstudio.app.domain.models.FilamentSpool

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpoolmanFilamentDropdown(
    modifier: Modifier = Modifier,
    filaments: List<FilamentSpool>,
    selectedFilament: FilamentSpool?,
    onFilamentSelected: (FilamentSpool?) -> Unit,
    spoolmanUrl: String,
    currentSpoolId: String?,
    isLoading: Boolean = false,
    infoButton: (@Composable () -> Unit)? = null
) {
    var expanded by remember { mutableStateOf(false) }
    var suppressNextToggle by remember { mutableStateOf(false) }

    LaunchedEffect(currentSpoolId, filaments, selectedFilament?.id) {
        val targetId = currentSpoolId?.toIntOrNull() ?: return@LaunchedEffect
        if (selectedFilament?.id == targetId) return@LaunchedEffect

        val match = filaments.firstOrNull { it.id == targetId }
        if (match != null) {
            onFilamentSelected(match)
        }
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { shouldExpand ->
            if (suppressNextToggle) {
                suppressNextToggle = false
            } else {
                expanded = shouldExpand && filaments.isNotEmpty() && !isLoading
            }
        },
        modifier = modifier
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = selectedFilament?.let { "${it.brand} - ${it.spoolmanName} - ${it.material}" } ?: "",
                onValueChange = { },
                readOnly = true,
                label = { Text("Select from Spoolman") },
                trailingIcon = {
                    when {
                        isLoading -> {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        }
                        selectedFilament != null -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                VerticalDivider(
                                    modifier = Modifier.height(24.dp),
                                    color = MaterialTheme.colorScheme.outline
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Id#${selectedFilament.id}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        else -> {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        }
                    }
                },
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth(),
                enabled = !isLoading && filaments.isNotEmpty(),
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                ),
                shape = RoundedCornerShape(20.dp)
            )

            if (selectedFilament != null && infoButton != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 12.dp, end = 30.dp)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            suppressNextToggle = true
                        }
                ) {
                    infoButton()
                }
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.clip(RoundedCornerShape(20.dp))
        ) {
            DropdownMenuItem(
                text = { Text("Clear Selection") },
                onClick = {
                    onFilamentSelected(null)
                    expanded = false
                }
            )

            filaments.forEach { filament ->
                DropdownMenuItem(
                    text = {
                        Text("${filament.brand} - ${filament.spoolmanName} - ${filament.material}")
                    },
                    onClick = {
                        onFilamentSelected(filament)
                        expanded = false
                    }
                )
            }
        }
    }
}
