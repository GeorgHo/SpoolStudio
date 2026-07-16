package com.spoolstudio.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spoolstudio.app.ui.theme.SpoolStudioColors
import com.spoolstudio.app.ui.theme.SpoolStudioDimens
import com.spoolstudio.app.ui.theme.SpoolStudioShape

@Composable
fun SpoolActionSection(
    primaryActionLabel: String,
    isCreateMode: Boolean,
    isSaveToSpoolmanEnabled: Boolean,
    isWriteTagEnabled: Boolean,
    onReadTag: () -> Unit,
    onSaveToSpoolman: () -> Unit,
    onWriteTag: () -> Unit,
    isNewFromSelectedEnabled: Boolean,
    onCreateNewSpool: () -> Unit,
    onCreateEmptySpool: () -> Unit,
    onOpenPrinterMapping: () -> Unit,
    isDeleteSpoolEnabled: Boolean,
    onDeleteSelectedSpool: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    fun clearThen(action: () -> Unit): () -> Unit = {
        focusManager.clearFocus()
        action()
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = SpoolStudioDimens.ScreenHorizontalPadding, vertical = 0.dp),
        shape = SpoolStudioShape.ScreenPanel,
        colors = CardDefaults.cardColors(containerColor = SpoolStudioColors.ScreenBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 0.dp, vertical = 2.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ReadTagButton(onClick = clearThen(onReadTag))

            if (isCreateMode) {
                SaveToSpoolmanButton(
                    text = "Create in Spoolman",
                    enabled = isSaveToSpoolmanEnabled,
                    onClick = clearThen(onSaveToSpoolman)
                )

                WriteTagButton(
                    enabled = isWriteTagEnabled,
                    onClick = clearThen(onWriteTag)
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SaveToSpoolmanButton(
                        text = spoolmanCompactLabel(primaryActionLabel),
                        enabled = isSaveToSpoolmanEnabled,
                        onClick = clearThen(onSaveToSpoolman),
                        modifier = Modifier.weight(1f)
                    )

                    WriteTagButton(
                        enabled = isWriteTagEnabled,
                        onClick = clearThen(onWriteTag),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SecondaryActionButton(
                        text = "New from selected",
                        onClick = clearThen(onCreateNewSpool),
                        enabled = isNewFromSelectedEnabled,
                        modifier = Modifier.weight(1f)
                    )

                    SecondaryActionButton(
                        text = "New spool",
                        onClick = clearThen(onCreateEmptySpool),
                        enabled = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            PrinterMappingButton(onClick = clearThen(onOpenPrinterMapping))

            DeleteSelectedSpoolButton(
                enabled = isDeleteSpoolEnabled,
                onClick = clearThen(onDeleteSelectedSpool)
            )
        }
    }
}

@Composable
private fun PrimaryActionButton(
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(SpoolStudioDimens.ButtonHeight),
        shape = SpoolStudioShape.Button,
        colors = ButtonDefaults.buttonColors(
            containerColor = SpoolStudioColors.AccentCyan,
            contentColor = Color.White,
            disabledContainerColor = SpoolStudioColors.GraphiteRaised,
            disabledContentColor = SpoolStudioColors.OnGraphiteMuted.copy(alpha = 0.45f)
        )
    ) {
        Text(
            text,
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp, lineHeight = 19.sp),
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun SecondaryActionButton(
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        border = BorderStroke(1.3.dp, if (enabled) SpoolStudioColors.Gold else SpoolStudioColors.GraphiteMuted),
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = SpoolStudioShape.Button,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (enabled) Color.Transparent else SpoolStudioColors.GraphiteRaised.copy(alpha = 0.28f),
            contentColor = SpoolStudioColors.GoldSoft,
            disabledContentColor = SpoolStudioColors.OnGraphiteMuted.copy(alpha = 0.45f)
        )
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp, lineHeight = 16.sp),
            fontWeight = FontWeight.SemiBold,
            maxLines = 2
        )
    }
}

@Composable
fun WriteTagButton(
    enabled: Boolean = true,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    CyanOutlineActionButton(
        text = "Write RFID",
        enabled = enabled,
        onClick = onClick,
        modifier = modifier
    )
}

@Composable
fun ReadTagButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    PrimaryActionButton(
        text = "Read RFID",
        onClick = onClick,
        modifier = modifier
    )
}

@Composable
fun PrinterMappingButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp),
        shape = SpoolStudioShape.Button,
        border = BorderStroke(1.1.dp, SpoolStudioColors.GraphiteMuted),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = SpoolStudioColors.OnGraphite,
            disabledContentColor = SpoolStudioColors.OnGraphiteMuted.copy(alpha = 0.45f)
        )
    ) {
        Text(
            "Printer Mapping",
            style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp, lineHeight = 16.sp),
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
    }
}

@Composable
private fun DeleteSelectedSpoolButton(
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp),
        shape = SpoolStudioShape.Button,
        border = BorderStroke(
            1.1.dp,
            if (enabled) SpoolStudioColors.Error else SpoolStudioColors.GraphiteMuted
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Transparent,
            contentColor = SpoolStudioColors.Error,
            disabledContentColor = SpoolStudioColors.OnGraphiteMuted.copy(alpha = 0.36f)
        )
    ) {
        Text(
            "Delete selected spool",
            style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp, lineHeight = 16.sp),
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
    }
}

@Composable
fun SaveToSpoolmanButton(text: String, enabled: Boolean = true, onClick: () -> Unit) {
    SaveToSpoolmanButton(text = text, enabled = enabled, onClick = onClick, modifier = Modifier)
}

@Composable
fun SaveToSpoolmanButton(
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    CyanOutlineActionButton(
        text = text,
        enabled = enabled,
        onClick = onClick,
        modifier = modifier
    )
}

private fun spoolmanCompactLabel(label: String): String =
    when {
        label.contains("Create", ignoreCase = true) -> "Create"
        label.contains("Update", ignoreCase = true) -> "Update Spoolman"
        label.contains("Write", ignoreCase = true) -> "Update Spoolman"
        else -> "Update Spoolman"
    }

@Composable
private fun CyanOutlineActionButton(
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        border = BorderStroke(
            1.3.dp,
            if (enabled) SpoolStudioColors.AccentCyan else SpoolStudioColors.GraphiteMuted.copy(alpha = 0.7f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(SpoolStudioDimens.ButtonHeight),
        shape = SpoolStudioShape.Button,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (enabled) Color.Transparent else SpoolStudioColors.GraphiteRaised.copy(alpha = 0.28f),
            contentColor = if (enabled) SpoolStudioColors.AccentCyan else SpoolStudioColors.OnGraphiteMuted.copy(alpha = 0.4f),
            disabledContentColor = SpoolStudioColors.OnGraphiteMuted.copy(alpha = 0.4f)
        )
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp, lineHeight = 16.sp),
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
    }
}
