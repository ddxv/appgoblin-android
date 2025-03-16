package dev.thirdgate.appgoblin.ui.screens

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import dev.thirdgate.appgoblin.AboutActivity
import dev.thirdgate.appgoblin.data.model.AppInfo
import dev.thirdgate.appgoblin.ui.components.CheckableAppList
import dev.thirdgate.appgoblin.data.repository.AppRepository
import dev.thirdgate.appgoblin.encodeURL
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
fun AppScreen(
    navController: NavHostController,
    appRepository: AppRepository,
    onAppsScanned: (List<AppInfo>) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("My Apps", "About")
    val icons = listOf(
        Icons.Default.Build,
        Icons.Default.Settings
    )

    val context = LocalContext.current  // Get context for launching activities
    var apiError by remember { mutableStateOf<String?>(null) }

    // Add states to manage the app scanning process
    var isScanning by remember { mutableStateOf(false) }
    var scannedApps by remember { mutableStateOf<List<AppInfo>>(emptyList()) }
    val hasApps = scannedApps.isNotEmpty()

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
                        onClick = {
                            if (index == 1) {
                                // Launch AboutActivity when "About" tab is clicked
                                val intent = Intent(context, AboutActivity::class.java)
                                context.startActivity(intent)
                            } else {
                                selectedTab = index
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        when (selectedTab) {
            0 -> {
                if (isScanning) {
                    // Show loading state when scanning apps
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(48.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Scanning installed apps...",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                } else if (!hasApps) {
                    // Show the scan button when no apps are loaded
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Scan apps",
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No apps scanned yet",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Scan your installed apps to identify SDKs and third-party libraries",
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 32.dp)
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = {
                                    // Start scanning apps
                                    isScanning = true
                                    CoroutineScope(Dispatchers.Main).launch {
                                        try {
                                            val installedApps = appRepository.getInstalledApps()
                                            scannedApps = installedApps
                                            // Call the callback to share the scanned apps
                                            onAppsScanned(installedApps)
                                            isScanning = false
                                            Log.i("AppGoblin", "Scanned ${installedApps.size} apps")
                                        } catch (e: Exception) {
                                            Log.e("AppGoblin", "Error scanning apps: ${e.message}")
                                            isScanning = false
                                            apiError = "Error: ${e.message}"
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Scan"
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "Scan Apps")
                            }
                        }
                    }
                } else {
                    // Show the checkable app list once apps are loaded
                    CheckableAppList(
                        apps = scannedApps,
                        apiError = apiError,
                        onSendSelected = { selected ->
                            // Handle the analysis and navigation to the results screen
                            CoroutineScope(Dispatchers.Main).launch {
                                try {
                                    val response = appRepository.analyzeApps(selected)
                                    val json = Json { ignoreUnknownKeys = true }
                                    val responseJson = json.encodeToString(response)

                                    // Navigate to results screen
                                    navController.navigate("results_screen/${responseJson.encodeURL()}")
                                } catch (e: Exception) {
                                    apiError = "Error: ${e.message}"
                                }
                            }
                        },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
            1 -> Content("About", modifier = Modifier.padding(innerPadding))
        }
    }
}