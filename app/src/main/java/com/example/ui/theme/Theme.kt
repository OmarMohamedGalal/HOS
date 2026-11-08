package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val HosDarkColorScheme = darkColorScheme(
    primary = HosPrimary,
    onPrimary = HosOnPrimary,
    primaryContainer = HosPrimaryContainer,
    onPrimaryContainer = HosOnPrimaryContainer,
    secondary = HosSecondary,
    onSecondary = HosOnSecondary,
    secondaryContainer = HosSecondaryContainer,
    onSecondaryContainer = HosOnSecondaryContainer,
    tertiary = HosTertiary,
    onTertiary = HosOnTertiary,
    tertiaryContainer = HosTertiaryContainer,
    onTertiaryContainer = HosOnTertiaryContainer,
    background = HosSurface,
    onBackground = HosOnSurface,
    surface = HosSurface,
    onSurface = HosOnSurface,
    surfaceVariant = HosSurfaceContainerHighest,
    onSurfaceVariant = HosOnSurfaceVariant,
    surfaceTint = HosPrimary,
    inverseSurface = HosOnSurface,
    inverseOnSurface = HosSurfaceContainer,
    error = HosError,
    onError = HosOnError,
    errorContainer = HosErrorContainer,
    onErrorContainer = HosOnErrorContainer,
    outline = HosOutline,
    outlineVariant = HosOutlineVariant,
    surfaceBright = HosSurfaceBright,
    surfaceDim = HosSurfaceDim,
    surfaceContainer = HosSurfaceContainer,
    surfaceContainerHigh = HosSurfaceContainerHigh,
    surfaceContainerHighest = HosSurfaceContainerHighest,
    surfaceContainerLow = HosSurfaceContainerLow,
    surfaceContainerLowest = HosSurfaceContainerLowest
)

@Composable
fun HosTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = false
                controller.isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = HosDarkColorScheme,
        typography = HosTypography,
        shapes = HosShapes,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    HosTheme(content = content)
}
