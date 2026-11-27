package com.first.pnrenq.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit

/**
 * Conservative references for the app's named text roles. Include all matching
 * roles rather than assuming that equal colors always describe the same surface.
 * Unknown and translucent colors are kept solid, with their requested alpha.
 * This is not a pixel-level inference of arbitrary composable backgrounds.
 */
internal fun pnrTextBackgroundGroups(
    foreground: Color,
    c: ColorScheme,
    settings: ThemeSettings,
    dark: Boolean,
    cache: PnrThemeColorCache? = null
): List<List<Color>> {
    if (foreground.alpha != 1f) {
        return emptyList()
    }

    val groups = mutableListOf<List<Color>>()

    fun solid(color: Color) {
        groups.add(listOf(color))
    }

    val normal = listOf(
        c.surface,
        c.surfaceVariant,
        c.surfaceContainerLowest,
        c.surfaceContainerLow,
        c.surfaceContainer,
        c.surfaceContainerHigh,
        c.surfaceContainerHighest
    )

    if (
        foreground in listOf(
            c.onSurface,
            c.onSurfaceVariant,
            c.primary,
            c.error
        )
    ) {
        normal.forEach(::solid)
    }

    if (
        foreground in listOf(
            c.onBackground,
            c.onSurfaceVariant,
            c.primary,
            c.error
        )
    ) {
        // Use the same color specifications that the background painter uses.
        solid(c.background)

        for (screen in PnrScreenType.entries) {
            val colors = cache?.screenColors(screen)
                ?: pnrScreenColors(
                    screen,
                    c.background,
                    settings,
                    dark
                )

            groups.add(colors.contrastRange())
        }
    }

    for (
    (on, background) in listOf(
        c.onPrimary to c.primary,
        c.onSecondary to c.secondary,
        c.onTertiary to c.tertiary,
        c.onError to c.error,
        c.onErrorContainer to c.errorContainer,
        c.inverseOnSurface to c.inverseSurface
    )
    ) {
        if (foreground == on) {
            solid(background)
        }
    }

    for (
    (on, background) in listOf(
        c.onPrimaryContainer to c.primaryContainer,
        c.onSecondaryContainer to c.secondaryContainer,
        c.onTertiaryContainer to c.tertiaryContainer,
        c.onSurface to c.surfaceContainerLow
    )
    ) {
        if (foreground == on) {
            val stops = cache?.accentColors(
                background,
                on,
                1.5f
            ) ?: pnrAccentColors(
                background,
                on,
                settings,
                dark,
                1.5f
            )

            groups.add(stops)

            // The train-number label has a 10% foreground-colored overlay.
            if (on == c.onPrimaryContainer) {
                groups.add(
                    stops.map {
                        on.copy(alpha = 0.10f)
                            .compositeOver(it)
                    }
                )
            }
        }
    }

    return groups.distinct()
}

/** First element is the solid color; additional stops enable gradient text. */
internal fun pnrReadableTextColors(
    foreground: Color,
    c: ColorScheme,
    settings: ThemeSettings,
    dark: Boolean,
    cache: PnrThemeColorCache? = null
): List<Color> {
    val backgrounds = pnrTextBackgroundGroups(
        foreground,
        c,
        settings,
        dark,
        cache
    )

    if (backgrounds.isEmpty()) {
        return listOf(foreground)
    }

    val references = backgrounds.map { range ->
        range.map { it.toArgb() }
    }

    val base = readablePnrForeground(
        foreground.toArgb(),
        references
    )

    if (settings.style != AppThemeStyle.GRADIENT) {
        return listOf(Color(base))
    }

    var middle = safePnrGradientStop(
        base,
        c.primary.toArgb(),
        references,
        requestedAmount = 0.42,
        textGradient = true
    )

    if (middle == base) {
        val anchor = listOf(
            Color.Black.toArgb(),
            Color.White.toArgb()
        ).maxByOrNull {
            minimumPnrTextContrast(
                listOf(it),
                references
            )
        }!!

        middle = safePnrGradientStop(
            base,
            anchor,
            references,
            requestedAmount = 0.20,
            textGradient = true
        )
    }

    return if (middle == base) {
        listOf(Color(base))
    } else {
        listOf(
            Color(base),
            Color(middle),
            Color(base)
        )
    }
}

/**
 * Keep the accepted full-layout path.
 *
 * This callback retains no layout or text and performs no state updates.
 */
private val fullTextLayoutCallback: (TextLayoutResult) -> Unit = {
    // No additional processing required.
}

@Composable
fun ThemeText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    style: TextStyle = LocalTextStyle.current,
    fontWeight: FontWeight? = null,
    fontStyle: FontStyle? = null,
    fontFamily: FontFamily? = null,
    fontSize: TextUnit = TextUnit.Unspecified,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    softWrap: Boolean = true,
    overflow: TextOverflow = TextOverflow.Clip,
    onTextLayout: ((TextLayoutResult) -> Unit)? = null
) {
    val scheme = MaterialTheme.colorScheme
    val settings = LocalPnrVisualSettings.current

    val dark = isDarkAppearance(
        settings.mode,
        isSystemInDarkTheme()
    )

    val foreground = when {
        color != Color.Unspecified -> color

        style.color != Color.Unspecified -> style.color

        LocalContentColor.current != Color.Unspecified ->
            LocalContentColor.current

        else -> scheme.onBackground
    }

    val visual = pnrVisualKey(
        settings,
        dark
    )

    val cache = LocalPnrThemeColorCache.current?.takeIf {
        it.matches(scheme, visual)
    }

    val colors = if (cache != null) {
        remember(foreground, cache) {
            cache.textColors(foreground)
        }
    } else {
        // A foreign MaterialTheme may update its scheme in place. Do not
        // remember results against its identity alone.
        pnrReadableTextColors(
            foreground,
            scheme,
            settings,
            dark
        )
    }

    val gradient = colors.size > 1

    // Shader brushes are size-dependent; remember per text, not in the shared cache.
    val brush = remember(colors) {
        if (gradient) {
            Brush.horizontalGradient(colors)
        } else {
            null
        }
    }

    val resolvedStyle = if (brush != null) {
        style.copy(
            brush = brush
        )
    } else {
        style.copy(
            color = colors.first()
        )
    }

    Text(
        text = text,
        modifier = modifier,
        color = if (gradient) {
            Color.Unspecified
        } else {
            colors.first()
        },
        style = resolvedStyle,
        fontWeight = fontWeight,
        fontStyle = fontStyle,
        fontFamily = fontFamily,
        fontSize = fontSize,
        letterSpacing = letterSpacing,
        textAlign = textAlign,
        lineHeight = lineHeight,
        textDecoration = textDecoration,
        maxLines = maxLines,
        minLines = minLines,
        softWrap = softWrap,
        overflow = overflow,

        // Preserve caller callbacks and the accepted full text-layout behavior.
        onTextLayout = onTextLayout ?: fullTextLayoutCallback
    )
}