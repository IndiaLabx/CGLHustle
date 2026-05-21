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
    selectedSubject: String,
    selectedTopic: String,
    selectedSubTopic: String,
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
            selectedItem = selectedSubject,
            onItemSelected = onSubjectSelected,
            enabled = true
        )

        val topicsEnabled = selectedSubject.isNotEmpty()
        ClassificationRow(
            title = "Topic",
            items = topics,
            selectedItem = selectedTopic,
            onItemSelected = onTopicSelected,
            enabled = topicsEnabled
        )

        val subTopicsEnabled = selectedTopic.isNotEmpty()
        ClassificationRow(
            title = "Sub-Topic",
            items = subTopics,
            selectedItem = selectedSubTopic,
            onItemSelected = onSubTopicSelected,
            enabled = subTopicsEnabled
        )
    }
}

@Composable
private fun ClassificationRow(
    title: String,
    items: List<String>,
    selectedItem: String,
    onItemSelected: (String) -> Unit,
    enabled: Boolean
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        if (items.isEmpty() && enabled) {
            Text(
                text = "No options available",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items) { item ->
                    // Apply glass style through colors mapping where appropriate, using standard FilterChip for semantics
                    FilterChip(
                        selected = item == selectedItem,
                        onClick = { if (enabled) onItemSelected(item) },
                        label = { Text(item) },
                        enabled = enabled,
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), // Glass-like off state
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }
        }
    }
}
