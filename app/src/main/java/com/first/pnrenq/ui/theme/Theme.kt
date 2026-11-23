
package com.first.pnrenq.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

data class PaletteColors(
    val light: Color,
    val dark: Color
)

fun paletteColors(
    palette: AppThemePalette
): PaletteColors = when (palette) {

    AppThemePalette.OCEAN ->
        PaletteColors(
            Color(0xFF2759AD),
            Color(0xFFB5CFFF)
        )

    AppThemePalette.FOREST ->
        PaletteColors(
            Color(0xFF23754C),
            Color(0xFFA9E1BD)
        )

    AppThemePalette.SUNSET ->
        PaletteColors(
            Color(0xFFB44F27),
            Color(0xFFFFBE98)
        )

    AppThemePalette.MIDNIGHT ->
        PaletteColors(
            Color(0xFF5740A3),
            Color(0xFFD0C3FF)
        )

    AppThemePalette.ROSE ->
        PaletteColors(
            Color(0xFFA7356B),
            Color(0xFFFFB7D6)
        )

    AppThemePalette.LAGOON ->
        PaletteColors(
            Color(0xFF006F78),
            Color(0xFF8FE6E6)
        )

    AppThemePalette.AMBER ->
        PaletteColors(
            Color(0xFF986108),
            Color(0xFFFFD68A)
        )

    AppThemePalette.SLATE ->
        PaletteColors(
            Color(0xFF365B72),
            Color(0xFFBDD7E6)
        )
}

private fun isDarkMode(
    mode: AppThemeMode,
    systemDark: Boolean
): Boolean = when (mode) {
    AppThemeMode.SYSTEM -> systemDark
    AppThemeMode.LIGHT,
    AppThemeMode.SOFT_LIGHT,
    AppThemeMode.HIGH_CONTRAST_LIGHT -> false

    AppThemeMode.DARK,
    AppThemeMode.AMOLED,
    AppThemeMode.SOFT_DARK,
    AppThemeMode.HIGH_CONTRAST_DARK -> true
}

private fun lightScheme(
    mode: AppThemeMode,
    colors: PaletteColors
): androidx.compose.material3.ColorScheme {

    val strongContrast =
        mode == AppThemeMode.HIGH_CONTRAST_LIGHT

    val soft =
        mode == AppThemeMode.SOFT_LIGHT

    val background = when {
        strongContrast -> Color.White

        soft -> lerp(
            Color(0xFFFFFCF7),
            colors.light,
            0.055f
        )

        else -> lerp(
            Color.White,
            colors.light,
            0.055f
        )
    }

    val surface = when {
        strongContrast -> Color.White
        soft -> Color(0xFFFFFDF9)
        else -> Color.White
    }

    val primaryContainer = lerp(
        Color.White,
        colors.light,
        if (strongContrast) 0.18f else 0.15f
    )

    val secondaryContainer = lerp(
        Color.White,
        colors.light,
        0.105f
    )

    val tertiaryContainer = lerp(
        Color.White,
        colors.dark,
        0.42f
    )

    val foreground = if (strongContrast) {
        Color(0xFF090B0F)
    } else {
        Color(0xFF20232B)
    }

    return lightColorScheme(
        primary = colors.light,
        onPrimary = Color.White,
        primaryContainer = primaryContainer,
        onPrimaryContainer = Color(0xFF152033),

        secondary = colors.light,
        onSecondary = Color.White,
        secondaryContainer = secondaryContainer,
        onSecondaryContainer = Color(0xFF20232B),

        tertiary = colors.light,
        onTertiary = Color.White,
        tertiaryContainer = tertiaryContainer,
        onTertiaryContainer = Color(0xFF20232B),

        background = background,
        onBackground = foreground,

        surface = surface,
        onSurface = foreground,

        surfaceVariant = secondaryContainer,
        onSurfaceVariant =
            if (strongContrast) {
                Color(0xFF292D34)
            } else {
                Color(0xFF515763)
            },

        surfaceContainerLowest = surface,
        surfaceContainerLow = lerp(
            surface,
            colors.light,
            0.045f
        ),
        surfaceContainer = lerp(
            surface,
            colors.light,
            0.075f
        ),
        surfaceContainerHigh = lerp(
            surface,
            colors.light,
            0.115f
        ),
        surfaceContainerHighest = lerp(
            surface,
            colors.light,
            0.15f
        ),

        outline = Color(0xFF767C87),
        outlineVariant = lerp(
            Color(0xFFE0E3E9),
            colors.light,
            0.10f
        ),

        error = Color(0xFFB42318),
        onError = Color.White,
        errorContainer = Color(0xFFFFDAD6),
        onErrorContainer = Color(0xFF410002)
    )
}

