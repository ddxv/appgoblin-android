    package dev.thirdgate.appgoblin

    import android.os.Bundle
    import androidx.activity.ComponentActivity
    import androidx.activity.compose.setContent
    import androidx.compose.foundation.isSystemInDarkTheme
    import androidx.navigation.compose.rememberNavController
    import dev.thirdgate.appgoblin.data.repository.AppRepository
    import dev.thirdgate.appgoblin.navigation.AppNavigation
    import dev.thirdgate.appgoblin.ui.theme.AppGoblinTheme
    import java.net.URLEncoder


    class MainActivity : ComponentActivity() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            val appRepository by lazy { AppRepository(applicationContext) }

            setContent {
                AppGoblinTheme(darkTheme = isSystemInDarkTheme(), dynamicColor = false) {
                    val navController = rememberNavController()

                        AppNavigation(
                            navController,
                            appRepository
                        )
                    }

            }
        }
    }

    // Helper function to encode URL
    fun String.encodeURL(): String = URLEncoder.encode(this, "UTF-8")

