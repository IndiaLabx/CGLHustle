package com.cglhustle.feature.quizconfig.ui.components

import androidx.compose.animation.core.*
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.cglhustle.core.designsystem.theme.AppSpacing
import com.cglhustle.feature.quizconfig.domain.model.QuizMode

@Composable
fun ModeSwitcher(
    selectedMode: QuizMode,
    onModeSelected: (QuizMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(AppSpacing.xs),
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
                activeColor = MaterialTheme.colorScheme.tertiary,
                onClick = { onModeSelected(QuizMode.MOCK) },
                modifier = Modifier.weight(1f)
            )
            GodModeSegment(
                isSelected = selectedMode == QuizMode.GOD_MODE,
                onClick = { onModeSelected(QuizMode.GOD_MODE) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun GodModeSegment(
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeColor = MaterialTheme.colorScheme.error

    // Pulse animation when selected
    val infiniteTransition = rememberInfiniteTransition(label = "god_mode_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isSelected) 1.15f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "god_mode_scale"
    )

    val backgroundColor = if (isSelected) activeColor.copy(alpha = 0.15f) else Color.Transparent
    val contentColor = if (isSelected) activeColor else MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(vertical = AppSpacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier
                .size(24.dp)
                .scale(scale)
        )
        Spacer(modifier = Modifier.height(AppSpacing.xs))
        Text(
            text = "God Mode",
            style = MaterialTheme.typography.labelMedium,
            color = contentColor
        )
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
            .clip(MaterialTheme.shapes.medium)
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(vertical = AppSpacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(AppSpacing.xs))
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = contentColor
        )
    }
}
