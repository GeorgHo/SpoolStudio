package com.spoolstudio.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
// import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import com.spoolstudio.app.ui.theme.SoraFontFamily

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign

import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.spoolstudio.app.R
import androidx.compose.ui.unit.Dp

@Composable

fun SpoolStudioLogo(
    color: Color = Color(0xFF2F2A27),
    modifier: Modifier = Modifier,
    logoSize: Dp = 240.dp,
    showTitle: Boolean = true,
    titleOffsetY: Dp = (-18).dp
){
    val animatedFilamentColor = animateColorAsState(
        targetValue = color,
        animationSpec = tween(
            durationMillis = 900,
            easing = FastOutSlowInEasing
        ),
        label = "filamentColor"
    ).value
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Box(
            modifier = Modifier
                .size(logoSize)
                .graphicsLayer {
                    scaleX = 1.14f
                    scaleY = 1.14f
                }
                .offset(y = (-8).dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_spool_base),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )

            Image(
                painter = painterResource(id = R.drawable.ic_filament_overlay),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
                colorFilter = ColorFilter.tint(
                    // color = animatedFilamentColor,
                    color = animatedFilamentColor.copy(alpha = 0.9f),
                    blendMode = BlendMode.SrcIn
                )
            )

            Image(
                painter = painterResource(id = R.drawable.ic_filament_overlay),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
                alpha = 0.14f
            )

        }

        Spacer(modifier = Modifier.height(0.dp))

        /*
        Text(
            text = "Spool Studio",
            fontFamily = SoraFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 25.sp,
            letterSpacing = (-0.2).sp,
            color = Color(0xFF2A2623),
            textAlign = TextAlign.Center,
            modifier = Modifier.offset(y = (-18).dp)
        )*/
        if (showTitle) {
            Text(
                text = "Spool Studio",
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 25.sp,
                letterSpacing = (-0.2).sp,
                color = Color(0xFF2A2623),
                textAlign = TextAlign.Center,
                modifier = Modifier.offset(y = titleOffsetY)
            )
        }
    }
}
