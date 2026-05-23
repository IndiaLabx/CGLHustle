package com.cglhustle.feature.quizconfig.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cglhustle.core.ui.theme.AppSpacing
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.CardPadding),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.lg)
        ) {
            val subjectsSelected = remember(subjectsState) { subjectsState.any { it.isSelected } }
            val topicsSelected = remember(topicsState) { topicsState.any { it.isSelected } }

            // SUBJECTS
            FilterCategoryRow(
                title = "Subjects",
                infoText = "Select broad academic domains",
                chips = subjectsState,
                onChipClick = onSubjectSelected,
                onSelectAll = { onSelectAll(FilterCategory.SUBJECT) },
                onClearAll = { onClearAll(FilterCategory.SUBJECT) }
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

            // TOPICS
            FilterCategoryRow(
                title = "Topics",
                infoText = "Select specific topics within the chosen subjects",
                chips = topicsState,
                onChipClick = onTopicSelected,
                onSelectAll = { onSelectAll(FilterCategory.TOPIC) },
                onClearAll = { onClearAll(FilterCategory.TOPIC) }
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

            // SUB-TOPICS
            FilterCategoryRow(
                title = "Sub-Topics",
                infoText = "Drill down into highly specific sub-topics",
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
    infoText: String,
    chips: ImmutableList<FilterChipState>,
    onChipClick: (String) -> Unit,
    onSelectAll: () -> Unit,
    onClearAll: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = AppSpacing.sm),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                TooltipBox(
                    positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                    tooltip = { PlainTooltip { Text(infoText) } },
                    state = rememberTooltipState()
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = "Info about $title",
                        modifier = Modifier
                            .padding(start = AppSpacing.sm)
                            .size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            val hasSelection = chips.any { it.isSelected }
            TextButton(
                onClick = if (hasSelection) onClearAll else onSelectAll,
                contentPadding = PaddingValues(horizontal = AppSpacing.sm, vertical = 0.dp),
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
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            chips.forEach { chipState ->
                FilterChip(
                    selected = chipState.isSelected,
                    onClick = { onChipClick(chipState.name) },
                    label = {
                        Text("${chipState.name} (${chipState.count})")
                    },
                    shape = MaterialTheme.shapes.small,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = MaterialTheme.colorScheme.outlineVariant,
                        enabled = true,
                        selected = chipState.isSelected
                    )
                )
            }
        }
    }
}
