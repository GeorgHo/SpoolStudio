package com.spoolstudio.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
    showDefaultDivider: Boolean = false,
    itemContent: (@Composable (T) -> Unit)? = null
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
                    .heightIn(max = 430.dp),
                shape = SpoolStudioShape.Dialog,
                colors = CardDefaults.cardColors(containerColor = SpoolStudioColors.Graphite),
                elevation = CardDefaults.cardElevation(defaultElevation = 18.dp),
                border = BorderStroke(1.dp, SpoolStudioColors.GraphiteMuted.copy(alpha = 0.85f))
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 18.sp,
                            lineHeight = 22.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = SpoolStudioColors.OnGraphite,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { onSearchQueryChange(it.take(60)) },
                        placeholder = { Text("Search") },
                        singleLine = true,
                        shape = SpoolStudioShape.Field,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 16.sp,
                            lineHeight = 22.sp
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = SpoolStudioColors.OnGraphite,
                            unfocusedTextColor = SpoolStudioColors.OnGraphite,
                            focusedPlaceholderColor = SpoolStudioColors.OnGraphiteMuted,
                            unfocusedPlaceholderColor = SpoolStudioColors.OnGraphiteMuted.copy(alpha = 0.75f),
                            focusedBorderColor = SpoolStudioColors.AccentCyan,
                            unfocusedBorderColor = SpoolStudioColors.GraphiteMuted,
                            cursorColor = SpoolStudioColors.AccentCyan,
                            focusedContainerColor = SpoolStudioColors.GraphiteRaised.copy(alpha = 0.48f),
                            unfocusedContainerColor = SpoolStudioColors.GraphiteRaised.copy(alpha = 0.48f)
                        )
                    )

                    topContent()

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(items) { index, item ->
                            Column {
                                if (itemContent != null) {
                                    itemContent(item)
                                } else {
                                    DropdownDialogItem(
                                        text = itemLabel(item),
                                        onClick = { onItemSelected(item) }
                                    )
                                }
                                if (showDefaultDivider && searchQuery.isBlank() && index == 0 && items.size > 1) {
                                    HorizontalDivider(
                                        color = SpoolStudioColors.GraphiteMuted.copy(alpha = 0.75f),
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                }
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
            .height(52.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(SpoolStudioColors.GraphiteRaised.copy(alpha = 0.72f))
            .clickable(onClick = onClick)
            .border(1.dp, SpoolStudioColors.GraphiteMuted.copy(alpha = 0.85f), RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = SpoolStudioColors.OnGraphite,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
