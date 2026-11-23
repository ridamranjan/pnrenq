
package com.first.pnrenq.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.LocalContentColor
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

    AppThemePalette.OCEAN -> PaletteColors(
        Color(0xFF2759AD),
        Color(0xFFB5CFFF)
    )

    AppThemePalette.FOREST -> PaletteColors(
        Color(0xFF23754C),
        Color(0xFFA9E1BD)
    )

    AppThemePalette.SUNSET -> PaletteColors(
        Color(0xFFB44F27),
        Color(0xFFFFBE98)
    )

    AppThemePalette.MIDNIGHT -> PaletteColors(
        Color(0xFF5740A3),
        Color(0xFFD0C3FF)
    )

    AppThemePalette.ROSE -> PaletteColors(
        Color(0xFFA7356B),
        Color(0xFFFFB7D6)
    )

    AppThemePalette.LAGOON -> PaletteColors(
        Color(0xFF006F78),
        Color(0xFF8FE6E6)
    )

    AppThemePalette.AMBER -> PaletteColors(
        Color(0xFF986108),
        Color(0xFFFFD68A)
    )

    AppThemePalette.SLATE -> PaletteColors(
        Color(0xFF365B72),
        Color(0xFFBDD7E6)
    )
}

fun isDarkAppearance(
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

private fun createLightScheme(
    mode: AppThemeMode,
    colors: PaletteColors
): ColorScheme {

    val highContrast =
        mode == AppThemeMode.HIGH_CONTRAST_LIGHT

    val soft =
        mode == AppThemeMode.SOFT_LIGHT

    val primary = colors.light

    val background = when {
        highContrast -> Color.White

        soft -> lerp(
            Color(0xFFFFFCF8),
            primary,
            0.045f
        )

        else -> lerp(
            Color.White,
            primary,
            0.045f
        )
    }

    val surface = if (soft) {
        Color(0xFFFFFDFB)
    } else {
        Color.White
    }

    val foreground = if (highContrast) {
        Color(0xFF080B10)
    } else {
        Color(0xFF171D27)
    }

    val secondaryText = if (highContrast) {
        Color(0xFF303944)
    } else {
        Color(0xFF424B58)
    }

    return lightColorScheme(
        primary = primary,
        onPrimary = Color.White,

        primaryContainer = lerp(
            Color.White,
            primary,
            0.15f
        ),
        onPrimaryContainer = Color(0xFF151D29),

        secondary = primary,
        onSecondary = Color.White,

        secondaryContainer = lerp(
            Color.White,
            primary,
            0.10f
        ),
        onSecondaryContainer = Color(0xFF1C2633),

        tertiary = primary,
        onTertiary = Color.White,

        tertiaryContainer = lerp(
            Color.White,
            primary,
            0.13f
        ),
        onTertiaryContainer = Color(0xFF1C2633),

        background = background,
        onBackground = foreground,

        surface = surface,
        onSurface = foreground,

        surfaceVariant = lerp(
            Color(0xFFF3F5F8),
            primary,
            0.055f
        ),
        onSurfaceVariant = secondaryText,

        surfaceContainerLowest = surface,

        surfaceContainerLow = lerp(
            surface,
            primary,
            0.035f
        ),

        surfaceContainer = lerp(
            surface,
            primary,
            0.065f
        ),

        surfaceContainerHigh = lerp(
            surface,
            primary,
            0.095f
        ),

        surfaceContainerHighest = lerp(
            surface,
            primary,
            0.13f
        ),

        outline = Color(0xFF697382),

        outlineVariant = lerp(
            Color(0xFFDCE1E8),
            primary,
            0.09f
        ),

        inverseSurface = Color(0xFF252B35),
        inverseOnSurface = Color(0xFFF4F6FA),
        inversePrimary = colors.dark,

        surfaceTint = primary,

        error = Color(0xFFB42318),
        onError = Color.White,

        errorContainer = Color(0xFFFFDAD6),
        onErrorContainer = Color(0xFF410002)
    )
}

private fun createDarkScheme(
    mode: AppThemeMode,
    colors: PaletteColors
): ColorScheme {

    val amoled =
        mode == AppThemeMode.AMOLED

    val highContrast =
        mode == AppThemeMode.HIGH_CONTRAST_DARK

    val soft =
        mode == AppThemeMode.SOFT_DARK

    val background = when {
        amoled || highContrast -> Color.Black
        soft -> Color(0xFF1B1E24)
        else -> Color(0xFF10141B)
    }

    val surface = when {
        amoled || highContrast -> Color.Black
        soft -> Color(0xFF22262D)
        else -> Color(0xFF181D25)
    }

    // Every dark appearance gets an explicitly light foreground.
    val foreground = if (highContrast) {
        Color.White
    } else {
        Color(0xFFF0F3F9)
    }

    val secondaryText = if (highContrast) {
        Color(0xFFE3E8F2)
    } else {
        Color(0xFFD4DAE5)
    }

    val darkBase = Color(0xFF0D1219)

    return darkColorScheme(
        primary = colors.dark,
        onPrimary = Color(0xFF101820),

        primaryContainer = lerp(
            darkBase,
            colors.light,
            0.29f
        ),
        onPrimaryContainer = Color(0xFFF7F9FF),

        secondary = colors.dark,
        onSecondary = Color(0xFF101820),

        secondaryContainer = lerp(
            darkBase,
            colors.light,
            0.22f
        ),
        onSecondaryContainer = Color(0xFFF2F6FF),

        tertiary = colors.dark,
        onTertiary = Color(0xFF101820),

        tertiaryContainer = lerp(
            darkBase,
            colors.light,
            0.25f
        ),
        onTertiaryContainer = Color(0xFFF2F6FF),

        background = background,
        onBackground = foreground,

        surface = surface,
        onSurface = foreground,

        surfaceVariant = Color(0xFF292F39),
        onSurfaceVariant = secondaryText,

        surfaceContainerLowest = if (amoled) {
            Color.Black
        } else {
            Color(0xFF11151B)
        },

        surfaceContainerLow = if (amoled) {
            Color(0xFF0A0A0A)
        } else {
            lerp(
                surface,
                colors.light,
                0.025f
            )
        },

        surfaceContainer = if (amoled) {
            Color(0xFF111111)
        } else {
            lerp(
                surface,
                colors.light,
                0.045f
            )
        },

        surfaceContainerHigh = if (amoled) {
            Color(0xFF191919)
        } else {
            lerp(
                surface,
                colors.light,
                0.075f
            )
        },

        surfaceContainerHighest = if (amoled) {
            Color(0xFF232323)
        } else {
            lerp(
                surface,
                colors.light,
                0.11f
            )
        },

        outline = Color(0xFFA4ADBB),
        outlineVariant = Color(0xFF444B57),

        inverseSurface = Color(0xFFE5E9F0),
        inverseOnSurface = Color(0xFF202730),
        inversePrimary = colors.light,

        surfaceTint = colors.dark,

        error = Color(0xFFFFB4AB),
        onError = Color(0xFF690005),

        errorContainer = Color(0xFF93000A),
        onErrorContainer = Color(0xFFFFDAD6)
    )
}

private fun createBackgroundBrush(
    scheme: ColorScheme,
    colors: PaletteColors,
    settings: ThemeSettings,
    dark: Boolean
): Brush {

    val base = scheme.background

    if (settings.style == AppThemeStyle.SIMPLIFIED) {
        return Brush.linearGradient(
            listOf(base, base)
        )
    }

    return if (dark) {

        val intensity = when (settings.mode) {
            AppThemeMode.AMOLED -> 0.32f
            AppThemeMode.HIGH_CONTRAST_DARK -> 0.17f
            AppThemeMode.SOFT_DARK -> 0.17f
            else -> 0.24f
        }

        Brush.linearGradient(
            colors = listOf(
                lerp(base, colors.light, intensity),
                lerp(base, colors.light, 0.065f),
                base
            )
        )

    } else {

        val intensity =
            if (
                settings.mode ==
                AppThemeMode.HIGH_CONTRAST_LIGHT
            ) {
                0.09f
            } else {
                0.135f
            }

        Brush.linearGradient(
            colors = listOf(
                lerp(base, colors.light, intensity),
                lerp(base, colors.dark, 0.10f),
                base
            )
        )
    }
}

@Composable
fun PnrenqTheme(
    settings: ThemeSettings = ThemeSettings(),
    content: @Composable () -> Unit
) {
    val dark = isDarkAppearance(
        mode = settings.mode,
        systemDark = isSystemInDarkTheme()
    )

    val colors = paletteColors(settings.palette)

    val scheme = if (dark) {
        createDarkScheme(settings.mode, colors)
    } else {
        createLightScheme(settings.mode, colors)
    }

    val backgroundBrush = createBackgroundBrush(
        scheme = scheme,
        colors = colors,
        settings = settings,
        dark = dark
    )

    MaterialTheme(
        colorScheme = scheme,
        typography = Typography
    ) {
        // Critical: set the app-wide content color explicitly.
        // Transparent screen Scaffolds inherit this color.
        CompositionLocalProvider(
            LocalPnrVisualSettings provides settings,
            LocalContentColor provides scheme.onBackground
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
