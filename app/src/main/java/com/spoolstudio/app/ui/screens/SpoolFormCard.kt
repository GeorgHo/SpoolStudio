package com.spoolstudio.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.spoolstudio.app.data.local.BrandDatabase
import com.spoolstudio.app.data.local.MaterialDatabase
import com.spoolstudio.app.data.local.VariantDatabase
import com.spoolstudio.app.domain.models.FilamentSpool
import com.spoolstudio.app.ui.SpoolMode
import com.spoolstudio.app.ui.components.ColorSelector
import com.spoolstudio.app.ui.components.SearchableDropdownDialog
import com.spoolstudio.app.ui.components.SpoolStudioLogo
import com.spoolstudio.app.ui.components.SpoolmanFilamentDropdown
import com.spoolstudio.app.ui.theme.SpoolStudioColors
import com.spoolstudio.app.ui.theme.SpoolStudioDimens
import com.spoolstudio.app.ui.theme.SpoolStudioShape
import com.spoolstudio.app.ui.remainingWeightWarningThreshold
import com.spoolstudio.app.ui.components.displayNameForSelectedColor
import com.spoolstudio.app.ui.components.sanitizeHexColorInput
import com.spoolstudio.app.utils.formatColorName
import kotlinx.coroutines.delay
import com.spoolstudio.app.utils.suggestColorName
import com.spoolstudio.app.utils.suggestHexFromName

@Composable
fun SpoolFormCard(
    form: SpoolFormState,
    spools: List<FilamentSpool>,
    selectedSpool: FilamentSpool?,
    spoolMode: SpoolMode,
    spoolmanUrl: String,
    currentSpoolId: String?,
    isLoadingSpools: Boolean,
    availableMaterials: List<String>,
    availableBrands: List<String>,
    availableVariants: List<String>,
    availableLocations: List<String>,
    showLotNumber: Boolean,
    showCommentField: Boolean,
    showEmptySpoolWeight: Boolean,
    isRemainingWeightValid: Boolean,
    onSpoolSelected: (FilamentSpool?) -> Unit,
    onClearAllSpoolFields: () -> Unit,
    onRefreshSelectedSpool: (Int) -> Unit,
    onRefreshSpoolmanCatalogIfStale: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = SpoolStudioDimens.ScreenHorizontalPadding, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SpoolHeroPanel(
            form = form,
            selectedSpool = selectedSpool,
            spoolMode = spoolMode,
            spools = spools,
            spoolmanUrl = spoolmanUrl,
            currentSpoolId = currentSpoolId,
            isLoadingSpools = isLoadingSpools,
            onSpoolSelected = onSpoolSelected,
            onClearAllSpoolFields = onClearAllSpoolFields,
            onRefreshSelectedSpool = onRefreshSelectedSpool,
            onRefreshSpoolmanCatalogIfStale = onRefreshSpoolmanCatalogIfStale
        )

        SpoolDataCard(
            form = form,
            spools = spools,
            selectedSpool = selectedSpool,
            availableMaterials = availableMaterials,
            availableBrands = availableBrands,
            availableVariants = availableVariants,
            availableLocations = availableLocations,
            showLotNumber = showLotNumber,
            showCommentField = showCommentField,
            showEmptySpoolWeight = showEmptySpoolWeight,
            isRemainingWeightValid = isRemainingWeightValid,
            onRefreshSpoolmanCatalogIfStale = onRefreshSpoolmanCatalogIfStale
        )

        TemperatureInputCard(form = form)
    }
}

