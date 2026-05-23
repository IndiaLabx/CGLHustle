package com.cglhustle.feature.quizconfig.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cglhustle.engine.facetedsearch.FilterCategory
import com.cglhustle.engine.facetedsearch.FilterChipState
import kotlinx.collections.immutable.ImmutableList

@Composable
fun ActiveFiltersSection(
    subjectsState: ImmutableList<FilterChipState>,
    topicsState: ImmutableList<FilterChipState>,
    subTopicsState: ImmutableList<FilterChipState>,
    difficultiesState: ImmutableList<FilterChipState>,
    onRemoveSubject: (String) -> Unit,
    onRemoveTopic: (String) -> Unit,
    onRemoveSubTopic: (String) -> Unit,
    onRemoveDifficulty: (String) -> Unit,
) {
    val activeSubjects = subjectsState.filter { it.isSelected }
    val activeTopics = topicsState.filter { it.isSelected }
    val activeSubTopics = subTopicsState.filter { it.isSelected }
    val activeDifficulties = difficultiesState.filter { it.isSelected }

    val hasAnyActiveFilters = activeSubjects.isNotEmpty() || activeTopics.isNotEmpty() ||
                             activeSubTopics.isNotEmpty() || activeDifficulties.isNotEmpty()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Active Filters",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (!hasAnyActiveFilters) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No filters selected.\nSelect criteria below to refine questions.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            } else {
                @OptIn(ExperimentalLayoutApi::class)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    activeSubjects.forEach { chip ->
                        RemovableChip(label = chip.name, onRemove = { onRemoveSubject(chip.name) })
                    }
                    activeTopics.forEach { chip ->
                        RemovableChip(label = chip.name, onRemove = { onRemoveTopic(chip.name) })
                    }
                    activeSubTopics.forEach { chip ->
                        RemovableChip(label = chip.name, onRemove = { onRemoveSubTopic(chip.name) })
                    }
                    activeDifficulties.forEach { chip ->
                        RemovableChip(label = chip.name, onRemove = { onRemoveDifficulty(chip.name) })
                    }
                }
            }
        }
    }
}
