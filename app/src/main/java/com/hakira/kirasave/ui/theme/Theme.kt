package com.hakira.kirasave.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

private val DarkScheme = darkColorScheme(
    primary = KiraOrange,
    onPrimary = KiraInk,
    primaryContainer = KiraOrangeDeep,
    onPrimaryContainer = KiraCream,
    secondary = KiraOliveLight,
    onSecondary = KiraOliveDeep,
    secondaryContainer = KiraOliveDeep,
    onSecondaryContainer = KiraOliveLight,
    tertiary = KiraPeach,
    onTertiary = KiraInk,
    tertiaryContainer = Color(0xFF5D3A27),
    onTertiaryContainer = KiraPeach,
    background = KiraInk,
    onBackground = KiraCream,
    surface = KiraSurface,
    onSurface = KiraCream,
    surfaceVariant = KiraSurfaceHigh,
    onSurfaceVariant = KiraMuted,
    surfaceTint = KiraOrange,
    surfaceDim = KiraInk,
    surfaceBright = Color(0xFF4A3225),
    surfaceContainerLowest = Color(0xFF120C09),
    surfaceContainerLow = KiraInkSoft,
    surfaceContainer = KiraSurface,
    surfaceContainerHigh = KiraSurfaceHigh,
    surfaceContainerHighest = Color(0xFF493227),
    outline = ColorOutlineDark,
    outlineVariant = ColorOutlineVariantDark,
    error = KiraError,
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    inverseSurface = KiraCream,
    inverseOnSurface = KiraInk,
    inversePrimary = KiraLightPrimary,
    scrim = Color.Black
)

private val LightScheme = lightColorScheme(
    primary = KiraLightPrimary,
    onPrimary = KiraCream,
    primaryContainer = KiraPeach,
    onPrimaryContainer = KiraInk,
    secondary = KiraOlive,
    onSecondary = KiraCream,
    secondaryContainer = KiraOliveLight,
    onSecondaryContainer = KiraOliveDeep,
    tertiary = KiraOrangeDeep,
    onTertiary = KiraCream,
    tertiaryContainer = Color(0xFFFFDBC8),
    onTertiaryContainer = Color(0xFF351000),
    background = KiraLightBg,
    onBackground = KiraLightInk,
    surface = KiraLightSurface,
    onSurface = KiraLightInk,
    surfaceVariant = KiraLightSurfaceVariant,
    onSurfaceVariant = KiraLightMuted,
    surfaceTint = KiraOrange,
    surfaceDim = Color(0xFFE8D5C8),
    surfaceBright = KiraLightSurface,
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = KiraLightBg,
    surfaceContainer = Color(0xFFFBEFE6),
    surfaceContainerHigh = KiraLightSurfaceVariant,
    surfaceContainerHighest = Color(0xFFEDD7C9),
    outline = ColorOutlineLight,
    outlineVariant = ColorOutlineVariantLight,
    error = ColorErrorLight,
    onError = Color.White,
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),
    inverseSurface = Color(0xFF3A2A20),
    inverseOnSurface = KiraCream,
    inversePrimary = KiraPeach,
    scrim = Color.Black
)

private val KiraShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(16.dp),
    medium = RoundedCornerShape(22.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp)
)

@Composable
fun KiraTheme(
    themeMode: String,
    content: @Composable () -> Unit
) {
    val dark = when (themeMode) {
        "LIGHT" -> false
        "SYSTEM" -> isSystemInDarkTheme()
        else -> true
    }
    val scheme = if (dark) DarkScheme else LightScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context as? Activity ?: return@SideEffect
            activity.window.statusBarColor = android.graphics.Color.TRANSPARENT
            activity.window.navigationBarColor = android.graphics.Color.TRANSPARENT
            WindowCompat.getInsetsController(activity.window, view).apply {
                isAppearanceLightStatusBars = !dark
                isAppearanceLightNavigationBars = !dark
            }
            activity.window.decorView.setBackgroundColor(scheme.background.toArgb())
        }
    }
    MaterialTheme(
        colorScheme = scheme,
        typography = KiraTypography,
        shapes = KiraShapes
    ) {
        // MaterialTheme alone does not provide LocalContentColor. Without this
        // Surface, any Text outside another Surface inherits platform black.
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = scheme.background,
            contentColor = scheme.onBackground,
            content = content
        )
    }
}
