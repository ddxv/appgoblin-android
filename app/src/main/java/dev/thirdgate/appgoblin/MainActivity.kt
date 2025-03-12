package dev.thirdgate.appgoblin

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.thirdgate.appgoblin.ui.theme.AppGoblinTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import kotlinx.serialization.Serializable



@Serializable
data class AppInfo(
    val name: String,
    val packageName: String,
    var isSelected: Boolean = false
)

@Serializable
data class ApiRequest(
    val store_ids: List<String>
)


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Fetch the list of installed apps
        val packageManager: PackageManager = packageManager
        val installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        val appList = installedApps.map { app ->
            val appName = app.loadLabel(packageManager).toString()
            val packageName = app.packageName
            AppInfo(name = appName, packageName = packageName)
        }

        setContent {
            AppGoblinTheme {
                AppScreen(appList)
            }
        }
    }
}

@Composable
fun AppScreen(apps: List<AppInfo>) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("My Apps", "Favorites", "About", "Settings")
    val icons = listOf(
        Icons.Default.Build,
        Icons.Default.Favorite,
        Icons.Default.Info,
        Icons.Default.Settings
    )

    // Keep track of the selected apps
    val selectedApps = remember { mutableStateListOf<AppInfo>().apply { addAll(apps) } }

    // Store API response
    var apiResponse by remember { mutableStateOf<String?>(null) }
    var apiError by remember { mutableStateOf<String?>(null) }

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
        // Display content based on selected tab
        when (selectedTab) {
            0 -> {
                CheckableAppList(
                apps = selectedApps,
                apiResponse = apiResponse,
                apiError = apiError,
                onSendSelected = { selected ->
                    CoroutineScope(Dispatchers.Main).launch {
                        try {
                            val response = sendSelectedAppsToApi(selected)
                            apiResponse = response
                            apiError = null
                        } catch(e : Exception) {
                            apiError = "Error : ${e.message}"
                            apiResponse = null
                        }
                    }

                },
                modifier = Modifier.padding(innerPadding)
            )
        }
            1 -> Content("Favorites", modifier = Modifier.padding(innerPadding))
            2 -> Content("About", modifier = Modifier.padding(innerPadding))
            3 -> Content("Settings", modifier = Modifier.padding(innerPadding))
        }
    }
}

@Composable
fun CheckableAppList(
    apps: List<AppInfo>,
    apiResponse: String?,
    apiError: String?,
    onSendSelected: (List<AppInfo>) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        // Button to send selected apps
        Button(
            onClick = { onSendSelected(apps.filter { it.isSelected }) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Icon(
                Icons.Default.Send,
                contentDescription = "Send",
                modifier = Modifier.padding(end = 8.dp)
            )
            Text("Send Selected Apps")
        }

        // Display API response if available
        apiResponse?.let {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "API Response:",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        // List of apps with checkboxes
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(apps) { app ->
                AppListItem(
                    app = app,
                    onCheckChanged = { isChecked ->
                        app.isSelected = isChecked
                    }
                )
            }
        }
    }
}

@Composable
fun AppListScreen(apps: List<AppInfo>, onCheckChanged: (AppInfo, Boolean) -> Unit) {
    val systemPackages = apps.filter { it.packageName.startsWith("com.android.") || it.packageName.startsWith("com.google.") }
    val userApps = apps - systemPackages // All apps not in the systemPackages list

    LazyColumn {
//        items(userApps) { app ->
//            AppListItem(app = app, onCheckChanged = { isChecked -> onCheckChanged(app, isChecked) })
            items(userApps) { app -> AppListScreen(apps = userApps, onCheckChanged = { app, isChecked -> onCheckChanged(app, isChecked) })  }
//        }

        if (systemPackages.isNotEmpty()) {
            item {
                Text(
                    text = "System Packages",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 16.dp)
                )
            }

            items(systemPackages) { app ->
                AppListItem(app = app, onCheckChanged = { isChecked -> onCheckChanged(app, isChecked) })
            }
        }
    }
}


@Composable
fun AppListItem(
    app: AppInfo,
    onCheckChanged: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = app.isSelected,
            onCheckedChange = onCheckChanged
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp)
        ) {
            Text(
                text = app.name,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = app.packageName,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

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

// Function to send selected apps to the API
suspend fun sendSelectedAppsToApi(selectedApps: List<AppInfo>): String {
    return withContext(Dispatchers.IO) {
        try {
            val packageNames = selectedApps.map { it.packageName }
            val requestBody = ApiRequest(store_ids = packageNames)

            val json = Json { prettyPrint = true }
            val jsonBody = json.encodeToString(requestBody)

            val client = OkHttpClient()
            val request = Request.Builder()
                .url("http://10.0.2.2:8000/api/public/sdks/apps")
                .post(jsonBody.toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                response.body?.string() ?: "No response"
            }
        } catch (e: Exception) {
            Log.e("AppGoblin", "Error sending data: ${e.message}")
            "Error: ${e.message}"
            throw e
        }
    }
}

// Helper function to call the suspend function from a composable
@Composable
fun SendSelectedApps(selectedApps: List<AppInfo>, onResponse: (String) -> Unit) {
    LaunchedEffect(selectedApps) {
        val response = sendSelectedAppsToApi(selectedApps)
        onResponse(response)
    }
}

@Preview(showBackground = true)
@Composable
fun AppScreenPreview() {
    AppGoblinTheme {
        AppScreen(apps = listOf(
            AppInfo("Sample App", "com.example.sample"),
            AppInfo("Another App", "com.example.another")
        ))
    }
}

@Preview(showBackground = true)
@Composable
fun CheckableAppListPreview() {
    AppGoblinTheme {
        CheckableAppList(
            apps = listOf(
                AppInfo("Sample App", "com.example.sample", true),
                AppInfo("Another App", "com.example.another")
            ),
            apiResponse = """{"data": {"apps": ["com.example.sample"]}}""",
            apiError= null,
            onSendSelected = {}
        )
    }
}
