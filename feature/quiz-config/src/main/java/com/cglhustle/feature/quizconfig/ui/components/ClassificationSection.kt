package com.cglhustle.feature.quizconfig.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cglhustle.feature.quizconfig.ui.state.FilterType

@Composable
fun ClassificationSection(
    subjects: List<String>,
    topics: List<String>,
    subTopics: List<String>,
    selectedSubjects: Set<String>,
    selectedTopics: Set<String>,
    selectedSubTopics: Set<String>,
    subjectCounts: Map<String, Int>,
    topicCounts: Map<String, Int>,
    subTopicCounts: Map<String, Int>,
    onSubjectSelected: (String) -> Unit,
    onTopicSelected: (String) -> Unit,
    onSubTopicSelected: (String) -> Unit,
    onSelectAll: (FilterType) -> Unit,
    onClearAll: (FilterType) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ClassificationRow(
            title = "Subject",
            type = FilterType.SUBJECT,
            items = subjects,
            selectedItems = selectedSubjects,
            itemCounts = subjectCounts,
            onItemSelected = onSubjectSelected,
            onSelectAll = onSelectAll,
            onClearAll = onClearAll,
            emptyStateMessage = "No subjects available."
        )

        AnimatedVisibility(
            visible = selectedSubjects.isNotEmpty(),
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            ClassificationRow(
                title = "Topic",
                type = FilterType.TOPIC,
                items = topics,
                selectedItems = selectedTopics,
                itemCounts = topicCounts,
                onItemSelected = onTopicSelected,
                onSelectAll = onSelectAll,
                onClearAll = onClearAll,
                emptyStateMessage = "No topics available for selected subjects."
            )
        }

        AnimatedVisibility(
            visible = selectedTopics.isNotEmpty(),
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            ClassificationRow(
                title = "Sub-Topic",
                type = FilterType.SUB_TOPIC,
                items = subTopics,
                selectedItems = selectedSubTopics,
                itemCounts = subTopicCounts,
                onItemSelected = onSubTopicSelected,
                onSelectAll = onSelectAll,
                onClearAll = onClearAll,
                emptyStateMessage = "No sub-topics available for selected topics."
            )
        }
    }
}

@Composable
private fun ClassificationRow(
    title: String,
    type: FilterType,
    items: List<String>,
    selectedItems: Set<String>,
    itemCounts: Map<String, Int>,
    onItemSelected: (String) -> Unit,
    onSelectAll: (FilterType) -> Unit,
    onClearAll: (FilterType) -> Unit,
    emptyStateMessage: String
) {
    val validItems = items.filter { (itemCounts[it] ?: 0) > 0 || selectedItems.contains(it) }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (validItems.isNotEmpty()) {
                val allSelected = selectedItems.containsAll(validItems) && selectedItems.isNotEmpty()
                Text(
                    text = if (allSelected) "Clear All" else "Select All",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {
                        if (allSelected) onClearAll(type) else onSelectAll(type)
                    }
                )
            }
        }

        if (validItems.isEmpty()) {
            Text(
                text = emptyStateMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(validItems, key = { it }) { item ->
                    val count = itemCounts[item] ?: 0
                    FilterChip(
                        selected = selectedItems.contains(item),
                        onClick = { onItemSelected(item) },
                        label = { Text("$item ($count)") },
                        enabled = count > 0 || selectedItems.contains(item),
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }
        }
    }
}
