package com.spoolstudio.app.ui.screens

import androidx.compose.foundation.layout.Box
import com.spoolstudio.app.ui.components.SpoolInfoCard
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.spoolstudio.app.data.local.MaterialDatabase
import com.spoolstudio.app.domain.models.FilamentSpool
import com.spoolstudio.app.domain.models.OpenSpoolData
import com.spoolstudio.app.ui.SpoolMode
import com.spoolstudio.app.ui.SpoolmanSaveRequest
import com.spoolstudio.app.ui.components.FilamentForm
import com.spoolstudio.app.ui.components.SpoolStudioLogo
import com.spoolstudio.app.ui.components.SpoolmanFilamentDropdown
import com.spoolstudio.app.utils.*

@Composable
fun SpoolStudioScreen(
    onWriteTag: (String) -> Unit,
    onReadTag: () -> Unit,
    readData: OpenSpoolData? = null,
    // Bam
    rawReadText: String? = null,
    rawReadVersion: Int = 0,
    onClearRawReadData: () -> Unit = {},
    //
    dataVersion: Int = 0,
    snackbarMessage: String = "",
    showSnackbar: Boolean = false,
    onSnackbarDismiss: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onBambuDataApplied: () -> Unit = {},
    onBambuExistingSpoolFound: () -> Unit = {},
    spools: List<FilamentSpool> = emptyList(),
    selectedSpool: FilamentSpool? = null,
    isLoadingSpools: Boolean = false,
    onSpoolSelected: (FilamentSpool?) -> Unit = {},
    onRefreshSpools: () -> Unit = {},
    onRefreshSelectedSpool: (Int) -> Unit = {},
    spoolmanUrl: String = "",
    currentSpoolId: String? = null,
    availableBrands: List<String> = emptyList(),
    availableMaterials: List<String> = emptyList(),
    availableVariants: List<String> = emptyList(),
    availableLocations: List<String> = emptyList(),
    spoolMode: SpoolMode = SpoolMode.CREATE,
    onDuplicateSpool: () -> Unit = {},
    isMoonrakerReachable: Boolean = false,
    onTestMoonrakerConnection: () -> Unit = {},
    printerTool1SpoolId: Int? = null,
    printerTool2SpoolId: Int? = null,
    printerTool3SpoolId: Int? = null,
    printerTool4SpoolId: Int? = null,
    activePrinterSpoolId: Int? = null,
    printerMappingLoadVersion: Int = 0,
    isLoadingPrinterMapping: Boolean = false,
    printerMappingSaveSuccessful: Boolean? = null,
    printerMappingStatusMessage: String? = null,
    printerMappingOperation: String? = null,
    onBeginPrinterMappingDialogSession: () -> Unit = {},
    onClearPrinterMappingDialogFeedback: () -> Unit = {},
    onLoadCurrentPrinterMapping: () -> Unit = {},
    onSavePrinterMapping: (Int?, Int?, Int?, Int?, Int?) -> Unit = { _, _, _, _, _ -> },
    onSaveSuccess: () -> Unit = {},
    showLotNumber: Boolean = false,
    showCommentField: Boolean = false,
    onCreateNewSpool: () -> Unit = {},
    onCreateInSpoolman: (SpoolmanSaveRequest) -> Unit = {},
    onCreateAndWriteTag: (SpoolmanSaveRequest) -> Unit = {},
) {
    var showBambuDialog by remember { mutableStateOf(false) }
    var bambuDialogText by remember { mutableStateOf("") }
    var showBambuDiffDialog by remember { mutableStateOf(false) }
    var bambuDiffDialogText by remember { mutableStateOf("") }
    var pendingBambuApply by remember { mutableStateOf<(() -> Unit)?>(null) }
    val defaultMaterial = MaterialDatabase.getMaterial("PLA")!!
    val form = remember { SpoolFormState(defaultMaterial) }
    var showPrinterMappingDialog by remember { mutableStateOf(false) }
    var toolhead1SpoolId by remember { mutableStateOf<Int?>(null) }
    var toolhead2SpoolId by remember { mutableStateOf<Int?>(null) }
    var toolhead3SpoolId by remember { mutableStateOf<Int?>(null) }
    var toolhead4SpoolId by remember { mutableStateOf<Int?>(null) }
    var activeDialogSpoolId by remember { mutableStateOf<Int?>(null) }

    val hasPrinterMappingChanges =
        toolhead1SpoolId != printerTool1SpoolId ||
                toolhead2SpoolId != printerTool2SpoolId ||
                toolhead3SpoolId != printerTool3SpoolId ||
                toolhead4SpoolId != printerTool4SpoolId ||
                activeDialogSpoolId != activePrinterSpoolId

    val activeSpoolOutsideMapping =
        activePrinterSpoolId != null &&
                activePrinterSpoolId !in listOf(
            toolhead1SpoolId,
            toolhead2SpoolId,
            toolhead3SpoolId,
            toolhead4SpoolId
        )

    val spoolColor = form.colorHex?.let { hex ->

        val normalized = hex.trim().removePrefix("#")

        if (normalized.matches(Regex("^[A-Fa-f0-9]{6}$"))) {
            Color(android.graphics.Color.parseColor("#$normalized"))
        } else {
            Color(0xFF4A423D)
        }

    } ?: Color(0xFF4A423D)

    val printerMappingBusyLabel = when (printerMappingOperation) {
        "load" -> "Loading printer mapping..."
        "save" -> "Saving printer mapping..."
        else -> null
    }

    val inlinePrinterMappingStatusColor = when {
        isLoadingPrinterMapping -> MaterialTheme.colorScheme.primary
        printerMappingSaveSuccessful == true -> MaterialTheme.colorScheme.primary
        printerMappingSaveSuccessful == false -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val inlinePrinterMappingStatusText = when {
        isLoadingPrinterMapping -> printerMappingBusyLabel
        !printerMappingStatusMessage.isNullOrBlank() -> printerMappingStatusMessage
        else -> null
    }

    LaunchedEffect(readData, dataVersion, selectedSpool, spoolMode, availableLocations) {
        Log.d("SpoolStudioScreen", "LaunchedEffect triggered - readData: $readData, dataVersion: $dataVersion, selectedSpool: $selectedSpool, spoolMode: $spoolMode")

        val sourceSpool = selectedSpool ?: readData?.let { FilamentSpool.fromOpenSpool(it) }

        if (sourceSpool != null) {
            form.filamentType = sourceSpool.material
            form.variant = sourceSpool.variant.ifBlank { "Basic" }
            form.colorHex = sourceSpool.colorHex
            form.colorName = formatColorName(
                sourceSpool.spoolmanName?.takeIf { it.isNotBlank() }
                    ?: sourceSpool.colorHex
                    ?: form.colorName
            )
            form.brand = sourceSpool.brand

            val loadedLocation = sourceSpool.location.orEmpty().trim()
            if (loadedLocation.isBlank()) {
                form.clearLocation()
            } else if (loadedLocation in availableLocations) {
                form.location = loadedLocation
                form.customLocation = ""
            } else {
                form.location = "Other"
                form.customLocation = loadedLocation
            }

            form.minTemp = sourceSpool.minTemp?.toString() ?: form.minTemp
            form.maxTemp = sourceSpool.maxTemp?.toString() ?: form.maxTemp
            form.bedMinTemp = sourceSpool.bedMinTemp?.toString() ?: form.bedMinTemp
            form.bedMaxTemp = sourceSpool.bedMaxTemp?.toString() ?: form.bedMaxTemp
            form.lotNr = sourceSpool.lotNr ?: OpenSpoolData.generateLotNr()
            form.comment = sourceSpool.comment ?: ""
            form.remainingWeight = sourceSpool.remainingWeight
                ?.takeIf { it >= 0f }
                ?.let { weight ->
                    if (weight % 1f == 0f) weight.toInt().toString() else weight.toString()
                }
                ?: ""
            form.colorHexInput = form.colorHex ?: ""
            form.colorNameWasManuallyEdited = false
        } else if (spoolMode == SpoolMode.CREATE) {
            form.clearLocation()
        }
    }

    LaunchedEffect(form.colorHex) {
        form.colorHexInput = form.colorHex ?: ""
        if (!form.colorNameWasManuallyEdited) {
            val suggested = suggestColorName(form.colorHex)
            if (suggested.isNotBlank()) {
                form.colorName = suggested
            }
        }
    }

    LaunchedEffect(showSnackbar, snackbarMessage) {
        if (showSnackbar && snackbarMessage.isNotBlank()) {
            kotlinx.coroutines.delay(2500)
            onSnackbarDismiss()
        }
    }

    //BAM
    LaunchedEffect(rawReadVersion) {
        val raw = rawReadText ?: return@LaunchedEffect

        val isBambuDump =
            raw.contains("Bambu RFID Parsed") ||
                    raw.contains("=== Sektor") ||
                    raw.contains("Block 0 (abs")

        if (isBambuDump) {
            bambuDialogText = raw
            showBambuDialog = true
        }
    }
    //

    LaunchedEffect(
        printerTool1SpoolId,
        printerTool2SpoolId,
        printerTool3SpoolId,
        printerTool4SpoolId,
        activePrinterSpoolId,
        printerMappingLoadVersion
    ) {
        toolhead1SpoolId = printerTool1SpoolId
        toolhead2SpoolId = printerTool2SpoolId
        toolhead3SpoolId = printerTool3SpoolId
        toolhead4SpoolId = printerTool4SpoolId
        activeDialogSpoolId = activePrinterSpoolId
    }

    fun isRemainingWeightValid(): Boolean = isRemainingWeightValid(form.remainingWeight)
    fun isFormValid(): Boolean =
        isSpoolFormValid(form.variant, form.brand, form.customBrand, form.filamentType, form.customMaterial, form.remainingWeight)
    fun validationMessage(): String? =
        spoolFormValidationMessage(form.variant, form.brand, form.customBrand, form.filamentType, form.customMaterial, form.remainingWeight)
    fun buildSaveRequest(): SpoolmanSaveRequest =
        buildSpoolmanSaveRequest(
            filamentType = form.filamentType,
            customMaterial = form.customMaterial,
            variant = form.variant,
            brand = form.brand,
            customBrand = form.customBrand,
            location = form.location,
            customLocation = form.customLocation,
            colorHex = form.colorHex,
            colorName = form.colorName,
            minTemp = form.minTemp,
            maxTemp = form.maxTemp,
            bedMinTemp = form.bedMinTemp,
            bedMaxTemp = form.bedMaxTemp,
            lotNr = form.lotNr,
            comment = form.comment,
            remainingWeight = form.remainingWeight,
            spoolMode = spoolMode,
            selectedSpool = selectedSpool
        )

    fun applyBambuDialogData() {
        val material = parsedBambuValue(bambuDialogText, "Filament Type")
        val detailedType = parsedBambuValue(bambuDialogText, "Detailed Type")
        val colorRaw = parsedBambuValue(bambuDialogText, "Filament Color")
            ?.substringBefore(" / ")
            ?.removePrefix("#")
            ?.uppercase()
        val minHotend = parsedBambuInt(bambuDialogText, "Min Hotend")
        val maxHotend = parsedBambuInt(bambuDialogText, "Max Hotend")
        val bedTemp = parsedBambuInt(bambuDialogText, "Bed Temp")
        val uid = parsedBambuValue(bambuDialogText, "UID")?.trim()

        val normalizedVariant = normalizeBambuVariant(
            material = material ?: form.filamentType,
            detailedType = detailedType
        )

        val applyIntoForm = {
            onSpoolSelected(null)

            material?.let {
                form.filamentType = it
                form.customMaterial = ""
            }

            form.variant = normalizedVariant

            colorRaw?.let { hex ->
                form.colorHex = hex
                form.colorHexInput = hex
                form.isHexManuallySet = false
                form.colorNameWasManuallyEdited = false

                val suggested = suggestColorName(hex)
                form.colorName = if (suggested.isNotBlank()) suggested else "#$hex"
            }

            form.brand = "Bambu Lab"
            form.customBrand = ""

            form.clearLocation()

            minHotend?.let { form.minTemp = it.toString() }
            maxHotend?.let { form.maxTemp = it.toString() }

            if (bedTemp != null) {
                if (bedTemp <= 0) {
                    form.bedMinTemp = "0"
                    form.bedMaxTemp = "0"
                } else {
                    form.bedMinTemp = (bedTemp - 10).coerceAtLeast(0).toString()
                    form.bedMaxTemp = (bedTemp + 10).toString()
                }
            }

            uid?.let {
                form.lotNr = it.take(32)
            }

            showBambuDialog = false
            onBambuDataApplied()
        }

        val matchingSpool = findMatchingSpoolByLotNr(spools, uid)

        when {
            matchingSpool == null -> {
                applyIntoForm()
            }

            isSameBambuData(
                spool = matchingSpool,
                material = material,
                normalizedVariant = normalizedVariant,
                colorHexValue = colorRaw
            ) -> {
                onSpoolSelected(matchingSpool)
                showBambuDialog = false
                onBambuExistingSpoolFound()
            }

            else -> {
                bambuDiffDialogText = buildBambuDiffText(
                    spool = matchingSpool,
                    material = material,
                    normalizedVariant = normalizedVariant,
                    colorHexValue = colorRaw
                )
                pendingBambuApply = applyIntoForm
                showBambuDialog = false
                showBambuDiffDialog = true
            }
        }
        onClearRawReadData()
    }

    val primaryActionLabel = when (spoolMode) {
        SpoolMode.CREATE -> "Write to Spoolman"
        SpoolMode.UPDATE -> "Update in Spoolman"
        SpoolMode.DUPLICATE -> "Duplicate in Spoolman"
    }

    val combinedActionLabel = when (spoolMode) {
        SpoolMode.CREATE -> "Write to Spoolman + RFID"
        SpoolMode.UPDATE -> "Update in Spoolman + RFID"
        SpoolMode.DUPLICATE -> "Duplicate in Spoolman + RFID"
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF3E7DE)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding(),
            verticalArrangement = Arrangement.Top
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                SpoolStudioLogo(
                    color = spoolColor,
                    modifier = Modifier.align(Alignment.Center)
                )

                IconButton(
                    onClick = onSettingsClick,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 8.dp, end = 8.dp)
                        .size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Settings",
                        tint = Color(0xFF1C252B),
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(0.dp))

            @OptIn(ExperimentalMaterial3Api::class)
            PullToRefreshBox(
                isRefreshing = isLoadingSpools,
                onRefresh = onRefreshSpools,
                indicator = {},
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).navigationBarsPadding().padding(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp, vertical = 0.dp),
                        shape = RoundedCornerShape(24.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            if (spools.isNotEmpty()) {
                                SpoolmanFilamentDropdown(
                                    modifier = Modifier.fillMaxWidth(),
                                    filaments = spools,
                                    selectedFilament = selectedSpool,
                                    onFilamentSelected = onSpoolSelected,
                                    spoolmanUrl = spoolmanUrl,
                                    currentSpoolId = currentSpoolId,
                                    isLoading = isLoadingSpools,
                                    infoButton = {
                                        selectedSpool?.let { spool ->
                                            SpoolInfoCard(
                                                spool = spool,
                                                onOpenRefreshRequested = {
                                                    spool.id?.let { id ->
                                                        onRefreshSelectedSpool(id)
                                                    }
                                                }
                                            )
                                        }
                                    }
                                )
                                Spacer(modifier = Modifier.height(16.dp))

                            } else if (isLoadingSpools) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Loading Spoolman filaments...")
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                            } else {
                                val hasConfiguredSpoolman = spoolmanUrl.isNotBlank()

                                if (!hasConfiguredSpoolman) {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                                        )
                                    ) {
                                        Text(
                                            text = "💡 Connect Spoolman server in settings for easy filament selection",
                                            modifier = Modifier.padding(12.dp),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                            }

                            FilamentForm(
                                filamentType = form.filamentType,
                                customMaterial = form.customMaterial,
                                variant = form.variant,
                                colorHex = form.colorHex,
                                colorName = form.colorName,
                                brand = form.brand,
                                customBrand = form.customBrand,
                                availableMaterials = availableMaterials,
                                availableBrands = availableBrands,
                                availableVariants = availableVariants,
                                onFilamentTypeChange = { material, min, max, bedMin, bedMax ->
                                    form.filamentType = material
                                    form.minTemp = min
                                    form.maxTemp = max
                                    form.bedMinTemp = bedMin
                                    form.bedMaxTemp = bedMax
                                },
                                onCustomMaterialChange = { form.customMaterial = it },
                                onVariantChange = { form.variant = it },
                                onColorChange = { newHex ->
                                    form.colorHex = newHex
                                    form.colorHexInput = newHex ?: ""
                                    form.isHexManuallySet = false

                                    if (!form.colorNameWasManuallyEdited) {
                                        val suggested = suggestColorName(newHex)
                                        if (suggested.isNotBlank()) {
                                            form.colorName = suggested
                                        }
                                    }
                                },
                                onColorNameChange = { newName ->
                                    val formatted = formatColorName(newName.take(40))
                                    form.colorName = formatted
                                    form.colorNameWasManuallyEdited = formatted.isNotBlank()

                                    if (!form.isHexManuallySet) {
                                        val matchedHex = suggestHexFromName(formatted)
                                        if (matchedHex != null) {
                                            form.colorHex = matchedHex
                                            form.colorHexInput = matchedHex
                                        }
                                    }
                                },
                                onBrandChange = { form.brand = it },
                                onCustomBrandChange = { form.customBrand = it }
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            LocationSection(
                                location = form.location,
                                customLocation = form.customLocation,
                                availableLocations = availableLocations,
                                onLocationChange = { form.location = it },
                                onCustomLocationChange = { form.customLocation = it }
                            )

                            if (showLotNumber) {
                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedTextField(
                                    value = form.lotNr,
                                    onValueChange = { input ->
                                        if (input.length <= 32) form.lotNr = input
                                    },
                                    label = { Text("Lot Number") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(16.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = form.remainingWeight,
                                onValueChange = { input ->
                                    if (input.length <= 8 && input.all { it.isDigit() || it == '.' || it == ',' }) {
                                        form.remainingWeight = input
                                    }
                                },
                                label = { Text("Remaining filament (g)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                isError = !isRemainingWeightValid(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                shape = RoundedCornerShape(16.dp)
                            )

                            if (showCommentField) {
                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedTextField(
                                    value = form.comment,
                                    onValueChange = { input ->
                                        if (input.length <= 120) form.comment = input
                                    },
                                    label = { Text("Comment") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(16.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            key(form.filamentType) {
                                TemperatureSection(
                                    nozzleMin = form.minTemp,
                                    nozzleMax = form.maxTemp,
                                    bedMin = form.bedMinTemp,
                                    bedMax = form.bedMaxTemp,
                                    onNozzleMinChange = { form.minTemp = it },
                                    onNozzleMaxChange = { form.maxTemp = it },
                                    onBedMinChange = { form.bedMinTemp = it },
                                    onBedMaxChange = { form.bedMaxTemp = it }
                                )
                            }
                        }
                    }

                    validationMessage()?.let { message ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 5.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = message,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    ReadTagButton(onClick = { onReadTag() })

                    SaveToSpoolmanButton(
                        text = primaryActionLabel,
                        enabled = isFormValid(),
                        onClick = {
                            onCreateInSpoolman(buildSaveRequest())
                        }
                    )

                    WriteTagButton(
                        enabled = isFormValid(),
                        onClick = {
                            buildOpenSpoolTagData(
                                filamentType = form.filamentType,
                                customMaterial = form.customMaterial,
                                variant = form.variant,
                                brand = form.brand,
                                customBrand = form.customBrand,
                                colorHex = form.colorHex,
                                minTemp = form.minTemp,
                                maxTemp = form.maxTemp,
                                bedMinTemp = form.bedMinTemp,
                                bedMaxTemp = form.bedMaxTemp,
                                lotNr = form.lotNr,
                                spoolMode = spoolMode,
                                selectedSpool = selectedSpool
                            )?.let { tagData ->
                                onWriteTag(tagData.toJson())
                            }
                        }
                    )

                    SaveAndWriteTagButton(
                        text = combinedActionLabel,
                        enabled = isFormValid(),
                        onClick = {
                            onCreateAndWriteTag(buildSaveRequest())
                        }
                    )

                    Button(
                        onClick = { onCreateNewSpool() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 5.dp)
                            .height(45.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            "Create New Spool",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    PrinterMappingButton(
                        onClick = {
                            toolhead1SpoolId = printerTool1SpoolId
                            toolhead2SpoolId = printerTool2SpoolId
                            toolhead3SpoolId = printerTool3SpoolId
                            toolhead4SpoolId = printerTool4SpoolId
                            activeDialogSpoolId = activePrinterSpoolId

                            showPrinterMappingDialog = true
                            onTestMoonrakerConnection()
                        }
                    )
                }
            }
        }

        if (showPrinterMappingDialog) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f))
            ) {
                Card(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        Text(
                            text = "Snapmaker U1 Mapping",
                            style = MaterialTheme.typography.headlineSmall
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = if (isMoonrakerReachable)
                                "Printer connected"
                            else
                                "Printer not reachable",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isMoonrakerReachable)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.error
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(20.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (activeSpoolOutsideMapping) {
                                Text(
                                    text = "Active spool ID $activePrinterSpoolId is not assigned to Toolhead 1–4",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }

                        Divider(
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        MappingRowDropdown(
                            label = "Toolhead 1",
                            spools = spools,
                            selectedSpoolId = toolhead1SpoolId,
                            isActive = activeDialogSpoolId != null && activeDialogSpoolId == toolhead1SpoolId,
                            enabled = !isLoadingPrinterMapping,
                            onSpoolSelected = { selectedId ->
                                toolhead1SpoolId = selectedId
                                if (activeDialogSpoolId != null && activeDialogSpoolId !in listOf(
                                        toolhead1SpoolId,
                                        toolhead2SpoolId,
                                        toolhead3SpoolId,
                                        toolhead4SpoolId
                                    )
                                ) {
                                    activeDialogSpoolId = null
                                }
                            },
                            onActiveCheckedChange = { checked ->
                                activeDialogSpoolId = if (checked) toolhead1SpoolId else null
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        MappingRowDropdown(
                            label = "Toolhead 2",
                            spools = spools,
                            selectedSpoolId = toolhead2SpoolId,
                            isActive = activeDialogSpoolId != null && activeDialogSpoolId == toolhead2SpoolId,
                            enabled = !isLoadingPrinterMapping,
                            onSpoolSelected = { selectedId ->
                                toolhead2SpoolId = selectedId
                                if (activeDialogSpoolId != null && activeDialogSpoolId !in listOf(
                                        toolhead1SpoolId,
                                        toolhead2SpoolId,
                                        toolhead3SpoolId,
                                        toolhead4SpoolId
                                    )
                                ) {
                                    activeDialogSpoolId = null
                                }
                            },
                            onActiveCheckedChange = { checked ->
                                activeDialogSpoolId = if (checked) toolhead2SpoolId else null
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        MappingRowDropdown(
                            label = "Toolhead 3",
                            spools = spools,
                            selectedSpoolId = toolhead3SpoolId,
                            isActive = activeDialogSpoolId != null && activeDialogSpoolId == toolhead3SpoolId,
                            enabled = !isLoadingPrinterMapping,
                            onSpoolSelected = { selectedId ->
                                toolhead3SpoolId = selectedId
                                if (activeDialogSpoolId != null && activeDialogSpoolId !in listOf(
                                        toolhead1SpoolId,
                                        toolhead2SpoolId,
                                        toolhead3SpoolId,
                                        toolhead4SpoolId
                                    )
                                ) {
                                    activeDialogSpoolId = null
                                }
                            },
                            onActiveCheckedChange = { checked ->
                                activeDialogSpoolId = if (checked) toolhead3SpoolId else null
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        MappingRowDropdown(
                            label = "Toolhead 4",
                            spools = spools,
                            selectedSpoolId = toolhead4SpoolId,
                            isActive = activeDialogSpoolId != null && activeDialogSpoolId == toolhead4SpoolId,
                            enabled = !isLoadingPrinterMapping,
                            onSpoolSelected = { selectedId ->
                                toolhead4SpoolId = selectedId
                                if (activeDialogSpoolId != null && activeDialogSpoolId !in listOf(
                                        toolhead1SpoolId,
                                        toolhead2SpoolId,
                                        toolhead3SpoolId,
                                        toolhead4SpoolId
                                    )
                                ) {
                                    activeDialogSpoolId = null
                                }
                            },
                            onActiveCheckedChange = { checked ->
                                activeDialogSpoolId = if (checked) toolhead4SpoolId else null
                            }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        if (inlinePrinterMappingStatusText != null) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (isLoadingPrinterMapping) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(14.dp),
                                        strokeWidth = 2.dp
                                    )
                                }
                                Text(
                                    text = inlinePrinterMappingStatusText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = inlinePrinterMappingStatusColor
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = {
                                    onClearPrinterMappingDialogFeedback()
                                    showPrinterMappingDialog = false
                                },
                                shape = RoundedCornerShape(18.dp)
                            ) {
                                Text("Cancel")
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        onLoadCurrentPrinterMapping()
                                    },
                                    enabled = isMoonrakerReachable && !isLoadingPrinterMapping,
                                    shape = RoundedCornerShape(18.dp)
                                ) {
                                    Text("Load current")
                                }

                                Button(
                                    onClick = {
                                        onSavePrinterMapping(
                                            toolhead1SpoolId,
                                            toolhead2SpoolId,
                                            toolhead3SpoolId,
                                            toolhead4SpoolId,
                                            activeDialogSpoolId
                                        )
                                    },
                                    enabled = isMoonrakerReachable && hasPrinterMappingChanges && !isLoadingPrinterMapping,
                                    shape = RoundedCornerShape(18.dp)
                                ) {
                                    Text("Save")
                                }
                            }
                        }
                    }
                }
            }
        }

        BambuRfidDumpDialog(
            visible = showBambuDialog,
            text = bambuDialogText,
            onDismiss = { showBambuDialog = false },
            onApply = { applyBambuDialogData() }
        )

        BambuRfidDiffDialog(
            visible = showBambuDiffDialog,
            text = bambuDiffDialogText,
            onDismiss = {
                showBambuDiffDialog = false
                pendingBambuApply = null
            },
            onUseExisting = {
                showBambuDiffDialog = false
                pendingBambuApply = null
            },
            onApplyBambuData = {
                showBambuDiffDialog = false
                pendingBambuApply?.invoke()
                pendingBambuApply = null
            }
        )

        CenteredSnackbarOverlay(
            message = snackbarMessage,
            visible = showSnackbar
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SpoolStudioScreenPreview() {
    MaterialTheme { SpoolStudioScreen(onWriteTag = { }, onReadTag = { }, dataVersion = 0) }
}
