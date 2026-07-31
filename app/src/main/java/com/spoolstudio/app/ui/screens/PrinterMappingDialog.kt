package com.spoolstudio.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.spoolstudio.app.domain.models.FilamentSpool
import com.spoolstudio.app.ui.components.filterSpoolmanDropdownFilaments
import com.spoolstudio.app.ui.components.spoolmanDropdownListLabel
import com.spoolstudio.app.ui.components.SpoolStudioLogo
import com.spoolstudio.app.ui.theme.SpoolStudioColors
import com.spoolstudio.app.ui.theme.SpoolStudioShape
import com.spoolstudio.app.ui.theme.spoolStudioBackground

@Composable
fun PrinterMappingDialogHost(
    visible: Boolean,
    spools: List<FilamentSpool>,
    isMoonrakerReachable: Boolean,
    isLoadingPrinterMapping: Boolean,
    printerIntegrationModeLabel: String?,
    inlineStatusText: String?,
    inlineStatusColor: Color,
    toolhead1SpoolId: Int?,
    toolhead2SpoolId: Int?,
    toolhead3SpoolId: Int?,
    toolhead4SpoolId: Int?,
    onCancel: () -> Unit,
    onLoadCurrentPrinterMapping: () -> Unit,
    onAssignPrinterToolhead: (Int, Int?) -> Unit
) {
    if (!visible) return

    PrinterMappingDialog(
        spools = spools,
        isMoonrakerReachable = isMoonrakerReachable,
        isLoadingPrinterMapping = isLoadingPrinterMapping,
        printerIntegrationModeLabel = printerIntegrationModeLabel,
        inlineStatusText = inlineStatusText,
        inlineStatusColor = inlineStatusColor,
        toolhead1SpoolId = toolhead1SpoolId,
        toolhead2SpoolId = toolhead2SpoolId,
        toolhead3SpoolId = toolhead3SpoolId,
        toolhead4SpoolId = toolhead4SpoolId,
        onCancel = onCancel,
        onLoadCurrentPrinterMapping = onLoadCurrentPrinterMapping,
        onAssignPrinterToolhead = onAssignPrinterToolhead
    )
}

@Composable
fun PrinterMappingDialog(
    spools: List<FilamentSpool>,
    isMoonrakerReachable: Boolean,
    isLoadingPrinterMapping: Boolean,
    printerIntegrationModeLabel: String?,
    inlineStatusText: String?,
    inlineStatusColor: Color,
    toolhead1SpoolId: Int?,
    toolhead2SpoolId: Int?,
    toolhead3SpoolId: Int?,
    toolhead4SpoolId: Int?,
    onCancel: () -> Unit,
    onLoadCurrentPrinterMapping: () -> Unit,
    onAssignPrinterToolhead: (Int, Int?) -> Unit
) {
    var showAppComposedData by remember { mutableStateOf(true) }
    var assigningToolheadIndex by remember { mutableStateOf<Int?>(null) }
    var assignSearchQuery by remember { mutableStateOf("") }
    val toolheads = listOf(
        "Toolhead 1" to spools.firstOrNull { it.id == toolhead1SpoolId },
        "Toolhead 2" to spools.firstOrNull { it.id == toolhead2SpoolId },
        "Toolhead 3" to spools.firstOrNull { it.id == toolhead3SpoolId },
        "Toolhead 4" to spools.firstOrNull { it.id == toolhead4SpoolId }
    )
    val assignedCount = toolheads.count { it.second != null }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .spoolStudioBackground()
    ) {
        Card(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(horizontal = 18.dp),
            shape = SpoolStudioShape.Dialog,
            colors = CardDefaults.cardColors(
                containerColor = SpoolStudioColors.Graphite
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 14.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 14.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Toolhead status",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = SpoolStudioColors.OnGraphite
                        )
                        Text(
                            text = "Current spool data reported by the printer",
                            style = MaterialTheme.typography.bodySmall,
                            color = SpoolStudioColors.OnGraphiteMuted
                        )
                    }

                    IconButton(onClick = onCancel) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = SpoolStudioColors.OnGraphite
                        )
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = SpoolStudioShape.Field,
                    colors = CardDefaults.cardColors(
                        containerColor = SpoolStudioColors.GraphiteRaised.copy(alpha = 0.62f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isMoonrakerReachable) "Printer connected" else "Printer not reachable",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isMoonrakerReachable) SpoolStudioColors.GoldSoft else SpoolStudioColors.Error
                            )
                            Text(
                                text = "$assignedCount of 4 toolheads assigned",
                                style = MaterialTheme.typography.bodySmall,
                                color = SpoolStudioColors.OnGraphiteMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        if (isLoadingPrinterMapping) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = SpoolStudioColors.AccentCyan
                            )
                        }
                    }
                }

                inlineStatusText?.let { status ->
                    Text(
                        text = status,
                        style = MaterialTheme.typography.bodySmall,
                        color = inlineStatusColor,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { showAppComposedData = true },
                        shape = SpoolStudioShape.Button,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (showAppComposedData) SpoolStudioColors.AccentCyan else Color.Transparent,
                            contentColor = if (showAppComposedData) SpoolStudioColors.OnGraphite else SpoolStudioColors.AccentCyan
                        )
                    ) {
                        Text(
                            text = "App labels",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    OutlinedButton(
                        onClick = { showAppComposedData = false },
                        shape = SpoolStudioShape.Button,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (!showAppComposedData) SpoolStudioColors.AccentCyan else Color.Transparent,
                            contentColor = if (!showAppComposedData) SpoolStudioColors.OnGraphite else SpoolStudioColors.AccentCyan
                        )
                    ) {
                        Text(
                            text = "SpoolLink",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                HorizontalDivider(color = SpoolStudioColors.GraphiteMuted.copy(alpha = 0.75f))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    toolheads.forEach { (label, spool) ->
                        val index = toolheads.indexOfFirst { it.first == label }
                        MappingToolheadStatusCard(
                            label = label,
                            spool = spool,
                            showAppComposedData = showAppComposedData,
                            assignEnabled = isMoonrakerReachable && !isLoadingPrinterMapping,
                            onAssignClick = {
                                assignSearchQuery = ""
                                assigningToolheadIndex = index
                            }
                        )
                    }
                }

                OutlinedButton(
                    onClick = onLoadCurrentPrinterMapping,
                    enabled = isMoonrakerReachable && !isLoadingPrinterMapping,
                    shape = SpoolStudioShape.Button,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = SpoolStudioColors.AccentCyan,
                        disabledContentColor = SpoolStudioColors.OnGraphiteMuted.copy(alpha = 0.55f)
                    )
                ) {
                    Text(
                        text = "Refresh toolhead status",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))
            }
        }

        assigningToolheadIndex?.let { index ->
            AssignToolheadSpoolDialog(
                toolheadLabel = "Toolhead ${index + 1}",
                spools = spools,
                searchQuery = assignSearchQuery,
                onSearchQueryChange = { assignSearchQuery = it },
                onDismiss = { assigningToolheadIndex = null },
                onSpoolSelected = { spoolId ->
                    assigningToolheadIndex = null
                    onAssignPrinterToolhead(index, spoolId)
                }
            )
        }
    }
}

