package com.cglhustle.feature.results

import androidx.compose.runtime.Composable
import com.cglhustle.feature.results.ui.ResultsScreen

@Composable
fun ResultsRoute(onDone: () -> Unit) {
    ResultsScreen(onDone = onDone)
}
