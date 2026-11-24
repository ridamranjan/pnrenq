
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

enum class PnrScreenType {
    HOME,
    DETAILS,
    ADD_PNR,
    SETTINGS
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

    val amoled = settings.mode == AppThemeMode.AMOLED

    val highContrast =
        settings.mode == AppThemeMode.HIGH_CONTRAST_LIGHT ||
                settings.mode == AppThemeMode.HIGH_CONTRAST_DARK

    val gradient =
        settings.style == AppThemeStyle.GRADIENT

    val palette = paletteColors(settings.palette)

    val base = scheme.background

    // All screens use the same selected palette.
    val accent = palette.light

    // Subtle coordinated solid backgrounds.
    val solidAmount = when (screen) {
        PnrScreenType.HOME -> 0.008f
        PnrScreenType.DETAILS -> 0.015f
        PnrScreenType.ADD_PNR -> 0.023f
        PnrScreenType.SETTINGS -> 0.013f
    }

    val solidBackground = when {
        amoled -> Color.Black
        highContrast -> base
        else -> lerp(base, accent, solidAmount)
    }

    // Noticeable gradients without making dark modes bright.
    val strength = when {
        highContrast && dark -> 0.12f
        highContrast -> 0.12f
        amoled -> 0.24f
        dark -> 0.29f
        else -> 0.26f
    }

    val paintedBackground = Modifier.drawWithCache {
        val width = size.width.coerceAtLeast(1f)
        val height = size.height.coerceAtLeast(1f)
        val largestSide = max(width, height)

        val mainBrush: Brush? =
            if (!gradient) {
                null
            } else {
                when (screen) {

                    // Accent near the top, fading downward.
                    PnrScreenType.HOME -> {
                        Brush.verticalGradient(
                            colors = listOf(
                                lerp(base, accent, strength),
                                lerp(
                                    base,
                                    accent,
                                    strength * 0.57f
                                ),
                                lerp(
                                    base,
                                    accent,
                                    strength * 0.15f
                                ),
                                base
                            ),
                            startY = 0f,
                            endY = height
                        )
                    }

                    // Accent behind the train summary.
                    PnrScreenType.DETAILS -> {
                        Brush.verticalGradient(
                            colors = listOf(
                                lerp(
                                    base,
                                    accent,
                                    strength * 1.05f
                                ),
                                lerp(
                                    base,
                                    accent,
                                    strength * 0.55f
                                ),
                                lerp(
                                    base,
                                    accent,
                                    strength * 0.10f
                                ),
                                base
                            ),
                            startY = 0f,
                            endY = height * 0.90f
                        )
                    }

                    // Diagonal background for the PNR form.
                    PnrScreenType.ADD_PNR -> {
                        Brush.linearGradient(
                            colors = listOf(
                                lerp(
                                    base,
                                    accent,
                                    strength
                                ),
                                lerp(
                                    base,
                                    accent,
                                    strength * 0.45f
                                ),
                                base
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(width, height)
                        )
                    }

                    // Settings uses radial glows instead.
                    PnrScreenType.SETTINGS -> null
                }
            }

        val topGlow =
            if (gradient && screen == PnrScreenType.SETTINGS) {
                Brush.radialGradient(
                    colors = listOf(
                        accent.copy(
                            alpha = when {
                                highContrast -> 0.13f
                                amoled -> 0.24f
                                dark -> 0.29f
                                else -> 0.25f
                            }
                        ),
                        Color.Transparent
                    ),
                    center = Offset(width, 0f),
                    radius = largestSide * 0.95f
                )
            } else {
                null
            }

        val bottomGlow =
            if (gradient && screen == PnrScreenType.SETTINGS) {
                Brush.radialGradient(
                    colors = listOf(
                        accent.copy(
                            alpha = when {
                                highContrast -> 0.07f
                                amoled -> 0.13f
                                dark -> 0.17f
                                else -> 0.14f
                            }
                        ),
                        Color.Transparent
                    ),
                    center = Offset(0f, height),
                    radius = largestSide * 0.85f
                )
            } else {
                null
            }

        onDrawBehind {
            if (!gradient) {
                drawRect(color = solidBackground)
            } else {
                // This is the bottom-most visible screen layer.
                drawRect(color = base)

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