@Composable
private fun SpoolDataCard(
    form: SpoolFormState,
    spools: List<FilamentSpool>,
    selectedSpool: FilamentSpool?,
    availableMaterials: List<String>,
    availableBrands: List<String>,
    availableVariants: List<String>,
    availableLocations: List<String>,
    showLotNumber: Boolean,
    showCommentField: Boolean,
    showEmptySpoolWeight: Boolean,
    isRemainingWeightValid: Boolean,
    onRefreshSpoolmanCatalogIfStale: () -> Unit
) {
    var showColorDialog by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val customLocationFocusRequester = remember { FocusRequester() }
    var focusCustomLocation by remember { mutableStateOf(false) }
    val materialOptions = remember(availableMaterials) {
        prioritizedOptions(
            preferred = listOf("PLA"),
            values = MaterialDatabase.materials.map { it.name } + availableMaterials,
            trailing = listOf("Other")
        )
    }
    val variantOptions = remember(availableVariants) {
        prioritizedOptions(
            preferred = listOf("Basic"),
            values = VariantDatabase.variants.filter { it.isNotBlank() } + availableVariants,
            trailing = emptyList()
        )
    }
    val brandOptions = remember(availableBrands) {
        prioritizedOptions(
            preferred = listOf("Generic"),
            values = BrandDatabase.brands + availableBrands,
            trailing = emptyList()
        )
    }
    val locationOptions = remember(availableLocations) {
        prioritizedOptions(
            preferred = listOf("New Location"),
            values = listOf("No Location") + availableLocations,
            trailing = emptyList()
        )
    }
    val emptySpoolWeightSuggestions = remember(spools) {
        buildEmptySpoolWeightSuggestions(spools)
    }

    fun applyColorNameInput(input: String, finalize: Boolean = false) {
        val rawName = input.take(40)
        val formattedName = formatColorName(rawName)
        val displayName = if (finalize) formattedName.ifBlank { "Unknown" } else rawName
        val lookupName = formattedName.ifBlank { rawName }

        form.colorName = displayName
        form.colorNameWasManuallyEdited = displayName.isNotBlank() && displayName != "Unknown"

        suggestHexFromName(lookupName)?.let { matchedHex ->
            form.colorHex = matchedHex
            form.colorHexInput = matchedHex
            form.isHexManuallySet = false
        }
    }

    val remainingWarning = remainingWeightWarningThreshold(form.remainingWeight)
    val remainingColor = when {
        !isRemainingWeightValid -> MaterialTheme.colorScheme.error
        remainingWarning == 150 -> MaterialTheme.colorScheme.tertiary
        remainingWarning == 100 -> MaterialTheme.colorScheme.error.copy(alpha = 0.82f)
        remainingWarning == 50 -> MaterialTheme.colorScheme.error
            else -> SpoolStudioColors.Ink
    }
    val remainingIsDefaultSuggestion = selectedSpool == null &&
        form.remainingWeight.trim().replace(',', '.').let { remaining ->
            remaining.isBlank() || remaining == "1000" || remaining == "1000.0" || remaining == "1000.00"
        }
    val remainingFieldColor = when {
        !isRemainingWeightValid -> remainingColor
        remainingIsDefaultSuggestion -> SpoolStudioColors.InkMuted
        else -> remainingColor
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = SpoolStudioShape.Small,
        colors = CardDefaults.cardColors(containerColor = SpoolStudioColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            CompactDropdownRow(
                label = "Filament Type",
                value = form.filamentType.ifBlank { "PLA" },
                options = materialOptions,
                onSelected = { material ->
                    if (material == "Other") {
                        form.filamentType = "Other"
                        return@CompactDropdownRow
                    }

                    val materialInfo = MaterialDatabase.getMaterial(material)
                    form.filamentType = material
                    form.customMaterial = ""
                    materialInfo?.let {
                        form.minTemp = it.defaultMinTemp.toString()
                        form.maxTemp = it.defaultMaxTemp.toString()
                        form.bedMinTemp = it.defaultBedMinTemp.toString()
                        form.bedMaxTemp = it.defaultBedMaxTemp.toString()
                    }
                },
                showTopDivider = false
            )

            if (form.filamentType == "Other") {
                CompactTextRow(
                    label = "Custom material",
                    value = form.customMaterial,
                    onValueChange = { form.customMaterial = it.take(40) }
                )
            }

            CompactDropdownRow(
                label = "Variant",
                value = form.variant.ifBlank { "Basic" },
                options = variantOptions,
                onSelected = { form.variant = it.ifBlank { "Basic" } }
            )

            CompactBrandRow(
                label = "Brand",
                value = form.brand.ifBlank { "Generic" },
                suggestions = brandOptions,
                onOpen = onRefreshSpoolmanCatalogIfStale,
                onValueChange = { brand ->
                    form.brand = brand.trim().ifBlank { "Generic" }.take(60)
                    form.customBrand = ""
                }
            )

            CompactDataRow(
                label = "Color",
                onClick = {
                    focusManager.clearFocus()
                    showColorDialog = true
                }
            ) {
                form.colorHex?.let {
                    Box(
                        modifier = Modifier
                            .padding(end = 6.dp)
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(parseSpoolColor(it))
                            .border(1.dp, SpoolStudioColors.Outline, CircleShape)
                    )
                }
                Text(
                    text = displayNameForSelectedColor(form.colorHex, form.colorName),
                    style = compactValueStyle(),
                    color = SpoolStudioColors.Ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = SpoolStudioColors.GoldDark,
                    modifier = Modifier
                        .padding(start = 6.dp)
                        .size(18.dp)
                )
            }

            CompactTextRow(
                label = "Color Name",
                value = form.colorName,
                placeholder = "Unknown",
                showEditIcon = true,
                onValueChange = { newName ->
                    applyColorNameInput(newName)
                },
                onFocusLost = {
                    applyColorNameInput(form.colorName, finalize = true)
                }
            )

            CompactTextRow(
                label = "HEX",
                value = form.colorHex ?: "",
                prefix = "#",
                showPrefixWhenBlank = false,
                showEditIcon = true,
                onValueChange = { input ->
                    val sanitized = sanitizeHexColorInput(input)
                    form.colorHex = sanitized
                    form.colorHexInput = sanitized ?: ""
                    form.isHexManuallySet = sanitized != null
                }
            )

            CompactWeightRow(
                label = "Remaining filament",
                value = form.remainingWeight,
                placeholder = "1000",
                valueColor = remainingFieldColor,
                showEditIcon = true,
                onValueChange = { input ->
                    form.remainingWeight = sanitizeWeightInput(input)
                }
            )

            if (showEmptySpoolWeight) {
                CompactEmptySpoolWeightRow(
                    value = form.emptySpoolWeight,
                    suggestions = emptySpoolWeightSuggestions,
                    onOpen = onRefreshSpoolmanCatalogIfStale,
                    onValueChange = { form.emptySpoolWeight = sanitizeWeightInput(it) }
                )
            }

            CompactDropdownRow(
                label = "Location",
                value = when {
                    form.location == "Other" -> form.customLocation.ifBlank { "New Location" }
                    form.location.isBlank() -> "No Location"
                    else -> form.location
                },
                options = locationOptions,
                onOpen = onRefreshSpoolmanCatalogIfStale,
                onSelected = {
                    focusManager.clearFocus()
                    if (it == "No Location") {
                        form.location = ""
                        form.customLocation = ""
                    } else if (it == "New Location") {
                        form.location = "Other"
                        form.customLocation = ""
                        focusCustomLocation = true
                    } else {
                        form.location = it
                        form.customLocation = ""
                    }
                }
            )

            if (form.location == "Other") {
                LaunchedEffect(focusCustomLocation) {
                    if (focusCustomLocation) {
                        customLocationFocusRequester.requestFocus()
                        focusCustomLocation = false
                    }
                }
                CompactTextRow(
                    label = "Custom location",
                    value = form.customLocation,
                    focusRequester = customLocationFocusRequester,
                    onValueChange = { form.customLocation = it.take(60) }
                )
            }

            if (showLotNumber) {
                CompactTextRow(
                    label = "Lot",
                    value = form.lotNr,
                    placeholder = "",
                    showEditIcon = true,
                    onValueChange = { form.lotNr = it.take(64) }
                )
            }

            if (showCommentField) {
                CompactTextRow(
                    label = "Comment",
                    value = form.comment,
                    placeholder = "-",
                    valueFontWeight = FontWeight.SemiBold,
                    showEditIcon = true,
                    onValueChange = { form.comment = it.take(120) },
                    showBottomDivider = false
                )
            }
        }
    }

    if (showColorDialog) {
        Dialog(
            onDismissRequest = { showColorDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp),
                shape = SpoolStudioShape.Dialog,
                colors = CardDefaults.cardColors(containerColor = SpoolStudioColors.Surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 14.dp),
                border = BorderStroke(1.dp, SpoolStudioColors.OutlineSoft)
            ) {
                Column(
                    modifier = Modifier.padding(start = 14.dp, top = 10.dp, end = 14.dp, bottom = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Color",
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp, lineHeight = 22.sp),
                            fontWeight = FontWeight.SemiBold,
                            color = SpoolStudioColors.Ink
                        )
                        IconButton(onClick = { showColorDialog = false }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = SpoolStudioColors.Ink
                            )
                        }
                    }
                    ColorSelector(
                        selectedColor = form.colorHex,
                        colorName = form.colorName,
                        onColorSelected = { newHex ->
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
                            applyColorNameInput(newName)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CompactDropdownRow(
    label: String,
    value: String,
    options: List<String>,
    onSelected: (String) -> Unit,
    onOpen: () -> Unit = {},
    showTopDivider: Boolean = false
) {
    var expanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val filteredOptions = remember(options, searchQuery) {
        if (searchQuery.isBlank()) {
            options
        } else {
            options.filter { it.contains(searchQuery, ignoreCase = true) }
        }
    }
    LaunchedEffect(expanded) {
        if (expanded) {
            searchQuery = ""
        }
    }
    Box(modifier = Modifier.fillMaxWidth()) {
        CompactDataRow(
            label = label,
            onClick = {
                focusManager.clearFocus()
                if (!expanded) onOpen()
                expanded = !expanded
            },
            showTopDivider = showTopDivider
        ) {
            Text(
                text = value,
                style = compactValueStyle(),
                color = SpoolStudioColors.Ink,
                textAlign = TextAlign.Start,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = SpoolStudioColors.InkMuted,
                modifier = Modifier
                    .padding(start = 6.dp)
                    .size(18.dp)
            )
        }

        if (expanded) {
            SearchableDropdownDialog(
                title = label,
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                items = filteredOptions,
                itemLabel = { it },
                onItemSelected = { option ->
                    onSelected(option)
                    focusManager.clearFocus()
                    expanded = false
                },
                onDismiss = {
                    focusManager.clearFocus()
                    expanded = false
                },
                showDefaultDivider = true
            )
        }
    }
}

@Composable
private fun CompactBrandRow(
    label: String,
    value: String,
    suggestions: List<String>,
    onOpen: () -> Unit = {},
    onValueChange: (String) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    CompactDataRow(
        label = label,
        onClick = {
            focusManager.clearFocus()
            onOpen()
            showDialog = true
        }
    ) {
        Text(
            text = value.ifBlank { "Generic" },
            style = compactValueStyle(),
            color = SpoolStudioColors.Ink,
            textAlign = TextAlign.Start,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Default.ArrowDropDown,
            contentDescription = null,
            tint = SpoolStudioColors.InkMuted,
            modifier = Modifier
                .padding(start = 6.dp)
                .size(18.dp)
        )
    }

    if (showDialog) {
        BrandDialog(
            value = value,
            suggestions = suggestions,
            onValueChange = onValueChange,
            onDismiss = { showDialog = false }
        )
    }
}

@Composable
private fun BrandDialog(
    value: String,
    suggestions: List<String>,
    onValueChange: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val uniqueSuggestions = remember(suggestions) {
        suggestions
            .map { it.trim() }
            .filter { it.isNotBlank() && !it.equals("Other", ignoreCase = true) }
            .distinctBy { it.lowercase(java.util.Locale.ROOT) }
    }
    var draft by remember(value) {
        mutableStateOf(TextFieldValue(value.ifBlank { "Generic" }, selection = TextRange(0, value.ifBlank { "Generic" }.length)))
    }
    val focusRequester = remember { FocusRequester() }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp),
            shape = SpoolStudioShape.Dialog,
            colors = CardDefaults.cardColors(containerColor = SpoolStudioColors.Surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 14.dp),
            border = BorderStroke(1.dp, SpoolStudioColors.OutlineSoft)
        ) {
            Column(
                modifier = Modifier.padding(start = 14.dp, top = 10.dp, end = 14.dp, bottom = 14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Brand",
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp, lineHeight = 22.sp),
                        fontWeight = FontWeight.SemiBold,
                        color = SpoolStudioColors.Ink
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = SpoolStudioColors.Ink
                        )
                    }
                }

                OutlinedTextField(
                    value = draft,
                    onValueChange = { draft = it.copy(text = it.text.take(60)) },
                    singleLine = true,
                    label = { Text("Brand") },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                )

                if (uniqueSuggestions.isNotEmpty()) {
                    Text(
                        text = "Used brands",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = SpoolStudioColors.InkMuted,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 190.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        uniqueSuggestions.forEach { suggestion ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onValueChange(suggestion)
                                        onDismiss()
                                    }
                                    .padding(vertical = 9.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = suggestion,
                                    style = compactValueStyle(),
                                    color = SpoolStudioColors.Ink,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = SpoolStudioColors.AccentCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            CompactDivider()
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Generic",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = SpoolStudioColors.GoldDark,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clip(SpoolStudioShape.Button)
                            .clickable {
                                onValueChange("Generic")
                                onDismiss()
                            }
                            .padding(vertical = 12.dp)
                    )
                    Text(
                        text = "Done",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clip(SpoolStudioShape.Button)
                            .background(SpoolStudioColors.Gold)
                            .clickable {
                                onValueChange(draft.text.ifBlank { "Generic" })
                                onDismiss()
                            }
                            .padding(vertical = 12.dp)
                    )
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

@Composable
private fun CompactWeightRow(
    label: String,
    value: String,
    placeholder: String,
    valueColor: Color,
    showEditIcon: Boolean = false,
    onValueChange: (String) -> Unit
) {
    val widthText = value.ifBlank { placeholder }
    val valueStyle = compactValueStyle(
        color = valueColor,
        textAlign = TextAlign.Start,
        fontWeight = FontWeight.SemiBold
    )
    val textMeasurer = rememberTextMeasurer()
    val measuredWidth = with(LocalDensity.current) {
        textMeasurer
            .measure(AnnotatedString(widthText.ifBlank { "000" }), style = valueStyle)
            .size
            .width
            .toDp()
    }
    val inputWidth = maxOf(26.dp, measuredWidth + 3.dp)
    val focusRequester = remember { FocusRequester() }
    var fieldValue by remember { mutableStateOf(TextFieldValue(value)) }
    var isFocused by remember { mutableStateOf(false) }
    var selectAllRequest by remember { mutableStateOf(0) }
    var forceSelectAll by remember { mutableStateOf(false) }
    fun requestSelectAll() {
        forceSelectAll = true
        selectAllRequest += 1
    }
    LaunchedEffect(value) {
        if (value != fieldValue.text) {
            fieldValue = TextFieldValue(value, selection = TextRange(value.length))
        }
    }
    LaunchedEffect(selectAllRequest) {
        if (selectAllRequest > 0 && fieldValue.text.isNotEmpty()) {
            delay(90)
            fieldValue = fieldValue.copy(selection = TextRange(0, fieldValue.text.length))
            delay(130)
            fieldValue = fieldValue.copy(selection = TextRange(0, fieldValue.text.length))
        }
    }
    CompactDataRow(
        label = label,
        onClick = {
            focusRequester.requestFocus()
            requestSelectAll()
        }
    ) {
        BasicTextField(
            value = fieldValue,
            onValueChange = {
                val onlySelectionChanged = it.text == fieldValue.text
                fieldValue = if (forceSelectAll && onlySelectionChanged && it.text.isNotEmpty()) {
                    forceSelectAll = false
                    it.copy(selection = TextRange(0, it.text.length))
                } else {
                    forceSelectAll = false
                    it
                }
                onValueChange(it.text)
            },
            singleLine = true,
            textStyle = valueStyle,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier
                .width(inputWidth)
                .focusRequester(focusRequester)
                .pointerInput(fieldValue.text) {
                    detectTapGestures(
                        onTap = {
                            requestSelectAll()
                        }
                    )
                }
                .onFocusChanged {
                    isFocused = it.isFocused
                    if (it.isFocused && fieldValue.text.isNotEmpty()) {
                        requestSelectAll()
                    }
                },
            decorationBox = { innerTextField ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (value.isBlank()) {
                        Text(
                            text = placeholder,
                            style = compactValueStyle(
                                color = SpoolStudioColors.InkMuted,
                                textAlign = TextAlign.Start,
                                fontWeight = FontWeight.SemiBold
                            ),
                            maxLines = 1
                        )
                    }
                    innerTextField()
                }
            }
        )
        Text(
            text = "g",
            style = compactValueStyle(
                color = valueColor,
                fontWeight = FontWeight.SemiBold
            ),
            modifier = Modifier.padding(start = 1.dp)
        )
        if (showEditIcon) {
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = null,
                tint = SpoolStudioColors.GoldDark,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun CompactEmptySpoolWeightRow(
    value: String,
    suggestions: List<EmptySpoolWeightSuggestion>,
    onOpen: () -> Unit = {},
    onValueChange: (String) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    CompactDataRow(
        label = "Empty spool weight",
        onClick = {
            onOpen()
            showDialog = true
        }
    ) {
        Text(
            text = formatWeightWithUnit(value).ifBlank { "-" },
            style = compactValueStyle(),
            color = SpoolStudioColors.Ink,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Default.Edit,
            contentDescription = null,
            tint = SpoolStudioColors.GoldDark,
            modifier = Modifier.size(18.dp)
        )
    }

    if (showDialog) {
        EmptySpoolWeightDialog(
            value = value,
            suggestions = suggestions,
            onValueChange = onValueChange,
            onDismiss = { showDialog = false }
        )
    }
}

@Composable
private fun EmptySpoolWeightDialog(
    value: String,
    suggestions: List<EmptySpoolWeightSuggestion>,
    onValueChange: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var draft by remember(value) { mutableStateOf(TextFieldValue(value, selection = TextRange(0, value.length))) }
    val focusRequester = remember { FocusRequester() }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            shape = SpoolStudioShape.Dialog,
            colors = CardDefaults.cardColors(containerColor = SpoolStudioColors.Surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 14.dp),
            border = BorderStroke(1.dp, SpoolStudioColors.OutlineSoft)
        ) {
            Column(
                modifier = Modifier.padding(start = 14.dp, top = 10.dp, end = 14.dp, bottom = 14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Empty spool weight",
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp, lineHeight = 22.sp),
                        fontWeight = FontWeight.SemiBold,
                        color = SpoolStudioColors.Ink
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = SpoolStudioColors.Ink
                        )
                    }
                }

                OutlinedTextField(
                    value = draft,
                    onValueChange = { draft = it.copy(text = sanitizeWeightInput(it.text)) },
                    singleLine = true,
                    label = { Text("Weight (g)") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                )

                if (suggestions.isNotEmpty()) {
                    Text(
                        text = "Used values",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = SpoolStudioColors.InkMuted,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 190.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        suggestions.forEach { suggestion ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onValueChange(suggestion.weightText)
                                        onDismiss()
                                    }
                                    .padding(vertical = 9.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = suggestion.label,
                                    style = compactValueStyle(),
                                    color = SpoolStudioColors.Ink,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = SpoolStudioColors.AccentCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            CompactDivider()
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Clear",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = SpoolStudioColors.GoldDark,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clip(SpoolStudioShape.Button)
                            .clickable {
                                onValueChange("")
                                onDismiss()
                            }
                            .padding(vertical = 12.dp)
                    )
                    Text(
                        text = "Done",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clip(SpoolStudioShape.Button)
                            .background(SpoolStudioColors.Gold)
                            .clickable {
                                onValueChange(draft.text)
                                onDismiss()
                            }
                            .padding(vertical = 12.dp)
                    )
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

@Composable
private fun CompactTextRow(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    prefix: String = "",
    suffix: String = "",
    showPrefixWhenBlank: Boolean = true,
    showEditIcon: Boolean = false,
    valueColor: Color = SpoolStudioColors.Ink,
    valueFontWeight: FontWeight = FontWeight.SemiBold,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    focusRequester: FocusRequester? = null,
    onFocusLost: () -> Unit = {},
    showBottomDivider: Boolean = true
) {
    var fieldValue by remember { mutableStateOf(TextFieldValue(value)) }
    var isFocused by remember { mutableStateOf(false) }
    var selectAllRequest by remember { mutableStateOf(0) }
    LaunchedEffect(value) {
        if (value != fieldValue.text) {
            fieldValue = TextFieldValue(value, selection = TextRange(value.length))
        }
    }
    LaunchedEffect(selectAllRequest) {
        if (selectAllRequest > 0 && fieldValue.text.isNotEmpty()) {
            delay(90)
            fieldValue = fieldValue.copy(selection = TextRange(0, fieldValue.text.length))
            delay(130)
            fieldValue = fieldValue.copy(selection = TextRange(0, fieldValue.text.length))
        }
    }
    CompactDataRow(
        label = label,
        showBottomDivider = showBottomDivider
    ) {
        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            if (prefix.isNotBlank() && (showPrefixWhenBlank || value.isNotBlank())) {
                Text(
                    text = prefix,
                    style = compactValueStyle(fontWeight = valueFontWeight),
                    color = valueColor
                )
            }
            BasicTextField(
                value = fieldValue,
                onValueChange = {
                    fieldValue = it
                    onValueChange(it.text)
                },
                singleLine = true,
                textStyle = compactValueStyle(
                    color = valueColor,
                    textAlign = TextAlign.Start,
                    fontWeight = valueFontWeight
                ),
                keyboardOptions = keyboardOptions,
                modifier = Modifier
                    .weight(1f)
                    .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier)
                    .pointerInput(fieldValue.text) {
                        detectTapGestures(
                            onTap = {
                                selectAllRequest += 1
                            }
                        )
                    }
                    .onFocusChanged {
                        val wasFocused = isFocused
                        isFocused = it.isFocused
                        if (it.isFocused && fieldValue.text.isNotEmpty()) {
                            selectAllRequest += 1
                        }
                        if (wasFocused && !it.isFocused) {
                            onFocusLost()
                        }
                    },
                decorationBox = { innerTextField ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (value.isBlank() && placeholder.isNotBlank()) {
                            Text(
                                text = placeholder,
                                style = compactValueStyle(
                                    color = SpoolStudioColors.InkMuted,
                                    textAlign = TextAlign.Start,
                                    fontWeight = valueFontWeight
                                ),
                                maxLines = 1
                            )
                        }
                        innerTextField()
                    }
                }
            )
            if (suffix.isNotBlank()) {
                Text(
                    text = suffix,
                    style = compactValueStyle(fontWeight = valueFontWeight),
                    color = valueColor
                )
            }
            if (showEditIcon) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = SpoolStudioColors.GoldDark,
                    modifier = Modifier
                        .padding(start = 6.dp)
                        .size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun CompactDataRow(
    label: String,
    onClick: (() -> Unit)? = null,
    showTopDivider: Boolean = false,
    showBottomDivider: Boolean = true,
    valueContent: @Composable RowScope.() -> Unit
) {
    val rowModifier = if (onClick != null) {
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    } else {
        Modifier.fillMaxWidth()
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        if (showTopDivider) {
            CompactDivider()
        }
        Row(
            modifier = rowModifier.padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, lineHeight = 18.sp),
                color = SpoolStudioColors.Ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1.02f)
            )
            Row(
                modifier = Modifier.weight(1.38f),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
                content = valueContent
            )
        }
        if (showBottomDivider) {
            CompactDivider()
        }
    }
}

@Composable
private fun CompactDivider() {
    HorizontalDivider(
        color = SpoolStudioColors.OutlineSoft.copy(alpha = 0.75f),
        thickness = 1.dp
    )
}

@Composable
private fun compactValueStyle(
    color: Color = SpoolStudioColors.Ink,
    textAlign: TextAlign = TextAlign.Start,
    fontWeight: FontWeight = FontWeight.SemiBold
): TextStyle =
    MaterialTheme.typography.bodyMedium.copy(
        fontSize = 14.sp,
        lineHeight = 18.sp,
        fontWeight = fontWeight,
        color = color,
        textAlign = textAlign
    )

private fun prioritizedOptions(
    preferred: List<String>,
    values: List<String>,
    trailing: List<String>
): List<String> {
    val seen = linkedSetOf<String>()
    preferred.filterTo(seen) { it.isNotBlank() }
    values.filterTo(seen) { it.isNotBlank() && it !in preferred && it !in trailing }
    trailing.filterTo(seen) { it.isNotBlank() }
    return seen.toList()
}

private data class EmptySpoolWeightSuggestion(
    val weight: Float,
    val brand: String
) {
    val weightText: String
        get() = trimWeightZeros(String.format(java.util.Locale.US, "%.2f", weight))
    val label: String
        get() = "$weightText g - $brand"
}

private fun buildEmptySpoolWeightSuggestions(spools: List<FilamentSpool>): List<EmptySpoolWeightSuggestion> {
    val seen = linkedSetOf<String>()
    return spools.mapNotNull { spool ->
        val weight = spool.emptySpoolWeight?.takeIf { it >= 0f } ?: return@mapNotNull null
        val brand = spool.brand.trim().ifBlank { "Unknown" }
        val weightKey = String.format(java.util.Locale.US, "%.2f", weight)
        val key = "${weightKey}|${brand.lowercase(java.util.Locale.ROOT)}"
        if (seen.add(key)) {
            EmptySpoolWeightSuggestion(weight, brand)
        } else {
            null
        }
    }.sortedWith(
        compareBy<EmptySpoolWeightSuggestion> { it.weight }
            .thenBy { it.brand.lowercase(java.util.Locale.ROOT) }
    )
}

private fun sanitizeWeightInput(input: String): String {
    val normalized = input.replace(',', '.')
    return buildString {
        normalized.forEach { char ->
            if (char.isDigit()) append(char)
            if (char == '.' && !contains('.')) append(char)
        }
    }.take(8)
}

private fun formatWeightWithUnit(value: String): String =
    value.trim().takeIf { it.isNotBlank() }?.let { "${trimWeightZeros(it)} g" }.orEmpty()

@Composable
private fun TemperatureInputCard(form: SpoolFormState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = SpoolStudioShape.Small,
        colors = CardDefaults.cardColors(containerColor = SpoolStudioColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "Temperature",
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp, lineHeight = 20.sp),
                fontWeight = FontWeight.SemiBold,
                color = SpoolStudioColors.Ink
            )

            key(form.filamentType) {
                CompactTemperatureRow(
                    label = "Nozzle",
                    minValue = form.minTemp,
                    maxValue = form.maxTemp,
                    onMinChange = { form.minTemp = it },
                    onMaxChange = { form.maxTemp = it }
                )
                HorizontalDivider(
                    color = SpoolStudioColors.OutlineSoft.copy(alpha = 0.8f),
                    thickness = 1.dp
                )
                CompactTemperatureRow(
                    label = "Bed",
                    minValue = form.bedMinTemp,
                    maxValue = form.bedMaxTemp,
                    onMinChange = { form.bedMinTemp = it },
                    onMaxChange = { form.bedMaxTemp = it }
                )
            }
        }
    }
}

@Composable
private fun CompactTemperatureRow(
    label: String,
    minValue: String,
    maxValue: String,
    onMinChange: (String) -> Unit,
    onMaxChange: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, lineHeight = 18.sp),
            color = SpoolStudioColors.Ink,
            modifier = Modifier.width(58.dp)
        )

        TemperatureStepper(
            value = minValue,
            onDecrease = { onMinChange(adjustTemperature(minValue, -1)) },
            onIncrease = { onMinChange(adjustTemperature(minValue, 1)) },
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "-",
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp),
            fontWeight = FontWeight.SemiBold,
            color = SpoolStudioColors.GoldDark
        )
        TemperatureStepper(
            value = maxValue,
            onDecrease = { onMaxChange(adjustTemperature(maxValue, -1)) },
            onIncrease = { onMaxChange(adjustTemperature(maxValue, 1)) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun TemperatureStepper(
    value: String,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(32.dp)
            .clip(SpoolStudioShape.Small)
            .background(SpoolStudioColors.SurfaceRaised)
            .border(1.dp, SpoolStudioColors.OutlineSoft, SpoolStudioShape.Small),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TemperatureStepButton("-", onDecrease)
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(32.dp)
                .background(SpoolStudioColors.OutlineSoft.copy(alpha = 0.75f))
        )
        Text(
            text = "${value.ifBlank { "-" }} °C",
            style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.sp, lineHeight = 16.sp),
            fontWeight = FontWeight.SemiBold,
            color = SpoolStudioColors.Ink,
            maxLines = 1,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(32.dp)
                .background(SpoolStudioColors.OutlineSoft.copy(alpha = 0.75f))
        )
        TemperatureStepButton("+", onIncrease)
    }
}

