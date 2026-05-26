package com.cglhustle.app

import android.os.Bundle
import androidx.activity.viewModels
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import com.cglhustle.core.designsystem.theme.CglHustleTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.cglhustle.core.network.auth.AuthRepository
import dagger.hilt.android.AndroidEntryPoint
import io.github.jan.supabase.gotrue.SessionStatus
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var authRepository: AuthRepository

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()

            CglHustleTheme(darkTheme = isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val sessionStatus by authRepository.sessionStatus.collectAsStateWithLifecycle()

                    LaunchedEffect(sessionStatus) {
                        when (sessionStatus) {
                            is SessionStatus.Authenticated -> {
                                navController.navigate("dashboard") {
                                    popUpTo("auth") { inclusive = true }
                                }
                            }
                            is SessionStatus.NotAuthenticated -> {
                                val currentRoute = navController.currentBackStackEntry?.destination?.route
                                if (currentRoute != null && currentRoute != "auth") {
                                    navController.navigate("auth") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            }
                            else -> {}
                        }
                    }

                    AppNavGraph(navController = navController, startDestination = "auth", onThemeToggle = viewModel::toggleTheme, isDarkMode = isDarkMode)
                }
            }
        }
    }
}