@Composable
private fun AssignToolheadSpoolDialog(
    toolheadLabel: String,
    spools: List<FilamentSpool>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSpoolSelected: (Int?) -> Unit
) {
    val filteredSpools = filterSpoolmanDropdownFilaments(spools, searchQuery)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 82.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 520.dp),
                shape = SpoolStudioShape.Dialog,
                colors = CardDefaults.cardColors(
                    containerColor = SpoolStudioColors.Graphite
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 18.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Assign spool",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = SpoolStudioColors.OnGraphite
                            )
                            Text(
                                text = toolheadLabel,
                                style = MaterialTheme.typography.bodySmall,
                                color = SpoolStudioColors.GoldSoft
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

                    Text(
                        text = "Manual assignments can be replaced when the printer reads an RFID tag for this toolhead.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SpoolStudioColors.OnGraphiteMuted
                    )

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { onSearchQueryChange(it.take(60)) },
                        placeholder = { Text("Search") },
                        singleLine = true,
                        shape = SpoolStudioShape.Field,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            color = SpoolStudioColors.OnGraphite,
                            fontWeight = FontWeight.SemiBold
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = SpoolStudioColors.OnGraphite,
                            unfocusedTextColor = SpoolStudioColors.OnGraphite,
                            focusedPlaceholderColor = SpoolStudioColors.OnGraphiteMuted,
                            unfocusedPlaceholderColor = SpoolStudioColors.OnGraphiteMuted.copy(alpha = 0.75f),
                            focusedBorderColor = SpoolStudioColors.AccentCyan,
                            unfocusedBorderColor = SpoolStudioColors.GraphiteMuted,
                            cursorColor = SpoolStudioColors.AccentCyan,
                            focusedContainerColor = SpoolStudioColors.GraphiteRaised.copy(alpha = 0.48f),
                            unfocusedContainerColor = SpoolStudioColors.GraphiteRaised.copy(alpha = 0.48f)
                        )
                    )

                    AssignSpoolRow(
                        title = "Clear manual assignment",
                        subtitle = "No spool assigned",
                        idText = "Empty",
                        accentColor = SpoolStudioColors.OnGraphiteMuted.copy(alpha = 0.35f),
                        onClick = { onSpoolSelected(null) }
                    )

                    HorizontalDivider(color = SpoolStudioColors.GraphiteMuted.copy(alpha = 0.75f))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredSpools) { spool ->
                            AssignSpoolRow(
                                title = spool.spoolmanName?.takeIf(String::isNotBlank) ?: spool.displayName,
                                subtitle = spoolmanDropdownListLabel(spool),
                                idText = "ID #${spool.id}",
                                accentColor = resolveSpoolColor(spool.colorHex),
                                onClick = { onSpoolSelected(spool.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AssignSpoolRow(
    title: String,
    subtitle: String,
    idText: String,
    accentColor: Color,
    onClick: () -> Unit
) {
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
            modifier = Modifier.width(54.dp),
            contentAlignment = Alignment.Center
        ) {
            SpoolStudioLogo(
                color = accentColor,
                logoSize = 46.dp,
                showTitle = false
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
                color = SpoolStudioColors.OnGraphite
            )
        }
    }
}
