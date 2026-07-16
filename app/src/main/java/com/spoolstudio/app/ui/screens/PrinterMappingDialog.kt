package com.spoolstudio.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.spoolstudio.app.domain.models.FilamentSpool
import com.spoolstudio.app.ui.theme.SpoolStudioColors
import com.spoolstudio.app.ui.theme.SpoolStudioShape
import com.spoolstudio.app.ui.theme.spoolStudioBackground

@Composable
fun PrinterMappingDialogHost(
    visible: Boolean,
    spools: List<FilamentSpool>,
    isMoonrakerReachable: Boolean,
    isLoadingPrinterMapping: Boolean,
    activeSpoolOutsideMapping: Boolean,
    activePrinterSpoolId: Int?,
    inlineStatusText: String?,
    inlineStatusColor: Color,
    hasPrinterMappingChanges: Boolean,
    toolhead1SpoolId: Int?,
    toolhead2SpoolId: Int?,
    toolhead3SpoolId: Int?,
    toolhead4SpoolId: Int?,
    activeDialogSpoolId: Int?,
    onToolhead1SpoolIdChange: (Int?) -> Unit,
    onToolhead2SpoolIdChange: (Int?) -> Unit,
    onToolhead3SpoolIdChange: (Int?) -> Unit,
    onToolhead4SpoolIdChange: (Int?) -> Unit,
    onActiveDialogSpoolIdChange: (Int?) -> Unit,
    onCancel: () -> Unit,
    onLoadCurrentPrinterMapping: () -> Unit,
    onSavePrinterMapping: (Int?, Int?, Int?, Int?, Int?) -> Unit
) {
    if (!visible) return

    PrinterMappingDialog(
        spools = spools,
        isMoonrakerReachable = isMoonrakerReachable,
        isLoadingPrinterMapping = isLoadingPrinterMapping,
        activeSpoolOutsideMapping = activeSpoolOutsideMapping,
        activePrinterSpoolId = activePrinterSpoolId,
        inlineStatusText = inlineStatusText,
        inlineStatusColor = inlineStatusColor,
        hasPrinterMappingChanges = hasPrinterMappingChanges,
        toolhead1SpoolId = toolhead1SpoolId,
        toolhead2SpoolId = toolhead2SpoolId,
        toolhead3SpoolId = toolhead3SpoolId,
        toolhead4SpoolId = toolhead4SpoolId,
        activeDialogSpoolId = activeDialogSpoolId,
        onToolhead1SpoolIdChange = onToolhead1SpoolIdChange,
        onToolhead2SpoolIdChange = onToolhead2SpoolIdChange,
        onToolhead3SpoolIdChange = onToolhead3SpoolIdChange,
        onToolhead4SpoolIdChange = onToolhead4SpoolIdChange,
        onActiveDialogSpoolIdChange = onActiveDialogSpoolIdChange,
        onCancel = onCancel,
        onLoadCurrentPrinterMapping = onLoadCurrentPrinterMapping,
        onSavePrinterMapping = onSavePrinterMapping
    )
}

