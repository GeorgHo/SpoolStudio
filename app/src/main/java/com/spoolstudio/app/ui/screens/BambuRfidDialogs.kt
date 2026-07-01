package com.spoolstudio.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.spoolstudio.app.ui.components.NfcDumpViewer

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
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onDismiss) {
                    Text("Schliessen")
                }
                TextButton(onClick = onApply) {
                    Text("Uebernehmen")
                }
            }
        },
        title = {
            Text("Bambu RFID Dump")
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
        title = {
            Text("Abweichungen gefunden")
        },
        text = {
            NfcDumpViewer(
                text = text,
                modifier = Modifier.height(260.dp)
            )
        },
        dismissButton = {
            TextButton(onClick = onUseExisting) {
                Text("Vorhandene Spule verwenden")
            }
        },
        confirmButton = {
            TextButton(onClick = onApplyBambuData) {
                Text("Bambu-Daten uebernehmen")
            }
        }
    )
}
