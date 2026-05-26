package com.cglhustle.app

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cglhustle.feature.auth.AuthRoute
import com.cglhustle.feature.quizconfig.navigation.QuizConfigRoute
import com.cglhustle.feature.activesession.ActiveSessionRoute
import com.cglhustle.feature.results.ResultsRoute
import com.cglhustle.feature.dashboard.ui.DashboardRoute
import com.cglhustle.feature.mcqs.ui.McqRoute

private data class TopLevelDestination(val route: String, val label: String, val icon: ImageVector)

private val topLevelDestinations = listOf(
    TopLevelDestination("dashboard", "Home", Icons.Rounded.Home),
    TopLevelDestination("mcqs", "Practice", Icons.AutoMirrored.Rounded.MenuBook),
    TopLevelDestination("results/last", "Progress", Icons.Rounded.BarChart),
    TopLevelDestination("mcqs", "Practice", Icons.Rounded.Psychology),
    TopLevelDestination("results/last", "Progress", Icons.Rounded.ShowChart),
    TopLevelDestination("auth", "Profile", Icons.Rounded.Person)
)

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = "auth",
    onThemeToggle: () -> Unit = {},
    isDarkMode: Boolean = false
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute in setOf("dashboard", "mcqs")

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    topLevelDestinations.forEach { destination ->
                        NavigationBarItem(
                            selected = currentRoute == destination.route,
                            onClick = {
                                if (destination.route == "results/last") return@NavigationBarItem
                                navController.navigate(destination.route) {
                                    popUpTo("dashboard") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(destination.icon, contentDescription = destination.label) },
                            label = { Text(destination.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = androidx.compose.ui.Modifier.padding(innerPadding)
        ) {
        composable("auth") {
            AuthRoute(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("dashboard") {
            DashboardRoute(
                onNavigateToMcq = {
                    navController.navigate("mcqs")
                },
                onNavigateToQuizConfig = {
                    navController.navigate("quizConfig")
                },
                onThemeToggle = onThemeToggle,
                isDarkMode = isDarkMode
            )
        }

        composable("mcqs") {
            McqRoute(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCreateQuiz = { navController.navigate("quizConfig") }
            )
        }

        composable("quizConfig") {
            QuizConfigRoute(
                onConfigComplete = { sessionId ->
                    navController.navigate("activeSession/$sessionId")
                }
            )
        }

        composable("activeSession/{sessionId}") {
            ActiveSessionRoute(
                onSessionComplete = { completedSessionId ->
                    navController.navigate("results/$completedSessionId") {
                        popUpTo("activeSession/{sessionId}") { inclusive = true }
                    }
                }
            )
        }

        composable("results/{sessionId}") {
            ResultsRoute(
                onDone = {
                    navController.navigate("dashboard") {
                        popUpTo("dashboard") { inclusive = false }
                    }
                }
            )
        }
        }
    }
}
