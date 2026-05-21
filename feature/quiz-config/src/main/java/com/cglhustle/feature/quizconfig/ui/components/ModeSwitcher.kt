package com.cglhustle.feature.quizconfig.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.cglhustle.feature.quizconfig.domain.model.QuizMode

@Composable
fun ModeSwitcher(
    selectedMode: QuizMode,
    onModeSelected: (QuizMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ModeSegment(
                title = "Learning",
                icon = Icons.Default.Create,
                isSelected = selectedMode == QuizMode.LEARNING,
                activeColor = MaterialTheme.colorScheme.primary,
                onClick = { onModeSelected(QuizMode.LEARNING) },
                modifier = Modifier.weight(1f)
            )
            ModeSegment(
                title = "Mock",
                icon = Icons.Default.Info,
                isSelected = selectedMode == QuizMode.MOCK,
                activeColor = Color(0xFFF57C00), // Amber/Orange
                onClick = { onModeSelected(QuizMode.MOCK) },
                modifier = Modifier.weight(1f)
            )
            ModeSegment(
                title = "God Mode",
                icon = Icons.Default.Star,
                isSelected = selectedMode == QuizMode.GOD_MODE,
                activeColor = Color(0xFF9C27B0), // Purple glow feel
                onClick = { onModeSelected(QuizMode.GOD_MODE) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ModeSegment(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) activeColor.copy(alpha = 0.15f) else Color.Transparent
    val contentColor = if (isSelected) activeColor else MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = contentColor
        )
    }
}
