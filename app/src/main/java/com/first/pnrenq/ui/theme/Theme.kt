
package com.first.pnrenq.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

/* =========================================================
   PALETTE DEFINITIONS
========================================================= */

data class PaletteColors(
    val light: Color,
    val dark: Color
)

fun paletteColors(
    palette: AppThemePalette
): PaletteColors {

    return when (palette) {

        AppThemePalette.OCEAN -> PaletteColors(
            light = Color(0xFF2759AD),
            dark = Color(0xFFB5CFFF)
        )

        AppThemePalette.FOREST -> PaletteColors(
            light = Color(0xFF23754C),
            dark = Color(0xFFA9E1BD)
        )

        AppThemePalette.SUNSET -> PaletteColors(
            light = Color(0xFFB44F27),
            dark = Color(0xFFFFBE98)
        )

        AppThemePalette.MIDNIGHT -> PaletteColors(
            light = Color(0xFF5740A3),
            dark = Color(0xFFD0C3FF)
        )

        AppThemePalette.ROSE -> PaletteColors(
            light = Color(0xFFA7356B),
            dark = Color(0xFFFFB7D6)
        )

        AppThemePalette.LAGOON -> PaletteColors(
            light = Color(0xFF006F78),
            dark = Color(0xFF8FE6E6)
        )

        AppThemePalette.AMBER -> PaletteColors(
            light = Color(0xFF986108),
            dark = Color(0xFFFFD68A)
        )

        AppThemePalette.SLATE -> PaletteColors(
            light = Color(0xFF365B72),
            dark = Color(0xFFBDD7E6)
        )
    }
}

/* =========================================================
   APPEARANCE CLASSIFICATION
========================================================= */

fun isDarkAppearance(
    mode: AppThemeMode,
    systemDark: Boolean
): Boolean {

    return when (mode) {

        AppThemeMode.SYSTEM -> systemDark

        AppThemeMode.LIGHT,
        AppThemeMode.SOFT_LIGHT,
        AppThemeMode.HIGH_CONTRAST_LIGHT -> false

        AppThemeMode.DARK,
        AppThemeMode.AMOLED,
        AppThemeMode.SOFT_DARK,
        AppThemeMode.HIGH_CONTRAST_DARK -> true
    }
}

/* =========================================================
   LIGHT COLOR SCHEME

   All normal text is dark on light surfaces.
   All tinted containers have matching dark text.
========================================================= */

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

    val primaryContainer = lerp(
        Color.White,
        primary,
        0.15f
    )

    val secondaryContainer = lerp(
        Color.White,
        primary,
        0.10f
    )

    val tertiaryContainer = lerp(
        Color.White,
        primary,
        0.13f
    )

    return lightColorScheme(

        primary = primary,
        onPrimary = Color.White,

        primaryContainer = primaryContainer,
        onPrimaryContainer = Color(0xFF151D29),

        secondary = primary,
        onSecondary = Color.White,

        secondaryContainer = secondaryContainer,
        onSecondaryContainer = Color(0xFF1C2633),

        tertiary = primary,
        onTertiary = Color.White,

        tertiaryContainer = tertiaryContainer,
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

/* =========================================================
   DARK COLOR SCHEME

   Foreground colors remain bright.
   Container surfaces remain dark even with vivid palettes.
========================================================= */

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

    val foreground = if (highContrast) {
        Color.White
    } else {
        Color(0xFFF0F3F9)
    }

    val secondaryText = if (highContrast) {
        Color(0xFFE3E8F2)
    } else {
        Color(0xFFCCD3DF)
    }

    val darkBase = Color(0xFF0D1219)

    val primaryContainer = lerp(
        darkBase,
        colors.light,
        0.29f
    )

    val secondaryContainer = lerp(
        darkBase,
        colors.light,
        0.22f
    )

    val tertiaryContainer = lerp(
        darkBase,
        colors.light,
        0.25f
    )

    return darkColorScheme(

        primary = colors.dark,
        onPrimary = Color(0xFF101820),

        primaryContainer = primaryContainer,
        onPrimaryContainer = Color(0xFFF7F9FF),

        secondary = colors.dark,
        onSecondary = Color(0xFF101820),

        secondaryContainer = secondaryContainer,
        onSecondaryContainer = Color(0xFFF2F6FF),

        tertiary = colors.dark,
        onTertiary = Color(0xFF101820),

        tertiaryContainer = tertiaryContainer,
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

/* =========================================================
   BACKGROUND BRUSH

   Simplified = solid background
   Gradient   = controlled, soft palette gradient

   Dark gradients stay dark enough for light foreground text.
========================================================= */

private fun createBackgroundBrush(
    scheme: ColorScheme,
    colors: PaletteColors,
    settings: ThemeSettings,
    dark: Boolean
): Brush {

    val base = scheme.background

    if (
        settings.style ==
        AppThemeStyle.SIMPLIFIED
    ) {
        return Brush.linearGradient(
            colors = listOf(base, base)
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
                lerp(
                    base,
                    colors.light,
                    intensity
                ),

                lerp(
                    base,
                    colors.light,
                    0.065f
                ),

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
                lerp(
                    base,
                    colors.light,
                    intensity
                ),

                lerp(
                    base,
                    colors.dark,
                    0.10f
                ),

                base
            )
        )
    }
}

/* =========================================================
   MAIN APPLICATION THEME
========================================================= */

@Composable
fun PnrenqTheme(
    settings: ThemeSettings = ThemeSettings(),
    content: @Composable () -> Unit
) {

    val systemDark = isSystemInDarkTheme()

    val dark = isDarkAppearance(
        mode = settings.mode,
        systemDark = systemDark
    )

    val colors = paletteColors(
        settings.palette
    )

    val scheme = if (dark) {
        createDarkScheme(
            mode = settings.mode,
            colors = colors
        )
    } else {
        createLightScheme(
            mode = settings.mode,
            colors = colors
        )
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
