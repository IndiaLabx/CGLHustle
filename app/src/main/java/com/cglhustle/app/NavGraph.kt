package com.cglhustle.app

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cglhustle.feature.auth.AuthRoute
import com.cglhustle.feature.quizconfig.QuizConfigRoute
import com.cglhustle.feature.activesession.ActiveSessionRoute
import com.cglhustle.feature.results.ResultsRoute

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
                onAuthSuccess = {
                    navController.navigate("quizConfig") {
                        popUpTo("auth") { inclusive = true }
                    }
                }
            )
        }

        composable("quizConfig") {
            QuizConfigRoute(
                onConfigComplete = { sessionId ->
                    navController.navigate("activeSession/$sessionId")
                }
            )
        }

        composable("activeSession/{sessionId}") { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
            ActiveSessionRoute(
                onSessionComplete = { completedSessionId ->
                    navController.navigate("results/$completedSessionId") {
                        popUpTo("quizConfig") { inclusive = false }
                    }
                }
            )
        }

        composable("results/{sessionId}") { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
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
