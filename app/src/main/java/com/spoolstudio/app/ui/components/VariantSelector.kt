package com.spoolstudio.app.ui.components

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.height
import androidx.compose.material3.HorizontalDivider
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
import com.spoolstudio.app.data.local.VariantDatabase

private data class VariantOption(val label: String, val value: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VariantSelector(
    selectedVariant: String,
    dynamicVariants: List<String> = emptyList(),
    onVariantChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var customVariant by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    val normalized = (VariantDatabase.variants + dynamicVariants)
        .map { it.trim() }
        .filter { it.isNotEmpty() && !it.equals("leer", ignoreCase = true) }
        .map { if (it.equals("Basic / leer", ignoreCase = true)) "Basic" else it }
        .distinctBy { it.lowercase() }
        .sortedBy { it.lowercase() }

    val options = buildList {
        add(VariantOption("leer", ""))
        add(VariantOption("Basic", "Basic"))

        normalized.forEach { value ->
            if (!value.equals("Basic", ignoreCase = true)) {
                add(VariantOption(value, value))
            }
        }

        add(VariantOption("Other", "Other"))
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
            modifier = if (selectedVariant == "Other") Modifier.width(120.dp) else Modifier.fillMaxWidth()
        ) {

            OutlinedTextField(
                value = selectedVariant,
                onValueChange = {},
                readOnly = true,
                label = { Text("Variant") },
                placeholder = { Text("Optional") },
                singleLine = true,
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
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
                options.forEach { option ->
                    if (option.value == "Other") {
                        HorizontalDivider()
                    }
                    DropdownMenuItem(
                        text = { Text(option.label) },
                        onClick = {
                            if (option.value == "Other") {
                                onVariantChange("Other")
                                customVariant = ""   // optional reset
                            } else {
                                onVariantChange(option.value)
                            }
                            expanded = false
                        }
                    )
                    if (option.value == "") {   // ← "leer"
                        HorizontalDivider()
                    }
                }
                LaunchedEffect(selectedVariant) {
                    if (selectedVariant == "Other") {
                        focusRequester.requestFocus()
                        keyboardController?.show()
                    }
                }
            }
        }

        // ✅ WICHTIG: JETZT AUSSERHALB!
        if (selectedVariant == "Other") {

            OutlinedTextField(
                value = customVariant,
                onValueChange = {
                    customVariant = it
                },
                label = { Text("Custom Variant") },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                singleLine = true,
                isError = customVariant.isBlank(),
                supportingText = {
                    if (customVariant.isBlank()) Text("Please enter a custom variant")
                },
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}
