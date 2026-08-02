package com.spoolstudio.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spoolstudio.app.ui.components.CustomSnackbar
import com.spoolstudio.app.ui.components.SpoolStudioLogo
import com.spoolstudio.app.ui.PrinterIntegrationMode
import com.spoolstudio.app.ui.theme.SpoolStudioColors
import com.spoolstudio.app.ui.theme.SpoolStudioShape
import com.spoolstudio.app.ui.theme.spoolStudioBackground
import androidx.compose.ui.window.Dialog
import com.spoolstudio.app.data.remote.spoolman.SpoolmanLegacyFilamentConversion

private enum class LegacyConversionSortMode(val label: String) {
    SPOOL_ID("Spool ID"),
    FILAMENT_ID("Filament ID"),
    BRAND("Brand")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    spoolmanUrl: String,
    moonrakerUrl: String,
    bambuMasterKey: String,
    showCommentField: Boolean,
    showEmptySpoolWeight: Boolean,
    printerIntegrationMode: PrinterIntegrationMode,
    spoolmanSortBy: String,
    snackbarMessage: String,
    showSnackbar: Boolean,
    spoolCount: Int,
    activeSpoolCount: Int,
    archivedSpoolCount: Int,
    spoolmanBrandCount: Int,
    spoolmanMaterialCount: Int,
    spoolmanLocationCount: Int,
    spoolmanColorCount: Int,
    spoolmanCardUidFieldSpoolCount: Int,
    spoolmanCardUidFieldKeys: List<String>,
    spoolmanMaterialModifierFieldAvailable: Boolean?,
    materialModifierFieldDeclined: Boolean,
    isCreatingMaterialModifierField: Boolean,
    moonrakerFirmwareVersion: String?,
    moonrakerVersion: String?,
    moonrakerSupportsSpoolLink: Boolean?,
    moonrakerHasSpoolmanComponent: Boolean?,
    moonrakerHasSpoolLinkComponent: Boolean?,
    moonrakerSpoolmanIntegrationEnabled: Boolean?,
    moonrakerSetSpoolIdCommandAvailable: Boolean?,
    moonrakerDetectedModeLabel: String?,
    legacyFilamentConversions: List<SpoolmanLegacyFilamentConversion>,
    isScanningLegacyFilaments: Boolean,
    isConvertingLegacyFilaments: Boolean,
    onSnackbarDismiss: () -> Unit,
    onTestSpoolmanConnection: (String) -> Unit,
    onCreateMaterialModifierField: (String) -> Unit,
    onScanLegacyFilamentConversions: (String) -> Unit,
    onClearLegacyFilamentConversions: () -> Unit,
    onConvertLegacyFilaments: (String, Set<Int>) -> Unit,
    onTestMoonrakerConnection: (String) -> Unit,
    onSave: (String, String, PrinterIntegrationMode, String, String, Boolean) -> Unit,
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
    onShowEmptySpoolWeightChanged: (Boolean) -> Unit,
    onBack: () -> Unit
) {
    var tempUrl by remember(spoolmanUrl) { mutableStateOf(spoolmanUrl) }
    var tempMoonrakerUrl by remember(moonrakerUrl) { mutableStateOf(moonrakerUrl) }
    var tempBambuKey by remember(bambuMasterKey) { mutableStateOf(bambuMasterKey) }
    var tempShowCommentField by remember(showCommentField) { mutableStateOf(showCommentField) }
    var tempPrinterIntegrationMode by remember(printerIntegrationMode) { mutableStateOf(printerIntegrationMode) }
    var tempSort by remember(spoolmanSortBy) { mutableStateOf(spoolmanSortBy.ifBlank { "" }) }
    var sortExpanded by remember { mutableStateOf(false) }
    var showSpoolmanInfo by remember { mutableStateOf(false) }
    var showMoonrakerInfo by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showLegacyConversionDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val versionName = remember(context) {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "3.0"
        }.getOrDefault("2.0")
    }

    var spoolmanTestTriggeredManually by remember { mutableStateOf(false) }
    var moonrakerTestTriggeredManually by remember { mutableStateOf(false) }
    var lastTestedSpoolmanUrl by remember(spoolmanUrl) {
        mutableStateOf(normalizeSettingsUrl(spoolmanUrl))
    }
    var lastTestedMoonrakerUrl by remember(moonrakerUrl) {
        mutableStateOf(normalizeMoonrakerSettingsUrl(moonrakerUrl))
    }

    LaunchedEffect(legacyFilamentConversions) {
        if (legacyFilamentConversions.isNotEmpty()) {
            showLegacyConversionDialog = true
        }
    }

    val sortOptions = listOf(
        "Default (ID)" to "",
        "Color (A-Z)" to "filament.name:asc",
        "Color (Z-A)" to "filament.name:desc",
        "Material (A-Z)" to "filament.material:asc",
        "Material (Z-A)" to "filament.material:desc",
        "Vendor (A-Z)" to "filament.vendor.name:asc",
        "Vendor (Z-A)" to "filament.vendor.name:desc",
        "Location (A-Z)" to "location:asc",
        "Location (Z-A)" to "location:desc"
    )

    val hasChanges = hasSettingsChanges(
        tempSpoolmanUrl = tempUrl,
        savedSpoolmanUrl = spoolmanUrl,
        tempMoonrakerUrl = tempMoonrakerUrl,
        savedMoonrakerUrl = moonrakerUrl,
        tempPrinterIntegrationMode = tempPrinterIntegrationMode,
        savedPrinterIntegrationMode = printerIntegrationMode,
        tempSort = tempSort,
        savedSort = spoolmanSortBy,
        tempBambuKey = tempBambuKey,
        savedBambuKey = bambuMasterKey,
        tempShowCommentField = tempShowCommentField,
        savedShowCommentField = showCommentField
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .spoolStudioBackground()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            SettingsTopBar(onBack = onBack)

            SettingsPanel(title = "Spoolman") {
                SettingsTextField(
                    value = tempUrl,
                    onValueChange = {
                        tempUrl = it
                        onClearSpoolmanStatus()
                    },
                    label = "Spoolman URL",
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
                        },
                )

                SettingsSortDropdown(
                    sortOptions = sortOptions,
                    selectedSort = tempSort,
                    expanded = sortExpanded,
                    onExpandedChange = { sortExpanded = !sortExpanded },
                    onSortSelected = {
                        tempSort = it
                        sortExpanded = false
                    }
                )

                SettingsPrimaryButton(
                    text = if (isTestingSpoolman) "Testing..." else "Test Spoolman Connection",
                    enabled = !isTestingSpoolman,
                    onClick = {
                        spoolmanTestTriggeredManually = true
                        val normalizedSpoolmanUrl = normalizeSettingsUrl(tempUrl)
                        tempUrl = normalizedSpoolmanUrl
                        onTestSpoolmanConnection(normalizedSpoolmanUrl)
                        lastTestedSpoolmanUrl = normalizedSpoolmanUrl
                    }
                )

                SettingsConnectionStatus(
                    message = spoolmanError ?: spoolmanStatus,
                    isError = spoolmanError != null
                )

                SettingsSecondaryButton(
                    text = if (showSpoolmanInfo) "Hide Spoolman Info" else "Show Spoolman Info",
                    onClick = { showSpoolmanInfo = !showSpoolmanInfo }
                )

                if (showSpoolmanInfo) {
                    SpoolmanInfoSummary(
                        spoolCount = spoolCount,
                        activeSpoolCount = activeSpoolCount,
                        archivedSpoolCount = archivedSpoolCount,
                        brandCount = spoolmanBrandCount,
                        materialCount = spoolmanMaterialCount,
                        locationCount = spoolmanLocationCount,
                        colorCount = spoolmanColorCount,
                        cardUidFieldSpoolCount = spoolmanCardUidFieldSpoolCount,
                        cardUidFieldKeys = spoolmanCardUidFieldKeys,
                        sortLabel = sortOptions.firstOrNull { it.second == tempSort }?.first ?: "Custom"
                    )

                    SettingsMaterialModifierFieldStatus(
                        available = spoolmanMaterialModifierFieldAvailable,
                        declined = materialModifierFieldDeclined,
                        isCreating = isCreatingMaterialModifierField,
                        onCreate = {
                            val normalizedSpoolmanUrl = normalizeSettingsUrl(tempUrl)
                            tempUrl = normalizedSpoolmanUrl
                            onCreateMaterialModifierField(normalizedSpoolmanUrl)
                        }
                    )

                    SettingsSecondaryButton(
                        text = if (isScanningLegacyFilaments) {
                            "Scanning legacy materials..."
                        } else {
                            "Scan legacy Spoolman materials"
                        },
                        enabled = !isScanningLegacyFilaments && !isConvertingLegacyFilaments,
                        onClick = {
                            val normalizedSpoolmanUrl = normalizeSettingsUrl(tempUrl)
                            tempUrl = normalizedSpoolmanUrl
                            onScanLegacyFilamentConversions(normalizedSpoolmanUrl)
                        }
                    )
                }
            }

            SettingsPanel(title = "Printer / Moonraker") {
                SettingsTextField(
                    value = tempMoonrakerUrl,
                    onValueChange = {
                        tempMoonrakerUrl = it
                        onClearMoonrakerStatus()
                    },
                    label = "Moonraker URL",
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
                        },
                )

                SettingsPrimaryButton(
                    text = if (isTestingMoonraker) "Testing..." else "Test Moonraker Connection",
                    enabled = !isTestingMoonraker,
                    onClick = {
                        moonrakerTestTriggeredManually = true
                        val normalizedMoonrakerUrl = normalizeMoonrakerSettingsUrl(tempMoonrakerUrl)
                        tempMoonrakerUrl = normalizedMoonrakerUrl
                        onTestMoonrakerConnection(normalizedMoonrakerUrl)
                        lastTestedMoonrakerUrl = normalizedMoonrakerUrl
                    }
                )

                SettingsConnectionStatus(
                    message = moonrakerError ?: moonrakerStatus,
                    isError = moonrakerError != null
                )

                SettingsSecondaryButton(
                    text = if (showMoonrakerInfo) "Hide Moonraker Info" else "Show Moonraker Info",
                    onClick = { showMoonrakerInfo = !showMoonrakerInfo }
                )

                if (showMoonrakerInfo) {
                    SettingsMoonrakerInfoSummary(
                        firmwareVersion = moonrakerFirmwareVersion,
                        moonrakerVersion = moonrakerVersion,
                        supportsSpoolLink = moonrakerSupportsSpoolLink,
                        hasSpoolmanComponent = moonrakerHasSpoolmanComponent,
                        hasSpoolLinkComponent = moonrakerHasSpoolLinkComponent,
                        spoolmanIntegrationEnabled = moonrakerSpoolmanIntegrationEnabled,
                        setSpoolIdCommandAvailable = moonrakerSetSpoolIdCommandAvailable,
                        detectedModeLabel = moonrakerDetectedModeLabel
                    )
                }
            }

            SettingsPanel(title = "Display") {
                SettingsSwitchRow(
                    label = "Show Product / Lot Code",
                    checked = showLotNumber,
                    onCheckedChange = onShowLotNumberChanged
                )

                SettingsSwitchRow(
                    label = "Show Comment Field",
                    checked = tempShowCommentField,
                    onCheckedChange = { tempShowCommentField = it }
                )

                SettingsSwitchRow(
                    label = "Show Empty Spool Weight",
                    checked = showEmptySpoolWeight,
                    onCheckedChange = onShowEmptySpoolWeightChanged
                )
            }

            SettingsPanel(title = "Bambu Lab") {
                SettingsTextField(
                    value = tempBambuKey,
                    onValueChange = { input ->
                        tempBambuKey = input
                            .uppercase()
                            .filter { it.isDigit() || it in 'A'..'F' }
                            .take(32)
                    },
                    label = "Master Key",
                    supportingText = "32 hex characters",
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            SettingsPanel(title = "About") {
                SettingsInfoRow(
                    label = "Version",
                    value = "Spool Studio v$versionName"
                )

                SettingsSecondaryButton(
                    text = "About Spool Studio",
                    onClick = { showAboutDialog = true }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SettingsPrimaryButton(
                    text = "Save",
                    enabled = hasChanges,
                    onClick = {
                        onSave(
                            normalizeSettingsUrl(tempUrl),
                            normalizeMoonrakerSettingsUrl(tempMoonrakerUrl),
                            tempPrinterIntegrationMode,
                            normalizeSettingsSort(tempSort),
                            tempBambuKey,
                            tempShowCommentField
                        )
                    },
                    modifier = Modifier.width(132.dp)
                )
            }
        }

        CustomSnackbar(
            message = snackbarMessage,
            isVisible = showSnackbar,
            onDismiss = onSnackbarDismiss
        )

        if (showAboutDialog) {
            SettingsAboutDialog(
                versionName = versionName,
                onDismiss = { showAboutDialog = false }
            )
        }

        if (showLegacyConversionDialog && legacyFilamentConversions.isNotEmpty()) {
            LegacyFilamentConversionDialog(
                candidates = legacyFilamentConversions,
                isConverting = isConvertingLegacyFilaments,
                onDismiss = {
                    showLegacyConversionDialog = false
                    onClearLegacyFilamentConversions()
                },
                onConvert = { selectedIds ->
                    val normalizedSpoolmanUrl = normalizeSettingsUrl(tempUrl)
                    tempUrl = normalizedSpoolmanUrl
                    onConvertLegacyFilaments(normalizedSpoolmanUrl, selectedIds)
                    showLegacyConversionDialog = false
                }
            )
        }
    }
}

