package com.cglhustle.feature.quizconfig.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun DifficultySelector(
    difficulties: List<String>,
    selectedDifficulties: Set<String>,
    difficultyCounts: Map<String, Int>,
    onDifficultySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Difficulty",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        val validDifficulties = difficulties.filter { (difficultyCounts[it] ?: 0) > 0 || selectedDifficulties.contains(it) }

        if (validDifficulties.isEmpty()) {
            Text(
                text = "No options available",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            return
        }

        GlassSurface(
            shape = RoundedCornerShape(12.dp),
            backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                validDifficulties.forEach { diff ->
                    DifficultySegment(
                        title = diff,
                        count = difficultyCounts[diff] ?: 0,
                        isSelected = selectedDifficulties.contains(diff),
                        onClick = { onDifficultySelected(diff) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun DifficultySegment(
    title: String,
    count: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeColor = when (title.lowercase()) {
        "easy" -> Color(0xFF4CAF50) // Green
        "medium" -> Color(0xFFFFC107) // Amber
        "hard" -> Color(0xFFF44336) // Red
        else -> MaterialTheme.colorScheme.primary
    }

    val backgroundColor = if (isSelected) activeColor else Color.Transparent
    val contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$title ($count)",
            style = MaterialTheme.typography.labelLarge,
            color = contentColor,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
