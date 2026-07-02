package com.spoolstudio.app.ui.components

import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize

data class BitmapTapCoordinates(
    val x: Int,
    val y: Int
)

fun mappedBitmapTapCoordinates(
    tapOffset: Offset,
    viewSize: IntSize,
    bitmapWidth: Int,
    bitmapHeight: Int
): BitmapTapCoordinates? {
    val viewWidth = viewSize.width.toFloat()
    val viewHeight = viewSize.height.toFloat()

    if (viewWidth <= 0f || viewHeight <= 0f || bitmapWidth <= 0 || bitmapHeight <= 0) {
        return null
    }

    val bitmapWidthFloat = bitmapWidth.toFloat()
    val bitmapHeightFloat = bitmapHeight.toFloat()

    val scale = maxOf(
        viewWidth / bitmapWidthFloat,
        viewHeight / bitmapHeightFloat
    )

    val scaledWidth = bitmapWidthFloat * scale
    val scaledHeight = bitmapHeightFloat * scale

    val offsetX = (scaledWidth - viewWidth) / 2f
    val offsetY = (scaledHeight - viewHeight) / 2f

    val bitmapX = ((tapOffset.x + offsetX) / scale)
        .toInt()
        .coerceIn(0, bitmapWidth - 1)

    val bitmapY = ((tapOffset.y + offsetY) / scale)
        .toInt()
        .coerceIn(0, bitmapHeight - 1)

    return BitmapTapCoordinates(bitmapX, bitmapY)
}

fun Bitmap.toSoftwareArgbBitmap(): Bitmap {
    return if (config == Bitmap.Config.ARGB_8888 && !isRecycled) {
        copy(Bitmap.Config.ARGB_8888, false)
    } else {
        copy(Bitmap.Config.ARGB_8888, false)
    }
}

fun Bitmap.rotate90IfLandscape(): Bitmap {
    if (width <= height) return this

    val matrix = Matrix().apply {
        postRotate(90f)
    }

    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}