@Composable
private fun TemperatureStepButton(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(28.dp)
            .height(32.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp, lineHeight = 18.sp),
            fontWeight = FontWeight.SemiBold,
            color = SpoolStudioColors.GoldDark
        )
    }
}

private fun adjustTemperature(value: String, delta: Int): String {
    val current = value.trim().toIntOrNull() ?: 0
    return (current + delta).coerceIn(0, 400).toString()
}

@Composable
private fun SpoolHeroPanel(
    form: SpoolFormState,
    selectedSpool: FilamentSpool?,
    spoolMode: SpoolMode,
    spools: List<FilamentSpool>,
    spoolmanUrl: String,
    currentSpoolId: String?,
    isLoadingSpools: Boolean,
    onSpoolSelected: (FilamentSpool?) -> Unit,
    onClearAllSpoolFields: () -> Unit,
    onRefreshSelectedSpool: (Int) -> Unit,
    onRefreshSpoolmanCatalogIfStale: () -> Unit
) {
    val isCreateMode = spoolMode != SpoolMode.UPDATE
    val remainingText = form.remainingWeight.ifBlank {
        selectedSpool?.remainingWeight?.let { "%.2f".format(it) }
            ?: if (isCreateMode) "1000.00" else ""
    }
    val remainingWarning = remainingWeightWarningThreshold(remainingText)
    val remainingColor = when (remainingWarning) {
        150 -> Color(0xFFFFC66A)
        100 -> Color(0xFFFF8E8E)
        50 -> Color(0xFFFF666B)
        else -> SpoolStudioColors.OnGraphite
    }
    val spoolColor = parseSpoolColor(form.colorHex)
    val initialWeightValue = selectedSpool?.let(::calculateInitialWeightValue)
        ?: parseWeightValue(remainingText)
        ?: 1000f
    val initialWeight = initialWeightValue.takeIf { it > 0f }?.let { "${it.toInt()} g" }
        ?: "1000 g"
    val remainingValue = parseWeightValue(remainingText)
    val remainingPercent = remainingValue?.let { remaining ->
        if (initialWeightValue > 0f) ((remaining / initialWeightValue) * 100f).coerceIn(0f, 100f) else null
    }
    val selectorOverride = when {
        spoolMode == SpoolMode.DUPLICATE -> "New spool from selected"
        spoolMode == SpoolMode.CREATE && selectedSpool == null -> "New empty spool"
        else -> null
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        SpoolmanSelectionSection(
            spools = spools,
            selectedSpool = selectedSpool,
            spoolmanUrl = spoolmanUrl,
            currentSpoolId = currentSpoolId,
            isLoadingSpools = isLoadingSpools,
            darkStyle = true,
            displayOverride = selectorOverride,
            showNewPill = isCreateMode && selectorOverride != null,
            onSpoolSelected = onSpoolSelected,
            onClearAllSpoolFields = onClearAllSpoolFields,
            onRefreshSelectedSpool = onRefreshSelectedSpool,
            onRefreshSpoolmanCatalogIfStale = onRefreshSpoolmanCatalogIfStale
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SpoolStudioLogo(
                color = spoolColor,
                logoSize = 200.dp,
                showTitle = false,
                modifier = Modifier
                    .weight(1.18f)
                    .defaultMinSize(minHeight = 156.dp)
            )

            StatusInfoPanel(
                newMode = isCreateMode,
                firstUse = if (isCreateMode) "-" else selectedSpool?.firstUsed?.let(::formatCompactDate) ?: "-",
                lastUse = if (isCreateMode) "-" else selectedSpool?.lastUsed?.let(::formatCompactDate) ?: "-",
                initialWeight = initialWeight,
                remainingWeight = remainingText.takeIf { it.isNotBlank() }?.let { "${trimWeightZeros(it)} g" } ?: "-",
                remainingPercent = remainingPercent,
                remainingColor = remainingColor,
                modifier = Modifier
                    .weight(1f)
                    .padding(bottom = 12.dp)
            )
        }
    }
}