@Composable
private fun SettingsTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    supportingText: String? = null
) {
    var fieldValue by remember { mutableStateOf(TextFieldValue(value)) }
    LaunchedEffect(value) {
        if (value != fieldValue.text) {
            fieldValue = TextFieldValue(value, selection = TextRange(value.length))
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    lineHeight = 17.sp
                ),
                color = SpoolStudioColors.OnGraphite,
                maxLines = 1,
                modifier = Modifier.width(112.dp)
            )
            BasicTextField(
                value = fieldValue,
                onValueChange = {
                    fieldValue = it
                    onValueChange(it.text)
                },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 15.sp,
                    lineHeight = 19.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SpoolStudioColors.OnGraphite
                ),
                cursorBrush = SolidColor(SpoolStudioColors.AccentCyan),
                modifier = Modifier
                    .weight(1.75f)
                    .clipToBounds()
            )
        }
        HorizontalDivider(color = SpoolStudioColors.GraphiteMuted.copy(alpha = 0.75f))
        if (supportingText != null) {
            Text(
                text = supportingText,
                style = MaterialTheme.typography.bodySmall,
                color = SpoolStudioColors.OnGraphiteMuted,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun SettingsTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium.copy(fontSize = 27.sp, lineHeight = 34.sp),
            fontWeight = FontWeight.SemiBold,
            color = SpoolStudioColors.OnGraphite
        )

        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = SpoolStudioColors.OnGraphite
            )
        }
    }
}

