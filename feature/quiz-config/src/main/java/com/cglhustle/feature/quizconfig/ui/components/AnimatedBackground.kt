package com.cglhustle.feature.quizconfig.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import com.cglhustle.feature.quizconfig.domain.model.QuizMode

@Composable
fun AnimatedBackground(
    mode: QuizMode,
    scrollState: LazyListState, // Kept to satisfy function signature without breaking dependents
    modifier: Modifier = Modifier
) {
    val themeBg = MaterialTheme.colorScheme.background
    val themePrimary = MaterialTheme.colorScheme.primary
    val themeSecondary = MaterialTheme.colorScheme.secondary
    val themeTertiary = MaterialTheme.colorScheme.tertiary

    val (primaryColor, secondaryColor) = when (mode) {
        QuizMode.LEARNING -> Pair(themePrimary, themeSecondary)
        QuizMode.MOCK -> Pair(themeSecondary, themeTertiary)
        QuizMode.GOD_MODE -> Pair(themeTertiary, themePrimary)
    }

    val animatedPrimary by animateColorAsState(targetValue = primaryColor, animationSpec = tween(500), label = "primary")
    val animatedSecondary by animateColorAsState(targetValue = secondaryColor, animationSpec = tween(500), label = "secondary")

    // Use extremely lightweight gradient background instead of heavy Gaussian blurs.
    // Greatly cuts down recomposition rendering overhead on the Main Thread.
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        themeBg,
                        animatedPrimary.copy(alpha = 0.05f),
                        animatedSecondary.copy(alpha = 0.05f),
                        themeBg
                    )
                )
            )
    )
}
