package com.cglhustle.feature.quizconfig.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import com.cglhustle.feature.quizconfig.ui.state.FilterType

@Composable
fun AdvancedFiltersSection(
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    selectedExamNames: Set<String>,
    selectedYears: Set<String>,
    selectedShifts: Set<String>,
    selectedTags: Set<String>,
    onOpenFilterSheet: (FilterType) -> Unit,
    onRemoveExamName: (String) -> Unit,
    onRemoveYear: (String) -> Unit,
    onRemoveShift: (String) -> Unit,
    onRemoveTag: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() }
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Advanced Filters",
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Advanced Filters",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )

                val rotation by animateFloatAsState(
                    targetValue = if (isExpanded) 180f else 0f,
                    label = "Chevron Rotation"
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    modifier = Modifier.rotate(rotation)
                )
            }

            // Expandable Content
            // Manually handle visibility without AnimatedVisibility which uses exit
            if (isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    FilterRowTrigger(
                        title = "Exam Name",
                        selectedItems = selectedExamNames,
                        onClick = { onOpenFilterSheet(FilterType.EXAM_NAME) },
                        onRemove = onRemoveExamName
                    )
                    FilterRowTrigger(
                        title = "Year",
                        selectedItems = selectedYears,
                        onClick = { onOpenFilterSheet(FilterType.EXAM_YEAR) },
                        onRemove = onRemoveYear
                    )
                    FilterRowTrigger(
                        title = "Shift",
                        selectedItems = selectedShifts,
                        onClick = { onOpenFilterSheet(FilterType.SHIFT) },
                        onRemove = onRemoveShift
                    )
                    FilterRowTrigger(
                        title = "Tags",
                        selectedItems = selectedTags,
                        onClick = { onOpenFilterSheet(FilterType.TAGS) },
                        onRemove = onRemoveTag
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FilterRowTrigger(
    title: String,
    selectedItems: Set<String>,
    onClick: () -> Unit,
    onRemove: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            TextButton(onClick = onClick) {
                Text(if (selectedItems.isEmpty()) "Select" else "Add more")
            }
        }
        if (selectedItems.isNotEmpty()) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                selectedItems.forEach { item ->
                    RemovableChip(
                        label = item,
                        onRemove = { onRemove(item) }
                    )
                }
            }
        }
    }
}
