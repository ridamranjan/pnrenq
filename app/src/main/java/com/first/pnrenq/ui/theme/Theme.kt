
package com.first.pnrenq.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

private data class ThemeColors(
    val lightPrimary: Color,
    val lightSecondary: Color,
    val lightContainer: Color,
    val lightBackground: Color,
    val lightSurface: Color,
    val darkPrimary: Color,
    val darkSecondary: Color,
    val darkContainer: Color
)

private fun colorsFor(
    palette: AppThemePalette
): ThemeColors {
    return when (palette) {

        AppThemePalette.OCEAN -> ThemeColors(
            lightPrimary = Color(0xFF3066C2),
            lightSecondary = Color(0xFF536889),
            lightContainer = Color(0xFFD9E5FF),
            lightBackground = Color(0xFFF6F8FF),
            lightSurface = Color.White,
            darkPrimary = Color(0xFFADC7FF),
            darkSecondary = Color(0xFFB5C7E6),
            darkContainer = Color(0xFF204783)
        )

        AppThemePalette.FOREST -> ThemeColors(
            lightPrimary = Color(0xFF287D56),
            lightSecondary = Color(0xFF526D5C),
            lightContainer = Color(0xFFD0F2DD),
            lightBackground = Color(0xFFF5FAF5),
            lightSurface = Color.White,
            darkPrimary = Color(0xFF9BDDB7),
            darkSecondary = Color(0xFFB4D2BD),
            darkContainer = Color(0xFF195B3B)
        )

        AppThemePalette.SUNSET -> ThemeColors(
            lightPrimary = Color(0xFFC46335),
            lightSecondary = Color(0xFF89604B),
            lightContainer = Color(0xFFFFDCC9),
            lightBackground = Color(0xFFFFF8F3),
            lightSurface = Color.White,
            darkPrimary = Color(0xFFFFB68D),
            darkSecondary = Color(0xFFEDBEA6),
            darkContainer = Color(0xFF8D421F)
        )

        AppThemePalette.MIDNIGHT -> ThemeColors(
            lightPrimary = Color(0xFF6750B7),
            lightSecondary = Color(0xFF655D7B),
            lightContainer = Color(0xFFE7DDFF),
            lightBackground = Color(0xFFF9F7FF),
            lightSurface = Color.White,
            darkPrimary = Color(0xFFCDBDFF),
            darkSecondary = Color(0xFFC8BFE0),
            darkContainer = Color(0xFF4D388D)
        )
    }
}

private fun createLightScheme(
    colors: ThemeColors
) = lightColorScheme(
    primary = colors.lightPrimary,
    onPrimary = Color.White,

    primaryContainer = colors.lightContainer,
    onPrimaryContainer = Color(0xFF18233A),

    secondary = colors.lightSecondary,
    onSecondary = Color.White,

    secondaryContainer = colors.lightContainer,
    onSecondaryContainer = Color(0xFF18233A),

    tertiary = colors.lightPrimary,
    onTertiary = Color.White,

    background = colors.lightBackground,
    onBackground = Color(0xFF1B1D22),

    surface = colors.lightSurface,
    onSurface = Color(0xFF1B1D22),

    surfaceVariant = Color(0xFFE7E9EF),
    onSurfaceVariant = Color(0xFF474A52),

    outline = Color(0xFF777B85),
    outlineVariant = Color(0xFFD9DCE3),

    error = Color(0xFFBA1A1A),
    onError = Color.White
)

private fun createDarkScheme(
    colors: ThemeColors,
    amoled: Boolean
) = darkColorScheme(
    primary = colors.darkPrimary,
    onPrimary = Color(0xFF152036),

    primaryContainer = colors.darkContainer,
    onPrimaryContainer = Color(0xFFE4E9FF),

    secondary = colors.darkSecondary,
    onSecondary = Color(0xFF233047),

    secondaryContainer = colors.darkContainer,
    onSecondaryContainer = Color(0xFFE4E9FF),

    tertiary = colors.darkPrimary,
    onTertiary = Color(0xFF152036),

    background = if (amoled) {
        Color.Black
    } else {
        Color(0xFF111318)
    },

    onBackground = Color(0xFFE5E7EE),

    surface = if (amoled) {
        Color.Black
    } else {
        Color(0xFF171A20)
    },

    onSurface = Color(0xFFE5E7EE),

    surfaceVariant = if (amoled) {
        Color(0xFF171717)
    } else {
        Color(0xFF292D35)
    },

    onSurfaceVariant = Color(0xFFC1C5D0),

    surfaceContainerLowest = if (amoled) {
        Color.Black
    } else {
        Color(0xFF101217)
    },

    surfaceContainerLow = if (amoled) {
        Color(0xFF080808)
    } else {
        Color(0xFF1A1D23)
    },

    surfaceContainer = if (amoled) {
        Color(0xFF101010)
    } else {
        Color(0xFF202329)
    },

    surfaceContainerHigh = if (amoled) {
        Color(0xFF181818)
    } else {
        Color(0xFF292C33)
    },

    surfaceContainerHighest = if (amoled) {
        Color(0xFF222222)
    } else {
        Color(0xFF33363D)
    },

    outline = Color(0xFF8B909A),
    outlineVariant = Color(0xFF434750),

    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

@Composable
fun PnrenqTheme(
    settings: ThemeSettings = ThemeSettings(),
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()

    val dark = when (settings.mode) {
        AppThemeMode.SYSTEM -> systemDark
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
        AppThemeMode.AMOLED -> true
    }

    val amoled = settings.mode == AppThemeMode.AMOLED

    val colors = colorsFor(settings.palette)

    val scheme = if (dark) {
        createDarkScheme(
            colors = colors,
            amoled = amoled
        )
    } else {
        createLightScheme(colors)
    }

    val baseColor = scheme.background

    val accentColor = if (dark) {
        colors.darkContainer
    } else {
        colors.lightContainer
    }

    // The backdrop changes with both palette and display mode.
    val gradientBrush = Brush.linearGradient(
        colors = listOf(
            lerp(
                baseColor,
                accentColor,
                if (amoled) 0.72f else if (dark) 0.58f else 0.78f
            ),
            lerp(
                baseColor,
                accentColor,
                if (amoled) 0.28f else 0.22f
            ),
            baseColor
        )
    )

    val backgroundModifier =
        if (settings.style == AppThemeStyle.GRADIENT) {
            Modifier.background(gradientBrush)
        } else {
            Modifier.background(baseColor)
        }

    MaterialTheme(
        colorScheme = scheme,
        typography = Typography
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(backgroundModifier)
        ) {
            content()
        }
    }
}
