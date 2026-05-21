package com.cglhustle.feature.quizconfig.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.cglhustle.feature.quizconfig.ui.QuizConfigScreen

@Composable
fun QuizConfigRoute(onConfigComplete: (String) -> Unit) {
    QuizConfigScreen(onConfigComplete = onConfigComplete)
}
