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


    modifier: Modifier = Modifier
) {
    // Rely completely on the global Theme's background instead of a hardcoded surface.
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    )
}