@Composable
private fun SettingsPanel(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = SpoolStudioShape.Dialog,
        colors = CardDefaults.cardColors(containerColor = SpoolStudioColors.Graphite),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        border = BorderStroke(1.dp, SpoolStudioColors.GraphiteMuted.copy(alpha = 0.85f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp, lineHeight = 22.sp),
                fontWeight = FontWeight.SemiBold,
                color = SpoolStudioColors.OnGraphite
            )

            content()
        }
    }
}

@Composable
private fun SettingsMaterialModifierFieldStatus(
    available: Boolean?,
    declined: Boolean,
    isCreating: Boolean,
    onCreate: () -> Unit
) {
    val statusText = when {
        available == true && !declined -> "Ready"
        declined -> "Disabled"
        available == false -> "Missing"
        else -> "Not tested"
    }
    val statusColor = when {
        available == true && !declined -> SpoolStudioColors.Success
        declined || available == false -> SpoolStudioColors.Error
        else -> SpoolStudioColors.OnGraphiteMuted
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = SpoolStudioColors.GraphiteRaised.copy(alpha = 0.62f),
                shape = SpoolStudioShape.Small
            )
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Material modifier",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                color = SpoolStudioColors.OnGraphite,
                modifier = Modifier.width(112.dp)
            )
            Text(
                text = statusText,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
                fontWeight = FontWeight.SemiBold,
                color = statusColor,
                modifier = Modifier.weight(1f)
            )
        }

        if ((available != true || declined) && !isCreating) {
            Text(
                text = "Create this Spoolman extra field to keep modifiers such as Plus or HS outside printer-safe material data.",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 14.sp),
                color = SpoolStudioColors.OnGraphiteMuted
            )
        }

        if (available != true || declined) {
            SettingsSecondaryButton(
                text = if (isCreating) "Creating..." else "Create Material Modifier Field",
                enabled = !isCreating,
                onClick = onCreate
            )
        }
    }
}

