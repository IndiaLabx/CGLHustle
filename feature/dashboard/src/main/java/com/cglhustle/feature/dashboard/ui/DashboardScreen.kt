package com.cglhustle.feature.dashboard.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.cglhustle.core.designsystem.theme.AppSpacing
import androidx.hilt.navigation.compose.hiltViewModel
import com.cglhustle.feature.dashboard.components.DashboardCard
import com.cglhustle.feature.dashboard.model.DashboardCardModel
import com.cglhustle.feature.dashboard.viewmodel.DashboardEvent
import com.cglhustle.feature.dashboard.viewmodel.DashboardViewModel
import kotlinx.coroutines.flow.collect
import java.time.LocalTime

@Composable
fun DashboardRoute(
    onNavigateToMcq: () -> Unit,
    modifier: Modifier = Modifier,
    onThemeToggle: () -> Unit = {},
    isDarkMode: Boolean = false,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isAdmin = viewModel.isAdmin()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is DashboardEvent.NavigateTo -> {
                    when (event.route) {
                        "mcqs" -> onNavigateToMcq()
                        else -> snackbarHostState.showSnackbar("\${event.route} is coming soon ✨")
                    }
                }
                is DashboardEvent.OpenExternalLink -> {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(event.url))
                    context.startActivity(intent)
                }
                is DashboardEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    DashboardScreen(
        userName = uiState.userName,
        isAdmin = isAdmin,
        snackbarHostState = snackbarHostState,
        onActionClick = viewModel::onActionClick,
        onThemeToggle = onThemeToggle,
        isDarkMode = isDarkMode,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    userName: String,
    isAdmin: Boolean,
    snackbarHostState: SnackbarHostState,
    onActionClick: (String?) -> Unit,
    onThemeToggle: () -> Unit,
    isDarkMode: Boolean,
    modifier: Modifier = Modifier
) {
    val greeting = remember(userName) { getGreeting(userName) }

    val cards = remember(isAdmin) {
        mutableListOf(
            DashboardCardModel("MCQs Quiz", "Practice and learn", Icons.Rounded.Psychology, "mcqs"),
            DashboardCardModel("English Zone", "Master the language", Icons.AutoMirrored.Rounded.MenuBook, "english"),
            DashboardCardModel("Tools", "Utilities & calculators", Icons.Rounded.Build, "tools"),
            DashboardCardModel("Analytics", "Track your progress", Icons.Rounded.Analytics, "analytics"),
            DashboardCardModel("Bookmarks", "Saved questions", Icons.Rounded.Bookmarks, "bookmarks")
        ).apply {
            if (isAdmin) {
                add(DashboardCardModel("Admin Room", "Manage content", Icons.Rounded.AdminPanelSettings, "admin"))
            }
            add(DashboardCardModel("Download", "Get offline resources", Icons.Rounded.Download, null))
            add(DashboardCardModel("About Us", "Our story", Icons.Rounded.Info, "about"))
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = AppSpacing.lg,
                        end = AppSpacing.lg,
                        top = AppSpacing.xl,
                        bottom = AppSpacing.lg
                    ),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Dashboard",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(AppSpacing.xs))
                    Text(
                        text = greeting,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = onThemeToggle) {
                    Icon(
                        imageVector = if (isDarkMode) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
                        contentDescription = "Toggle Theme",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = AppSpacing.lg)
            ) {
                val columns = when {
                    maxWidth < 600.dp -> 2
                    maxWidth < 900.dp -> 3
                    else -> 4
                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(columns),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
                    contentPadding = PaddingValues(bottom = AppSpacing.xl)
                ) {
                    items(cards) { card ->
                        DashboardCard(
                            model = card,
                            onClick = { onActionClick(card.route) }
                        )
                    }
                }
            }
        }
    }
}

fun getGreeting(name: String?): String {
    val hour = LocalTime.now().hour

    val greeting = when (hour) {
        in 0..11 -> "Good Morning"
        in 12..17 -> "Good Afternoon"
        else -> "Good Evening"
    }

    val finalName = if (name.isNullOrBlank()) "buddy" else name

    return "$greeting, $finalName!"
}
