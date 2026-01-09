package com.rentquest.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Purple500,
    onPrimary = Gray50,
    primaryContainer = Purple700,
    onPrimaryContainer = Purple400,
    secondary = Emerald500,
    onSecondary = Gray900,
    secondaryContainer = Emerald600,
    onSecondaryContainer = Emerald400,
    tertiary = SolanaPurple,
    onTertiary = Gray50,
    background = BackgroundDark,
    onBackground = Gray50,
    surface = SurfaceDark,
    onSurface = Gray50,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = Gray300,
    error = ErrorRed,
    onError = Gray50,
    outline = Gray600,
    outlineVariant = Gray700
)

private val LightColorScheme = lightColorScheme(
    primary = Purple500,
    onPrimary = Gray50,
    primaryContainer = Purple400,
    onPrimaryContainer = Purple700,
    secondary = Emerald500,
    onSecondary = Gray50,
    secondaryContainer = Emerald400,
    onSecondaryContainer = Emerald600,
    tertiary = SolanaPurple,
    onTertiary = Gray50,
    background = Gray50,
    onBackground = Gray900,
    surface = Gray100,
    onSurface = Gray900,
    surfaceVariant = Gray200,
    onSurfaceVariant = Gray700,
    error = ErrorRed,
    onError = Gray50,
    outline = Gray400,
    outlineVariant = Gray300
)

@Composable
fun RentQuestTheme(
    darkTheme: Boolean = true, // Default to dark theme
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
