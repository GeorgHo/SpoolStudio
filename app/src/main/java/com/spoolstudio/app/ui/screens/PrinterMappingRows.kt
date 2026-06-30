package com.spoolstudio.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import com.spoolstudio.app.domain.models.FilamentSpool
import com.spoolstudio.app.ui.components.SpoolStudioLogo

@Composable
fun MappingRowPlaceholder(label: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = "Keine Spule gewÃ¤hlt",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MappingRowDropdown(
    label: String,
    spools: List<FilamentSpool>,
    selectedSpoolId: Int?,
    isActive: Boolean,
    enabled: Boolean,
    onSpoolSelected: (Int?) -> Unit,
    onActiveCheckedChange: (Boolean) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val selectedSpool = spools.firstOrNull { it.id == selectedSpoolId }

    @Suppress("DEPRECATION")
    val iconColor = selectedSpool?.colorHex
        ?.takeIf { it.isNotBlank() }
        ?.let { hex ->
            try {
                Color(android.graphics.Color.parseColor("#$hex"))
            } catch (_: Exception) {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        }
        ?: MaterialTheme.colorScheme.onSurfaceVariant

    val displayText = selectedSpool?.let { spool ->
        "ID ${spool.id ?: "-"} Â· ${spool.brand} Â· ${spool.spoolmanName ?: spool.displayName}"
    } ?: "Select from Spoolman"

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.offset(y = (-7).dp)
            )

            SpoolStudioLogo(
                color = iconColor,
                logoSize = 52.dp,
                showTitle = false,
                modifier = Modifier.width(52.dp)
            )
        }

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { shouldExpand ->
                expanded = shouldExpand && spools.isNotEmpty() && enabled
            },
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-12).dp)
        ) {
            OutlinedButton(
                onClick = {
                    if (spools.isNotEmpty() && enabled) expanded = true
                },
                enabled = spools.isNotEmpty() && enabled,
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp)
            ) {
                Text(
                    text = displayText,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (selectedSpool != null) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    maxLines = 1
                )

                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "â€” Empty â€”",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    onClick = {
                        onSpoolSelected(null)
                        expanded = false
                        focusManager.clearFocus(force = true)
                    }
                )
                Divider()

                spools.forEach { spool ->
                    val labelText =
                        "ID ${spool.id ?: "-"} Â· ${spool.brand} Â· ${spool.spoolmanName ?: spool.displayName}"

                    DropdownMenuItem(
                        text = {
                            Text(
                                text = labelText,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        onClick = {
                            onSpoolSelected(spool.id)
                            expanded = false
                        }
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp)
                .offset(y = (-8).dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(22.dp),
                contentAlignment = Alignment.Center
            ) {
                Checkbox(
                    checked = isActive,
                    onCheckedChange = if (selectedSpoolId != null && enabled) {
                        onActiveCheckedChange
                    } else {
                        null
                    },
                    modifier = Modifier.size(16.dp),
                    enabled = selectedSpoolId != null && enabled
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Active spool",
                style = MaterialTheme.typography.bodyMedium,
                color = if (selectedSpoolId != null && enabled) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}
