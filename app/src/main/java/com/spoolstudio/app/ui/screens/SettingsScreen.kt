package com.spoolstudio.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spoolstudio.app.ui.components.CustomSnackbar
import com.spoolstudio.app.ui.components.SpoolStudioLogo
import com.spoolstudio.app.ui.theme.SpoolStudioColors
import com.spoolstudio.app.ui.theme.SpoolStudioShape
import com.spoolstudio.app.ui.theme.spoolStudioBackground
import androidx.compose.ui.window.Dialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    spoolmanUrl: String,
    moonrakerUrl: String,
    bambuMasterKey: String,
    showCommentField: Boolean,
    showEmptySpoolWeight: Boolean,
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
    onShowEmptySpoolWeightChanged: (Boolean) -> Unit,
    onBack: () -> Unit
) {
    var tempUrl by remember(spoolmanUrl) { mutableStateOf(spoolmanUrl) }
    var tempMoonrakerUrl by remember(moonrakerUrl) { mutableStateOf(moonrakerUrl) }
    var tempBambuKey by remember(bambuMasterKey) { mutableStateOf(bambuMasterKey) }
    var tempShowCommentField by remember(showCommentField) { mutableStateOf(showCommentField) }
    var tempSort by remember(spoolmanSortBy) { mutableStateOf(spoolmanSortBy.ifBlank { "" }) }
    var sortExpanded by remember { mutableStateOf(false) }
    var showSpoolmanInfo by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val versionName = remember(context) {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "2.0"
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
                        sortLabel = sortOptions.firstOrNull { it.second == tempSort }?.first ?: "Custom"
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
            }

            SettingsPanel(title = "Display") {
                SettingsSwitchRow(
                    label = "Show Lot Number",
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
                color = SpoolStudioColors.Ink,
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
                    color = SpoolStudioColors.Ink
                ),
                modifier = Modifier
                    .weight(1.75f)
                    .clipToBounds()
            )
        }
        HorizontalDivider(color = SpoolStudioColors.OutlineSoft.copy(alpha = 0.75f))
        if (supportingText != null) {
            Text(
                text = supportingText,
                style = MaterialTheme.typography.bodySmall,
                color = SpoolStudioColors.InkMuted,
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
        shape = SpoolStudioShape.Small,
        colors = CardDefaults.cardColors(containerColor = SpoolStudioColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp, lineHeight = 22.sp),
                fontWeight = FontWeight.SemiBold,
                color = SpoolStudioColors.Ink
            )

            content()
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
    sortLabel: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = SpoolStudioColors.SurfaceMuted.copy(alpha = 0.45f),
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
        SettingsInfoRow(label = "Sort", value = sortLabel)

        Text(
            text = "Values are based on the currently loaded Spoolman catalog.",
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 14.sp),
            color = SpoolStudioColors.InkMuted,
            modifier = Modifier.padding(top = 6.dp)
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
                color = SpoolStudioColors.Ink,
                modifier = Modifier.width(112.dp)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
                fontWeight = FontWeight.SemiBold,
                color = SpoolStudioColors.Ink,
                modifier = Modifier.weight(1f)
            )
        }
        HorizontalDivider(color = SpoolStudioColors.OutlineSoft.copy(alpha = 0.75f))
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
            disabledContainerColor = SpoolStudioColors.SurfaceMuted,
            disabledContentColor = SpoolStudioColors.InkMuted.copy(alpha = 0.55f)
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
            disabledContentColor = SpoolStudioColors.InkMuted.copy(alpha = 0.55f)
        )
    ) {
        Text(text, maxLines = 1)
    }
}
