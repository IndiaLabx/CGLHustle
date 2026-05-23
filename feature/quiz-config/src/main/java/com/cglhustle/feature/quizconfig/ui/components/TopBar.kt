package com.cglhustle.feature.quizconfig.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizConfigTopBar(
    availableQuestionCount: Int,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = "Create New Quiz",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "Select from ${"%,d".format(availableQuestionCount)} available questions",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Navigate back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.95f),
            scrolledContainerColor = MaterialTheme.colorScheme.background
        ),
        modifier = modifier
    )
}