@Composable
private fun SpoolmanInfoSummary(
    spoolCount: Int,
    activeSpoolCount: Int,
    archivedSpoolCount: Int,
    brandCount: Int,
    materialCount: Int,
    locationCount: Int,
    colorCount: Int,
    cardUidFieldSpoolCount: Int,
    cardUidFieldKeys: List<String>,
    sortLabel: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = SpoolStudioColors.GraphiteRaised.copy(alpha = 0.62f),
                shape = SpoolStudioShape.Small
            )
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        SettingsInfoRow(label = "Loaded spools", value = spoolCount.toString())
        SettingsInfoRow(label = "Active spools", value = activeSpoolCount.toString())
        SettingsInfoRow(label = "Archived spools", value = archivedSpoolCount.toString())
        SettingsInfoRow(label = "Brands", value = brandCount.toString())
        SettingsInfoRow(label = "Materials", value = materialCount.toString())
        SettingsInfoRow(label = "Locations", value = locationCount.toString())
        SettingsInfoRow(label = "Colors", value = colorCount.toString())
        SettingsInfoRow(
            label = "Card UID data",
            value = if (cardUidFieldSpoolCount > 0) {
                "$cardUidFieldSpoolCount spools (${cardUidFieldKeys.joinToString().ifBlank { "extra" }})"
            } else {
                "Not found"
            }
        )
        SettingsInfoRow(label = "Sort", value = sortLabel)

        Text(
            text = "Values are based on the currently loaded Spoolman catalog.",
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 14.sp),
            color = SpoolStudioColors.OnGraphiteMuted,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}

