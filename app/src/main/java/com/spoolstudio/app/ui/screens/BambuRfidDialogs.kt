package com.spoolstudio.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.spoolstudio.app.ui.components.NfcDumpViewer
import com.spoolstudio.app.ui.theme.SpoolStudioColors
import com.spoolstudio.app.ui.theme.SpoolStudioShape

@Composable
fun BambuRfidDialogHost(
    showDumpDialog: Boolean,
    dumpText: String,
    showDiffDialog: Boolean,
    diffText: String,
    onDismissDump: () -> Unit,
    onApplyDump: () -> Unit,
    onDismissDiff: () -> Unit,
    onUseExisting: () -> Unit,
    onApplyBambuData: () -> Unit
) {
    BambuRfidDumpDialog(
        visible = showDumpDialog,
        text = dumpText,
        onDismiss = onDismissDump,
        onApply = onApplyDump
    )

    BambuRfidDiffDialog(
        visible = showDiffDialog,
        text = diffText,
        onDismiss = onDismissDiff,
        onUseExisting = onUseExisting,
        onApplyBambuData = onApplyBambuData
    )
}

@Composable
fun BambuRfidDumpDialog(
    visible: Boolean,
    text: String,
    onDismiss: () -> Unit,
    onApply: () -> Unit
) {
    if (!visible) return

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = SpoolStudioShape.Dialog,
        containerColor = SpoolStudioColors.GraphiteRaised,
        titleContentColor = SpoolStudioColors.OnGraphite,
        textContentColor = SpoolStudioColors.OnGraphiteMuted,
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.textButtonColors(contentColor = SpoolStudioColors.GoldSoft)
                ) {
                    Text("Close")
                }
                TextButton(
                    onClick = onApply,
                    colors = ButtonDefaults.textButtonColors(contentColor = SpoolStudioColors.AccentCyan)
                ) {
                    Text("Apply")
                }
            }
        },
        title = {
            Text(
                text = "Bambu RFID Dump",
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            NfcDumpViewer(
                text = text,
                modifier = Modifier.height(320.dp)
            )
        }
    )
}

@Composable
fun BambuRfidDiffDialog(
    visible: Boolean,
    text: String,
    onDismiss: () -> Unit,
    onUseExisting: () -> Unit,
    onApplyBambuData: () -> Unit
) {
    if (!visible) return

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = SpoolStudioShape.Dialog,
        containerColor = SpoolStudioColors.GraphiteRaised,
        titleContentColor = SpoolStudioColors.OnGraphite,
        textContentColor = SpoolStudioColors.OnGraphiteMuted,
        title = {
            Text(
                text = "Differences found",
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            NfcDumpViewer(
                text = text,
                modifier = Modifier.height(260.dp)
            )
        },
        dismissButton = {
            TextButton(
                onClick = onUseExisting,
                colors = ButtonDefaults.textButtonColors(contentColor = SpoolStudioColors.GoldSoft)
            ) {
                Text("Use existing spool")
            }
        },
        confirmButton = {
            TextButton(
                onClick = onApplyBambuData,
                colors = ButtonDefaults.textButtonColors(contentColor = SpoolStudioColors.AccentCyan)
            ) {
                Text("Apply Bambu data")
            }
        }
    )
}
