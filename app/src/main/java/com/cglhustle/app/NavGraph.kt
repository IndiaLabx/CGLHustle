package com.cglhustle.app

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cglhustle.feature.auth.AuthRoute
import com.cglhustle.feature.quizconfig.navigation.QuizConfigRoute
import com.cglhustle.feature.activesession.ActiveSessionRoute
import com.cglhustle.feature.results.ResultsRoute
import com.cglhustle.feature.dashboard.ui.DashboardRoute
import com.cglhustle.feature.mcqs.ui.McqRoute

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = "auth"
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("auth") {
            AuthRoute(
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("dashboard") {
            DashboardRoute(
                onNavigateToMcq = {
                    navController.navigate("mcqs")
                }
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
                        popUpTo("quizConfig") { inclusive = false }
                    }
                }
            )
        }

        composable("results/{sessionId}") {
            ResultsRoute(
                onDone = {
                    navController.navigate("quizConfig") {
                        popUpTo("quizConfig") { inclusive = true }
                    }
                }
            )
        }
    }
}
