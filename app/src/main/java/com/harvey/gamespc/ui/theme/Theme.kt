package com.harvey.gamespc.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Light Theme Colors
val LightBackground = Color(0xFFFFFFFF)
val LightPrimary = Color(0xFFF0F0F0) // Light gray for primary, e.g., toolbar/bottom nav background
val LightPrimaryDark = Color(0xFFCCCCCC) // Slightly darker gray
val LightAccent = Color(0xFF2196F3) // A vibrant blue for accent
val LightTextPrimary = Color(0xFF000000)
val LightTextSecondary = Color(0x8A000000) // 54% black

// Dark Theme Colors
val DarkBackground = Color(0xFF1A1A2E)
val DarkPrimary = Color(0xFF1A1A2E) // Using dark_background as primary for dark theme
val DarkPrimaryDark = Color(0xFF1A1A2E) // Using dark_background as primary_dark for dark theme
val DarkCyanNeon = Color(0xFF00FFFF)
val DarkMagentaNeon = Color(0xFFFF00FF)
val DarkTextPrimary = Color(0xFFFFFFFF)
val DarkTextSecondary = Color(0xFFE0E0E0)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkTextPrimary,
    primaryContainer = DarkPrimaryDark,
    onPrimaryContainer = DarkTextPrimary,
    secondary = DarkCyanNeon,
    onSecondary = DarkBackground,
    tertiary = DarkMagentaNeon, // Using magenta for tertiary
    onTertiary = DarkTextPrimary,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkBackground,
    onSurface = DarkTextPrimary,
    error = Color(0xFFB00020), // Standard error color
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightTextPrimary,
    primaryContainer = LightPrimaryDark,
    onPrimaryContainer = LightTextPrimary,
    secondary = LightAccent,
    onSecondary = LightTextPrimary,
    tertiary = LightAccent, // Using accent for tertiary
    onTertiary = LightTextPrimary,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightBackground,
    onSurface = LightTextPrimary,
    error = Color(0xFFB00020), // Standard error color
    onError = Color.White
)

@Composable
fun GamesPCTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true, // Keep dynamic color option
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
        typography = MaterialTheme.typography, // Use default MaterialTheme typography for now
        content = content
    )
}