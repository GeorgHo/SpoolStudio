package com.spoolstudio.app.ui.components

import com.spoolstudio.app.utils.formatColorName
import com.spoolstudio.app.utils.suggestColorName
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import androidx.compose.ui.focus.onFocusChanged

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorSelector(
    selectedColor: String?,
    colorName: String,
    onColorSelected: (String?) -> Unit,
    onColorNameChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var showColorPicker by remember { mutableStateOf(false) }
    var originalColor by remember { mutableStateOf(selectedColor) }
    var colorNameInput by remember { mutableStateOf(colorName) }

    val commonColors = mapOf(
        "White" to "FFFFFF",
        "Red" to "FF0000",
        "Blue" to "0000FF",
        "Green" to "00FF00",
        "Yellow" to "FFFF00",
        "Orange" to "FFA500",
        "Pink" to "FFC0CB",
        "Black" to "000000"
    )

    val normalizedColor = selectedColor?.uppercase()
    val presetName = normalizedColor?.let { color ->
        commonColors.entries.find { it.value.equals(color, ignoreCase = true) }?.key
    }
    val isPresetColor = presetName != null

    LaunchedEffect(selectedColor) {
        when {
            selectedColor == null -> if (colorName.isNotEmpty()) onColorNameChange("")
            isPresetColor -> if (colorName != presetName) onColorNameChange(presetName!!)
            colorName.isBlank() -> onColorNameChange(suggestColorName(normalizedColor ?: "FFFFFF"))
        }
    }

    LaunchedEffect(colorName) {
        colorNameInput = colorName
    }

    val displayValue = when {
        selectedColor == null -> "No Color"
        isPresetColor -> presetName!!
        colorName.isNotBlank() -> colorName
        else -> suggestColorName(normalizedColor ?: "FFFFFF")
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = displayValue,
                onValueChange = { },
                readOnly = true,
                label = { Text("Color") },
                leadingIcon = {
                    if (selectedColor != null) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color(android.graphics.Color.parseColor("#${normalizedColor}")))
                                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                        )
                    } else {
                        NoColorIcon(size = 24.dp)
                    }
                },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                ),
                shape = RoundedCornerShape(20.dp)
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .zIndex(1f)
                    .clip(RoundedCornerShape(20.dp))
            ) {
                DropdownMenuItem(
                    text = { Text("No Color") },
                    onClick = {
                        onColorSelected(null)
                        onColorNameChange("")
                        expanded = false
                    }
                )

                Divider()

                commonColors.forEach { (name, hex) ->
                    DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(15.dp)
                                        .clip(CircleShape)
                                        .background(Color(android.graphics.Color.parseColor("#$hex")))
                                        .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                                )
                                Text(name)
                            }
                        },
                        onClick = {
                            onColorSelected(hex)
                            onColorNameChange(name)
                            expanded = false
                        }
                    )
                }

                Divider()

                DropdownMenuItem(
                    text = { Text("Color Wheel") },
                    onClick = {
                        originalColor = selectedColor
                        showColorPicker = true
                        expanded = false
                    }
                )
            }
        }

        OutlinedTextField(
            value = colorNameInput,
            onValueChange = { input ->
                colorNameInput = input.take(40)
            },
            label = { Text("Color Name") },
            placeholder = { Text("Editable color name") },
            singleLine = true,
            readOnly = false,
            enabled = true,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    if (!focusState.isFocused) {
                        val formatted = formatColorName(colorNameInput)
                        colorNameInput = formatted
                        onColorNameChange(formatted)
                    }
                },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.None,
                autoCorrectEnabled = false
            ),
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.SemiBold
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            shape = RoundedCornerShape(20.dp)
        )

        OutlinedTextField(
            value = selectedColor ?: "",
            onValueChange = { input ->
                val sanitized = input
                    .trim()
                    .removePrefix("#")
                    .uppercase()
                    .filter { it in "0123456789ABCDEF" }
                    .take(6)

                val normalized = sanitized.ifBlank { null }
                onColorSelected(normalized)
            },
            label = { Text("HEX (optional)") },
            placeholder = { Text("RRGGBB") },
            prefix = { Text("#") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodySmall,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            shape = RoundedCornerShape(16.dp)
        )

        Text(
            text = "Suggested: ${suggestColorName(selectedColor ?: "FFFFFF")}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )


    }

    if (showColorPicker) {
        Dialog(onDismissRequest = { showColorPicker = false }) {
            Card(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(40.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF424242)
                ),
                border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Choose color",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )

                    ColorWheel(
                        selectedColor = selectedColor ?: "FFFFFF",
                        onColorSelected = { color ->
                            onColorSelected(color)
                            onColorNameChange(suggestColorName(color))
                        }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                onColorSelected(originalColor)

                                val selectedOriginalColor = originalColor

                                if (selectedOriginalColor.isNullOrBlank()) {
                                    onColorNameChange("")
                                } else {
                                    val originalPreset = commonColors.entries.find {
                                        it.value.equals(selectedOriginalColor, ignoreCase = true)
                                    }?.key

                                    onColorNameChange(
                                        originalPreset ?: suggestColorName(selectedOriginalColor)
                                    )
                                }

                                showColorPicker = false
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = { showColorPicker = false },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Done")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NoColorIcon(size: androidx.compose.ui.unit.Dp) {
    val outlineColor = MaterialTheme.colorScheme.outline
    Canvas(
        modifier = Modifier
            .size(size)
    ) {
        drawCircle(color = outlineColor, style = Stroke(1.dp.toPx()))
        drawLine(
            color = outlineColor,
            start = Offset(this.size.width * 0.15f, this.size.height * 0.85f),
            end = Offset(this.size.width * 0.85f, this.size.height * 0.15f),
            strokeWidth = 1.5.dp.toPx()
        )
    }
}

@Composable
private fun ColorWheel(
    selectedColor: String,
    onColorSelected: (String) -> Unit
) {
    var hue by remember { mutableFloatStateOf(0f) }
    var saturation by remember { mutableFloatStateOf(1f) }
    var brightness by remember { mutableFloatStateOf(1f) }

    var redValue by remember { mutableIntStateOf(255) }
    var greenValue by remember { androidx.compose.runtime.mutableIntStateOf(0) }
    var blueValue by remember { androidx.compose.runtime.mutableIntStateOf(0) }

    // Initialize sliders with current selected color
    LaunchedEffect(selectedColor) {
        val currentColor = android.graphics.Color.parseColor("#$selectedColor")
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(currentColor, hsv)
        hue = hsv[0]
        saturation = hsv[1]
        brightness = hsv[2]

        redValue = android.graphics.Color.red(currentColor)
        greenValue = android.graphics.Color.green(currentColor)
        blueValue = android.graphics.Color.blue(currentColor)
    }

    fun updateFromHSV() {
        val color = android.graphics.Color.HSVToColor(floatArrayOf(hue, saturation, brightness))
        val hex = String.format("%06X", 0xFFFFFF and color)
        onColorSelected(hex)
    }

    fun updateFromRGB() {
        val color = android.graphics.Color.rgb(redValue, greenValue, blueValue)
        val hex = String.format("%06X", 0xFFFFFF and color)
        onColorSelected(hex)
    }

    fun updateBrightnessOnly() {
        // Just update brightness without affecting RGB sliders
        val color = android.graphics.Color.HSVToColor(floatArrayOf(hue, saturation, brightness))
        val hex = String.format("%06X", 0xFFFFFF and color)
        onColorSelected(hex)
    }

    fun updateRedOnly() {
        val color = android.graphics.Color.rgb(redValue, greenValue, blueValue)
        val hex = String.format("%06X", 0xFFFFFF and color)
        onColorSelected(hex)
    }

    fun updateGreenOnly() {
        val color = android.graphics.Color.rgb(redValue, greenValue, blueValue)
        val hex = String.format("%06X", 0xFFFFFF and color)
        onColorSelected(hex)
    }

    fun updateBlueOnly() {
        val color = android.graphics.Color.rgb(redValue, greenValue, blueValue)
        val hex = String.format("%06X", 0xFFFFFF and color)
        onColorSelected(hex)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // HSV Color Wheel
        Box(
            modifier = Modifier.size(180.dp), // Smaller wheel
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .size(160.dp) // Smaller visual size
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                val center = Offset(size.width / 2f, size.height / 2f)
                                val deltaOffset = offset - center
                                val distance =
                                    sqrt(deltaOffset.x * deltaOffset.x + deltaOffset.y * deltaOffset.y)
                                val radius = minOf(size.width, size.height) / 2f

                                // Allow selection even outside the visual circle
                                val clampedDistance = distance.coerceAtMost(radius)
                                hue = (atan2(
                                    deltaOffset.y,
                                    deltaOffset.x
                                ) * 180 / PI + 360).toFloat() % 360f
                                saturation = (clampedDistance / radius).coerceIn(0f, 1f)
                                updateFromHSV()
                            }
                        ) { change, _ ->
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val offset = change.position - center
                            val distance = sqrt(offset.x * offset.x + offset.y * offset.y)
                            val radius = minOf(size.width, size.height) / 2f

                            // Allow selection even outside the visual circle
                            val clampedDistance = distance.coerceAtMost(radius)
                            hue = (atan2(offset.y, offset.x) * 180 / PI + 360).toFloat() % 360f
                            saturation = (clampedDistance / radius).coerceIn(0f, 1f)
                            updateFromHSV()
                        }
                    }
            ) {
                val center = Offset(size.width / 2, size.height / 2)
                val radius = minOf(size.width, size.height) / 2

                // Draw HSV wheel
                for (angle in 0..360 step 2) {
                    for (r in 0..radius.toInt() step 4) {
                        val sat = r / radius
                        val color = Color.hsv(angle.toFloat(), sat, brightness)
                        val x = center.x + r * cos(angle * PI / 180).toFloat()
                        val y = center.y + r * sin(angle * PI / 180).toFloat()
                        drawCircle(color, 2.dp.toPx(), Offset(x, y))
                    }
                }

                // Draw selector
                val selectorRadius = saturation * radius
                val selectorX = center.x + selectorRadius * cos(hue * PI / 180).toFloat()
                val selectorY = center.y + selectorRadius * sin(hue * PI / 180).toFloat()

                drawCircle(
                    Color.White,
                    12.dp.toPx(),
                    Offset(selectorX, selectorY),
                    style = Stroke(3.dp.toPx())
                )
                drawCircle(
                    Color(
                        android.graphics.Color.HSVToColor(
                            floatArrayOf(
                                hue,
                                saturation,
                                brightness
                            )
                        )
                    ),
                    8.dp.toPx(),
                    Offset(selectorX, selectorY)
                )
            }
        }

        // Brightness slider
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Brightness", style = MaterialTheme.typography.labelLarge)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(Color.Black, RoundedCornerShape(4.dp))
                )
                Slider(
                    value = brightness,
                    onValueChange = { newValue ->
                        brightness = newValue
                        updateBrightnessOnly()
                    },
                    valueRange = 0f..1f,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .padding(horizontal = 7.dp),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.outline
                    )
                )
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(Color.White, RoundedCornerShape(4.dp))
                        .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
                )
            }
        }
    }
}