package com.first.pnrenq.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp

/** Shared by the accent brush and text contrast reference calculation. */
internal fun pnrAccentColors(
    baseColor: Color,
    foregroundColor: Color,
    settings: ThemeSettings,
    dark: Boolean,
    strength: Float = 1f
): List<Color> {
    if (settings.style == AppThemeStyle.SIMPLIFIED) {
        return listOf(baseColor)
    }

    val palette = paletteColors(settings.palette)

    val accent = if (dark) {
        palette.dark
    } else {
        palette.light
    }

    val highContrast =
        settings.mode == AppThemeMode.HIGH_CONTRAST_LIGHT ||
                settings.mode == AppThemeMode.HIGH_CONTRAST_DARK

    val intensity = when {
        highContrast -> 0.13f
        settings.mode == AppThemeMode.AMOLED -> 0.25f
        dark -> 0.26f
        else -> 0.27f
    } * strength.coerceIn(0f, 1.5f)

    fun stop(amount: Float): Color = Color(
        safePnrGradientStop(
            baseColor.toArgb(),
            accent.toArgb(),
            listOf(
                listOf(foregroundColor.toArgb())
            ),
            amount.toDouble(),
            textGradient = false,
            backgroundOverlayFraction = 0.10
        )
    )

    return listOf(
        stop(intensity),
        stop(intensity * 0.50f),
        baseColor
    )
}

/** Solid in Simplified mode; conservative contrast-checked stops in Gradient mode. */
@Composable
fun pnrAccentBrush(
    baseColor: Color,
    foregroundColor: Color,
    strength: Float = 1f
): Brush {
    val settings = LocalPnrVisualSettings.current

    val dark = isDarkAppearance(
        settings.mode,
        isSystemInDarkTheme()
    )

    val visual = pnrVisualKey(
        settings,
        dark
    )

    val scheme = MaterialTheme.colorScheme

    val cache = LocalPnrThemeColorCache.current?.takeIf {
        it.matches(scheme, visual)
    }

    val colors = remember(
        baseColor,
        foregroundColor,
        strength,
        visual,
        cache
    ) {
        cache?.accentColors(
            baseColor,
            foregroundColor,
            strength
        ) ?: pnrAccentColors(
            baseColor,
            foregroundColor,
            settings,
            dark,
            strength
        )
    }

    // A brush belongs to this composition site, not to the shared color cache.
    return remember(colors) {
        if (colors.size == 1) {
            SolidColor(colors.first())
        } else {
            Brush.linearGradient(colors = colors)
        }
    }
}

/** Replaces an ordinary Surface only where a gradient accent is desired. */
@Composable
fun PnrAccentSurface(
    modifier: Modifier = Modifier,
    backgroundColor: Color,
    foregroundColor: Color,
    shape: Shape = RoundedCornerShape(20.dp),
    border: BorderStroke? = null,
    strength: Float = 1f,
    content: @Composable () -> Unit
) {
    val brush = pnrAccentBrush(
        baseColor = backgroundColor,
        foregroundColor = foregroundColor,
        strength = strength
    )

    Surface(
        modifier = modifier
            .clip(shape)
            .background(brush),
        shape = shape,
        color = Color.Transparent,
        contentColor = foregroundColor,
        border = border
    ) {
        Box {
            content()
        }
    }
}