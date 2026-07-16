package com.spoolstudio.app.ui.components

import com.spoolstudio.app.utils.formatColorName
import com.spoolstudio.app.utils.suggestColorName
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import androidx.compose.ui.focus.onFocusChanged
import com.spoolstudio.app.utils.normalizeHexColor
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.unit.IntSize
import com.spoolstudio.app.utils.PhotoColorDetector
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.ui.platform.LocalContext
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import com.spoolstudio.app.ui.theme.SpoolStudioColors
import com.spoolstudio.app.ui.theme.SpoolStudioShape
import kotlinx.coroutines.delay

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
        val coordinates = mappedBitmapTapCoordinates(
            tapOffset = tapOffset,
            viewSize = photoViewSize,
            bitmapWidth = bitmap.width,
            bitmapHeight = bitmap.height
        ) ?: return

        val detectedHex = PhotoColorDetector.detectAverageHexColor(
            bitmap = bitmap,
            x = coordinates.x,
            y = coordinates.y,
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

    val displayValue = displayNameForSelectedColor(selectedColor, colorName)

    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
        Box(modifier = Modifier.fillMaxWidth()) {
            ColorSelectorRow(
                label = "Color",
                value = displayValue,
                leading = {
                    ColorSwatch(hex = normalizedColor)
                },
                trailing = {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = SpoolStudioColors.InkMuted
                    )
                },
                onClick = { expanded = true }
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .zIndex(1f)
                    .clip(SpoolStudioShape.Small)
                    .background(SpoolStudioColors.Surface)
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
    }

    if (showColorPicker) {
        Dialog(
            onDismissRequest = { showColorPicker = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp),
                shape = SpoolStudioShape.Dialog,
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
                            shape = SpoolStudioShape.Button
                        ) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = { showColorPicker = false },
                            modifier = Modifier.weight(1f),
                            shape = SpoolStudioShape.Button
                        ) {
                            Text("Done")
                        }
                    }
                }
            }
        }
    }

    val currentPhotoBitmap = photoBitmap
    if (showPhotoDialog && currentPhotoBitmap != null) {
        ColorPhotoDialog(
            bitmap = currentPhotoBitmap,
            detectedHex = detectedPhotoHex,
            detectedName = detectedPhotoName,
            onPhotoViewSizeChange = { photoViewSize = it },
            onPhotoTap = { detectColorFromPhotoTap(it) },
            onUseDetectedColor = {
                val hex = detectedPhotoHex ?: return@ColorPhotoDialog

                if (detectedPhotoName.isNotBlank()) {
                    onColorNameChange(detectedPhotoName)
                }

                onColorSelected(hex)
                showPhotoDialog = false
            },
            onDismiss = { showPhotoDialog = false }
        )
            }
}

@Composable
private fun ColorSelectorRow(
    label: String,
    value: String,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = SpoolStudioColors.Ink,
                modifier = Modifier.weight(1f)
            )
            if (leading != null) {
                leading()
            }
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = SpoolStudioColors.Ink,
                maxLines = 1,
                modifier = Modifier.weight(1.25f)
            )
            if (trailing != null) {
                Box(
                    modifier = Modifier.size(22.dp),
                    contentAlignment = Alignment.Center
                ) {
                    trailing()
                }
            }
        }
        HorizontalDivider(color = SpoolStudioColors.InkMuted.copy(alpha = 0.18f))
    }
}

@Composable
private fun InlineColorTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    prefix: String = "",
    placeholder: String = "",
    onFocusLost: (() -> Unit)? = null
) {
    var fieldValue by remember { mutableStateOf(TextFieldValue(value)) }
    var isFocused by remember { mutableStateOf(false) }
    LaunchedEffect(value) {
        if (value != fieldValue.text) {
            fieldValue = TextFieldValue(value, selection = TextRange(value.length))
        }
    }
    LaunchedEffect(isFocused) {
        if (isFocused && fieldValue.text.isNotEmpty()) {
            delay(90)
            fieldValue = fieldValue.copy(selection = TextRange(0, fieldValue.text.length))
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = SpoolStudioColors.Ink,
            modifier = Modifier.weight(1f)
        )
        Row(
            modifier = Modifier.weight(1.45f),
            horizontalArrangement = Arrangement.spacedBy(0.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (prefix.isNotBlank()) {
                Text(
                    text = prefix,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 15.sp,
                        lineHeight = 19.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = SpoolStudioColors.InkMuted
                )
            }
            BasicTextField(
            value = fieldValue,
            onValueChange = {
                fieldValue = it
                onValueChange(it.text)
            },
            singleLine = true,
            modifier = Modifier
                .weight(1f)
                .pointerInput(fieldValue.text) {
                    detectTapGestures(
                        onTap = {
                            fieldValue = fieldValue.copy(selection = TextRange(0, fieldValue.text.length))
                        }
                    )
                }
                .then(
                    if (onFocusLost != null) {
                        Modifier.onFocusChanged { focusState ->
                            if (!focusState.isFocused) {
                                onFocusLost()
                            }
                            isFocused = focusState.isFocused
                        }
                    } else {
                        Modifier.onFocusChanged { focusState ->
                            isFocused = focusState.isFocused
                        }
                    }
                ),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.None,
                autoCorrectEnabled = false
            ),
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 15.sp,
                lineHeight = 19.sp,
                fontWeight = FontWeight.SemiBold,
                color = SpoolStudioColors.Ink
            ),
            decorationBox = { innerTextField ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (fieldValue.text.isBlank() && placeholder.isNotBlank()) {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 15.sp,
                                lineHeight = 19.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = SpoolStudioColors.InkMuted
                        )
                    }
                    innerTextField()
                }
            }
            )
        }
    }
    HorizontalDivider(color = SpoolStudioColors.InkMuted.copy(alpha = 0.18f))
}

@Composable
private fun ColorSwatch(hex: String?) {
    if (!hex.isNullOrBlank()) {
        val safeColor = runCatching {
            require(hex.matches(Regex("^[A-Fa-f0-9]{6}$")))
            Color(android.graphics.Color.parseColor("#$hex"))
        }.getOrElse {
            SpoolStudioColors.SurfaceMuted
        }
        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(CircleShape)
                .background(safeColor)
                .border(1.dp, SpoolStudioColors.InkMuted, CircleShape)
        )
    } else {
        NoColorIcon(size = 18.dp)
    }
}
