package dev.thirdgate.appgoblin.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import dev.openattribution.sdk.OpenAttribution
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
                    .padding(padding) // Use padding passed to content to ensure proper space
            ) {
                when (selectedTab) {
                    0 -> ByCompanyCategoryScreen(
                        results = results,
                        navController = navController,
                        installedApps = installedApps
                    )

                    1 -> ByStoreIdScreen(results = results, navController = navController,)
                }
            }
        }
            )
    }

