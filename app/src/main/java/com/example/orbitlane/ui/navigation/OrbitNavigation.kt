package com.example.orbitlane.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.orbitlane.ui.screens.AboutScreen
import com.example.orbitlane.ui.screens.GameScreen
import com.example.orbitlane.ui.screens.HomeScreen
import com.example.orbitlane.ui.screens.ResultScreen
import com.example.orbitlane.ui.screens.SettingsScreen
import com.example.orbitlane.ui.screens.SplashScreen
import com.example.orbitlane.viewmodel.GameViewModel

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Home : Screen("home")
    object Game : Screen("game")
    object Result : Screen("result")
    object Settings : Screen("settings")
    object About : Screen("about")
}

@Composable
fun OrbitNavigation(
    viewModel: GameViewModel
) {
    val navController = rememberNavController()

    val gameState by viewModel.gameState.collectAsState()
    val bestScore by viewModel.bestScore.collectAsState()
    val highestStreak by viewModel.highestStreak.collectAsState()
    val difficulty by viewModel.difficulty.collectAsState()
    val soundEnabled by viewModel.soundEnabled.collectAsState()
    val hapticsEnabled by viewModel.hapticsEnabled.collectAsState()
    val themePreference by viewModel.themePreference.collectAsState()
    val isNewBestScore by viewModel.isNewBestScore.collectAsState()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onSplashFinished = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                bestScore = bestScore,
                highestStreak = highestStreak,
                selectedDifficulty = difficulty,
                onDifficultySelected = { viewModel.setDifficulty(it) },
                onPlayClicked = {
                    viewModel.startGame()
                    navController.navigate(Screen.Game.route)
                },
                onSettingsClicked = {
                    navController.navigate(Screen.Settings.route)
                },
                onAboutClicked = {
                    navController.navigate(Screen.About.route)
                }
            )
        }

        composable(Screen.Game.route) {
            GameScreen(
                gameState = gameState,
                onSwitchInward = { viewModel.switchInward() },
                onSwitchOutward = { viewModel.switchOutward() },
                onPauseToggle = { viewModel.togglePause() },
                onResumeGame = { viewModel.resumeGame() },
                onRestartGame = {
                    viewModel.startGame()
                },
                onExitToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onNavigateToResult = {
                    navController.navigate(Screen.Result.route) {
                        popUpTo(Screen.Game.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Result.route) {
            val dodgedCount = gameState.dodgeFlashCounter
            ResultScreen(
                finalScore = gameState.score,
                obstaclesAvoided = dodgedCount,
                lapsCompleted = gameState.laps,
                highestStreak = gameState.bestStreak,
                bestScore = bestScore,
                isNewBest = isNewBestScore,
                onPlayAgain = {
                    viewModel.startGame()
                    navController.navigate(Screen.Game.route) {
                        popUpTo(Screen.Result.route) { inclusive = true }
                    }
                },
                onHomeClicked = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                currentTheme = themePreference,
                soundEnabled = soundEnabled,
                hapticsEnabled = hapticsEnabled,
                currentDifficulty = difficulty,
                onThemeChanged = { viewModel.setThemePreference(it) },
                onSoundChanged = { viewModel.setSoundEnabled(it) },
                onHapticsChanged = { viewModel.setHapticsEnabled(it) },
                onDifficultyChanged = { viewModel.setDifficulty(it) },
                onResetStats = { viewModel.resetStatistics() },
                onBackClicked = { navController.popBackStack() }
            )
        }

        composable(Screen.About.route) {
            AboutScreen(
                onBackClicked = { navController.popBackStack() }
            )
        }
    }
}
