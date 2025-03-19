    package dev.thirdgate.appgoblin.navigation

    import androidx.compose.runtime.Composable
    import androidx.compose.runtime.mutableStateOf
    import androidx.compose.runtime.remember
    import androidx.navigation.NavHostController
    import androidx.navigation.compose.NavHost
    import androidx.navigation.compose.composable
    import dev.thirdgate.appgoblin.ui.screens.AppScreen
    import dev.thirdgate.appgoblin.data.model.AppInfo
    import dev.thirdgate.appgoblin.data.model.AppAnalysisResult
    import dev.thirdgate.appgoblin.data.repository.AppRepository
    import dev.thirdgate.appgoblin.ui.screens.ResultsComparisonScreen
    import kotlinx.serialization.json.Json
    import java.net.URLDecoder
    import java.nio.charset.StandardCharsets

    @Composable
    fun AppNavigation(navController: NavHostController, appRepository: AppRepository) {
        // Create a shared state to store scanned apps
        val scannedApps = remember { mutableStateOf<List<AppInfo>>(emptyList()) }

        NavHost(navController = navController, startDestination = "app_screen") {
            composable("app_screen") {
                AppScreen(
                    navController = navController,
                    appRepository = appRepository,
                    onAppsScanned = { apps ->
                        // Update the shared state when apps are scanned
                        scannedApps.value = apps
                    }
                )
            }

            // Results screen, allowing comparison
            composable("results_screen/{selectedApps}") { backStackEntry ->
                val selectedJson = backStackEntry.arguments?.getString("selectedApps") ?: ""
                val decodedJson = URLDecoder.decode(selectedJson, StandardCharsets.UTF_8.name())

                ResultsComparisonScreen(
                    resultsJson = decodedJson,
                    navController = navController,
                    installedApps = scannedApps.value,
                    appRepository = appRepository
                )
            }
        }
    }