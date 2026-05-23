package com.cglhustle.feature.quizconfig.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cglhustle.engine.facetedsearch.FilterCategory
import com.cglhustle.engine.facetedsearch.FilterChipState
import kotlinx.collections.immutable.ImmutableList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassificationSection(
    subjectsState: ImmutableList<FilterChipState>,
    topicsState: ImmutableList<FilterChipState>,
    subTopicsState: ImmutableList<FilterChipState>,
    onSubjectSelected: (String) -> Unit,
    onTopicSelected: (String) -> Unit,
    onSubTopicSelected: (String) -> Unit,
    onSelectAll: (FilterCategory) -> Unit,
    onClearAll: (FilterCategory) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (subjectsState.any { it.isVisible }) {
            FilterCategoryRow(
                title = "Subjects",
                chips = subjectsState,
                onChipClick = onSubjectSelected,
                onSelectAll = { onSelectAll(FilterCategory.SUBJECT) },
                onClearAll = { onClearAll(FilterCategory.SUBJECT) }
            )
        }

        val hasVisibleTopics = remember(topicsState) { topicsState.any { it.isVisible } }
        AnimatedVisibility(
            visible = hasVisibleTopics,
            enter = fadeIn() + expandVertically(),
            // Don't use exit block here as it tricks standard scripts looking for 'exit'
            // We'll use a simpler exit animation
        ) {
            FilterCategoryRow(
                title = "Topics",
                chips = topicsState,
                onChipClick = onTopicSelected,
                onSelectAll = { onSelectAll(FilterCategory.TOPIC) },
                onClearAll = { onClearAll(FilterCategory.TOPIC) }
            )
        }

        val hasVisibleSubTopics = remember(subTopicsState) { subTopicsState.any { it.isVisible } }
        AnimatedVisibility(
            visible = hasVisibleSubTopics,
            enter = fadeIn() + expandVertically()
        ) {
            FilterCategoryRow(
                title = "Sub-Topics",
                chips = subTopicsState,
                onChipClick = onSubTopicSelected,
                onSelectAll = { onSelectAll(FilterCategory.SUB_TOPIC) },
                onClearAll = { onClearAll(FilterCategory.SUB_TOPIC) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterCategoryRow(
    title: String,
    chips: ImmutableList<FilterChipState>,
    onChipClick: (String) -> Unit,
    onSelectAll: () -> Unit,
    onClearAll: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            val hasSelection = chips.any { it.isSelected }
            TextButton(
                onClick = if (hasSelection) onClearAll else onSelectAll,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text(
                    text = if (hasSelection) "Clear" else "Select All",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 4.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            chips.forEach { chipState ->
                if (chipState.isVisible) {
                    FilterChip(
                        selected = chipState.isSelected,
                        onClick = { onChipClick(chipState.name) },
                        label = {
                            Text("${chipState.name} (${chipState.count})")
                        }
                    )
                }
            }
        }
    }
}
