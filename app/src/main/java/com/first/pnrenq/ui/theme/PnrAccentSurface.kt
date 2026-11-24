
package com.first.pnrenq.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import kotlin.math.max
import kotlin.math.min

private fun contrastRatio(
    foreground: Color,
    background: Color
): Float {
    val first = foreground.luminance()
    val second = background.luminance()

    return (max(first, second) + 0.05f) /
            (min(first, second) + 0.05f)
}

private fun safeAccentBlend(
    base: Color,
    accent: Color,
    foreground: Color,
    requestedAmount: Float
): Color {
    var amount = requestedAmount.coerceIn(0f, 1f)

    repeat(16) {
        val candidate = lerp(base, accent, amount)

        if (contrastRatio(foreground, candidate) >= 4.5f) {
            return candidate
        }

        amount *= 0.78f
    }

    return base
}

/**
 * Returns a solid brush in Simplified mode and a
 * matching palette gradient in Gradient mode.
 *
 * The foreground color is used for contrast checks.
 */
@Composable
fun pnrAccentBrush(
    baseColor: Color,
    foregroundColor: Color,
    strength: Float = 1f
): Brush {
    val settings = LocalPnrVisualSettings.current

    if (settings.style == AppThemeStyle.SIMPLIFIED) {
        return Brush.linearGradient(
            listOf(baseColor, baseColor)
        )
    }

    val dark = isDarkAppearance(
        mode = settings.mode,
        systemDark = isSystemInDarkTheme()
    )

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

    val startColor = safeAccentBlend(
        base = baseColor,
        accent = accent,
        foreground = foregroundColor,
        requestedAmount = intensity
    )

    val middleColor = safeAccentBlend(
        base = baseColor,
        accent = accent,
        foreground = foregroundColor,
        requestedAmount = intensity * 0.50f
    )

    return Brush.linearGradient(
        colors = listOf(
            startColor,
            middleColor,
            baseColor
        )
    )
}

/**
 * Replaces an ordinary Surface only where a
 * gradient accent is desired.
 *
 * In Simplified mode it retains the supplied
 * original solid background color.
 */
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
