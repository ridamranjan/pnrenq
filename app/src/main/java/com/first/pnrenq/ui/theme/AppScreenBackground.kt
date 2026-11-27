package com.first.pnrenq.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.lerp
import kotlin.math.max

enum class PnrScreenType {
    HOME,
    DETAILS,
    ADD_PNR,
    SETTINGS
}

/** Color-only specification shared by painting and conservative contrast references. */
internal data class PnrScreenColors(
    val base: Color,
    val solid: Color,
    val gradient: Boolean,
    val stops: List<Color>,
    val topGlow: Color?,
    val bottomGlow: Color?
) {
    fun contrastRange(): List<Color> {
        if (!gradient) {
            return listOf(solid)
        }

        if (stops.isNotEmpty()) {
            return stops
        }

        val top = requireNotNull(topGlow)
            .compositeOver(base)

        val bottom = requireNotNull(bottomGlow)

        return listOf(
            base,
            top,
            bottom.compositeOver(base),
            bottom.compositeOver(top)
        )
    }
}

internal fun pnrScreenColors(
    screen: PnrScreenType,
    base: Color,
    settings: ThemeSettings,
    dark: Boolean
): PnrScreenColors {
    val amoled =
        settings.mode == AppThemeMode.AMOLED

    val highContrast =
        settings.mode == AppThemeMode.HIGH_CONTRAST_LIGHT ||
                settings.mode == AppThemeMode.HIGH_CONTRAST_DARK

    val gradient =
        settings.style == AppThemeStyle.GRADIENT

    // All screens use the same selected palette.
    val accent = paletteColors(settings.palette).light

    val solidAmount = when (screen) {
        PnrScreenType.HOME -> 0.008f
        PnrScreenType.DETAILS -> 0.015f
        PnrScreenType.ADD_PNR -> 0.023f
        PnrScreenType.SETTINGS -> 0.013f
    }

    val solid = when {
        amoled -> Color.Black
        highContrast -> base
        else -> lerp(
            base,
            accent,
            solidAmount
        )
    }

    if (!gradient) {
        return PnrScreenColors(
            base,
            solid,
            false,
            emptyList(),
            null,
            null
        )
    }

    // Noticeable gradients without making dark modes bright.
    val strength = when {
        highContrast -> 0.12f
        amoled -> 0.24f
        dark -> 0.29f
        else -> 0.26f
    }

    val factors = when (screen) {
        PnrScreenType.HOME ->
            listOf(1f, 0.57f, 0.15f)

        PnrScreenType.DETAILS ->
            listOf(1.05f, 0.55f, 0.10f)

        PnrScreenType.ADD_PNR ->
            listOf(1f, 0.45f)

        PnrScreenType.SETTINGS ->
            emptyList()
    }

    val stops = if (factors.isEmpty()) {
        emptyList()
    } else {
        factors.map {
            lerp(
                base,
                accent,
                strength * it
            )
        } + base
    }

    val topGlow = if (
        screen == PnrScreenType.SETTINGS
    ) {
        accent.copy(
            alpha = when {
                highContrast -> 0.13f
                amoled -> 0.24f
                dark -> 0.29f
                else -> 0.25f
            }
        )
    } else {
        null
    }

    val bottomGlow = if (
        screen == PnrScreenType.SETTINGS
    ) {
        accent.copy(
            alpha = when {
                highContrast -> 0.07f
                amoled -> 0.13f
                dark -> 0.17f
                else -> 0.14f
            }
        )
    } else {
        null
    }

    return PnrScreenColors(
        base,
        solid,
        true,
        stops,
        topGlow,
        bottomGlow
    )
}

@Composable
fun AppScreenBackground(
    screen: PnrScreenType,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val settings = LocalPnrVisualSettings.current
    val scheme = MaterialTheme.colorScheme

    val dark = isDarkAppearance(
        settings.mode,
        isSystemInDarkTheme()
    )

    val visual = pnrVisualKey(
        settings,
        dark
    )

    val cache = LocalPnrThemeColorCache.current?.takeIf {
        it.matches(scheme, visual)
    }

    val colors = if (cache != null) {
        remember(screen, cache) {
            cache.screenColors(screen)
        }
    } else {
        pnrScreenColors(
            screen,
            scheme.background,
            settings,
            dark
        )
    }

    // Keep drawing objects local to this surface.
    // A size change rebuilds the brushes.
    val paintedBackground = remember(
        screen,
        colors
    ) {
        Modifier.drawWithCache {
            val width = size.width.coerceAtLeast(1f)
            val height = size.height.coerceAtLeast(1f)

            val largestSide = max(
                width,
                height
            )

            val mainBrush: Brush? =
                if (!colors.gradient) {
                    null
                } else {
                    when (screen) {

                        // Accent near the top, fading downward.
                        PnrScreenType.HOME -> {
                            Brush.verticalGradient(
                                colors = colors.stops,
                                startY = 0f,
                                endY = height
                            )
                        }

                        // Accent behind the train summary.
                        PnrScreenType.DETAILS -> {
                            Brush.verticalGradient(
                                colors = colors.stops,
                                startY = 0f,
                                endY = height * 0.90f
                            )
                        }

                        // Diagonal background for the PNR form.
                        PnrScreenType.ADD_PNR -> {
                            Brush.linearGradient(
                                colors = colors.stops,
                                start = Offset(0f, 0f),
                                end = Offset(width, height)
                            )
                        }

                        // Settings uses radial glows instead.
                        PnrScreenType.SETTINGS -> null
                    }
                }

            val topGlow = colors.topGlow?.let {
                Brush.radialGradient(
                    colors = listOf(
                        it,
                        Color.Transparent
                    ),
                    center = Offset(width, 0f),
                    radius = largestSide * 0.95f
                )
            }

            val bottomGlow = colors.bottomGlow?.let {
                Brush.radialGradient(
                    colors = listOf(
                        it,
                        Color.Transparent
                    ),
                    center = Offset(0f, height),
                    radius = largestSide * 0.85f
                )
            }

            onDrawBehind {
                if (!colors.gradient) {
                    drawRect(
                        color = colors.solid
                    )
                } else {
                    // Preserve the original base and overlay drawing order.
                    drawRect(
                        color = colors.base
                    )

                    mainBrush?.let {
                        drawRect(brush = it)
                    }

                    topGlow?.let {
                        drawRect(brush = it)
                    }

                    bottomGlow?.let {
                        drawRect(brush = it)
                    }
                }
            }
        }
    }

    CompositionLocalProvider(
        LocalContentColor provides scheme.onBackground
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .then(paintedBackground)
        ) {
            content()
        }
    }
}