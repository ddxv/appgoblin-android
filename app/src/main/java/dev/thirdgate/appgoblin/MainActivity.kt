    package dev.thirdgate.appgoblin


    import android.content.pm.PackageManager
    import android.os.Bundle
    import android.util.Log
    import androidx.activity.ComponentActivity
    import androidx.activity.compose.setContent
    import androidx.activity.enableEdgeToEdge
    import androidx.compose.runtime.mutableStateOf
    import androidx.compose.runtime.remember
    import androidx.lifecycle.lifecycleScope
    import androidx.navigation.compose.rememberNavController
    import dev.thirdgate.appgoblin.data.model.AppInfo
    import dev.thirdgate.appgoblin.data.repository.AppRepository
    import dev.thirdgate.appgoblin.navigation.AppNavigation
    import dev.thirdgate.appgoblin.ui.theme.AppGoblinTheme
    import kotlinx.coroutines.launch
    import java.net.URLEncoder


    class MainActivity : ComponentActivity() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            enableEdgeToEdge()

            val packageManager: PackageManager = packageManager
            val appRepository = AppRepository(packageManager)  // Initialize repository

            val installedApps = mutableStateOf<List<AppInfo>>(emptyList())
            // Fetch installed apps safely
            lifecycleScope.launch {
                try {
                    installedApps.value = appRepository.getInstalledApps()
                } catch (e: Exception) {
                    Log.e("AppGoblin", "Error fetching installed apps: ${e.message}")
                }
            }

            Log.i("AppGoblin", "Installed apps: $installedApps")

            setContent {
                AppGoblinTheme {
                    val navController = rememberNavController()
                    AppNavigation(navController, installedApps.value, appRepository)  // Pass repository
                }
            }
        }
    }

    // Helper function to encode URL
    fun String.encodeURL(): String = URLEncoder.encode(this, "UTF-8")

