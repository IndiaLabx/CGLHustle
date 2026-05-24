package com.cglhustle.core.ui.theme

import android.app.Activity
import android.os.Build
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
    primary = BrandPrimaryDark,
    onPrimary = BackgroundDark,
    primaryContainer = BrandPrimaryDark.copy(alpha = 0.2f),
    onPrimaryContainer = BrandPrimaryDark,

    background = BackgroundDark,
    onBackground = TextHighEmphasisDark,

    surface = SurfaceDark,
    onSurface = TextHighEmphasisDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextMediumEmphasisDark,

    outline = OutlineDark,
    outlineVariant = OutlineDark.copy(alpha = 0.5f),

    error = SemanticErrorDark,
    onError = BackgroundDark,
    errorContainer = SemanticErrorDark.copy(alpha = 0.2f),
    onErrorContainer = SemanticErrorDark
)

private val LightColorScheme = lightColorScheme(
    primary = BrandPrimaryLight,
    onPrimary = SurfaceLight,
    primaryContainer = BrandPrimaryLight.copy(alpha = 0.1f),
    onPrimaryContainer = BrandPrimaryLight,

    background = BackgroundLight,
    onBackground = TextHighEmphasisLight,

    surface = SurfaceLight,
    onSurface = TextHighEmphasisLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = TextMediumEmphasisLight,

    outline = OutlineLight,
    outlineVariant = OutlineLight.copy(alpha = 0.5f),

    error = SemanticError,
    onError = SurfaceLight,
    errorContainer = SemanticError.copy(alpha = 0.1f),
    onErrorContainer = SemanticError
)

@Composable
fun CglHustleTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Use transparent status bar so content can draw behind it cleanly if needed,
            // or match background strictly for premium feel.
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme

            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
