package com.spoolstudio.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spoolstudio.app.domain.models.FilamentSpool
import com.spoolstudio.app.ui.theme.SpoolStudioColors
import com.spoolstudio.app.ui.theme.SpoolStudioShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpoolmanFilamentDropdown(
    modifier: Modifier = Modifier,
    filaments: List<FilamentSpool>,
    selectedFilament: FilamentSpool?,
    onFilamentSelected: (FilamentSpool?) -> Unit,
    spoolmanUrl: String,
    currentSpoolId: String?,
    isLoading: Boolean = false,
    darkStyle: Boolean = false,
    displayOverride: String? = null,
    showNewPill: Boolean = false,
    onClearAll: (() -> Unit)? = null,
    onOpen: () -> Unit = {},
    infoButton: (@Composable () -> Unit)? = null
) {
    val focusManager = LocalFocusManager.current
    var expanded by remember { mutableStateOf(false) }
    var suppressNextToggle by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val filteredFilaments = filterSpoolmanDropdownFilaments(filaments, searchQuery)
    LaunchedEffect(expanded) {
        if (expanded) {
            searchQuery = ""
        }
    }

    LaunchedEffect(currentSpoolId, filaments, selectedFilament?.id) {
        val targetId = currentSpoolId?.toIntOrNull() ?: return@LaunchedEffect
        if (selectedFilament?.id == targetId) return@LaunchedEffect

        val match = filaments.firstOrNull { it.id == targetId }
        if (match != null) {
            onFilamentSelected(match)
        }
    }

    if (darkStyle) {
        DarkSpoolmanDropdown(
            modifier = modifier,
            filaments = filaments,
            selectedFilament = selectedFilament,
            filteredFilaments = filteredFilaments,
            displayOverride = displayOverride,
            showNewPill = showNewPill,
            isLoading = isLoading,
            expanded = expanded,
            searchQuery = searchQuery,
            onExpandedChange = { shouldExpand ->
                if (shouldExpand && !expanded) {
                    onOpen()
                }
                expanded = shouldExpand && filaments.isNotEmpty() && !isLoading
            },
            onSearchQueryChange = { searchQuery = it.take(60) },
            onFilamentSelected = {
                onFilamentSelected(it)
                expanded = false
            },
            onClearAll = onClearAll
        )
        return
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { shouldExpand ->
            if (suppressNextToggle) {
                suppressNextToggle = false
            } else {
                if (shouldExpand) {
                    focusManager.clearFocus()
                    if (!expanded) {
                        onOpen()
                    }
                }
                expanded = shouldExpand && filaments.isNotEmpty() && !isLoading
            }
        },
        modifier = modifier
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = displayOverride ?: selectedFilament?.let(::spoolmanDropdownLabel) ?: "",
                onValueChange = { },
                readOnly = true,
                singleLine = true,
                label = { Text("Select from Spoolman") },
                trailingIcon = {
                    val trailingColor = if (darkStyle) {
                        SpoolStudioColors.OnGraphiteMuted
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                    when {
                        isLoading -> {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        }
                        showNewPill -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "New",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = SpoolStudioColors.OnGraphite,
                                    modifier = Modifier
                                        .clip(SpoolStudioShape.Small)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                        selectedFilament != null -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                VerticalDivider(
                                    modifier = Modifier.height(24.dp),
                                    color = if (darkStyle) SpoolStudioColors.GraphiteMuted else MaterialTheme.colorScheme.outline
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Id#${selectedFilament.id}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = trailingColor
                                )
                            }
                        }
                        else -> {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        }
                    }
                },
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth(),
                enabled = !isLoading && filaments.isNotEmpty(),
                textStyle = (if (darkStyle) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge).copy(
                    fontWeight = FontWeight.SemiBold
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (darkStyle) SpoolStudioColors.GoldSoft else MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = if (darkStyle) SpoolStudioColors.GraphiteMuted else MaterialTheme.colorScheme.outline,
                    focusedTextColor = if (darkStyle) SpoolStudioColors.OnGraphite else MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = if (darkStyle) SpoolStudioColors.OnGraphite else MaterialTheme.colorScheme.onSurface,
                    focusedLabelColor = if (darkStyle) SpoolStudioColors.GoldSoft else MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = if (darkStyle) SpoolStudioColors.OnGraphiteMuted else MaterialTheme.colorScheme.onSurfaceVariant,
                    cursorColor = if (darkStyle) SpoolStudioColors.GoldSoft else MaterialTheme.colorScheme.primary,
                    focusedContainerColor = if (darkStyle) SpoolStudioColors.GraphiteRaised else MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = if (darkStyle) SpoolStudioColors.GraphiteRaised else MaterialTheme.colorScheme.surface,
                    disabledContainerColor = if (darkStyle) SpoolStudioColors.GraphiteRaised else MaterialTheme.colorScheme.surface,
                    focusedTrailingIconColor = if (darkStyle) SpoolStudioColors.OnGraphiteMuted else MaterialTheme.colorScheme.onSurfaceVariant,
                    unfocusedTrailingIconColor = if (darkStyle) SpoolStudioColors.OnGraphiteMuted else MaterialTheme.colorScheme.onSurfaceVariant
                ),
                shape = SpoolStudioShape.Field
            )

            if (selectedFilament != null && infoButton != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 12.dp, end = 30.dp)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            suppressNextToggle = true
                        }
                ) {
                    infoButton()
                }
            }
        }

        if (expanded) {
            SearchableDropdownDialog(
                title = "Select from Spoolman",
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                items = filteredFilaments,
                itemLabel = ::spoolmanDropdownListLabel,
                onItemSelected = { filament ->
                    onFilamentSelected(filament)
                    focusManager.clearFocus()
                    expanded = false
                },
                onDismiss = {
                    focusManager.clearFocus()
                    expanded = false
                },
                itemContent = { filament ->
                    SpoolmanDialogSpoolRow(
                        filament = filament,
                        onClick = {
                            onFilamentSelected(filament)
                            focusManager.clearFocus()
                            expanded = false
                        }
                    )
                },
                topContent = {
                    if (onClearAll != null) {
                        DropdownDialogItem(
                            text = "Clear all",
                            onClick = {
                                onClearAll()
                                focusManager.clearFocus()
                                expanded = false
                            }
                        )
                        HorizontalDivider(color = SpoolStudioColors.OutlineSoft)
                    }
                }
            )
        }
    }
}

