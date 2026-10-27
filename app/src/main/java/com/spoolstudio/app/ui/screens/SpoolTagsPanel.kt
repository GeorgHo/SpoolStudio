package com.spoolstudio.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.spoolstudio.app.data.remote.spoolman.SpoolmanService
import com.spoolstudio.app.domain.models.FilamentSpool
import com.spoolstudio.app.ui.theme.SpoolStudioColors
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
fun SpoolTagsPanel(url: String, spool: FilamentSpool, onRefresh: () -> Unit) {
    var expanded by remember(spool.id) { mutableStateOf(false) }
    var pending by remember(spool.id) { mutableStateOf<String?>(null) }
    var busy by remember(spool.id) { mutableStateOf(false) }
    var error by remember(spool.id) { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val buttonColors = ButtonDefaults.textButtonColors(
        contentColor = SpoolStudioColors.AccentCyan,
        disabledContentColor = SpoolStudioColors.OnGraphiteMuted.copy(alpha = 0.55f)
    )
    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        TextButton(colors = buttonColors, onClick = { expanded = !expanded }) { Text(if (expanded) "Hide tag links" else "Manage tag links (${spool.cardUids.size})") }
        if (expanded) {
            spool.cardUids.forEach { uid ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("$uid · ${if (uid in spool.nativeTagUids) "native" else "legacy"}", color = SpoolStudioColors.OnGraphiteMuted, modifier = Modifier.weight(1f).padding(top = 12.dp))
                    TextButton(colors = buttonColors, enabled = !busy, onClick = { pending = uid }) { Text("Unlink") }
                }
            }
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        }
    }
    pending?.let { uid ->
        AlertDialog(onDismissRequest = { if (!busy) pending = null }, title = { Text("Unlink tag?") },
            containerColor = SpoolStudioColors.Graphite,
            titleContentColor = SpoolStudioColors.OnGraphite,
            textContentColor = SpoolStudioColors.OnGraphiteMuted,
            text = { Text("Remove $uid from spool #${spool.id} in both native and compatibility storage. This does not clear the printer's loaded-spool assignment. The physical tag is unchanged.") },
            confirmButton = { TextButton(colors = buttonColors, enabled = !busy, onClick = {
                scope.launch {
                    busy = true
                    try {
                        SpoolmanService(url).unlinkCardUid(spool.id ?: error("Missing spool ID"), uid)
                        error = null
                    } catch (e: CancellationException) { throw e
                    } catch (e: Exception) { error = "Unlink incomplete: ${e.message}. Refresh and check remaining links." }
                    finally { busy = false; pending = null; onRefresh() }
                }
            }) { Text(if (busy) "Unlinking…" else "Unlink") } },
            dismissButton = { TextButton(colors = buttonColors, enabled = !busy, onClick = { pending = null }) { Text("Cancel") } })
    }
}
