package com.cglhustle.feature.quizconfig.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.cglhustle.core.ui.theme.AppSpacing
import com.cglhustle.feature.quizconfig.domain.model.QuizMode

@Composable
fun QuickStartButtons(
    onQuickStart: (count: Int, mode: QuizMode?) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
    ) {
        item {
            QuickStartChip("Quick 10") { onQuickStart(10, null) }
        }
        item {
            QuickStartChip("Quick 20") { onQuickStart(20, null) }
        }
        item {
            QuickStartChip("Quick 50") { onQuickStart(50, null) }
        }
        item {
            QuickStartChip("Revision Mode") { onQuickStart(30, QuizMode.LEARNING) }
        }
    }
}

@Composable
private fun QuickStartChip(
    label: String,
    onClick: () -> Unit
) {
    AssistChip(
        onClick = onClick,
        label = { Text(label, style = MaterialTheme.typography.labelMedium) },
        shape = MaterialTheme.shapes.small,
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        border = AssistChipDefaults.assistChipBorder(
            borderColor = MaterialTheme.colorScheme.outlineVariant,
            enabled = true
        )
    )
}
