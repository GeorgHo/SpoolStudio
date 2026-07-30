package com.spoolstudio.app.ui.screens

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    printerIntegrationModeLabel: String?,
    inlineStatusText: String?,
    inlineStatusColor: Color,
    toolhead1SpoolId: Int?,
    toolhead2SpoolId: Int?,
    toolhead3SpoolId: Int?,
    toolhead4SpoolId: Int?,
    onCancel: () -> Unit,
    onLoadCurrentPrinterMapping: () -> Unit
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
        onLoadCurrentPrinterMapping = onLoadCurrentPrinterMapping
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
    onLoadCurrentPrinterMapping: () -> Unit
) {
    var showAppComposedData by remember { mutableStateOf(true) }
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
                        MappingToolheadStatusCard(
                            label = label,
                            spool = spool,
                            showAppComposedData = showAppComposedData
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
    }
}
