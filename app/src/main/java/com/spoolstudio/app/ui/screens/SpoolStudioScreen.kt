package com.spoolstudio.app.ui.screens

import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.foundation.layout.Box
import com.spoolstudio.app.ui.components.SpoolInfoCard
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalFocusManager
import com.spoolstudio.app.data.local.MaterialDatabase
import com.spoolstudio.app.domain.models.FilamentSpool
import com.spoolstudio.app.domain.models.OpenSpoolData
import com.spoolstudio.app.ui.SpoolMode
import com.spoolstudio.app.ui.components.FilamentForm
import com.spoolstudio.app.ui.components.SpoolStudioLogo
import com.spoolstudio.app.ui.components.SpoolmanFilamentDropdown
import com.spoolstudio.app.ui.components.TemperatureControl
import com.spoolstudio.app.utils.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import com.spoolstudio.app.ui.components.NfcDumpViewer
import com.spoolstudio.app.data.local.VariantDatabase

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
    onCreateInSpoolman: (material: String, variant: String, brand: String, location: String, colorHex: String?, colorName: String, minTemp: String, maxTemp: String, bedMinTemp: String, bedMaxTemp: String, lotNr: String, comment: String, existingSpoolId: Int?) -> Unit = { _, _, _, _, _, _, _, _, _, _, _, _, _ -> },
    onCreateAndWriteTag: (material: String, variant: String, brand: String, location: String, colorHex: String?, colorName: String, minTemp: String, maxTemp: String, bedMinTemp: String, bedMaxTemp: String, lotNr: String, comment: String, existingSpoolId: Int?) -> Unit = { _, _, _, _, _, _, _, _, _, _, _, _, _ -> },
) {
    var showBambuDialog by remember { mutableStateOf(false) }
    var bambuDialogText by remember { mutableStateOf("") }
    var showBambuDiffDialog by remember { mutableStateOf(false) }
    var bambuDiffDialogText by remember { mutableStateOf("") }
    var lastShownBambuRawReadVersion by remember { mutableStateOf(-1) }
    var pendingBambuApply by remember { mutableStateOf<(() -> Unit)?>(null) }
    var colorHex by remember { mutableStateOf<String?>(null) }
    var colorName by remember { mutableStateOf("") }
    val defaultMaterial = MaterialDatabase.getMaterial("PLA")!!
    var filamentType by remember { mutableStateOf("PLA") }
    var customMaterial by remember { mutableStateOf("") }
    var variant by remember { mutableStateOf("Basic") }
    var brand by remember { mutableStateOf("Generic") }
    var customBrand by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var customLocation by remember { mutableStateOf("") }
    var minTemp by remember { mutableStateOf(defaultMaterial.defaultMinTemp.toString()) }
    var maxTemp by remember { mutableStateOf(defaultMaterial.defaultMaxTemp.toString()) }
    var bedMinTemp by remember { mutableStateOf(defaultMaterial.defaultBedMinTemp.toString()) }
    var bedMaxTemp by remember { mutableStateOf(defaultMaterial.defaultBedMaxTemp.toString()) }
    var lotNr by remember { mutableStateOf(OpenSpoolData.generateLotNr()) }
    var comment by remember { mutableStateOf("Created by Spool Studio") }
    var colorHexInput by remember { mutableStateOf(colorHex ?: "") }
    var colorNameWasManuallyEdited by remember { mutableStateOf(false) }
    var isHexManuallySet by remember { mutableStateOf(false) }
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

    val spoolColor = colorHex?.let {
        Color(android.graphics.Color.parseColor("#$it"))
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
            filamentType = sourceSpool.material
            variant = sourceSpool.variant.ifBlank { "Basic" }
            colorHex = sourceSpool.colorHex
            colorName = formatColorName(
                sourceSpool.spoolmanName?.takeIf { it.isNotBlank() }
                    ?: sourceSpool.colorHex
                    ?: colorName
            )
            brand = sourceSpool.brand

            val loadedLocation = sourceSpool.location.orEmpty().trim()
            if (loadedLocation.isBlank()) {
                location = ""
                customLocation = ""
            } else if (loadedLocation in availableLocations) {
                location = loadedLocation
                customLocation = ""
            } else {
                location = "Other"
                customLocation = loadedLocation
            }

            minTemp = sourceSpool.minTemp?.toString() ?: minTemp
            maxTemp = sourceSpool.maxTemp?.toString() ?: maxTemp
            bedMinTemp = sourceSpool.bedMinTemp?.toString() ?: bedMinTemp
            bedMaxTemp = sourceSpool.bedMaxTemp?.toString() ?: bedMaxTemp
            lotNr = sourceSpool.lotNr ?: OpenSpoolData.generateLotNr()
            colorHexInput = colorHex ?: ""
            colorNameWasManuallyEdited = false
        } else if (spoolMode == SpoolMode.CREATE) {
            location = ""
            customLocation = ""
        }
    }

    LaunchedEffect(colorHex) {
        colorHexInput = colorHex ?: ""
        if (!colorNameWasManuallyEdited) {
            val suggested = suggestColorName(colorHex)
            if (suggested.isNotBlank()) {
                colorName = suggested
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

    fun currentMaterialName(): String =
        if (filamentType == "Other" && customMaterial.isNotBlank()) customMaterial else filamentType
    fun currentBrandName(): String =
        if (brand == "Other" && customBrand.isNotBlank()) customBrand else brand
    fun currentVariantName(): String =
        if (variant == "Other") "" else variant
    fun currentLocationName(): String =
        when {
            location == "Other" -> customLocation.trim()
            location.isBlank() -> ""
            else -> location.trim()
        }
    fun normalizeHexInput(raw: String): String? {
        val cleaned = raw.trim().removePrefix("#").uppercase()
        return if (cleaned.matches(Regex("^[0-9A-F]{6}$"))) cleaned else null
    }
    fun isVariantValid(): Boolean = variant != "Other"
    fun isBrandValid(): Boolean = brand != "Other" || customBrand.isNotBlank()
    fun isMaterialValid(): Boolean = filamentType != "Other" || customMaterial.isNotBlank()
    fun isFormValid(): Boolean =
        isVariantValid() && isBrandValid() && isMaterialValid()
    fun validationMessage(): String? = when {
        !isMaterialValid() -> "Please enter a custom filament type"
        !isVariantValid() -> "Please enter a custom variant"
        !isBrandValid() -> "Please enter a custom brand"
        else -> null
    }

    fun parsedValue(label: String): String? {
        return bambuDialogText
            .lineSequence()
            .firstOrNull { it.startsWith("$label: ") }
            ?.substringAfter(": ")
            ?.trim()
            ?.takeIf { it.isNotBlank() }
    }

    fun parsedInt(label: String): Int? {
        return parsedValue(label)
            ?.replace(" C", "")
            ?.replace(" g", "")
            ?.replace(" m", "")
            ?.replace(" mm", "")
            ?.trim()
            ?.toIntOrNull()
    }

    fun normalizeHexForCompare(value: String?): String {
        return value
            ?.trim()
            ?.removePrefix("#")
            ?.uppercase()
            .orEmpty()
    }

    fun findMatchingSpoolByLotNr(lotNrValue: String?): FilamentSpool? {
        val normalizedLotNr = lotNrValue?.trim().orEmpty()
        if (normalizedLotNr.isBlank()) return null

        return spools.firstOrNull { spool ->
            spool.lotNr?.trim().equals(normalizedLotNr, ignoreCase = true)
        }
    }

    fun isSameBambuData(
        spool: FilamentSpool,
        material: String?,
        normalizedVariant: String,
        colorHexValue: String?
    ): Boolean {
        return spool.material.equals(material.orEmpty(), ignoreCase = true) &&
                spool.variant.equals(normalizedVariant, ignoreCase = true) &&
                normalizeHexForCompare(spool.colorHex) == normalizeHexForCompare(colorHexValue)
    }

    fun buildBambuDiffText(
        spool: FilamentSpool,
        material: String?,
        normalizedVariant: String,
        colorHexValue: String?
    ): String {
        val lines = mutableListOf<String>()
        lines += "Spule mit gleicher Lot Nummer gefunden."
        lines += ""
        lines += "ID: ${spool.id ?: "-"}"
        lines += "Lot Number: ${spool.lotNr ?: "-"}"
        lines += ""

        if (!spool.material.equals(material.orEmpty(), ignoreCase = true)) {
            lines += "Material:"
            lines += "- Datenbank: ${spool.material}"
            lines += "- Bambu: ${material ?: "-"}"
            lines += ""
        }

        if (!spool.variant.equals(normalizedVariant, ignoreCase = true)) {
            lines += "Variant:"
            lines += "- Datenbank: ${spool.variant.ifBlank { "Basic" }}"
            lines += "- Bambu: ${normalizedVariant.ifBlank { "Basic" }}"
            lines += ""
        }

        if (normalizeHexForCompare(spool.colorHex) != normalizeHexForCompare(colorHexValue)) {
            lines += "Color:"
            lines += "- Datenbank: ${spool.colorHex?.let { "#$it" } ?: "-"}"
            lines += "- Bambu: ${colorHexValue?.let { "#$it" } ?: "-"}"
            lines += ""
        }

        return lines.joinToString("\n").trim()
    }

    fun normalizeBambuVariant(material: String, detailedType: String?): String {
        val raw = detailedType.orEmpty().trim()
        if (raw.isBlank()) return "Basic"

        val cleaned = raw
            .removePrefix(material)
            .removePrefix("$material ")
            .removePrefix("$material-")
            .removePrefix("$material -")
            .trim()

        val knownVariants = VariantDatabase.variants.filter { it.isNotBlank() }

        val match = knownVariants.firstOrNull { variant ->
            cleaned.equals(variant, ignoreCase = true) ||
                    cleaned.contains(variant, ignoreCase = true)
        }

        return match ?: cleaned.ifBlank { "Basic" }
    }

    fun applyBambuDialogData() {
        val material = parsedValue("Filament Type")
        val detailedType = parsedValue("Detailed Type")
        val colorRaw = parsedValue("Filament Color")
            ?.substringBefore(" / ")
            ?.removePrefix("#")
            ?.uppercase()
        val minHotend = parsedInt("Min Hotend")
        val maxHotend = parsedInt("Max Hotend")
        val bedTemp = parsedInt("Bed Temp")
        val uid = parsedValue("UID")?.trim()

        val normalizedVariant = normalizeBambuVariant(
            material = material ?: filamentType,
            detailedType = detailedType
        )

        val applyIntoForm = {
            onSpoolSelected(null)

            material?.let {
                filamentType = it
                customMaterial = ""
            }

            variant = normalizedVariant

            colorRaw?.let { hex ->
                colorHex = hex
                colorHexInput = hex
                isHexManuallySet = false
                colorNameWasManuallyEdited = false

                val suggested = suggestColorName(hex)
                colorName = if (suggested.isNotBlank()) suggested else "#$hex"
            }

            brand = "Bambu Lab"
            customBrand = ""

            location = ""
            customLocation = ""

            minHotend?.let { minTemp = it.toString() }
            maxHotend?.let { maxTemp = it.toString() }

            if (bedTemp != null) {
                if (bedTemp <= 0) {
                    bedMinTemp = "0"
                    bedMaxTemp = "0"
                } else {
                    bedMinTemp = (bedTemp - 10).coerceAtLeast(0).toString()
                    bedMaxTemp = (bedTemp + 10).toString()
                }
            }

            uid?.let {
                lotNr = it.take(32)
            }

            showBambuDialog = false
            onBambuDataApplied()
        }

        val matchingSpool = findMatchingSpoolByLotNr(uid)

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
                                filamentType = filamentType,
                                customMaterial = customMaterial,
                                variant = variant,
                                colorHex = colorHex,
                                colorName = colorName,
                                brand = brand,
                                customBrand = customBrand,
                                availableMaterials = availableMaterials,
                                availableBrands = availableBrands,
                                availableVariants = availableVariants,
                                onFilamentTypeChange = { material, min, max, bedMin, bedMax ->
                                    filamentType = material
                                    minTemp = min
                                    maxTemp = max
                                    bedMinTemp = bedMin
                                    bedMaxTemp = bedMax
                                },
                                onCustomMaterialChange = { customMaterial = it },
                                onVariantChange = { variant = it },
                                onColorChange = { newHex ->
                                    colorHex = newHex
                                    colorHexInput = newHex ?: ""
                                    isHexManuallySet = false

                                    if (!colorNameWasManuallyEdited) {
                                        val suggested = suggestColorName(newHex)
                                        if (suggested.isNotBlank()) {
                                            colorName = suggested
                                        }
                                    }
                                },
                                onColorNameChange = { newName ->
                                    val formatted = formatColorName(newName.take(40))
                                    colorName = formatted
                                    colorNameWasManuallyEdited = formatted.isNotBlank()

                                    if (!isHexManuallySet) {
                                        val matchedHex = suggestHexFromName(formatted)
                                        if (matchedHex != null) {
                                            colorHex = matchedHex
                                            colorHexInput = matchedHex
                                        }
                                    }
                                },
                                onBrandChange = { brand = it },
                                onCustomBrandChange = { customBrand = it }
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            var locationExpanded by remember { mutableStateOf(false) }

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
                                        .menuAnchor()
                                        .fillMaxWidth(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(16.dp)
                                )

                                DropdownMenu(
                                    expanded = locationExpanded,
                                    onDismissRequest = { locationExpanded = false }
                                ) {
                                    availableLocations.forEach { item ->
                                        DropdownMenuItem(
                                            text = { Text(item) },
                                            onClick = {
                                                location = item
                                                customLocation = ""
                                                locationExpanded = false
                                            }
                                        )
                                    }
                                    HorizontalDivider()

                                    DropdownMenuItem(
                                        text = { Text("Other") },
                                        onClick = {
                                            location = "Other"
                                            locationExpanded = false
                                        }
                                    )
                                }
                            }

                            if (location == "Other") {
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = customLocation,
                                    onValueChange = { input -> if (input.length <= 60) customLocation = input },
                                    label = { Text("Custom Location") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(16.dp)
                                )
                            }

                            if (showLotNumber) {
                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedTextField(
                                    value = lotNr,
                                    onValueChange = { input ->
                                        if (input.length <= 32) lotNr = input
                                    },
                                    label = { Text("Lot Number") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(16.dp)
                                )
                            }

                            if (showCommentField) {
                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedTextField(
                                    value = comment,
                                    onValueChange = { input ->
                                        if (input.length <= 120) comment = input
                                    },
                                    label = { Text("Comment") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(16.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            key(filamentType) {
                                TemperatureSection(
                                    nozzleMin = minTemp,
                                    nozzleMax = maxTemp,
                                    bedMin = bedMinTemp,
                                    bedMax = bedMaxTemp,
                                    onNozzleMinChange = { minTemp = it },
                                    onNozzleMaxChange = { maxTemp = it },
                                    onBedMinChange = { bedMinTemp = it },
                                    onBedMaxChange = { bedMaxTemp = it }
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
                            onCreateInSpoolman(
                                currentMaterialName(),
                                currentVariantName(),
                                currentBrandName(),
                                currentLocationName(),
                                colorHex,
                                colorName,
                                minTemp,
                                maxTemp,
                                bedMinTemp,
                                bedMaxTemp,
                                lotNr,
                                comment,
                                if (spoolMode == SpoolMode.UPDATE) selectedSpool?.id else null
                            )
                        }
                    )

                    WriteTagButton(
                        enabled = isFormValid(),
                        onClick = {
                            val tagData = OpenSpoolData(
                                type = currentMaterialName(),
                                colorHex = colorHex,
                                brand = currentBrandName(),
                                minTemp = minTemp,
                                maxTemp = maxTemp,
                                bedMinTemp = bedMinTemp.ifBlank { null },
                                bedMaxTemp = bedMaxTemp.ifBlank { null },
                                subtype = currentVariantName().ifBlank { "Basic" },
                                spoolId = if (spoolMode == SpoolMode.UPDATE) selectedSpool?.id?.toString() else null,
                                lotNr = lotNr
                            )
                            onWriteTag(tagData.toJson())
                        }
                    )

                    SaveAndWriteTagButton(
                        text = combinedActionLabel,
                        enabled = isFormValid(),
                        onClick = {
                            onCreateAndWriteTag(
                                currentMaterialName(),
                                currentVariantName(),
                                currentBrandName(),
                                currentLocationName(),
                                colorHex,
                                colorName,
                                minTemp,
                                maxTemp,
                                bedMinTemp,
                                bedMaxTemp,
                                lotNr,
                                comment,
                                if (spoolMode == SpoolMode.UPDATE) selectedSpool?.id else null
                            )
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

        if (showBambuDialog) {
            AlertDialog(
                onDismissRequest = { showBambuDialog = false },
                confirmButton = {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { showBambuDialog = false }) {
                            Text("Schließen")
                        }
                        TextButton(onClick = { applyBambuDialogData() }) {
                            Text("Übernehmen")
                        }
                    }
                },
                title = {
                    Text("Bambu RFID Dump")
                },
                text = {
                    NfcDumpViewer(
                        text = bambuDialogText,
                        modifier = Modifier.height(320.dp)
                    )
                }
            )
        }

        if (showBambuDiffDialog) {
            AlertDialog(
                onDismissRequest = {
                    showBambuDiffDialog = false
                    pendingBambuApply = null
                },
                title = {
                    Text("Abweichungen gefunden")
                },
                text = {
                    NfcDumpViewer(
                        text = bambuDiffDialogText,
                        modifier = Modifier.height(260.dp)
                    )
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showBambuDiffDialog = false
                            pendingBambuApply = null
                        }
                    ) {
                        Text("Vorhandene Spule verwenden")
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showBambuDiffDialog = false
                            pendingBambuApply?.invoke()
                            pendingBambuApply = null
                        }
                    ) {
                        Text("Bambu-Daten übernehmen")
                    }
                }
            )
        }

        if (showSnackbar && snackbarMessage.isNotBlank()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Text(
                        text = snackbarMessage,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun WriteTagButton(
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 5.dp)
            .height(45.dp),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
            disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)
        )
    ) {
        Text("Write to RFID", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onPrimary)
    }
}

@Composable
private fun ReadTagButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 5.dp)
            .height(45.dp),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Text("Read RFID Tag", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun PrinterMappingButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 5.dp)
            .height(45.dp),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Text("Printer Mapping", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun SaveToSpoolmanButton(text: String, enabled: Boolean = true, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        border = null,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 5.dp)
            .height(45.dp),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
            disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)
        )
    ) {
        Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun SaveAndWriteTagButton(text: String, enabled: Boolean = true, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 5.dp)
            .height(45.dp),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
            disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)
        )
    ) {
        Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onPrimary)
    }
}