@Composable
private fun DarkSpoolmanDropdown(
    modifier: Modifier,
    filaments: List<FilamentSpool>,
    selectedFilament: FilamentSpool?,
    filteredFilaments: List<FilamentSpool>,
    displayOverride: String?,
    showNewPill: Boolean,
    isLoading: Boolean,
    expanded: Boolean,
    searchQuery: String,
    onExpandedChange: (Boolean) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onFilamentSelected: (FilamentSpool?) -> Unit,
    onClearAll: (() -> Unit)?
) {
    val focusManager = LocalFocusManager.current
    Box(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(SpoolStudioShape.Small)
                .background(SpoolStudioColors.GraphiteRaised)
                .border(1.dp, SpoolStudioColors.GraphiteMuted, SpoolStudioShape.Small)
                .clickable(enabled = !isLoading && filaments.isNotEmpty()) {
                    focusManager.clearFocus()
                    onExpandedChange(!expanded)
                }
                .padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            Text(
                text = "Select from Spoolman",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, lineHeight = 10.sp),
                color = SpoolStudioColors.OnGraphiteMuted,
                maxLines = 1,
                modifier = Modifier.align(Alignment.TopStart)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterStart)
                    .padding(top = 8.dp, end = 76.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = displayOverride ?: selectedFilament?.let(::spoolmanDropdownLabel) ?: "Select from Spoolman",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        lineHeight = 17.sp
                    ),
                    color = SpoolStudioColors.OnGraphite,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = SpoolStudioColors.OnGraphiteMuted,
                    modifier = Modifier.size(18.dp)
                )

            }

            Box(
                modifier = Modifier.align(Alignment.CenterEnd),
                contentAlignment = Alignment.Center
            ) {
                when {
                    isLoading -> CircularProgressIndicator(modifier = Modifier.size(16.dp))
                    showNewPill -> SelectorPill(text = "New", showDot = false)
                    selectedFilament != null -> SelectorPill(text = "ID #${selectedFilament.id}", showDot = true)
                }
            }
        }

        if (expanded) {
            SearchableDropdownDialog(
                title = "Select from Spoolman",
                searchQuery = searchQuery,
                onSearchQueryChange = onSearchQueryChange,
                items = filteredFilaments,
                itemLabel = ::spoolmanDropdownListLabel,
                onItemSelected = { filament ->
                    onFilamentSelected(filament)
                    focusManager.clearFocus()
                },
                onDismiss = {
                    focusManager.clearFocus()
                    onExpandedChange(false)
                },
                itemContent = { filament ->
                    SpoolmanDialogSpoolRow(
                        filament = filament,
                        onClick = {
                            onFilamentSelected(filament)
                            focusManager.clearFocus()
                            onExpandedChange(false)
                        }
                    )
                },
                topContent = {
                    if (onClearAll != null) {
                        DropdownDialogItem(
                            text = "Clear all",
                            onClick = {
                                onClearAll()
                                focusManager.clearFocus()
                                onExpandedChange(false)
                            }
                        )
                        HorizontalDivider(color = SpoolStudioColors.OutlineSoft)
                    }
                }
            )
        }
    }
}

