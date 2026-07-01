package com.spoolstudio.app.ui.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

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
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        isError = !isRemainingWeightValid,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
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
