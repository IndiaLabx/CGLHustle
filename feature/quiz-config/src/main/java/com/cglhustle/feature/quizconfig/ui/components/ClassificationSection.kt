package com.cglhustle.feature.quizconfig.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

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
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ClassificationRow(
            title = "Subject",
            items = subjects,
            selectedItems = selectedSubjects,
            itemCounts = subjectCounts,
            onItemSelected = onSubjectSelected,
            enabled = true
        )

        ClassificationRow(
            title = "Topic",
            items = topics,
            selectedItems = selectedTopics,
            itemCounts = topicCounts,
            onItemSelected = onTopicSelected,
            enabled = selectedSubjects.isNotEmpty()
        )

        ClassificationRow(
            title = "Sub-Topic",
            items = subTopics,
            selectedItems = selectedSubTopics,
            itemCounts = subTopicCounts,
            onItemSelected = onSubTopicSelected,
            enabled = selectedTopics.isNotEmpty()
        )
    }
}

@Composable
private fun ClassificationRow(
    title: String,
    items: List<String>,
    selectedItems: Set<String>,
    itemCounts: Map<String, Int>,
    onItemSelected: (String) -> Unit,
    enabled: Boolean
) {
    // Only show items that have > 0 results available if we were to click them
    val validItems = items.filter { (itemCounts[it] ?: 0) > 0 || selectedItems.contains(it) }

    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        if (validItems.isEmpty() && enabled) {
            Text(
                text = "No options available",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(validItems) { item ->
                    val count = itemCounts[item] ?: 0
                    FilterChip(
                        selected = selectedItems.contains(item),
                        onClick = { if (enabled) onItemSelected(item) },
                        label = { Text("$item ($count)") },
                        enabled = enabled && count > 0,
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