@Composable
private fun SelectorPill(
    text: String,
    showDot: Boolean
) {
    val pillBackground = if (showDot) {
        SpoolStudioColors.Graphite.copy(alpha = 0.72f)
    } else {
        SpoolStudioColors.AccentCyan.copy(alpha = 0.86f)
    }
    val pillBorder = if (showDot) {
        SpoolStudioColors.GraphiteMuted.copy(alpha = 0.9f)
    } else {
        SpoolStudioColors.AccentCyan
    }
    val pillTextColor = if (showDot) {
        SpoolStudioColors.OnGraphite
    } else {
        Color.White
    }

    Row(
        modifier = Modifier
            .clip(SpoolStudioShape.Small)
            .background(pillBackground)
            .border(1.dp, pillBorder, SpoolStudioShape.Small)
            .padding(horizontal = 7.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showDot) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF57C75E))
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, lineHeight = 12.sp),
            color = pillTextColor,
            maxLines = 1
        )
    }
}

@Composable
private fun SpoolmanDialogSpoolRow(
    filament: FilamentSpool,
    onClick: () -> Unit
) {
    val materialParts = listOf(
        filament.brand,
        listOf(filament.material, filament.materialModifier)
            .filter { it.isNotBlank() && !it.equals("None", ignoreCase = true) }
            .joinToString(" "),
        filament.variant
    ).filter { it.isNotBlank() && !it.equals("None", ignoreCase = true) }
    val title = filament.spoolmanName?.takeIf(String::isNotBlank) ?: filament.displayName
    val subtitle = materialParts.joinToString(" / ")
    val idText = filament.id?.let { "ID #$it" } ?: "No ID"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(SpoolStudioShape.Field)
            .background(SpoolStudioColors.GraphiteRaised.copy(alpha = 0.72f))
            .border(
                width = 1.dp,
                color = SpoolStudioColors.GraphiteMuted.copy(alpha = 0.85f),
                shape = SpoolStudioShape.Field
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(width = 56.dp, height = 60.dp),
            contentAlignment = Alignment.Center
        ) {
            SpoolStudioLogo(
                color = dialogSpoolColor(filament.colorHex),
                logoSize = 48.dp,
                showTitle = false,
                modifier = Modifier.padding(top = 6.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = SpoolStudioColors.OnGraphite,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = SpoolStudioColors.OnGraphiteMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Box(
            modifier = Modifier
                .clip(SpoolStudioShape.Small)
                .background(SpoolStudioColors.Graphite.copy(alpha = 0.78f))
                .border(
                    width = 1.dp,
                    color = SpoolStudioColors.GraphiteMuted.copy(alpha = 0.9f),
                    shape = SpoolStudioShape.Small
                )
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(
                text = idText,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = SpoolStudioColors.OnGraphite,
                maxLines = 1
            )
        }
    }
}

private fun dialogSpoolColor(colorHex: String?): Color {
    val normalized = colorHex?.trim()?.removePrefix("#").orEmpty()
    return if (normalized.matches(Regex("^[A-Fa-f0-9]{6}$"))) {
        Color(android.graphics.Color.parseColor("#$normalized"))
    } else {
        Color(0xFF4A423D)
    }
}
