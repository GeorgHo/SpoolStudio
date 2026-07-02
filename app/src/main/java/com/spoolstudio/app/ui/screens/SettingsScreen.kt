package com.spoolstudio.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.spoolstudio.app.ui.components.CustomSnackbar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.foundation.background

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    spoolmanUrl: String,
    moonrakerUrl: String,
    bambuMasterKey: String,
    showCommentField: Boolean,
    spoolmanSortBy: String,
    snackbarMessage: String,
    showSnackbar: Boolean,
    onSnackbarDismiss: () -> Unit,
    onTestSpoolmanConnection: (String) -> Unit,
    onTestMoonrakerConnection: (String) -> Unit,
    onSave: (String, String, String, String, Boolean) -> Unit,
    spoolmanStatus: String? = null,
    spoolmanError: String? = null,
    moonrakerStatus: String? = null,
    moonrakerError: String? = null,
    onClearSpoolmanStatus: () -> Unit,
    onClearMoonrakerStatus: () -> Unit,
    isTestingSpoolman: Boolean = false,
    isTestingMoonraker: Boolean = false,
    showLotNumber: Boolean,
    onShowLotNumberChanged: (Boolean) -> Unit,
    onBack: () -> Unit
) {

    var tempUrl by remember(spoolmanUrl) { mutableStateOf(spoolmanUrl) }
    var tempMoonrakerUrl by remember(moonrakerUrl) { mutableStateOf(moonrakerUrl) }
    var tempBambuKey by remember(bambuMasterKey) { mutableStateOf(bambuMasterKey) }
    var tempShowCommentField by remember(showCommentField) { mutableStateOf(showCommentField) }
    var tempSort by remember(spoolmanSortBy) {
        mutableStateOf(spoolmanSortBy.ifBlank { "" })
    }

    var sortExpanded by remember { mutableStateOf(false) }

    var spoolmanTestTriggeredManually by remember { mutableStateOf(false) }
    var moonrakerTestTriggeredManually by remember { mutableStateOf(false) }

    var lastTestedSpoolmanUrl by remember(spoolmanUrl) {
        mutableStateOf(normalizeSettingsUrl(spoolmanUrl))
    }
    var lastTestedMoonrakerUrl by remember(moonrakerUrl) {
        mutableStateOf(normalizeSettingsUrl(moonrakerUrl))
    }

    val sortOptions = listOf(
        "Default (ID)" to "",
        "Color (A–Z)" to "filament.name:asc",
        "Color (Z–A)" to "filament.name:desc",
        "Material (A–Z)" to "filament.material:asc",
        "Material (Z–A)" to "filament.material:desc",
        "Vendor (A–Z)" to "filament.vendor.name:asc",
        "Vendor (Z–A)" to "filament.vendor.name:desc",
        "Location (A–Z)" to "location:asc",
        "Location (Z–A)" to "location:desc"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {

            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = tempUrl,
                onValueChange = {
                    tempUrl = it
                    onClearSpoolmanStatus()
                },
                label = { Text("Spoolman URL") },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { focusState ->
                        if (!focusState.isFocused) {
                            runUrlRetestIfNeeded(
                                currentValue = tempUrl,
                                lastTestedValue = lastTestedSpoolmanUrl,
                                isTesting = isTestingSpoolman,
                                triggeredManually = spoolmanTestTriggeredManually,
                                onTest = onTestSpoolmanConnection,
                                onLastTestedChange = { lastTestedSpoolmanUrl = it }
                            )
                        }
                        spoolmanTestTriggeredManually = false
                    }
            )

            Spacer(modifier = Modifier.height(12.dp))

            ExposedDropdownMenuBox(
                expanded = sortExpanded,
                onExpandedChange = { sortExpanded = !sortExpanded }
            ) {
                OutlinedTextField(
                    value = sortOptions.find { it.second == tempSort }?.first ?: "Default (ID)",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Sort (optional)") },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null
                        )
                    },
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = sortExpanded,
                    onDismissRequest = { sortExpanded = false }
                ) {
                    sortOptions.forEach { (label, value) ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                tempSort = value
                                sortExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    spoolmanTestTriggeredManually = true
                    onTestSpoolmanConnection(tempUrl)
                    lastTestedSpoolmanUrl = normalizeSettingsUrl(tempUrl)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isTestingSpoolman,
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(if (isTestingSpoolman) "Testing..." else "Test Spoolman Connection")
            }

            val spoolmanMessage = spoolmanError ?: spoolmanStatus
            if (spoolmanMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (spoolmanError == null) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(16.dp)
                        )

                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    Text(
                        text = spoolmanMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (spoolmanError != null)
                            MaterialTheme.colorScheme.error
                        else
                            Color(0xFF2E7D32)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = tempMoonrakerUrl,
                onValueChange = {
                    tempMoonrakerUrl = it
                    onClearMoonrakerStatus()
                },
                label = { Text("Moonraker URL") },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { focusState ->
                        if (!focusState.isFocused) {
                            runUrlRetestIfNeeded(
                                currentValue = normalizeMoonrakerSettingsUrl(tempMoonrakerUrl),
                                lastTestedValue = lastTestedMoonrakerUrl,
                                isTesting = isTestingMoonraker,
                                triggeredManually = moonrakerTestTriggeredManually,
                                onTest = onTestMoonrakerConnection,
                                onLastTestedChange = { lastTestedMoonrakerUrl = it }
                            )
                        }
                        moonrakerTestTriggeredManually = false
                    }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    moonrakerTestTriggeredManually = true
                    val normalizedMoonrakerUrl = normalizeMoonrakerSettingsUrl(tempMoonrakerUrl)
                    tempMoonrakerUrl = normalizedMoonrakerUrl
                    onTestMoonrakerConnection(normalizedMoonrakerUrl)
                    lastTestedMoonrakerUrl = normalizedMoonrakerUrl
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isTestingMoonraker,
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(if (isTestingMoonraker) "Testing..." else "Test Moonraker Connection")
            }

            val moonrakerMessage = moonrakerError ?: moonrakerStatus

            if (moonrakerMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (moonrakerError == null) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(16.dp)
                        )

                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    Text(
                        text = moonrakerMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (moonrakerError != null)
                            MaterialTheme.colorScheme.error
                        else
                            Color(0xFF2E7D32)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Show Lot Number",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Switch(
                    checked = showLotNumber,
                    onCheckedChange = { onShowLotNumberChanged(it) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Show Comment Field",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Switch(
                    checked = tempShowCommentField,
                    onCheckedChange = { tempShowCommentField = it }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = tempBambuKey,
                onValueChange = { input ->
                    val cleaned = input
                        .uppercase()
                        .filter { it.isDigit() || it in 'A'..'F' }
                        .take(32)
                    tempBambuKey = cleaned
                },
                label = { Text("Bambu Lab Master Key") },
                supportingText = { Text("32 hex characters") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(onClick = onBack) {
                    Text("Back")
                }

                val hasChanges = hasSettingsChanges(
                    tempSpoolmanUrl = tempUrl,
                    savedSpoolmanUrl = spoolmanUrl,
                    tempMoonrakerUrl = tempMoonrakerUrl,
                    savedMoonrakerUrl = moonrakerUrl,
                    tempSort = tempSort,
                    savedSort = spoolmanSortBy,
                    tempBambuKey = tempBambuKey,
                    savedBambuKey = bambuMasterKey,
                    tempShowCommentField = tempShowCommentField,
                    savedShowCommentField = showCommentField
                )

                Button(
                    onClick = {
                        onSave(
                            normalizeSettingsUrl(tempUrl),
                            normalizeMoonrakerSettingsUrl(tempMoonrakerUrl),
                            normalizeSettingsSort(tempSort),
                            tempBambuKey,
                            tempShowCommentField
                        )
                    },
                    enabled = hasChanges
                ) {
                    Text("Save")
                }
            }
        }

        CustomSnackbar(
            message = snackbarMessage,
            isVisible = showSnackbar,
            onDismiss = onSnackbarDismiss
        )
    }
}
