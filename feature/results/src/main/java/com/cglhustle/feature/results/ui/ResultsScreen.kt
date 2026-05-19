package com.cglhustle.feature.results.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cglhustle.feature.results.data.remote.dto.AttemptedQuestionDto
import com.cglhustle.feature.results.data.remote.dto.BookmarkedQuestionDto
import com.cglhustle.feature.results.data.remote.dto.ResultsAnalyticsDto
import com.cglhustle.feature.results.viewmodel.ResultsUiState
import com.cglhustle.feature.results.viewmodel.ResultsViewModel
import com.cglhustle.core.common.error.AppError
import com.cglhustle.core.common.error.UnknownError

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultsScreen(
    onDone: () -> Unit,
    viewModel: ResultsViewModel = hiltViewModel()
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Analytics", "Attempted", "Bookmarks")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Results") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            PrimaryTabRow(
                selectedTabIndex = selectedTabIndex
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = {
                            selectedTabIndex = index
                            when (index) {
                                0 -> viewModel.fetchAnalytics()
                                1 -> viewModel.fetchAttemptedQuestions()
                                2 -> viewModel.fetchBookmarkedQuestions()
                            }
                        },
                        text = { Text(title) }
                    )
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                when (selectedTabIndex) {
                    0 -> AnalyticsContent(viewModel)
                    1 -> AttemptedContent(viewModel)
                    2 -> BookmarksContent(viewModel)
                }
            }
        }
    }
}

@Composable
fun AnalyticsContent(viewModel: ResultsViewModel) {
    val state by viewModel.analyticsState.collectAsState()

    when (val currentState = state) {
        is ResultsUiState.Loading -> SkeletonList()
        is ResultsUiState.Success -> AnalyticsView(currentState.data)
        is ResultsUiState.Error -> ErrorView(getErrorMessage(currentState.error))
        is ResultsUiState.Empty -> EmptyView("No analytics available")
    }
}

@Composable
fun AnalyticsView(data: ResultsAnalyticsDto) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Final Score",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "${data.finalScore} / ${data.totalQuestions}",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Rank: ${data.rank}")
                Text(text = "Time Taken: ${data.timeTakenSeconds}s")
            }
        }
    }
}

@Composable
fun AttemptedContent(viewModel: ResultsViewModel) {
    val state by viewModel.attemptedState.collectAsState()

    when (val currentState = state) {
        is ResultsUiState.Loading -> SkeletonList()
        is ResultsUiState.Success -> AttemptedListView(currentState.data)
        is ResultsUiState.Error -> ErrorView(getErrorMessage(currentState.error))
        is ResultsUiState.Empty -> EmptyView("You haven't attempted any questions yet")
    }
}

@Composable
fun AttemptedListView(questions: List<AttemptedQuestionDto>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
    ) {
        items(questions) { question ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (question.isCorrect) {
                            Icon(Icons.Default.Check, contentDescription = "Correct", tint = Color.Green)
                        } else {
                            Icon(Icons.Default.Close, contentDescription = "Incorrect", tint = Color.Red)
                        }
                        Text(
                            text = question.questionText,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Your Answer: ${question.userAnswer}")
                    if (!question.isCorrect) {
                        Text(text = "Correct Answer: ${question.correctAnswer}", color = Color.Red)
                    }
                }
            }
        }
    }
}

@Composable
fun BookmarksContent(viewModel: ResultsViewModel) {
    val state by viewModel.bookmarksState.collectAsState()

    when (val currentState = state) {
        is ResultsUiState.Loading -> SkeletonList()
        is ResultsUiState.Success -> BookmarksListView(currentState.data)
        is ResultsUiState.Error -> ErrorView(getErrorMessage(currentState.error))
        is ResultsUiState.Empty -> EmptyView("You haven't bookmarked any questions yet")
    }
}

@Composable
fun BookmarksListView(questions: List<BookmarkedQuestionDto>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
    ) {
        items(questions) { question ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = question.questionText,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

@Composable
fun SkeletonList() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        repeat(5) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .padding(bottom = 8.dp)
                    .background(Color.LightGray.copy(alpha = 0.5f))
            )
        }
    }
}

@Composable
fun ErrorView(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = message, color = Color.Red)
    }
}

@Composable
fun EmptyView(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = message, style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
    }
}

fun getErrorMessage(error: AppError): String {
    return when(error) {
        is UnknownError -> error.exception?.message ?: "An unknown error occurred"
        else -> "An error occurred: \${error.telemetryCode}"
    }
}