@Composable
fun PrinterMappingDialog(
    spools: List<FilamentSpool>,
    isMoonrakerReachable: Boolean,
    isLoadingPrinterMapping: Boolean,
    activeSpoolOutsideMapping: Boolean,
    activePrinterSpoolId: Int?,
    inlineStatusText: String?,
    inlineStatusColor: Color,
    hasPrinterMappingChanges: Boolean,
    toolhead1SpoolId: Int?,
    toolhead2SpoolId: Int?,
    toolhead3SpoolId: Int?,
    toolhead4SpoolId: Int?,
    activeDialogSpoolId: Int?,
    onToolhead1SpoolIdChange: (Int?) -> Unit,
    onToolhead2SpoolIdChange: (Int?) -> Unit,
    onToolhead3SpoolIdChange: (Int?) -> Unit,
    onToolhead4SpoolIdChange: (Int?) -> Unit,
    onActiveDialogSpoolIdChange: (Int?) -> Unit,
    onCancel: () -> Unit,
    onLoadCurrentPrinterMapping: () -> Unit,
    onSavePrinterMapping: (Int?, Int?, Int?, Int?, Int?) -> Unit
) {
    fun clearActiveIfMissing(vararg toolheadIds: Int?) {
        if (activeDialogSpoolId != null && activeDialogSpoolId !in toolheadIds) {
            onActiveDialogSpoolIdChange(null)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .spoolStudioBackground()
    ) {
        Card(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = SpoolStudioShape.Dialog,
            colors = CardDefaults.cardColors(
                containerColor = SpoolStudioColors.Graphite
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Snapmaker U1 Mapping",
                        style = MaterialTheme.typography.headlineSmall,
                        color = SpoolStudioColors.OnGraphite
                    )
                    IconButton(onClick = onCancel) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = SpoolStudioColors.OnGraphite
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = if (isMoonrakerReachable) "Printer connected" else "Printer not reachable",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isMoonrakerReachable) {
                        SpoolStudioColors.GoldSoft
                    } else {
                        SpoolStudioColors.Error
                    }
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
                            text = "Active spool ID $activePrinterSpoolId is not assigned to Toolhead 1-4",
                            style = MaterialTheme.typography.bodySmall,
                            color = SpoolStudioColors.Error
                        )
                    }
                }

                HorizontalDivider(
                    color = SpoolStudioColors.GraphiteMuted.copy(alpha = 0.75f)
                )
                Spacer(modifier = Modifier.height(10.dp))

                MappingRowDropdown(
                    label = "Toolhead 1",
                    spools = spools,
                    selectedSpoolId = toolhead1SpoolId,
                    isActive = activeDialogSpoolId != null && activeDialogSpoolId == toolhead1SpoolId,
                    enabled = !isLoadingPrinterMapping,
                    onSpoolSelected = { selectedId ->
                        onToolhead1SpoolIdChange(selectedId)
                        clearActiveIfMissing(selectedId, toolhead2SpoolId, toolhead3SpoolId, toolhead4SpoolId)
                    },
                    onActiveCheckedChange = { checked ->
                        onActiveDialogSpoolIdChange(if (checked) toolhead1SpoolId else null)
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
                        onToolhead2SpoolIdChange(selectedId)
                        clearActiveIfMissing(toolhead1SpoolId, selectedId, toolhead3SpoolId, toolhead4SpoolId)
                    },
                    onActiveCheckedChange = { checked ->
                        onActiveDialogSpoolIdChange(if (checked) toolhead2SpoolId else null)
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
                        onToolhead3SpoolIdChange(selectedId)
                        clearActiveIfMissing(toolhead1SpoolId, toolhead2SpoolId, selectedId, toolhead4SpoolId)
                    },
                    onActiveCheckedChange = { checked ->
                        onActiveDialogSpoolIdChange(if (checked) toolhead3SpoolId else null)
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
                        onToolhead4SpoolIdChange(selectedId)
                        clearActiveIfMissing(toolhead1SpoolId, toolhead2SpoolId, toolhead3SpoolId, selectedId)
                    },
                    onActiveCheckedChange = { checked ->
                        onActiveDialogSpoolIdChange(if (checked) toolhead4SpoolId else null)
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                if (inlineStatusText != null) {
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
                            text = inlineStatusText,
                            style = MaterialTheme.typography.bodySmall,
                            color = inlineStatusColor
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onLoadCurrentPrinterMapping,
                        enabled = isMoonrakerReachable && !isLoadingPrinterMapping,
                        shape = SpoolStudioShape.Button,
                        modifier = Modifier
                            .weight(1.45f)
                            .height(42.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = SpoolStudioColors.GoldSoft,
                            disabledContentColor = SpoolStudioColors.OnGraphiteMuted.copy(alpha = 0.55f)
                        )
                    ) {
                        Text("Load current", maxLines = 1, overflow = TextOverflow.Ellipsis)
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
                        shape = SpoolStudioShape.Button,
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SpoolStudioColors.AccentCyan,
                            contentColor = Color.White,
                            disabledContainerColor = SpoolStudioColors.GraphiteRaised,
                            disabledContentColor = SpoolStudioColors.OnGraphiteMuted.copy(alpha = 0.55f)
                        )
                    ) {
                        Text("Save", maxLines = 1)
                    }
                }
            }
        }
    }
}
