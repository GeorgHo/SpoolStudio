package com.spoolstudio.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.spoolstudio.app.domain.models.FilamentSpool
import com.spoolstudio.app.domain.models.displayMaterialWithModifier
import com.spoolstudio.app.ui.components.SpoolStudioLogo
import com.spoolstudio.app.ui.theme.SpoolStudioColors
import com.spoolstudio.app.ui.theme.SpoolStudioShape

@Composable
fun MappingToolheadStatusCard(
    label: String,
    spool: FilamentSpool?,
    showAppComposedData: Boolean = true,
    assignEnabled: Boolean = true,
    onAssignClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val accentColor = resolveSpoolColor(spool?.colorHex)
    val title = spool?.let { it.spoolmanName?.takeIf(String::isNotBlank) ?: it.displayName }
        ?: "No spool assigned"
    val subtitle = spool?.let { mappingSubtitle(it, showAppComposedData) }.orEmpty()
    val idText = spool?.id?.let { "ID #$it" } ?: "Empty"

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(SpoolStudioShape.Field)
            .background(SpoolStudioColors.GraphiteRaised.copy(alpha = 0.72f))
            .border(
                width = 1.dp,
                color = SpoolStudioColors.GraphiteMuted.copy(alpha = 0.9f),
                shape = SpoolStudioShape.Field
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.width(64.dp),
            contentAlignment = Alignment.Center
        ) {
            SpoolStudioLogo(
                color = accentColor,
                logoSize = 58.dp,
                showTitle = false
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = SpoolStudioColors.GoldSoft,
                    maxLines = 1
                )

                Text(
                    text = idText,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (spool != null) SpoolStudioColors.OnGraphite else SpoolStudioColors.OnGraphiteMuted,
                    maxLines = 1
                )
            }

            HorizontalDivider(
                color = SpoolStudioColors.GraphiteMuted.copy(alpha = 0.65f),
                modifier = Modifier.padding(vertical = 2.dp)
            )

            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (spool != null) SpoolStudioColors.OnGraphite else SpoolStudioColors.OnGraphiteMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = subtitle.ifBlank { "Waiting for printer data" },
                style = MaterialTheme.typography.bodySmall,
                color = if (subtitle.isNotBlank()) SpoolStudioColors.OnGraphiteMuted else SpoolStudioColors.OnGraphiteMuted.copy(alpha = 0.72f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Box(
            modifier = Modifier
                .width(32.dp)
                .height(70.dp)
        ) {
            if (spool != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .offset(y = (-12).dp)
                        .size(12.dp)
                        .clip(SpoolStudioShape.Small)
                        .background(accentColor)
                        .border(
                            width = 1.dp,
                            color = SpoolStudioColors.OnGraphite.copy(alpha = 0.65f),
                            shape = SpoolStudioShape.Small
                        )
                )
            } else {
                Spacer(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .offset(y = (-12).dp)
                        .size(12.dp)
                )
            }

            IconButton(
                onClick = onAssignClick,
                enabled = assignEnabled,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .offset(x = 8.dp, y = 12.dp)
                    .size(30.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Assign spool manually",
                    modifier = Modifier.size(22.dp),
                    tint = if (assignEnabled) {
                        SpoolStudioColors.Gold
                    } else {
                        SpoolStudioColors.OnGraphiteMuted.copy(alpha = 0.45f)
                    }
                )
            }
        }
    }
}

private fun mappingSubtitle(spool: FilamentSpool, showAppComposedData: Boolean): String =
    listOf(
        spool.brand,
        if (showAppComposedData) {
            displayMaterialWithModifier(spool.material, spool.materialModifier)
        } else {
            spool.material
        },
        spool.variant.ifBlank { "Basic" }
    ).filterNotNull()
        .filter { it.isNotBlank() }
        .joinToString(" / ")