@Preview(showBackground = true)
@Composable
fun SpoolStudioScreenPreview() {
    MaterialTheme { SpoolStudioScreen(onWriteTag = { }, onReadTag = { }, dataVersion = 0) }
}

@Composable
fun TemperatureSection(
    nozzleMin: String,
    nozzleMax: String,
    bedMin: String,
    bedMax: String,
    onNozzleMinChange: (String) -> Unit,
    onNozzleMaxChange: (String) -> Unit,
    onBedMinChange: (String) -> Unit,
    onBedMaxChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Temperature", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Nozzle", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.width(60.dp))
            TemperatureControl(value = nozzleMin, onValueChange = onNozzleMinChange, modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(16.dp))
            TemperatureControl(value = nozzleMax, onValueChange = onNozzleMaxChange, modifier = Modifier.weight(1f))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Bed", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.width(60.dp))
            TemperatureControl(value = bedMin, onValueChange = onBedMinChange, modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(16.dp))
            TemperatureControl(value = bedMax, onValueChange = onBedMaxChange, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun MappingRowPlaceholder(label: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = "Keine Spule gewählt",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MappingRowDropdown(
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
        "ID ${spool.id ?: "-"} · ${spool.brand} · ${spool.spoolmanName ?: spool.displayName}"
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
                            text = "— Empty —",
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
                        "ID ${spool.id ?: "-"} · ${spool.brand} · ${spool.spoolmanName ?: spool.displayName}"

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
