package com.cglhustle.feature.dashboard.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForwardIos
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cglhustle.feature.dashboard.viewmodel.DashboardEvent
import com.cglhustle.feature.dashboard.viewmodel.DashboardViewModel
import java.time.LocalTime

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
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is DashboardEvent.NavigateTo -> {
                    when (event.route) {
                        "mcqs" -> onNavigateToMcq()
                        "create_quiz" -> onNavigateToQuizConfig()
                        else -> snackbarHostState.showSnackbar("${event.route} is coming soon ✨")
                    }
                }
                is DashboardEvent.OpenExternalLink -> context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(event.url)))
                is DashboardEvent.ShowToast -> Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    DashboardScreen(
        userName = uiState.userName,
        snackbarHostState = snackbarHostState,
        onActionClick = viewModel::onActionClick,
        onThemeToggle = onThemeToggle,
        isDarkMode = isDarkMode,
        modifier = modifier
    )
}

@Composable
fun DashboardScreen(
    userName: String,
    snackbarHostState: SnackbarHostState,
    onActionClick: (String?) -> Unit,
    onThemeToggle: () -> Unit,
    isDarkMode: Boolean,
    modifier: Modifier = Modifier
) {
    val greeting = remember(userName) { getGreeting(userName) }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }, containerColor = MaterialTheme.colorScheme.background, modifier = modifier.fillMaxSize()) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = {}) { Icon(Icons.Rounded.Menu, contentDescription = null) }
                IconButton(onClick = {}) { Icon(Icons.Outlined.NotificationsNone, contentDescription = null) }
            }

            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(86.dp).clip(RoundedCornerShape(43.dp))
                        .background(Brush.linearGradient(listOf(Color(0xFF205FFF), Color(0xFF1C2E90)))),
                    contentAlignment = Alignment.Center
                ) {
                    Text("E.", style = MaterialTheme.typography.displaySmall, color = Color.White)
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text("Welcome back,", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.72f))
                    Text(greeting, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                    Text("Let’s keep your learning streak going.", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.72f))
                }
                Box(
                    modifier = Modifier.size(56.dp).clip(RoundedCornerShape(16.dp)).background(Color(0xFFF1F2F8)),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Rounded.DarkMode, contentDescription = null, tint = Color(0xFF1B245A)) }
            }

            Spacer(Modifier.height(16.dp))

            Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF1FF))) {
                Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Continue learning", color = Color(0xFF5A63FF), style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        Text("English Grammar Quiz", style = MaterialTheme.typography.headlineMedium)
                        Text("12 / 20 Questions", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
                        Spacer(Modifier.height(16.dp))
                        LinearProgressIndicator(progress = { 0.6f }, modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(8.dp)))
                    }
                    Spacer(Modifier.width(12.dp))
                    FilledTonalIconButton(
                        onClick = { onActionClick("mcqs") },
                        modifier = Modifier.size(72.dp),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = Color.White)
                    ) {
                        Icon(Icons.Rounded.PlayArrow, contentDescription = null, tint = Color(0xFF5A63FF), modifier = Modifier.size(34.dp))
                    }
                }
            }

            Spacer(Modifier.height(14.dp))
            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF101E57))) {
                Row(Modifier.fillMaxWidth().padding(vertical = 16.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                    StatItem("🔥", "7", "Day Streak")
                    DividerLine()
                    StatItem("◉", "850", "XP Earned")
                    DividerLine()
                    StatItem("🏆", "12", "Badges")
                }
            }

            Spacer(Modifier.height(20.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Quick actions", style = MaterialTheme.typography.headlineMedium)
                TextButton(onClick = {}) { Text("View all"); Icon(Icons.AutoMirrored.Outlined.ArrowForwardIos, contentDescription = null, modifier = Modifier.size(14.dp)) }
            }
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                QuickActionCard("Continue Quiz", Icons.Rounded.PlayArrow, Color(0xFFF0EBFF), Modifier.weight(1f)) { onActionClick("mcqs") }
                QuickActionCard("Create Quiz", Icons.Rounded.AddBox, Color(0xFFEEF1FF), Modifier.weight(1f)) { onActionClick("create_quiz") }
                QuickActionCard("Progress", Icons.Rounded.TrendingUp, Color(0xFFEAF8F5), Modifier.weight(1f)) { onActionClick("analytics") }
                QuickActionCard("Bookmarks", Icons.Rounded.BookmarkBorder, Color(0xFFFFF4E9), Modifier.weight(1f)) { onActionClick("bookmarks") }
            }

            Spacer(Modifier.height(22.dp))
            Text("Explore", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ExploreCard("English Zone", "Master the language", Icons.AutoMirrored.Rounded.MenuBook, Color(0xFFEFF2FF), Modifier.weight(1f)) { onActionClick("english") }
                ExploreCard("Tools", "Utilities & calculators", Icons.Rounded.Build, Color(0xFFF2EEFF), Modifier.weight(1f)) { onActionClick("tools") }
            }
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ExploreCard("Mock Test", "Exam-like challenge", Icons.Rounded.Timer, Color(0xFFE8F7F2), Modifier.weight(1f)) { onActionClick("mocktest") }
                ExploreCard("Downloads", "Offline resources", Icons.Rounded.Download, Color(0xFFFFF3E8), Modifier.weight(1f)) { onActionClick(null) }
            }
            Spacer(Modifier.height(24.dp))
            BottomNavMock()
        }
    }
}
@Composable private fun DividerLine() { Box(Modifier.width(1.dp).height(56.dp).background(Color.White.copy(alpha = 0.2f))) }

@Composable
private fun StatItem(icon: String, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("$icon  $value", color = Color.White, style = MaterialTheme.typography.headlineMedium)
        Text(label, color = Color.White.copy(alpha = 0.86f), style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun QuickActionCard(title: String, icon: ImageVector, bg: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = modifier.height(124.dp), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = bg)) {
        Column(Modifier.fillMaxSize().padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(icon, contentDescription = null, tint = Color(0xFF4E5BFF), modifier = Modifier.size(28.dp))
            Spacer(Modifier.height(8.dp))
            Text(title, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun ExploreCard(title: String, subtitle: String, icon: ImageVector, iconBg: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = modifier.height(150.dp), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(Modifier.fillMaxSize().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(62.dp).clip(RoundedCornerShape(16.dp)).background(iconBg), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = Color(0xFF4E5BFF))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.headlineSmall)
                Text(subtitle, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            }
            Icon(Icons.AutoMirrored.Outlined.ArrowForwardIos, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
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

@Composable
private fun BottomNavMock() {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            BottomItem("Home", Icons.Rounded.Home, true)
            BottomItem("Practice", Icons.AutoMirrored.Rounded.MenuBook, false)
            BottomItem("Progress", Icons.Rounded.ShowChart, false)
            BottomItem("Profile", Icons.Rounded.PersonOutline, false)
        }
    }
}

@Composable
private fun BottomItem(label: String, icon: ImageVector, selected: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = if (selected) Color(0xFF2463FF) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f))
        Spacer(Modifier.height(8.dp))
        Text(label, color = if (selected) Color(0xFF2463FF) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f))
    }
}
