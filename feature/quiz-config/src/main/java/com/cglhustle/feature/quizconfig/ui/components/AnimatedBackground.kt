package com.cglhustle.feature.quizconfig.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.cglhustle.feature.quizconfig.domain.model.QuizMode

@Composable
fun AnimatedBackground(
    mode: QuizMode,
    scrollState: LazyListState, // Kept to satisfy function signature without breaking dependents
    modifier: Modifier = Modifier
) {
    // Pure clean surface to ensure high contrast, maximum performance, and crisp shimmers.
    val themeBg = MaterialTheme.colorScheme.surface

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(themeBg)
    )
}
