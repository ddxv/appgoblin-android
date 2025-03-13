package dev.thirdgate.appgoblin.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import dev.thirdgate.appgoblin.ui.screens.AppScreen
import dev.thirdgate.appgoblin.ui.screens.ResultsScreen
import dev.thirdgate.appgoblin.data.model.AppInfo
import dev.thirdgate.appgoblin.data.model.AppAnalysisResult
import dev.thirdgate.appgoblin.data.repository.AppRepository
import kotlinx.serialization.json.Json
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@Composable
fun AppNavigation(navController: NavHostController, appList: List<AppInfo>, appRepository: AppRepository) {
    NavHost(navController = navController, startDestination = "app_screen") {
        composable("app_screen") { AppScreen(appList, navController, appRepository) }
        composable("results_screen/{results}") { backStackEntry ->
            val resultsJson = backStackEntry.arguments?.getString("results") ?: ""
            val decodedJson = URLDecoder.decode(resultsJson, StandardCharsets.UTF_8.name()) // Decode URL
            val results = Json.decodeFromString<List<AppAnalysisResult>>(decodedJson)
            ResultsScreen(results, navController)
        }
    }
}
