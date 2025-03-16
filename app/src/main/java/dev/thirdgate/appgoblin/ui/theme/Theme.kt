package dev.thirdgate.appgoblin.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFB4BEFE),  // Lavender
    secondary = Color(0xFFF2CDCD), // Flamingo
    tertiary = Color(0xFFA6E3A1),  // Green
    background = Color(0xFF1E1E2E), // Base (Mocha)
    surface = Color(0xFF181825),    // Mantle
    onPrimary = Color(0xFF1E1E2E),  // Dark foreground text
    onSecondary = Color(0xFF1E1E2E),
    onTertiary = Color(0xFF1E1E2E),
    onBackground = Color(0xFFCDD6F4), // Text color
    onSurface = Color(0xFFCDD6F4)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF7287FD),  // Lavender
    secondary = Color(0xFFDC8A78), // Flamingo
    tertiary = Color(0xFF40A02B),  // Green
    background = Color(0xFFEFF1F5), // Base (Latte)
    surface = Color(0xFFE6E9EF),    // Mantle
    onPrimary = Color(0xFFFFFFFF),  // White text on primary
    onSecondary = Color(0xFFFFFFFF),
    onTertiary = Color(0xFFFFFFFF),
    onBackground = Color(0xFF4C4F69), // Text color
    onSurface = Color(0xFF4C4F69)
)

@Composable
fun AppGoblinTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}