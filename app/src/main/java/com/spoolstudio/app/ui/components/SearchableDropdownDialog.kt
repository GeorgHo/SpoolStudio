package com.spoolstudio.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.spoolstudio.app.ui.theme.SpoolStudioColors
import com.spoolstudio.app.ui.theme.SpoolStudioShape

@Composable
fun <T> SearchableDropdownDialog(
    title: String,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    items: List<T>,
    itemLabel: (T) -> String,
    onItemSelected: (T) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    topContent: @Composable ColumnScope.() -> Unit = {},
    showDefaultDivider: Boolean = false
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 22.dp, vertical = 76.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Card(
                modifier = modifier
                    .fillMaxWidth()
                    .heightIn(max = 390.dp),
                shape = SpoolStudioShape.Dialog,
                colors = CardDefaults.cardColors(containerColor = SpoolStudioColors.Surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 18.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontSize = 12.sp,
                            lineHeight = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = SpoolStudioColors.InkMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { onSearchQueryChange(it.take(60)) },
                        placeholder = { Text("Search") },
                        singleLine = true,
                        shape = SpoolStudioShape.Small,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 16.sp,
                            lineHeight = 22.sp
                        )
                    )

                    topContent()

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 270.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        items.forEachIndexed { index, item ->
                            DropdownDialogItem(
                                text = itemLabel(item),
                                onClick = { onItemSelected(item) }
                            )
                            if (showDefaultDivider && searchQuery.isBlank() && index == 0 && items.size > 1) {
                                HorizontalDivider(color = SpoolStudioColors.OutlineSoft)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DropdownDialogItem(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(SpoolStudioColors.Surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = SpoolStudioColors.Ink,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
