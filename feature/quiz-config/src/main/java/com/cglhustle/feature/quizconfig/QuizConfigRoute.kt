package com.cglhustle.feature.quizconfig

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.cglhustle.feature.quizconfig.presentation.QuizConfigScreen

@Composable
fun QuizConfigRoute(onConfigComplete: (String) -> Unit) {
    QuizConfigScreen(onConfigComplete = onConfigComplete)
}