@Composable
private fun SettingsMoonrakerInfoSummary(
    firmwareVersion: String?,
    moonrakerVersion: String?,
    supportsSpoolLink: Boolean?,
    hasSpoolmanComponent: Boolean?,
    hasSpoolLinkComponent: Boolean?,
    spoolmanIntegrationEnabled: Boolean?,
    setSpoolIdCommandAvailable: Boolean?,
    detectedModeLabel: String?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = SpoolStudioColors.GraphiteRaised.copy(alpha = 0.62f),
                shape = SpoolStudioShape.Small
            )
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        SettingsInfoRow(
            label = "Printer firmware",
            value = when (supportsSpoolLink) {
                true -> "${firmwareVersion ?: "Unknown"} OK"
                false -> "${firmwareVersion ?: "Unknown"} - requires 1.5.0+"
                null -> "${firmwareVersion ?: "Unknown"} - not tested"
            }
        )
        SettingsInfoRow(
            label = "Moonraker",
            value = if (moonrakerVersion.isNullOrBlank()) {
                "Not tested"
            } else {
                "$moonrakerVersion OK"
            }
        )
        SettingsInfoRow(
            label = "Spoolman service",
            value = when (hasSpoolmanComponent) {
                true -> "Loaded"
                false -> "Missing"
                null -> "Not tested"
            }
        )
        SettingsInfoRow(
            label = "RFID bridge",
            value = when (hasSpoolLinkComponent) {
                true -> "Loaded"
                false -> "Missing"
                null -> "Not tested"
            }
        )
        SettingsInfoRow(
            label = "Assignment command",
            value = when (setSpoolIdCommandAvailable) {
                true -> "SET_SPOOL_ID loaded"
                false -> "SET_SPOOL_ID missing"
                null -> "Not tested"
            }
        )

        val integrationText = when (spoolmanIntegrationEnabled) {
            true -> if (setSpoolIdCommandAvailable == false) {
                "Not ready: SET_SPOOL_ID is missing. Enable AFC/Spoolman options and restart Klipper/Moonraker."
            } else {
                "Ready: printer Spoolman integration is active."
            }
            false -> "Not ready: enable Spoolman Integration in the printer config and restart Klipper/Moonraker."
            null -> "Run the Moonraker connection test to check printer Spoolman integration."
        }
        Text(
            text = integrationText,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 14.sp),
            color = when (spoolmanIntegrationEnabled) {
                true -> if (setSpoolIdCommandAvailable == false) SpoolStudioColors.Error else SpoolStudioColors.Success
                false -> SpoolStudioColors.Error
                null -> SpoolStudioColors.OnGraphiteMuted
            }
        )
    }
}

