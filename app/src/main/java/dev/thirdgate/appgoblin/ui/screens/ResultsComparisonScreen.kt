package dev.thirdgate.appgoblin.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import dev.thirdgate.appgoblin.data.model.AppAnalysisResult
import dev.thirdgate.appgoblin.data.model.AppInfo

@Composable
fun ResultsComparisonScreen(results: AppAnalysisResult, navController: NavHostController, installedApps: List<AppInfo>) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("By SDK Companies", "By App")
    val icons = listOf(
        Icons.Default.Info,
        Icons.Default.Build,
    )

    Scaffold(
        topBar = {
            Text("Comparison", style = MaterialTheme.typography.headlineMedium)
        },
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
        }
    ) { innerPadding ->
        when (selectedTab) {
            0 -> ByCompanyCategoryScreen(results = results, navController = navController, installedApps = installedApps)
            1 -> ByStoreIdScreen(results = results, navController = navController, )
        }
    }
}
