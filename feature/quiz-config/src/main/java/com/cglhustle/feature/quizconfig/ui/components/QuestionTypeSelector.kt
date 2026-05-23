package com.cglhustle.feature.quizconfig.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cglhustle.core.ui.theme.AppSpacing

@Composable
fun QuestionTypeSelector(
    modifier: Modifier = Modifier
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
            modifier = modifier
                .fillMaxWidth()
                .padding(AppSpacing.CardPadding)
        ) {
            Text(
                text = "Question Type",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = AppSpacing.sm)
            )
            // Hardcoded to MCQ for Phase 1 as per requirements, but visibly presented to signal future options
            FilterChip(
                selected = true,
                onClick = { },
                label = { Text("MCQ") },
                enabled = true,
                shape = MaterialTheme.shapes.small,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = MaterialTheme.colorScheme.outlineVariant,
                    enabled = true,
                    selected = true
                )
            )
        }
    }
}