@Composable
private fun StatusInfoPanel(
    newMode: Boolean,
    firstUse: String,
    lastUse: String,
    initialWeight: String,
    remainingWeight: String,
    remainingPercent: Float?,
    remainingColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(164.dp),
        shape = SpoolStudioShape.Small,
        colors = CardDefaults.cardColors(containerColor = SpoolStudioColors.Graphite.copy(alpha = 0.74f)),
        border = BorderStroke(
            1.dp,
            SpoolStudioColors.GraphiteMuted.copy(alpha = 0.95f)
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 9.dp)
        ) {
            if (newMode) {
                Column(
                    modifier = Modifier.height(54.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = "Spool status",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, lineHeight = 12.sp),
                        color = SpoolStudioColors.OnGraphiteMuted
                    )
                    Text(
                        text = "New spool",
                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.sp, lineHeight = 16.sp),
                        fontWeight = FontWeight.SemiBold,
                        color = SpoolStudioColors.AccentCyan
                    )
                    Text(
                        text = "No ID yet",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, lineHeight = 12.sp),
                        color = SpoolStudioColors.OnGraphiteMuted
                    )
                }
            } else {
                StatusInfoLine("First use", firstUse, modifier = Modifier.height(30.dp))
                StatusInfoLine("Last use", lastUse, modifier = Modifier.height(30.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(SpoolStudioColors.GraphiteMuted.copy(alpha = 0.75f))
            )
            Spacer(modifier = Modifier.height(8.dp))
            StatusInfoLine("Initial", initialWeight, modifier = Modifier.height(21.dp))
            StatusInfoLine(
                "Remaining",
                buildString {
                    append(remainingWeight)
                    remainingPercent?.let {
                        append("\n")
                        append(it.toInt())
                        append("%")
                    }
                },
                remainingColor,
                modifier = Modifier.height(33.dp)
            )
            RemainingFillBar(
                percent = remainingPercent ?: 0f,
                color = remainingColor
            )
        }
    }
}

