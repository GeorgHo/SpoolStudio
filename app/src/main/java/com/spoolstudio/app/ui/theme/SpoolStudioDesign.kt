package com.spoolstudio.app.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object SpoolStudioColors {
    val AppBackground = Color(0xFFF1E8DF)
    val ScreenBackground = Color(0xFF12262E)
    val ScreenBackgroundDeep = Color(0xFF0A171D)
    val ScreenGlowTop = Color(0xFF2D5964)
    val ScreenGlowBottom = Color(0xFF2B2416)
    val Surface = Color(0xFFFFF8FF)
    val SurfaceRaised = Color(0xFFF8F1F8)
    val SurfaceMuted = Color(0xFFE9E1E9)
    val Graphite = Color(0xFF24242B)
    val GraphiteRaised = Color(0xFF303039)
    val GraphiteMuted = Color(0xFF4A4652)
    val OnGraphite = Color(0xFFF7F2F7)
    val OnGraphiteMuted = Color(0xFFCFC7D3)
    val Ink = Color(0xFF1D1B20)
    val InkMuted = Color(0xFF625B66)
    val Outline = Color(0xFF817983)
    val OutlineSoft = Color(0xFFD4CAD4)
    val Gold = Color(0xFFC28F00)
    val GoldDark = Color(0xFF8C6700)
    val GoldSoft = Color(0xFFE7CF98)
    val AccentCyan = Color(0xFF16AFCF)
    val Error = Color(0xFFC64B4B)
}

object SpoolStudioShape {
    val ScreenPanel = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp, bottomStart = 18.dp, bottomEnd = 18.dp)
    val Field = RoundedCornerShape(16.dp)
    val Button = RoundedCornerShape(15.dp)
    val Dialog = RoundedCornerShape(22.dp)
    val Small = RoundedCornerShape(8.dp)
}

object SpoolStudioDimens {
    val FieldHeight = 58.dp
    val ButtonHeight = 48.dp
    val ScreenHorizontalPadding = 12.dp
}

fun Modifier.spoolStudioBackground(): Modifier = this
    .background(
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF111820),
                Color(0xFF15313A),
                Color(0xFF102B34),
                Color(0xFF0A171D)
            )
        )
    )
    .background(
        Brush.radialGradient(
            colors = listOf(
                SpoolStudioColors.ScreenGlowTop.copy(alpha = 0.52f),
                Color.Transparent
            ),
            center = Offset(120f, 120f),
            radius = 760f
        )
    )
    .background(
        Brush.radialGradient(
            colors = listOf(
                SpoolStudioColors.AccentCyan.copy(alpha = 0.16f),
                Color.Transparent
            ),
            center = Offset(760f, 520f),
            radius = 720f
        )
    )
    .background(
        Brush.radialGradient(
            colors = listOf(
                SpoolStudioColors.Gold.copy(alpha = 0.10f),
                Color.Transparent
            ),
            center = Offset(180f, 1600f),
            radius = 900f
        )
    )
