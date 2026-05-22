package com.cglhustle.feature.quizconfig.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.cglhustle.feature.quizconfig.domain.model.QuizMode

@Composable
fun AnimatedBackground(
    mode: QuizMode,
    scrollState: LazyListState,
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

    val parallaxOffset by remember {
        derivedStateOf {
            if (scrollState.layoutInfo.totalItemsCount > 0) {
                scrollState.firstVisibleItemScrollOffset / 4
            } else {
                0
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(themeBg)
    ) {
        // Floating Orb 1 (Top Right)
        Box(
            modifier = Modifier
                .size(300.dp)
                .offset { IntOffset(x = 200, y = -100 - parallaxOffset) }
                .blur(radius = 80.dp)
                .background(animatedPrimary.copy(alpha = 0.15f), CircleShape)
        )

        // Floating Orb 2 (Bottom Left)
        Box(
            modifier = Modifier
                .size(400.dp)
                .offset { IntOffset(x = -150, y = 600 - (parallaxOffset / 2)) }
                .blur(radius = 100.dp)
                .background(animatedSecondary.copy(alpha = 0.1f), CircleShape)
        )
    }
}