@Composable
private fun RemainingFillBar(
    percent: Float,
    color: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(7.dp)
            .clip(CircleShape)
            .background(SpoolStudioColors.GraphiteMuted.copy(alpha = 0.45f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth((percent / 100f).coerceIn(0.02f, 1f))
                .height(7.dp)
                .clip(CircleShape)
                .background(color)
        )
    }
}

@Composable
private fun StatusInfoLine(
    label: String,
    value: String,
    valueColor: Color = SpoolStudioColors.OnGraphite,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp, lineHeight = 14.sp),
            color = SpoolStudioColors.OnGraphiteMuted,
            maxLines = 1,
            modifier = Modifier.weight(1.08f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium.copy(fontSize = 10.5.sp, lineHeight = 13.sp),
            fontWeight = FontWeight.SemiBold,
            color = valueColor,
            textAlign = TextAlign.End,
            maxLines = 2,
            modifier = Modifier.weight(0.92f)
        )
    }
}

@Composable
private fun InstrumentPanelSection(
    title: String,
    compact: Boolean = false,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = SpoolStudioShape.Small,
        colors = CardDefaults.cardColors(containerColor = SpoolStudioColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = if (compact) 9.dp else 11.dp),
            verticalArrangement = Arrangement.spacedBy(if (compact) 7.dp else 9.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp, lineHeight = 20.sp),
                    fontWeight = FontWeight.SemiBold,
                    color = SpoolStudioColors.Ink
                )

                if (actionLabel != null && onAction != null) {
                    Text(
                        text = actionLabel,
                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp, lineHeight = 16.sp),
                        fontWeight = FontWeight.SemiBold,
                        color = SpoolStudioColors.GoldDark,
                        maxLines = 1,
                        modifier = Modifier.clickable(onClick = onAction)
                    )
                }
            }
            content()
        }
    }
}