@Composable
private fun SettingsInfoRow(
    label: String,
    value: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                color = SpoolStudioColors.OnGraphite,
                modifier = Modifier.width(112.dp)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
                fontWeight = FontWeight.SemiBold,
                color = SpoolStudioColors.OnGraphite,
                modifier = Modifier.weight(1f)
            )
        }
        HorizontalDivider(color = SpoolStudioColors.GraphiteMuted.copy(alpha = 0.75f))
    }
}

@Composable
private fun LegacyFilamentConversionDialog(
    candidates: List<SpoolmanLegacyFilamentConversion>,
    isConverting: Boolean,
    onDismiss: () -> Unit,
    onConvert: (Set<Int>) -> Unit
) {
    var selectedIds by remember(candidates) {
        mutableStateOf(candidates.map { it.filamentId }.toSet())
    }
    var sortMode by remember { mutableStateOf(LegacyConversionSortMode.SPOOL_ID) }
    val sortedCandidates = remember(candidates, sortMode) {
        when (sortMode) {
            LegacyConversionSortMode.SPOOL_ID -> candidates.sortedWith(
                compareBy<SpoolmanLegacyFilamentConversion> { it.spoolIds.minOrNull() ?: Int.MAX_VALUE }
                    .thenBy { it.filamentId }
            )
            LegacyConversionSortMode.FILAMENT_ID -> candidates.sortedBy { it.filamentId }
            LegacyConversionSortMode.BRAND -> candidates.sortedWith(
                compareBy<SpoolmanLegacyFilamentConversion> { it.vendorName.lowercase() }
                    .thenBy { it.name.lowercase() }
                    .thenBy { it.filamentId }
            )
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = SpoolStudioShape.Dialog,
            colors = CardDefaults.cardColors(containerColor = SpoolStudioColors.Graphite),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            border = BorderStroke(1.dp, SpoolStudioColors.GraphiteMuted.copy(alpha = 0.85f))
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Legacy material conversion",
                            style = MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp),
                            fontWeight = FontWeight.SemiBold,
                            color = SpoolStudioColors.OnGraphite
                        )
                        Text(
                            text = "Choose which Spoolman filament records should be converted to the paxx12 v3 field layout.",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 16.sp),
                            color = SpoolStudioColors.OnGraphiteMuted
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = SpoolStudioColors.OnGraphite
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SettingsSecondaryButton(
                        text = "Select all",
                        enabled = !isConverting,
                        onClick = { selectedIds = candidates.map { it.filamentId }.toSet() },
                        modifier = Modifier.weight(1f)
                    )
                    SettingsSecondaryButton(
                        text = "Clear",
                        enabled = !isConverting,
                        onClick = { selectedIds = emptySet() },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sort by",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = SpoolStudioColors.OnGraphiteMuted
                    )
                    LegacyConversionSortMode.entries.forEach { mode ->
                        val selected = mode == sortMode
                        Text(
                            text = mode.label,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.sp,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
                            ),
                            color = if (selected) SpoolStudioColors.Graphite else SpoolStudioColors.GoldSoft,
                            modifier = Modifier
                                .background(
                                    color = if (selected) SpoolStudioColors.GoldSoft else Color.Transparent,
                                    shape = SpoolStudioShape.Small
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (selected) SpoolStudioColors.GoldSoft else SpoolStudioColors.GraphiteMuted,
                                    shape = SpoolStudioShape.Small
                                )
                                .clickable(enabled = !isConverting) { sortMode = mode }
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 380.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    sortedCandidates.forEach { candidate ->
                        val checked = candidate.filamentId in selectedIds
                        val spoolIdText = when (candidate.spoolIds.size) {
                            0 -> "No affected spool IDs"
                            1 -> "Spool ID #${candidate.spoolIds.first()}"
                            else -> "Spool IDs ${candidate.spoolIds.joinToString { "#$it" }}"
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = SpoolStudioColors.GraphiteRaised.copy(alpha = 0.72f),
                                    shape = SpoolStudioShape.Small
                                )
                                .clickable(enabled = !isConverting) {
                                    selectedIds = if (checked) {
                                        selectedIds - candidate.filamentId
                                    } else {
                                        selectedIds + candidate.filamentId
                                    }
                                }
                                .padding(horizontal = 8.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Checkbox(
                                checked = checked,
                                onCheckedChange = { enabled ->
                                    selectedIds = if (enabled) {
                                        selectedIds + candidate.filamentId
                                    } else {
                                        selectedIds - candidate.filamentId
                                    }
                                },
                                enabled = !isConverting,
                                colors = CheckboxDefaults.colors(
                                    checkedColor = SpoolStudioColors.AccentCyan,
                                    uncheckedColor = SpoolStudioColors.OnGraphiteMuted,
                                    checkmarkColor = SpoolStudioColors.Graphite
                                )
                            )
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Text(
                                    text = "${candidate.vendorName} - ${candidate.name}",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                                    fontWeight = FontWeight.SemiBold,
                                    color = SpoolStudioColors.OnGraphite
                                )
                                Text(
                                    text = "Filament #${candidate.filamentId} - $spoolIdText",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = SpoolStudioColors.OnGraphiteMuted
                                )
                                Text(
                                    text = "${candidate.currentLabel} -> ${candidate.targetLabel}",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 15.sp),
                                    color = SpoolStudioColors.GoldSoft
                                )
                            }
                        }
                    }
                }

                Text(
                    text = "Only selected filament records are patched. Existing spools, UID assignments and product / lot codes are kept.",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 15.sp),
                    color = SpoolStudioColors.OnGraphiteMuted
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SettingsSecondaryButton(
                        text = "Cancel",
                        enabled = !isConverting,
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    )
                    SettingsPrimaryButton(
                        text = if (isConverting) "Converting..." else "Convert selected",
                        enabled = !isConverting && selectedIds.isNotEmpty(),
                        onClick = { onConvert(selectedIds) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsAboutDialog(
    versionName: String,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = SpoolStudioShape.Dialog,
            colors = CardDefaults.cardColors(containerColor = SpoolStudioColors.Graphite),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "About",
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp),
                        fontWeight = FontWeight.SemiBold,
                        color = SpoolStudioColors.OnGraphite
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = SpoolStudioColors.OnGraphite
                        )
                    }
                }

                SpoolStudioLogo(
                    color = SpoolStudioColors.AccentCyan,
                    logoSize = 150.dp,
                    showTitle = false,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Spool Studio",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = SpoolStudioColors.OnGraphite,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Version $versionName",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = SpoolStudioColors.AccentCyan,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                HorizontalDivider(color = SpoolStudioColors.GraphiteMuted.copy(alpha = 0.75f))

                Text(
                    text = "2026 Spool Studio by Hovi",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SpoolStudioColors.OnGraphite,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Based on SpoolPainter by ni4223.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SpoolStudioColors.OnGraphiteMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "With many thanks to ni4223, OpenSpool, Spoolman and the open-source community.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SpoolStudioColors.OnGraphiteMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(2.dp))
            }
        }
    }
}

@Composable
private fun SettingsPrimaryButton(
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = SpoolStudioShape.Button,
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = SpoolStudioColors.Gold,
            contentColor = Color.White,
            disabledContainerColor = SpoolStudioColors.GraphiteRaised,
            disabledContentColor = SpoolStudioColors.OnGraphiteMuted.copy(alpha = 0.55f)
        )
    ) {
        Text(text, maxLines = 1)
    }
}

@Composable
private fun SettingsSecondaryButton(
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = SpoolStudioShape.Button,
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = SpoolStudioColors.AccentCyan,
            disabledContentColor = SpoolStudioColors.OnGraphiteMuted.copy(alpha = 0.55f)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (enabled) SpoolStudioColors.GraphiteMuted else SpoolStudioColors.GraphiteMuted.copy(alpha = 0.45f)
        )
    ) {
        Text(text, maxLines = 1)
    }
}
