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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

data class PaletteColors(
    val light: Color,
    val dark: Color
)

fun paletteColors(
    palette: AppThemePalette
): PaletteColors =
    when (palette) {
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
    palette: PaletteColors
): ColorScheme {
    val primary = palette.light

    val highContrast =
        mode == AppThemeMode.HIGH_CONTRAST_LIGHT

    val soft = mode == AppThemeMode.SOFT_LIGHT

    val background = when {
        highContrast -> Color.White
        soft -> lerp(
            Color(0xFFFFFCF8),
            primary,
            0.035f
        )
        else -> lerp(
            Color.White,
            primary,
            0.035f
        )
    }

    val surface = if (soft) {
        Color(0xFFFFFDFB)
    } else {
        Color.White
    }

    val foreground = if (highContrast) {
        Color(0xFF090B0F)
    } else {
        Color(0xFF171D27)
    }

    val secondaryText = if (highContrast) {
        Color(0xFF303944)
    } else {
        Color(0xFF434D5C)
    }

    return lightColorScheme(
        primary = primary,
        onPrimary = Color.White,

        primaryContainer = lerp(
            surface,
            primary,
            0.15f
        ),
        onPrimaryContainer = Color(0xFF162334),

        secondary = primary,
        onSecondary = Color.White,

        secondaryContainer = lerp(
            surface,
            primary,
            0.10f
        ),
        onSecondaryContainer = Color(0xFF1D2837),

        tertiary = primary,
        onTertiary = Color.White,

        tertiaryContainer = lerp(
            surface,
            primary,
            0.13f
        ),
        onTertiaryContainer = Color(0xFF1D2837),

        background = background,
        onBackground = foreground,

        surface = surface,
        onSurface = foreground,

        surfaceVariant = lerp(
            Color(0xFFF1F4F8),
            primary,
            0.05f
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
            0.06f
        ),
        surfaceContainerHigh = lerp(
            surface,
            primary,
            0.09f
        ),
        surfaceContainerHighest = lerp(
            surface,
            primary,
            0.12f
        ),

        outline = Color(0xFF697382),
        outlineVariant = lerp(
            Color(0xFFDCE1E8),
            primary,
            0.08f
        ),

        inverseSurface = Color(0xFF252B35),
        inverseOnSurface = Color(0xFFF4F6FA),
        inversePrimary = palette.dark,
        surfaceTint = primary,

        error = Color(0xFFB42318),
        onError = Color.White,
        errorContainer = Color(0xFFFFDAD6),
        onErrorContainer = Color(0xFF410002)
    )
}

private fun createDarkScheme(
    mode: AppThemeMode,
    palette: PaletteColors
): ColorScheme {
    val amoled = mode == AppThemeMode.AMOLED

    val highContrast =
        mode == AppThemeMode.HIGH_CONTRAST_DARK

    val soft = mode == AppThemeMode.SOFT_DARK

    val background = when {
        amoled || highContrast -> Color.Black
        soft -> Color(0xFF1B1E24)
        else -> Color(0xFF10141B)
    }

    val surface = when {
        amoled || highContrast -> Color.Black
        soft -> Color(0xFF23262D)
        else -> Color(0xFF191D25)
    }

    val foreground = if (highContrast) {
        Color.White
    } else {
        Color(0xFFF1F4FA)
    }

    val secondaryText = if (highContrast) {
        Color(0xFFE4E8F0)
    } else {
        Color(0xFFD1D8E4)
    }

    val darkBase = Color(0xFF0E131B)

    return darkColorScheme(
        primary = palette.dark,
        onPrimary = Color(0xFF101820),

        primaryContainer = lerp(
            darkBase,
            palette.light,
            0.29f
        ),
        onPrimaryContainer = Color(0xFFF7F9FF),

        secondary = palette.dark,
        onSecondary = Color(0xFF101820),

        secondaryContainer = lerp(
            darkBase,
            palette.light,
            0.22f
        ),
        onSecondaryContainer = Color(0xFFF2F6FF),

        tertiary = palette.dark,
        onTertiary = Color(0xFF101820),

        tertiaryContainer = lerp(
            darkBase,
            palette.light,
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
            Color(0xFF0B0B0B)
        } else {
            lerp(surface, palette.light, 0.025f)
        },

        surfaceContainer = if (amoled) {
            Color(0xFF121212)
        } else {
            lerp(surface, palette.light, 0.045f)
        },

        surfaceContainerHigh = if (amoled) {
            Color(0xFF1B1B1B)
        } else {
            lerp(surface, palette.light, 0.075f)
        },

        surfaceContainerHighest = if (amoled) {
            Color(0xFF252525)
        } else {
            lerp(surface, palette.light, 0.11f)
        },

        outline = Color(0xFFA4ADBB),
        outlineVariant = Color(0xFF444B57),

        inverseSurface = Color(0xFFE5E9F0),
        inverseOnSurface = Color(0xFF202730),
        inversePrimary = palette.light,
        surfaceTint = palette.dark,

        error = Color(0xFFFFB4AB),
        onError = Color(0xFF690005),
        errorContainer = Color(0xFF93000A),
        onErrorContainer = Color(0xFFFFDAD6)
    )
}

/** Pure scheme selection, shared with the contrast regression matrix. */
internal fun pnrColorScheme(
    settings: ThemeSettings,
    systemDark: Boolean
): ColorScheme {
    val dark = isDarkAppearance(
        settings.mode,
        systemDark
    )

    val palette = paletteColors(settings.palette)

    return if (dark) {
        createDarkScheme(
            settings.mode,
            palette
        )
    } else {
        createLightScheme(
            settings.mode,
            palette
        )
    }
}

@Composable
fun PnrenqTheme(
    settings: ThemeSettings = ThemeSettings(),
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()

    val dark = isDarkAppearance(
        settings.mode,
        systemDark
    )

    val visual = pnrVisualKey(
        settings,
        dark
    )

    // Neither background style nor time format changes the Material color scheme.
    val scheme = remember(
        settings.mode,
        settings.palette,
        dark
    ) {
        pnrColorScheme(
            settings,
            systemDark
        )
    }

    MaterialTheme(
        colorScheme = scheme,
        typography = Typography
    ) {
        // Use the scheme actually exposed by MaterialTheme, including versions
        // that maintain their own remembered copy.
        val activeScheme = MaterialTheme.colorScheme

        val cache = remember(
            scheme,
            activeScheme,
            visual
        ) {
            PnrThemeColorCache(
                activeScheme,
                visual
            )
        }

        CompositionLocalProvider(
            LocalPnrVisualSettings provides settings,
            LocalPnrThemeColorCache provides cache,
            LocalContentColor provides scheme.onBackground
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(scheme.background)
            ) {
                content()
            }
        }
    }
}