@Composable
private fun EditTray(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(SpoolStudioShape.Small)
            .background(SpoolStudioColors.SurfaceRaised)
            .border(1.dp, SpoolStudioColors.OutlineSoft, SpoolStudioShape.Small)
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        content = content
    )
}

@Composable
private fun FilamentSummaryList(form: SpoolFormState) {
    val colorValue = form.colorName.ifBlank { form.colorHex?.let { "#$it" } ?: "No Color" }
    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
        SummaryRow("Type", form.filamentType.ifBlank { "PLA" })
        SummaryDivider()
        SummaryRow("Variant", form.variant.ifBlank { "Basic" })
        SummaryDivider()
        SummaryRow("Brand", form.brand.ifBlank { "Generic" })
        SummaryDivider()
        SummaryRow(
            label = "Color",
            value = colorValue,
            swatchColor = form.colorHex?.let(::parseSpoolColor)
        )
        SummaryDivider()
        SummaryRow("HEX", form.colorHex?.let { "#$it" } ?: "-")
    }
}

@Composable
private fun SpoolWeightSummaryList(
    form: SpoolFormState,
    selectedSpool: FilamentSpool?
) {
    val remainingText = form.remainingWeight.ifBlank {
        selectedSpool?.remainingWeight?.let { "%.2f".format(it) } ?: "1000.00"
    }
    val remainingWarning = remainingWeightWarningThreshold(remainingText)
    val remainingColor = when (remainingWarning) {
        150 -> MaterialTheme.colorScheme.tertiary
        100 -> MaterialTheme.colorScheme.error.copy(alpha = 0.82f)
        50 -> MaterialTheme.colorScheme.error
        else -> SpoolStudioColors.Ink
    }
    val initialWeight = selectedSpool?.let(::calculateInitialWeightText) ?: "1000 g"
    val usedWeight = selectedSpool?.usedWeight?.coerceAtLeast(0f)?.let { "${it.toInt()} g" } ?: "-"

    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
        SummaryRow("Initial", initialWeight)
        SummaryDivider()
        SummaryRow("Remaining", "$remainingText g", valueColor = remainingColor)
        SummaryDivider()
        SummaryRow("Used", usedWeight)
    }
}

