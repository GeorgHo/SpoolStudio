package com.spoolstudio.app.utils

import android.graphics.Bitmap
import android.graphics.Color
import kotlin.math.max
import kotlin.math.min

object PhotoColorDetector {

    fun detectAverageHexColor(
        bitmap: Bitmap,
        x: Int,
        y: Int,
        radius: Int = 10
    ): String {
        val startX = max(0, x - radius)
        val endX = min(bitmap.width - 1, x + radius)
        val startY = max(0, y - radius)
        val endY = min(bitmap.height - 1, y + radius)

        var rSum = 0L
        var gSum = 0L
        var bSum = 0L
        var count = 0L

        for (px in startX..endX) {
            for (py in startY..endY) {
                val pixel = bitmap.getPixel(px, py)
                rSum += Color.red(pixel)
                gSum += Color.green(pixel)
                bSum += Color.blue(pixel)
                count++
            }
        }

        if (count == 0L) return "000000"

        val r = (rSum / count).toInt()
        val g = (gSum / count).toInt()
        val b = (bSum / count).toInt()

        return String.format("%02X%02X%02X", r, g, b)
    }
}