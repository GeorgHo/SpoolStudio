package com.spoolstudio.app.ui.components

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.material3.HorizontalDivider
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.spoolstudio.app.data.local.MaterialDatabase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialSelector(
    selectedMaterial: String,
    customMaterial: String,
    dynamicMaterials: List<String> = emptyList(),
    onMaterialSelected: (String, String, String, String, String) -> Unit,
    onCustomMaterialChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val materialNames = (MaterialDatabase.materials.map { it.name } + dynamicMaterials + listOf("Other"))
        .distinct()
        .sortedWith(compareBy<String> { it == "Other" }.thenBy { it })

    LaunchedEffect(selectedMaterial) {
        if (selectedMaterial == "Other") {
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = if (selectedMaterial == "Other") Modifier.width(120.dp) else Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedMaterial,
                onValueChange = { },
                readOnly = true,
                label = { Text("Filament Type") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                ),
                shape = RoundedCornerShape(20.dp)
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.clip(RoundedCornerShape(20.dp)),
                tonalElevation = 8.dp
            ) {
                materialNames.forEach { materialName ->

                    if (materialName == "Other") {
                        HorizontalDivider()
                    }

                    DropdownMenuItem(
                        text = { Text(materialName) },
                        onClick = {
                            expanded = false
                            val material = MaterialDatabase.getMaterial(materialName)
                            if (material != null) {
                                onMaterialSelected(
                                    material.name,
                                    material.defaultMinTemp.toString(),
                                    material.defaultMaxTemp.toString(),
                                    material.defaultBedMinTemp.toString(),
                                    material.defaultBedMaxTemp.toString()
                                )
                            } else {
                                onMaterialSelected(materialName, "200", "220", "50", "70")
                            }
                        }
                    )
                }
            }
        }

        if (selectedMaterial == "Other") {
            OutlinedTextField(
                value = customMaterial,
                onValueChange = { input ->
                    val sanitized = input.filter { it.isLetterOrDigit() || it in "-+" }
                        .take(16)
                        .uppercase()
                    onCustomMaterialChange(sanitized)
                },
                label = { Text("Custom Material") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                isError = customMaterial.isBlank(),
                supportingText = {
                    if (customMaterial.isBlank()) Text("Please enter a custom material")
                },
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                ),
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}
