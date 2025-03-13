package dev.thirdgate.appgoblin.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import dev.thirdgate.appgoblin.data.model.AppAnalysisResult
import dev.thirdgate.appgoblin.data.model.AppInfo
import dev.thirdgate.appgoblin.ui.components.CheckableAppList
import dev.thirdgate.appgoblin.data.repository.AppRepository
import dev.thirdgate.appgoblin.encodeURL
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

@Composable
fun Content(title: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        Text(
            text = "$title Screen",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
fun AppScreen(apps: List<AppInfo>, navController: NavHostController, appRepository: AppRepository) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("My Apps", "Favorites", "About", "Settings")
    val icons = listOf(
        Icons.Default.Build,
        Icons.Default.Favorite,
        Icons.Default.Info,
        Icons.Default.Settings
    )

    var apiError by remember { mutableStateOf<String?>(null) }

    // Manage selected apps state here
    val selectedApps = remember { mutableStateListOf<AppInfo>() }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
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
            0 -> CheckableAppList(
                apps = apps,  // Use the original apps list here
//                selectedApps = selectedApps, // Pass selectedApps for managing the selection state
                apiError = apiError,
                onSendSelected = { selected ->
                    // Handle the analysis and navigation to the results screen
                    CoroutineScope(Dispatchers.Main).launch {
                        try {
                            val response = appRepository.analyzeApps(selected)
                            val json = Json { ignoreUnknownKeys = true }
                            val responseJson = json.encodeToString(ListSerializer(AppAnalysisResult.serializer()), response)

                            // Navigate to results screen
                            navController.navigate("results_screen/${responseJson.encodeURL()}")
                        } catch (e: Exception) {
                            apiError = "Error: ${e.message}"
                        }
                    }
                },
                modifier = Modifier.padding(innerPadding)
            )
            1 -> Content("Favorites", modifier = Modifier.padding(innerPadding))
            2 -> Content("About", modifier = Modifier.padding(innerPadding))
            3 -> Content("Settings", modifier = Modifier.padding(innerPadding))
        }
    }
}
