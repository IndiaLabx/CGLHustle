package com.cglhustle.feature.dashboard.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cglhustle.feature.dashboard.components.DashboardCard
import com.cglhustle.feature.dashboard.model.DashboardCardModel
import com.cglhustle.feature.dashboard.viewmodel.DashboardViewModel
import kotlinx.coroutines.launch
import java.time.LocalTime

@Composable
fun DashboardRoute(
    onNavigateToMcq: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isAdmin = viewModel.isAdmin()

    DashboardScreen(
        userName = uiState.userName,
        isAdmin = isAdmin,
        onNavigateToMcq = onNavigateToMcq
    )
}

@Composable
fun DashboardScreen(
    userName: String,
    isAdmin: Boolean,
    onNavigateToMcq: () -> Unit
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val greeting = remember(userName) { getGreeting(userName) }

    val cards = remember(isAdmin) {
        mutableListOf(
            DashboardCardModel("MCQs Quiz", "Practice and learn", Color(0xFFD946EF), Icons.Rounded.Psychology, "mcqs"),
            DashboardCardModel("English Zone", "Master the language", Color(0xFFF43F5E), Icons.AutoMirrored.Rounded.MenuBook, "english"),
            DashboardCardModel("Tools", "Utilities & calculators", Color(0xFFF59E0B), Icons.Rounded.Build, "tools"),
            DashboardCardModel("Analytics", "Track your progress", Color(0xFF3B82F6), Icons.Rounded.Analytics, "analytics"),
            DashboardCardModel("Bookmarks", "Saved questions", Color(0xFF8B5CF6), Icons.Rounded.Bookmarks, "bookmarks")
        ).apply {
            if (isAdmin) {
                add(DashboardCardModel("Admin Room", "Manage content", Color(0xFFEF4444), Icons.Rounded.AdminPanelSettings, "admin"))
            }
            add(DashboardCardModel("Download", "Get offline resources", Color(0xFF06B6D4), Icons.Rounded.Download, null) {
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://drive.google.com/drive/folders/1Owy8_qnvMOTw5WLRGLQajCiScN-dOHtF")
                )
                context.startActivity(intent)
                Toast.makeText(context, "Your download page has been opened in next tab go and see", Toast.LENGTH_LONG).show()
            })
            add(DashboardCardModel("About Us", "Our story", Color(0xFF64748B), Icons.Rounded.Info, "about"))
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent // Let background shine through
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0F172A), // Deep charcoal
                            Color(0xFF020617)  // Near black
                        )
                    )
                )
                .padding(paddingValues)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Column(
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 20.dp)
                ) {
                    Text(
                        text = "Dashboard",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 36.sp,
                        color = Color.White,
                        letterSpacing = (-0.5).sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = greeting,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }

                BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
                    val columns = when {
                        maxWidth < 600.dp -> 2
                        maxWidth < 900.dp -> 3
                        else -> 4
                    }

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(columns),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(cards) { card ->
                            DashboardCard(model = card) {
                                if (card.action != null) {
                                    card.action.invoke()
                                } else {
                                    when (card.route) {
                                        "mcqs" -> onNavigateToMcq()
                                        else -> {
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar("${card.title} is coming soon ✨")
                                            }
                                        }
                                    }
                                }
                            }
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
