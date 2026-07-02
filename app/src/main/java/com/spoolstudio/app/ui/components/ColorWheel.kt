package com.spoolstudio.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun NoColorIcon(size: Dp) {
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
fun ColorWheel(
    selectedColor: String,
    onColorSelected: (String) -> Unit
) {
    var hue by remember { mutableFloatStateOf(0f) }
    var saturation by remember { mutableFloatStateOf(1f) }
    var brightness by remember { mutableFloatStateOf(1f) }

    var redValue by remember { mutableIntStateOf(255) }
    var greenValue by remember { mutableIntStateOf(0) }
    var blueValue by remember { mutableIntStateOf(0) }

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

    fun updateBrightnessOnly() {
        val color = android.graphics.Color.HSVToColor(floatArrayOf(hue, saturation, brightness))
        val hex = String.format("%06X", 0xFFFFFF and color)
        onColorSelected(hex)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier.size(180.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .size(160.dp)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                val center = Offset(size.width / 2f, size.height / 2f)
                                val deltaOffset = offset - center
                                val distance =
                                    sqrt(deltaOffset.x * deltaOffset.x + deltaOffset.y * deltaOffset.y)
                                val radius = minOf(size.width, size.height) / 2f

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

                            val clampedDistance = distance.coerceAtMost(radius)
                            hue = (atan2(offset.y, offset.x) * 180 / PI + 360).toFloat() % 360f
                            saturation = (clampedDistance / radius).coerceIn(0f, 1f)
                            updateFromHSV()
                        }
                    }
            ) {
                val center = Offset(size.width / 2, size.height / 2)
                val radius = minOf(size.width, size.height) / 2

                for (angle in 0..360 step 2) {
                    for (r in 0..radius.toInt() step 4) {
                        val sat = r / radius
                        val color = Color.hsv(angle.toFloat(), sat, brightness)
                        val x = center.x + r * cos(angle * PI / 180).toFloat()
                        val y = center.y + r * sin(angle * PI / 180).toFloat()
                        drawCircle(color, 2.dp.toPx(), Offset(x, y))
                    }
                }

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
