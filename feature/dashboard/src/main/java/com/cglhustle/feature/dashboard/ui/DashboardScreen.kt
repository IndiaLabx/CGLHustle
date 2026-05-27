package com.cglhustle.feature.dashboard.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material.icons.automirrored.rounded.ShowChart
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

private val primaryCards = listOf(
    DashboardCardModel("Continue Quiz", "Jump back in", Icons.Rounded.PlayArrow, "mcqs"),
    DashboardCardModel("Create Quiz", "Customize filters", Icons.Rounded.Tune, "create_quiz"),
    DashboardCardModel("Progress", "See results", Icons.AutoMirrored.Rounded.ShowChart, "analytics"),
    DashboardCardModel("Bookmarks", "Saved questions", Icons.Rounded.Bookmarks, "bookmarks")
)

private val defaultExploreCards = listOf(
    DashboardCardModel("English Zone", "Master the language", Icons.AutoMirrored.Rounded.MenuBook, "english"),
    DashboardCardModel("Tools", "Utilities & calculators", Icons.Rounded.Build, "tools"),
    DashboardCardModel("Mock Test", "Exam-like challenge", Icons.Rounded.Timer, "mocktest"),
    DashboardCardModel("Downloads", "Offline resources", Icons.Rounded.Download, null),
    DashboardCardModel("About Us", "Our story", Icons.Rounded.Info, "about")
)

private val adminCard = DashboardCardModel("Admin Room", "Manage content", Icons.Rounded.AdminPanelSettings, "admin")

@Composable
fun DashboardRoute(
    onNavigateToMcq: () -> Unit,
    onNavigateToQuizConfig: () -> Unit,
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
                        "create_quiz" -> onNavigateToQuizConfig()
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

    val exploreCards = remember(isAdmin) {
        if (isAdmin) defaultExploreCards + adminCard else defaultExploreCards
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
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = AppSpacing.lg,
                        end = AppSpacing.lg,
                        top = AppSpacing.xl,
                        bottom = AppSpacing.md
                    ),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.lg),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Welcome",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = greeting,
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    FilledTonalIconButton(onClick = onThemeToggle) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
                            contentDescription = "Toggle Theme"
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSpacing.lg)
            ) {
                Text(
                    text = "Quick actions",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(AppSpacing.sm))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                ) {
                    primaryCards.take(2).forEach { card ->
                        FilledTonalButton(
                            onClick = { onActionClick(card.route) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(card.icon, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(card.title)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(AppSpacing.sm))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                ) {
                    primaryCards.drop(2).forEach { card ->
                        OutlinedButton(
                            onClick = { onActionClick(card.route) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(card.icon, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(card.title)
                        }
                    }
                }
            }

            Text(
                text = "Explore",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = AppSpacing.lg, vertical = AppSpacing.md)
            )

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
                    items(items = exploreCards, key = { card -> card.title }) { card ->
                        Box(modifier = Modifier.clip(RoundedCornerShape(20.dp))) {
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