private fun darkScheme(
    mode: AppThemeMode,
    colors: PaletteColors
): androidx.compose.material3.ColorScheme {

    val amoled = mode == AppThemeMode.AMOLED

    val highContrast =
        mode == AppThemeMode.HIGH_CONTRAST_DARK

    val soft =
        mode == AppThemeMode.SOFT_DARK

    val background = when {
        amoled || highContrast -> Color.Black
        soft -> Color(0xFF1C1E23)
        else -> Color(0xFF101319)
    }

    val surface = when {
        amoled || highContrast -> Color.Black
        soft -> Color(0xFF22242A)
        else -> Color(0xFF181B22)
    }

    val foreground = if (highContrast) {
        Color.White
    } else {
        Color(0xFFE7EAF0)
    }

    val darkContainer = lerp(
        Color(0xFF10141B),
        colors.light,
        0.32f
    )

    return darkColorScheme(
        primary = colors.dark,
        onPrimary = Color(0xFF10151D),

        primaryContainer = darkContainer,
        onPrimaryContainer = Color(0xFFF5F7FF),

        secondary = colors.dark,
        onSecondary = Color(0xFF10151D),

        secondaryContainer = lerp(
            Color(0xFF151920),
            colors.light,
            0.24f
        ),
        onSecondaryContainer = Color(0xFFF1F3FF),

        tertiary = colors.dark,
        onTertiary = Color(0xFF10151D),

        tertiaryContainer = lerp(
            Color(0xFF172027),
            colors.light,
            0.25f
        ),
        onTertiaryContainer = Color(0xFFF1F4F9),

        background = background,
        onBackground = foreground,

        surface = surface,
        onSurface = foreground,

        surfaceVariant = Color(0xFF2A2E35),
        onSurfaceVariant =
            if (highContrast) {
                Color(0xFFE8EBF1)
            } else {
                Color(0xFFC6CBD6)
            },

        surfaceContainerLowest =
            if (amoled) Color.Black
            else Color(0xFF13161B),

        surfaceContainerLow =
            if (amoled) Color(0xFF090909)
            else lerp(
                surface,
                colors.light,
                0.045f
            ),

        surfaceContainer =
            if (amoled) Color(0xFF101010)
            else lerp(
                surface,
                colors.light,
                0.075f
            ),

        surfaceContainerHigh =
            if (amoled) Color(0xFF191919)
            else lerp(
                surface,
                colors.light,
                0.11f
            ),

        surfaceContainerHighest =
            if (amoled) Color(0xFF222222)
            else Color(0xFF333840),

        outline = Color(0xFF939BA8),
        outlineVariant = Color(0xFF474C55),

        error = Color(0xFFFFB4AB),
        onError = Color(0xFF690005),
        errorContainer = Color(0xFF93000A),
        onErrorContainer = Color(0xFFFFDAD6)
    )
}

@Composable
fun PnrenqTheme(
    settings: ThemeSettings = ThemeSettings(),
    content: @Composable () -> Unit
) {
    val dark = isDarkMode(
        settings.mode,
        isSystemInDarkTheme()
    )

    val colors = paletteColors(settings.palette)

    val scheme = if (dark) {
        darkScheme(settings.mode, colors)
    } else {
        lightScheme(settings.mode, colors)
    }

    val backgroundBrush = if (
        settings.style == AppThemeStyle.GRADIENT
    ) {
        if (dark) {
            Brush.linearGradient(
                listOf(
                    lerp(
                        scheme.background,
                        colors.light,
                        if (
                            settings.mode == AppThemeMode.AMOLED
                        ) 0.48f else 0.38f
                    ),
                    lerp(
                        scheme.background,
                        colors.dark,
                        0.09f
                    ),
                    scheme.background
                )
            )
        } else {
            Brush.linearGradient(
                listOf(
                    lerp(
                        scheme.background,
                        colors.light,
                        0.22f
                    ),
                    lerp(
                        scheme.background,
                        colors.dark,
                        0.22f
                    ),
                    scheme.background
                )
            )
        }
    } else {
        Brush.linearGradient(
            listOf(
                scheme.background,
                scheme.background
            )
        )
    }

    MaterialTheme(
        colorScheme = scheme,
        typography = Typography
    ) {
        CompositionLocalProvider(
            LocalPnrVisualSettings provides settings
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundBrush)
            ) {
                content()
            }
        }
    }
}