@Composable
private fun TemperatureSummaryList(form: SpoolFormState) {
    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
        SummaryRow("Nozzle", "${form.minTemp.ifBlank { "-" }} - ${form.maxTemp.ifBlank { "-" }} \u00B0C")
        SummaryDivider()
        SummaryRow("Bed", "${form.bedMinTemp.ifBlank { "-" }} - ${form.bedMaxTemp.ifBlank { "-" }} \u00B0C")
    }
}

@Composable
private fun RfidSummaryList() {
    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
        SummaryRow("Tag", "OpenSpool")
        SummaryDivider()
        SummaryRow("Status", "Ready", valueColor = SpoolStudioColors.AccentCyan)
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: String,
    valueColor: Color = SpoolStudioColors.Ink,
    swatchColor: Color? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.sp, lineHeight = 17.sp),
            color = SpoolStudioColors.Ink,
            maxLines = 1,
            modifier = Modifier.weight(1f)
        )

        Row(
            modifier = Modifier.weight(1.35f),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (swatchColor != null) {
                Box(
                    modifier = Modifier
                        .padding(end = 5.dp)
                        .size(13.dp)
                        .clip(CircleShape)
                        .background(swatchColor)
                        .border(1.dp, SpoolStudioColors.Outline, CircleShape)
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.sp, lineHeight = 17.sp),
                fontWeight = FontWeight.SemiBold,
                color = valueColor,
                textAlign = TextAlign.End,
                maxLines = 2
            )
        }
    }
}

