package com.spoolstudio.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.spoolstudio.app.data.local.TagStorageSettings
import com.spoolstudio.app.data.remote.spoolman.SpoolmanService
import com.spoolstudio.app.ui.theme.SpoolStudioColors
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
fun TagStoragePanel(url: String, onChanged: () -> Unit) {
    val server = normalizeSettingsUrl(url)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var nativeOnly by remember(server) { mutableStateOf(TagStorageSettings.nativeOnly(server)) }
    var busy by remember(server) { mutableStateOf(false) }
    var preview by remember(server) { mutableStateOf<String?>(null) }
    var report by remember(server) { mutableStateOf<String?>(null) }
    var clearLegacy by remember(server) { mutableStateOf(false) }
    val checkboxColors = CheckboxDefaults.colors(
        checkedColor = SpoolStudioColors.AccentCyan,
        uncheckedColor = SpoolStudioColors.OnGraphiteMuted,
        checkmarkColor = SpoolStudioColors.Graphite
    )
    val buttonColors = ButtonDefaults.textButtonColors(
        contentColor = SpoolStudioColors.AccentCyan,
        disabledContentColor = SpoolStudioColors.OnGraphiteMuted.copy(alpha = 0.55f)
    )
    fun runOperation(action: suspend () -> Unit) {
        scope.launch {
            busy = true
            try { action() }
            catch (e: CancellationException) { throw e }
            catch (e: Exception) { report = e.message ?: "Tag operation failed" }
            finally { busy = false }
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("NFC / RFID tag storage", style = MaterialTheme.typography.titleSmall, color = SpoolStudioColors.OnGraphite)
        Text("Native tags take priority. Compatibility mode also writes card_uids for older Paxx12 firmware. Changes apply immediately to this server.",
            style = MaterialTheme.typography.bodySmall, color = SpoolStudioColors.OnGraphiteMuted)
        Text("Paxx12 firmware 2.0.0-paxx12-24 still requires compatibility mode. Leave native-only off and retain legacy links when migrating.",
            style = MaterialTheme.typography.bodySmall, color = SpoolStudioColors.OnGraphiteMuted)
        Row {
            Checkbox(checked = nativeOnly, colors = checkboxColors, enabled = !busy && server.isNotBlank(), onCheckedChange = { value ->
                runOperation {
                    if (value) check(SpoolmanService(server).hasNativeTags()) { "Native tags require Spoolman 0.27.0 or newer." }
                    TagStorageSettings.initialize(context)
                    TagStorageSettings.setNativeOnly(server, value)
                    nativeOnly = value
                    onChanged()
                }
            })
            Text("Native tags only (requires compatible printer firmware)", color = SpoolStudioColors.OnGraphite, modifier = Modifier.padding(top = 12.dp))
        }
        OutlinedButton(colors = ButtonDefaults.outlinedButtonColors(
            contentColor = SpoolStudioColors.AccentCyan,
            disabledContentColor = SpoolStudioColors.OnGraphiteMuted.copy(alpha = 0.55f)
        ), enabled = !busy && server.isNotBlank(), onClick = {
            runOperation {
                clearLegacy = false
                preview = SpoolmanService(server).previewTagMigration()
            }
        }) { Text(if (busy) "Working…" else "Review legacy tag migration") }
    }
    preview?.let { description ->
        AlertDialog(onDismissRequest = { if (!busy) preview = null }, title = { Text("Migrate tag links") },
            containerColor = SpoolStudioColors.Graphite,
            titleContentColor = SpoolStudioColors.OnGraphite,
            textContentColor = SpoolStudioColors.OnGraphiteMuted,
            text = { Column(Modifier.verticalScroll(rememberScrollState())) {
                Text(server)
                Text(description)
                Row { Checkbox(checked = clearLegacy, colors = checkboxColors, enabled = !busy, onCheckedChange = { clearLegacy = it })
                    Text("Clear successfully migrated card_uids (breaks older firmware links)") }
            } },
            confirmButton = { TextButton(colors = buttonColors, enabled = !busy, onClick = {
                runOperation {
                    report = SpoolmanService(server).migrateLegacyTags(clearLegacy).summary
                    preview = null
                    onChanged()
                }
            }) { Text(if (busy) "Migrating…" else "Migrate") } },
            dismissButton = { TextButton(colors = buttonColors, enabled = !busy, onClick = { preview = null }) { Text("Cancel") } })
    }
    report?.let { message ->
        AlertDialog(onDismissRequest = { report = null }, title = { Text("Tag storage result") },
            containerColor = SpoolStudioColors.Graphite,
            titleContentColor = SpoolStudioColors.OnGraphite,
            textContentColor = SpoolStudioColors.OnGraphiteMuted,
            text = { Text(message, modifier = Modifier.verticalScroll(rememberScrollState())) },
            confirmButton = { TextButton(colors = buttonColors, onClick = { report = null }) { Text("Close") } })
    }
}
