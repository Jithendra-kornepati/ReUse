package uk.ac.tees.mad.reuse.ui.theme

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

// Custom Dark Theme Colors (ReUse Focused Green Palette)
private val DarkColorScheme = darkColorScheme(
    primary = GreenPrimaryDark,
    primaryContainer = GreenPrimaryVariantDark,
    secondary = YellowSecondaryDark,
    background = Color(0xFF0E1A12),
    surface = Color(0xFF122418),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
    error = ErrorDark
)

// Custom Light Theme Colors (ReUse Focused Green Palette)
private val LightColorScheme = lightColorScheme(
    primary = GreenPrimaryLight,
    primaryContainer = GreenPrimaryVariantLight,
    secondary = YellowSecondaryLight,
    background = Color(0xFFE8F5E9), // Eco-friendly soft green background for light mode
    surface = Color(0xFFFFFFFF),    // Clean white surface for components
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color(0xFF1B1B1B),
    onSurface = Color(0xFF1B1B1B),
    error = ErrorLight
)

@Composable
fun ReUseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        // Use dynamic colors for Android 12 and above
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        // Fallback to our custom green palette
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
