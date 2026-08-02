package com.spoolstudio.app.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.spoolstudio.app.ui.theme.SpoolStudioColors
import com.spoolstudio.app.ui.theme.SpoolStudioShape

@Composable
fun ColorPhotoDialog(
    bitmap: Bitmap,
    detectedHex: String?,
    detectedName: String,
    onPhotoViewSizeChange: (IntSize) -> Unit,
    onPhotoTap: (androidx.compose.ui.geometry.Offset) -> Unit,
    onUseDetectedColor: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .padding(horizontal = 28.dp)
                .fillMaxWidth(),
            shape = SpoolStudioShape.Dialog,
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
            colors = CardDefaults.cardColors(containerColor = SpoolStudioColors.Graphite),
            border = BorderStroke(1.dp, SpoolStudioColors.GraphiteMuted.copy(alpha = 0.85f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Detect color from photo",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SpoolStudioColors.OnGraphite
                )

                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Captured filament photo",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .border(1.dp, SpoolStudioColors.GraphiteMuted.copy(alpha = 0.85f), RoundedCornerShape(20.dp))
                        .onSizeChanged(onPhotoViewSizeChange)
                        .pointerInput(bitmap) {
                            detectTapGestures { offset ->
                                onPhotoTap(offset)
                            }
                        },
                    contentScale = ContentScale.Crop
                )

                detectedHex?.let { hex ->
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
                                .border(1.dp, SpoolStudioColors.GraphiteMuted, RoundedCornerShape(12.dp))
                        )

                        Column {
                            Text(
                                text = "#$hex",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = SpoolStudioColors.OnGraphite
                            )

                            if (detectedName.isNotBlank()) {
                                Text(
                                    text = detectedName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SpoolStudioColors.OnGraphiteMuted
                                )
                            }
                        }
                    }
                }

                Button(
                    onClick = onUseDetectedColor,
                    enabled = detectedHex != null,
                    modifier = Modifier.fillMaxWidth(),
                    shape = SpoolStudioShape.Button,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SpoolStudioColors.AccentCyan,
                        contentColor = Color.White,
                        disabledContainerColor = SpoolStudioColors.GraphiteRaised,
                        disabledContentColor = SpoolStudioColors.OnGraphiteMuted.copy(alpha = 0.55f)
                    )
                ) {
                    Text("Use detected color")
                }

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = SpoolStudioShape.Button,
                    border = BorderStroke(1.dp, SpoolStudioColors.GoldSoft.copy(alpha = 0.72f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = SpoolStudioColors.GoldSoft
                    )
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}
