package com.homewifimanager.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = BrandPurple,
    onPrimary = Color.White,

    primaryContainer = BrandPurpleLight,
    onPrimaryContainer = PrimaryText,

    background = AppBackground,
    onBackground = PrimaryText,

    surface = AppSurface,
    onSurface = PrimaryText,

    surfaceVariant = AppSurface,
    onSurfaceVariant = SecondaryText,

    outline = DividerColor,

    error = LimitRed,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = BrandPurple,
    onPrimary = Color.White,

    primaryContainer = BrandPurpleDark,
    onPrimaryContainer = DarkPrimaryText,

    background = DarkBackground,
    onBackground = DarkPrimaryText,

    surface = DarkSurface,
    onSurface = DarkPrimaryText,

    surfaceVariant = DarkSurface,
    onSurfaceVariant = DarkSecondaryText,

    outline = Color(0xFF2C2D32),

    error = LimitRed,
    onError = Color.White
)

@Composable
fun HomeWiFiManagerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    val view = LocalView.current

    if (!view.isInEditMode) {
        val window = (view.context as Activity).window

        WindowCompat.getInsetsController(window, view).apply {
            isAppearanceLightStatusBars = !darkTheme
            isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}