package com.spoolstudio.app.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ColorPhotoHelpersTest {
    @Test
    fun mappedBitmapTapCoordinatesMapsCenterTapWithCropScale() {
        val coordinates = mappedBitmapTapCoordinates(
            tapOffset = Offset(50f, 50f),
            viewSize = IntSize(100, 100),
            bitmapWidth = 200,
            bitmapHeight = 100
        )

        assertEquals(BitmapTapCoordinates(100, 50), coordinates)
    }

    @Test
    fun mappedBitmapTapCoordinatesClampsOutsideTapToBitmapBounds() {
        val coordinates = mappedBitmapTapCoordinates(
            tapOffset = Offset(-100f, 500f),
            viewSize = IntSize(100, 100),
            bitmapWidth = 200,
            bitmapHeight = 100
        )

        assertEquals(BitmapTapCoordinates(0, 99), coordinates)
    }

    @Test
    fun mappedBitmapTapCoordinatesReturnsNullForInvalidSizes() {
        assertNull(
            mappedBitmapTapCoordinates(
                tapOffset = Offset.Zero,
                viewSize = IntSize.Zero,
                bitmapWidth = 200,
                bitmapHeight = 100
            )
        )
    }
}
