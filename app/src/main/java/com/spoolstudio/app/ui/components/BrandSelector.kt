package com.spoolstudio.app.ui.components

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.zIndex
import com.spoolstudio.app.data.local.BrandDatabase
import com.spoolstudio.app.ui.theme.SpoolStudioShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrandSelector(
    selectedBrand: String,
    customBrand: String,
    dynamicBrands: List<String> = emptyList(),
    onBrandSelected: (String) -> Unit,
    onCustomBrandChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val regularBrands = (BrandDatabase.brands + dynamicBrands)
        .distinct()
        .filterNot { it == "Other" || it == "Generic" }
    val brands = listOf("Other", "Generic") + regularBrands

    LaunchedEffect(selectedBrand) {
        if (selectedBrand == "Other") {
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    LaunchedEffect(expanded) {
        if (!expanded) searchQuery = ""
    }

    val filteredBrands = brands.filter {
        searchQuery.isBlank() || it.contains(searchQuery, ignoreCase = true)
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
            modifier = if (selectedBrand == "Other") Modifier.width(120.dp) else Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedBrand,
                onValueChange = { },
                readOnly = true,
                label = { Text("Brand") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                ),
                shape = SpoolStudioShape.Field
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .zIndex(1f)
                    .clip(SpoolStudioShape.Field)
                    .height(320.dp),
                tonalElevation = 8.dp
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it.take(32) },
                    label = { Text("Search") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    textStyle = MaterialTheme.typography.bodyMedium,
                    shape = SpoolStudioShape.Field
                )

                filteredBrands.forEach { brand ->

                    DropdownMenuItem(
                        text = { Text(brand) },
                        onClick = {
                            expanded = false
                            onBrandSelected(brand)
                        }
                    )

                    if (brand == "Generic") {
                        HorizontalDivider()
                    }
                }
            }
        }

        if (selectedBrand == "Other") {
            OutlinedTextField(
                value = customBrand,
                onValueChange = { input ->
                    val sanitized = input.filter { it.isLetterOrDigit() || it in " .-" }
                        .take(32)
                        .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                    onCustomBrandChange(sanitized)
                },
                label = { Text("Custom Brand") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                isError = customBrand.isBlank(),
                supportingText = {
                    if (customBrand.isBlank()) Text("Please enter a custom brand")
                },
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                ),
                shape = SpoolStudioShape.Field
            )
        }
    }
}
