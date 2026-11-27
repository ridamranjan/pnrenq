package com.first.pnrenq.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

/** Time format does not affect any of these color calculations. */
internal data class PnrVisualKey(
    val mode: AppThemeMode,
    val palette: AppThemePalette,
    val style: AppThemeStyle,
    val dark: Boolean
)

internal fun pnrVisualKey(
    settings: ThemeSettings,
    dark: Boolean
) = PnrVisualKey(
    settings.mode,
    settings.palette,
    settings.style,
    dark
)

/** One cache per active theme; only colors and color specifications are retained. */
internal class PnrThemeColorCache(
    private val scheme: ColorScheme,
    private val visual: PnrVisualKey
) {
    private val settings = ThemeSettings(
        visual.mode,
        visual.palette,
        visual.style
    )

    private data class AccentKey(
        val base: Color,
        val foreground: Color,
        val strength: Float
    )

    private val text =
        PnrBoundedCache<Color, List<Color>>(64)

    private val accents =
        PnrBoundedCache<AccentKey, List<Color>>(64)

    private val screens =
        PnrBoundedCache<PnrScreenType, PnrScreenColors>(4)

    internal val textEntryCount: Int
        get() = text.size

    fun matches(
        candidate: ColorScheme,
        key: PnrVisualKey
    ): Boolean =
        scheme === candidate && visual == key

    fun textColors(
        foreground: Color
    ): List<Color> =
        text.getOrCompute(foreground) {
            pnrReadableTextColors(
                foreground,
                scheme,
                settings,
                visual.dark,
                cache = this
            )
        }

    fun accentColors(
        base: Color,
        foreground: Color,
        strength: Float
    ): List<Color> =
        accents.getOrCompute(
            AccentKey(base, foreground, strength)
        ) {
            pnrAccentColors(
                base,
                foreground,
                settings,
                visual.dark,
                strength
            )
        }

    fun screenColors(
        screen: PnrScreenType
    ): PnrScreenColors =
        screens.getOrCompute(screen) {
            pnrScreenColors(
                screen,
                scheme.background,
                settings,
                visual.dark
            )
        }
}

// No process-global cache. Unwrapped previews and nested themes can use direct calculations.
internal val LocalPnrThemeColorCache =
    compositionLocalOf<PnrThemeColorCache?> {
        null
    }