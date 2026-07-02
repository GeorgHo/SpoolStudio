package com.spoolstudio.app.ui.components

import com.spoolstudio.app.utils.formatColorName
import com.spoolstudio.app.utils.suggestColorName
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import androidx.compose.ui.focus.onFocusChanged
import com.spoolstudio.app.utils.normalizeHexColor
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import com.spoolstudio.app.utils.PhotoColorDetector
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.ui.platform.LocalContext
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import android.graphics.Matrix

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

    var photoBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var showPhotoDialog by remember { mutableStateOf(false) }
    var photoViewSize by remember { mutableStateOf(IntSize.Zero) }
    var detectedPhotoHex by remember { mutableStateOf<String?>(null) }
    var detectedPhotoName by remember { mutableStateOf("") }

    val context = LocalContext.current

    val takePhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            photoBitmap = bitmap
                .toSoftwareArgbBitmap()
                .rotate90IfLandscape()
            detectedPhotoHex = null
            detectedPhotoName = ""
            showPhotoDialog = true
        }
    }

    val pickPhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.decodeBitmap(
                    ImageDecoder.createSource(context.contentResolver, uri)
                ) { decoder, _, _ ->
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                }
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            }.toSoftwareArgbBitmap()

            photoBitmap = bitmap
                .toSoftwareArgbBitmap()
                .rotate90IfLandscape()
            detectedPhotoHex = null
            detectedPhotoName = ""
            showPhotoDialog = true
        }
    }

    fun detectColorFromPhotoTap(tapOffset: Offset) {
        val bitmap = photoBitmap ?: return
        val viewWidth = photoViewSize.width.toFloat()
        val viewHeight = photoViewSize.height.toFloat()

        if (viewWidth <= 0f || viewHeight <= 0f) return

        val bitmapWidth = bitmap.width.toFloat()
        val bitmapHeight = bitmap.height.toFloat()

        val scale = maxOf(
            viewWidth / bitmapWidth,
            viewHeight / bitmapHeight
        )

        val scaledWidth = bitmapWidth * scale
        val scaledHeight = bitmapHeight * scale

        val offsetX = (scaledWidth - viewWidth) / 2f
        val offsetY = (scaledHeight - viewHeight) / 2f

        val bitmapX = ((tapOffset.x + offsetX) / scale)
            .toInt()
            .coerceIn(0, bitmap.width - 1)

        val bitmapY = ((tapOffset.y + offsetY) / scale)
            .toInt()
            .coerceIn(0, bitmap.height - 1)

        val detectedHex = PhotoColorDetector.detectAverageHexColor(
            bitmap = bitmap,
            x = bitmapX,
            y = bitmapY,
            radius = 10
        )

        detectedPhotoHex = detectedHex
        detectedPhotoName = suggestColorName(detectedHex)
    }

    val normalizedColor = normalizeHexColor(selectedColor)
    val presetName = presetNameForColor(normalizedColor)
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

    val displayValue = displayNameForSelectedColor(selectedColor, colorName)

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
                        val safeColor = runCatching {
                            val hex = normalizedColor ?: ""
                            require(hex.matches(Regex("^[A-Fa-f0-9]{6}$")))
                            Color(android.graphics.Color.parseColor("#$hex"))
                        }.getOrElse {
                            MaterialTheme.colorScheme.surfaceVariant
                        }

                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(safeColor)
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.outline,
                                    CircleShape
                                )
                        )
                    } else {
                        NoColorIcon(size = 24.dp)
                    }
                },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
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

                HorizontalDivider()

                commonColorPresets.forEach { (name, hex) ->
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

                HorizontalDivider()

                DropdownMenuItem(
                    text = { Text("Color Wheel") },
                    onClick = {
                        originalColor = selectedColor
                        showColorPicker = true
                        expanded = false
                    }
                )

                DropdownMenuItem(
                    text = { Text("Detect from photo") },
                    onClick = {
                        expanded = false
                        takePhotoLauncher.launch(null)
                    }
                )

                DropdownMenuItem(
                    text = { Text("Select photo from gallery") },
                    onClick = {
                        expanded = false
                        pickPhotoLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
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
                onColorSelected(sanitizeHexColorInput(input))
            },
            label = { Text("HEX") },
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

                                onColorNameChange(colorNameForSelectedColor(originalColor))

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

    if (showPhotoDialog && photoBitmap != null) {
        Dialog(onDismissRequest = { showPhotoDialog = false }) {
            Card(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(32.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Detect color from photo",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Image(
                        bitmap = photoBitmap!!.asImageBitmap(),
                        contentDescription = "Captured filament photo",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(320.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .onSizeChanged { photoViewSize = it }
                            .pointerInput(photoBitmap) {
                                detectTapGestures { offset ->
                                    detectColorFromPhotoTap(offset)
                                }
                            },
                        contentScale = ContentScale.Crop
                    )

                    detectedPhotoHex?.let { hex ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(android.graphics.Color.parseColor("#$hex")))
                            )

                            Column {
                                Text(
                                    text = "#$hex",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                if (detectedPhotoName.isNotBlank()) {
                                    Text(
                                        text = detectedPhotoName,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    Button(
                        onClick = {
                            val hex = detectedPhotoHex ?: return@Button

                            if (detectedPhotoName.isNotBlank()) {
                                onColorNameChange(detectedPhotoName)
                            }

                            onColorSelected(hex)
                            showPhotoDialog = false
                        },
                        enabled = detectedPhotoHex != null,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Use detected color")
                    }

                    OutlinedButton(
                        onClick = { showPhotoDialog = false },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}

private fun Bitmap.toSoftwareArgbBitmap(): Bitmap {
    return if (config == Bitmap.Config.ARGB_8888 && !isRecycled) {
        copy(Bitmap.Config.ARGB_8888, false)
    } else {
        copy(Bitmap.Config.ARGB_8888, false)
    }
}

private fun Bitmap.rotate90IfLandscape(): Bitmap {
    if (width <= height) return this

    val matrix = Matrix().apply {
        postRotate(90f)
    }

    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}
