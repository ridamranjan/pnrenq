
package com.first.pnrenq.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

import kotlin.math.max

/* =========================================================
   SCREEN TYPES
========================================================= */

enum class PnrScreenType {
    HOME,
    DETAILS,
    ADD_PNR,
    SETTINGS
}

/* =========================================================
   SCREEN-SPECIFIC BACKGROUND

   One color system for the entire app.
   Four different background patterns.

   Supports:
   - 8 appearance modes
   - 8 palettes
   - Simplified / Gradient
   - AMOLED
   - High contrast modes
========================================================= */

@Composable
fun AppScreenBackground(
    screen: PnrScreenType,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val settings = LocalPnrVisualSettings.current
    val scheme = MaterialTheme.colorScheme

    val dark = isDarkAppearance(
        mode = settings.mode,
        systemDark = isSystemInDarkTheme()
    )

    val amoled = settings.mode == AppThemeMode.AMOLED

    val highContrast =
        settings.mode == AppThemeMode.HIGH_CONTRAST_LIGHT ||
                settings.mode == AppThemeMode.HIGH_CONTRAST_DARK

    val palette = paletteColors(settings.palette)

    val base = scheme.background

    // The light palette shade acts as the accent.
    // It is blended into the background at low intensity.
    val accent = palette.light

    /* =====================================================
       SIMPLIFIED COLORS

       Distinct but restrained solid backgrounds.

       AMOLED remains black in Simplified mode.
    ===================================================== */

    val simplifiedColor = if (amoled) {
        Color.Black
    } else {
        val amount = when (screen) {
            PnrScreenType.HOME -> 0f
            PnrScreenType.DETAILS -> if (dark) 0.020f else 0.012f
            PnrScreenType.ADD_PNR -> if (dark) 0.035f else 0.025f
            PnrScreenType.SETTINGS -> if (dark) 0.014f else 0.009f
        }

        lerp(base, accent, amount)
    }

    /* =====================================================
       GRADIENT INTENSITY

       Keep dark modes dark enough for white text.

       Reduce gradients in high contrast modes.

       AMOLED gets restrained color glow on black.
    ===================================================== */

    val intensity = when {
        highContrast -> if (dark) 0.11f else 0.065f
        amoled -> 0.23f
        dark -> 0.21f
        else -> 0.15f
    }

    val gradientEnabled =
        settings.style == AppThemeStyle.GRADIENT

    /* =====================================================
       DRAW BACKGROUND

       Cached brushes avoid repeated shader allocation
       during ordinary frame drawing.
    ===================================================== */

    val backgroundModifier = Modifier.drawWithCache {

        val width = size.width.coerceAtLeast(1f)
        val height = size.height.coerceAtLeast(1f)

        val maxDimension = max(width, height)

        val mainBrush: Brush? =
            if (!gradientEnabled) {
                null
            } else {
                when (screen) {

                    /* -------------------------------------
                       HOME — SIGNATURE GRADIENT
                    ------------------------------------- */

                    PnrScreenType.HOME -> {
                        Brush.verticalGradient(
                            colors = listOf(
                                lerp(
                                    base,
                                    accent,
                                    intensity
                                ),
                                lerp(
                                    base,
                                    accent,
                                    intensity * 0.48f
                                ),
                                base
                            ),
                            startY = 0f,
                            endY = height
                        )
                    }

                    /* -------------------------------------
                       DETAILS — JOURNEY GRADIENT

                       Most noticeable behind the train
                       summary, fading toward lower cards.
                    ------------------------------------- */

                    PnrScreenType.DETAILS -> {
                        Brush.verticalGradient(
                            colors = listOf(
                                lerp(
                                    base,
                                    accent,
                                    intensity * 1.13f
                                ),
                                lerp(
                                    base,
                                    accent,
                                    intensity * 0.20f
                                ),
                                base,
                                base
                            ),
                            startY = 0f,
                            endY = height * 0.85f
                        )
                    }

                    /* -------------------------------------
                       ADD PNR — DIAGONAL GRADIENT
                    ------------------------------------- */

                    PnrScreenType.ADD_PNR -> {
                        Brush.linearGradient(
                            colors = listOf(
                                lerp(
                                    base,
                                    accent,
                                    intensity * 0.85f
                                ),
                                lerp(
                                    base,
                                    accent,
                                    intensity * 0.33f
                                ),
                                base
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(width, height)
                        )
                    }

                    /* -------------------------------------
                       SETTINGS — CORNER GLOWS

                       Drawn separately below.
                    ------------------------------------- */

                    PnrScreenType.SETTINGS -> null
                }
            }

        // Ambient gradients are used only on Settings.
        val firstGlow =
            if (
                gradientEnabled &&
                screen == PnrScreenType.SETTINGS
            ) {
                Brush.radialGradient(
                    colors = listOf(
                        accent.copy(
                            alpha = if (dark) 0.19f else 0.12f
                        ),
                        Color.Transparent
                    ),
                    center = Offset(width, 0f),
                    radius = maxDimension * 0.92f
                )
            } else {
                null
            }

        val secondGlow =
            if (
                gradientEnabled &&
                screen == PnrScreenType.SETTINGS
            ) {
                Brush.radialGradient(
                    colors = listOf(
                        accent.copy(
                            alpha = if (dark) 0.12f else 0.075f
                        ),
                        Color.Transparent
                    ),
                    center = Offset(0f, height),
                    radius = maxDimension * 0.82f
                )
            } else {
                null
            }

        onDrawBehind {

            if (!gradientEnabled) {

                drawRect(
                    color = simplifiedColor
                )

            } else {

                // Opaque base prevents the previous screen
                // background from showing through.
                drawRect(
                    color = base
                )

                mainBrush?.let { brush ->
                    drawRect(brush = brush)
                }

                firstGlow?.let { brush ->
                    drawRect(brush = brush)
                }

                secondGlow?.let { brush ->
                    drawRect(brush = brush)
                }
            }
        }
    }

    /* =====================================================
       CORRECT FOREGROUND FOR EVERY SCREEN

       Cards can still override this through their
       Material contentColor properties.
    ===================================================== */

    CompositionLocalProvider(
        LocalContentColor provides scheme.onBackground
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .then(backgroundModifier)
        ) {
            content()
        }
    }
}
