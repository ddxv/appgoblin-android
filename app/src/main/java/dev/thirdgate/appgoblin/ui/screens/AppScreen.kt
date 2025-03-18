package dev.thirdgate.appgoblin.ui.screens

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.Checkbox
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
import androidx.compose.runtime.saveable.rememberSaveable
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
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var apiError by rememberSaveable { mutableStateOf<String?>(null) }

    var isScanning by rememberSaveable { mutableStateOf(false) }
    var scannedApps by rememberSaveable { mutableStateOf<List<AppInfo>>(emptyList()) }

    // Store selected apps as a List and convert it to a Set when needed
    var selectedApps by rememberSaveable { mutableStateOf<List<AppInfo>>(emptyList()) }

    val hasApps = scannedApps.isNotEmpty()
    val context = LocalContext.current

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                listOf("My Apps", "About").forEachIndexed { index, title ->
                    NavigationBarItem(
                        icon = { Icon(if (index == 0) Icons.Default.Build else Icons.Default.Settings, contentDescription = title) },
                        label = { Text(title) },
                        selected = selectedTab == index,
                        onClick = {
                            if (index == 1) {
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
                    LoadingScreen(innerPadding)
                } else if (!hasApps) {
                    ScanPrompt(
                        innerPadding,
                        onScanApps = {
                            isScanning = true
                            CoroutineScope(Dispatchers.Main).launch {
                                try {
                                    val installedApps = appRepository.getInstalledApps()
                                    scannedApps = installedApps
                                    onAppsScanned(installedApps)
                                } catch (e: Exception) {
                                    apiError = "Error: ${e.message}"
                                }
                                isScanning = false
                            }
                        }
                    )
                } else {
                    CheckableAppList(
                        apps = scannedApps,
                        apiError = apiError,
                        onSendSelected = { selected ->
                            selectedApps = selected // Persist selection
                            CoroutineScope(Dispatchers.Main).launch {
                                try {
                                    val response = appRepository.analyzeApps(selected)
                                    val json = Json { ignoreUnknownKeys = true }.encodeToString(response)
                                    navController.navigate("results_screen/${json.encodeURL()}")
                                } catch (e: Exception) {
                                    apiError = "Error: ${e.message}"
                                }
                            }
                        },
                        modifier = Modifier.padding(innerPadding),
                        initialSelectedApps = selectedApps.toSet() // Restore selection
                    )
                }
            }
            1 -> Content("About", modifier = Modifier.padding(innerPadding))
        }
    }
}


@Composable
fun LoadingScreen(innerPadding: PaddingValues) {
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
            CircularProgressIndicator(modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text("Building list of installed apps...", style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
fun ScanPrompt(innerPadding: PaddingValues, onScanApps: () -> Unit) {
    var userConsent by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
            Icon(Icons.Default.Search, contentDescription = "Build List of installed apps", modifier = Modifier.size(64.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text("Data Collection Consent", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "AppGoblin needs your consent before scanning your device:",
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Checkbox and consent text
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Checkbox(
                    checked = userConsent,
                    onCheckedChange = { userConsent = it }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "I allow AppGoblin to build a list of my installed apps and packages. " +
                            "I understand I can select which app IDs are sent to AppGoblin's server to request those apps' tracking SDK data.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onScanApps,
                enabled = userConsent
            ) {
                Icon(Icons.Default.Search, contentDescription = "Search")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Build list of installed apps")
            }
        }
    }
}