package com.cglhustle.feature.quizconfig.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cglhustle.engine.facetedsearch.FilterCategory

@Composable
fun AdvancedFiltersSection(
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    selectedExamNames: Set<String>,
    selectedYears: Set<String>,
    selectedShifts: Set<String>,
    selectedTags: Set<String>,
    onOpenFilterSheet: (FilterCategory) -> Unit,
    onRemoveExamName: (String) -> Unit,
    onRemoveYear: (String) -> Unit,
    onRemoveShift: (String) -> Unit,
    onRemoveTag: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Advanced Filters",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onToggleExpand) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Toggle Advanced Filters"
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    FilterActionRow(
                        title = "Exam Names",
                        selectedItems = selectedExamNames,
                        onAddClick = { onOpenFilterSheet(FilterCategory.EXAM_NAME) },
                        onRemoveItem = onRemoveExamName
                    )

                    FilterActionRow(
                        title = "Years",
                        selectedItems = selectedYears,
                        onAddClick = { onOpenFilterSheet(FilterCategory.EXAM_YEAR) },
                        onRemoveItem = onRemoveYear
                    )

                    FilterActionRow(
                        title = "Shifts",
                        selectedItems = selectedShifts,
                        onAddClick = { onOpenFilterSheet(FilterCategory.SHIFT) },
                        onRemoveItem = onRemoveShift
                    )

                    FilterActionRow(
                        title = "Tags",
                        selectedItems = selectedTags,
                        onAddClick = { onOpenFilterSheet(FilterCategory.TAGS) },
                        onRemoveItem = onRemoveTag
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterActionRow(
    title: String,
    selectedItems: Set<String>,
    onAddClick: () -> Unit,
    onRemoveItem: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = title, style = MaterialTheme.typography.labelLarge)
            TextButton(
                onClick = onAddClick,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text("Select")
            }
        }

        if (selectedItems.isNotEmpty()) {
            @OptIn(ExperimentalLayoutApi::class)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                selectedItems.forEach { item ->
                    RemovableChip(
                        label = item,
                        onRemove = { onRemoveItem(item) }
                    )
                }
            }
        }
    }
}
