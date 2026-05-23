package com.cglhustle.feature.quizconfig.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.cglhustle.core.ui.theme.AppSpacing

@Composable
fun ShimmerSkeleton(
    modifier: Modifier = Modifier
) {
    val shimmerBase = MaterialTheme.colorScheme.surfaceVariant

    val shimmerColors = listOf(
        shimmerBase.copy(alpha = 0.6f),
        shimmerBase.copy(alpha = 0.2f),
        shimmerBase.copy(alpha = 0.6f)
    )

    val transition = rememberInfiniteTransition(label = "shimmer")

    val translateAnim = transition.animateFloat(
        initialValue = -500f,
        targetValue = 2000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translation"
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(x = translateAnim.value - 500f, y = translateAnim.value - 500f),
        end = Offset(x = translateAnim.value, y = translateAnim.value)
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xl)
    ) {
        // Mode Switcher Skeleton
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .clip(MaterialTheme.shapes.large)
                .background(brush)
        )

        // Quick Start Chips Skeleton
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            repeat(4) {
                Box(
                    modifier = Modifier
                        .height(32.dp)
                        .width(80.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(brush)
                )
            }
        }

        // Active Filters Skeleton (Optional, small)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .clip(MaterialTheme.shapes.medium)
                .background(brush)
        )

        // Classification Card Skeleton
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp) // Approximate height of the classification card
                .clip(MaterialTheme.shapes.medium)
                .background(brush)
        )

        // Difficulty Card Skeleton
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .clip(MaterialTheme.shapes.medium)
                .background(brush)
        )

        // Advanced Filters Skeleton
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .clip(MaterialTheme.shapes.medium)
                .background(brush)
        )
    }
}