@Composable
private fun SummaryDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(SpoolStudioColors.OutlineSoft.copy(alpha = 0.7f))
    )
}

private fun parseSpoolColor(colorHex: String?): Color {
    val normalized = colorHex?.trim()?.removePrefix("#")
    if (normalized.isNullOrBlank() || !normalized.matches(Regex("^[A-Fa-f0-9]{6}$"))) {
        return Color(0xFF4F5357)
    }
    return runCatching {
        Color(android.graphics.Color.parseColor("#$normalized"))
    }.getOrDefault(Color(0xFF4F5357))
}

private fun calculateInitialWeightText(spool: FilamentSpool): String {
    val total = calculateInitialWeightValue(spool)
    return if (total > 0f) "${total.toInt()} g" else "-"
}

private fun calculateInitialWeightValue(spool: FilamentSpool): Float {
    val used = spool.usedWeight.coerceAtLeast(0f)
    val remaining = (spool.remainingWeight ?: 0f).coerceAtLeast(0f)
    return used + remaining
}

private fun parseWeightValue(value: String): Float? =
    value.trim().replace(",", ".").toFloatOrNull()

private fun trimWeightZeros(value: String): String =
    value.trim()
        .replace(",", ".")
        .toFloatOrNull()
        ?.let {
            if (it % 1f == 0f) it.toInt().toString() else "%.2f".format(java.util.Locale.US, it)
        }
        ?: value

private fun formatCompactDate(value: String): String {
    val cleaned = value.replace("T", " ")
        .removeSuffix("Z")
        .substringBefore(".")
        .trim()

    val date = cleaned.substringBefore(" ")
    val time = cleaned.substringAfter(" ", "").take(5)
    val displayDate = runCatching {
        val parts = date.split("-")
        "${parts[0]}-${parts[1]}-${parts[2]}"
    }.getOrDefault(date)

    return if (time.isNotBlank()) "$displayDate\n$time" else displayDate
}

@Composable
private fun SpoolmanSelectionSection(
    spools: List<FilamentSpool>,
    selectedSpool: FilamentSpool?,
    spoolmanUrl: String,
    currentSpoolId: String?,
    isLoadingSpools: Boolean,
    darkStyle: Boolean = false,
    displayOverride: String? = null,
    showNewPill: Boolean = false,
    onSpoolSelected: (FilamentSpool?) -> Unit,
    onClearAllSpoolFields: () -> Unit,
    onRefreshSelectedSpool: (Int) -> Unit,
    onRefreshSpoolmanCatalogIfStale: () -> Unit
) {
    if (spools.isNotEmpty()) {
        SpoolmanFilamentDropdown(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            filaments = spools,
            selectedFilament = selectedSpool,
            onFilamentSelected = onSpoolSelected,
            spoolmanUrl = spoolmanUrl,
            currentSpoolId = currentSpoolId,
            isLoading = isLoadingSpools,
            darkStyle = darkStyle,
            displayOverride = displayOverride,
            showNewPill = showNewPill,
            onClearAll = onClearAllSpoolFields,
            onOpen = onRefreshSpoolmanCatalogIfStale,
            infoButton = null
        )
        return
    }

    if (isLoadingSpools) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Loading Spoolman filaments...",
                color = if (darkStyle) SpoolStudioColors.OnGraphiteMuted else MaterialTheme.colorScheme.onSurface
            )
        }
        return
    }

    if (spoolmanUrl.isBlank()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (darkStyle) SpoolStudioColors.GraphiteRaised else MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Text(
                text = "Tip: Connect Spoolman server in settings for easy filament selection",
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodySmall,
                color = if (darkStyle) SpoolStudioColors.OnGraphiteMuted else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
