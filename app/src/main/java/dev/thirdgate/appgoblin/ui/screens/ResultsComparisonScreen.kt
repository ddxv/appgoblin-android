package dev.thirdgate.appgoblin.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import dev.thirdgate.appgoblin.data.model.AppAnalysisResult
import dev.thirdgate.appgoblin.data.model.AppInfo
import dev.thirdgate.appgoblin.data.repository.AppRepository
import kotlinx.serialization.json.Json
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@Composable
fun ResultsComparisonScreen(
    navController: NavHostController,
    installedApps: List<AppInfo>,
    resultsJson: String,
    appRepository: AppRepository
) {
    var results by remember { mutableStateOf<AppAnalysisResult?>(null) }
    var apiError by remember { mutableStateOf<String?>(null) }
    val selectedApps = Json.decodeFromString<List<AppInfo>>(URLDecoder.decode(resultsJson, StandardCharsets.UTF_8.name()))

    LaunchedEffect(selectedApps) {
        try {
            results = appRepository.analyzeApps(selectedApps)
        } catch (e: Exception) {
            apiError = "Error: ${e.message}"
        }
    }

    when {
        results == null && apiError == null -> {
            LoadingScreen()
        }

        apiError != null -> {
//            ErrorScreen(apiError!!) // Display error message if API fails
            Text(apiError!!)
        }

        results != null -> {
            val nonNullResults = results!! // Smart cast because we checked for null

            var selectedTab by remember { mutableStateOf(0) }
            val tabs = listOf("By SDK Companies", "By App")
            val icons = listOf(Icons.Default.Info, Icons.Default.Build)

            Scaffold(
                bottomBar = {
                    NavigationBar {
                        tabs.forEachIndexed { index, title ->
                            NavigationBarItem(
                                icon = { Icon(icons[index], contentDescription = title) },
                                label = { Text(title) },
                                selected = selectedTab == index,
                                onClick = { selectedTab = index }
                            )
                        }
                    }
                },
                content = { padding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                    ) {
                        when (selectedTab) {
                            0 -> ByCompanyCategoryScreen(
                                results = nonNullResults,
                                navController = navController,
                                installedApps = installedApps
                            )
                            1 -> ByStoreIdScreen(results = nonNullResults, navController = navController)
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun LoadingScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("Loading SDK data from AppGoblin...", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground)
        }
    }
}
