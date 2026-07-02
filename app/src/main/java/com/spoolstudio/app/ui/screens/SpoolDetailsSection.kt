package com.spoolstudio.app.ui.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.spoolstudio.app.ui.remainingWeightWarningThreshold

@Composable
fun SpoolDetailsSection(
    lotNr: String,
    remainingWeight: String,
    comment: String,
    showLotNumber: Boolean,
    showCommentField: Boolean,
    isRemainingWeightValid: Boolean,
    onLotNrChange: (String) -> Unit,
    onRemainingWeightChange: (String) -> Unit,
    onCommentChange: (String) -> Unit
) {
    val focusManager = LocalFocusManager.current
    var remainingWeightWasFocused by remember { mutableStateOf(false) }
    val remainingWarningColor = when (remainingWeightWarningThreshold(remainingWeight)) {
        150 -> MaterialTheme.colorScheme.tertiary
        100 -> MaterialTheme.colorScheme.error.copy(alpha = 0.82f)
        50 -> MaterialTheme.colorScheme.error
        else -> null
    }
    val remainingTextColor = remainingWarningColor ?: MaterialTheme.colorScheme.onSurface
    val remainingLabelColor = remainingWarningColor ?: MaterialTheme.colorScheme.onSurfaceVariant

    fun normalizeRemainingWeight() {
        val formatted = formatRemainingWeightInput(remainingWeight) ?: return
        if (formatted != remainingWeight) {
            onRemainingWeightChange(formatted)
        }
    }

    if (showLotNumber) {
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = lotNr,
            onValueChange = { input ->
                if (input.length <= 32) {
                    onLotNrChange(input)
                }
            },
            label = { Text("Lot Number") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(16.dp)
        )
    }

    Spacer(modifier = Modifier.height(12.dp))

    OutlinedTextField(
        value = remainingWeight,
        onValueChange = { input ->
            if (input.length <= 8 && input.all { it.isDigit() || it == '.' || it == ',' }) {
                onRemainingWeightChange(input)
            }
        },
        label = { Text("Remaining filament (g)") },
        placeholder = { Text("1000") },
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged { focusState ->
                if (remainingWeightWasFocused && !focusState.isFocused) {
                    normalizeRemainingWeight()
                }
                remainingWeightWasFocused = focusState.isFocused
            },
        singleLine = true,
        isError = !isRemainingWeightValid,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = remainingTextColor,
            unfocusedTextColor = remainingTextColor,
            focusedLabelColor = remainingLabelColor,
            unfocusedLabelColor = remainingLabelColor,
            focusedBorderColor = remainingWarningColor ?: MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = remainingWarningColor ?: MaterialTheme.colorScheme.outline
        ),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                normalizeRemainingWeight()
                focusManager.clearFocus()
            }
        ),
        shape = RoundedCornerShape(16.dp)
    )

    if (showCommentField) {
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = comment,
            onValueChange = { input ->
                if (input.length <= 120) {
                    onCommentChange(input)
                }
            },
            label = { Text("Comment") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(16.dp)
        )
    }
}
