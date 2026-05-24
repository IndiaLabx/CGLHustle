package com.cglhustle.feature.quizconfig.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import android.util.Log
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cglhustle.core.designsystem.theme.AppSpacing
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
    LaunchedEffect(isExpanded) {
        Log.d("QuizConfigUI", "[DEBUG_RECOMPOSITION] AdvancedFiltersSection recomposed. Expanded: $isExpanded")
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggleExpand)
                    .padding(AppSpacing.lg),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Advanced Filters",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = "Toggle Advanced Filters",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isExpanded) { // [DEBUG] Disabled AnimatedVisibility for layout test
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = AppSpacing.lg, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.lg)
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
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = title, style = MaterialTheme.typography.labelLarge)
            TextButton(
                onClick = onAddClick,
                contentPadding = PaddingValues(horizontal = AppSpacing.sm, vertical = 0.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text("Select")
            }
        }

        if (selectedItems.isNotEmpty()) {
            @OptIn(ExperimentalLayoutApi::class)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
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
