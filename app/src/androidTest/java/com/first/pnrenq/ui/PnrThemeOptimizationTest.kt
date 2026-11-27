package com.first.pnrenq.ui.theme

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.text.TextLayoutResult
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PnrThemeOptimizationTest {

    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private val initial = ThemeSettings(
        mode = AppThemeMode.LIGHT,
        palette = AppThemePalette.OCEAN,
        style = AppThemeStyle.GRADIENT,
        timeFormat = AppTimeFormat.HOUR_24
    )

    @Test
    fun cachedColorsAndBackgroundReferencesMatchAcrossAllThemes() {
        var cases = 0

        for (mode in AppThemeMode.entries) {
            val systemChoices = if (
                mode == AppThemeMode.SYSTEM
            ) {
                listOf(false, true)
            } else {
                listOf(false)
            }

            for (systemDark in systemChoices) {
                for (palette in AppThemePalette.entries) {
                    for (style in AppThemeStyle.entries) {
                        val settings = ThemeSettings(
                            mode,
                            palette,
                            style
                        )

                        val dark = isDarkAppearance(
                            mode,
                            systemDark
                        )

                        val scheme = pnrColorScheme(
                            settings,
                            systemDark
                        )

                        val cache = PnrThemeColorCache(
                            scheme,
                            pnrVisualKey(settings, dark)
                        )

                        val label =
                            "$mode/$systemDark/$palette/$style"

                        for (
                        role in listOf(
                            scheme.onBackground,
                            scheme.onSurface,
                            scheme.onSurfaceVariant,
                            scheme.onPrimary,
                            scheme.onPrimaryContainer,
                            scheme.onSecondary,
                            scheme.onSecondaryContainer,
                            scheme.onTertiary,
                            scheme.onTertiaryContainer,
                            scheme.onError,
                            scheme.onErrorContainer,
                            scheme.inverseOnSurface,
                            scheme.primary,
                            scheme.error,
                            scheme.onSurface.copy(alpha = 0.38f)
                        ).distinct()
                        ) {
                            val cached =
                                cache.textColors(role)

                            assertEquals(
                                label,
                                pnrReadableTextColors(
                                    role,
                                    scheme,
                                    settings,
                                    dark
                                ),
                                cached
                            )

                            assertSame(
                                cached,
                                cache.textColors(role)
                            )
                        }

                        for (
                        (bg, fg) in listOf(
                            scheme.primaryContainer to
                                    scheme.onPrimaryContainer,

                            scheme.secondaryContainer to
                                    scheme.onSecondaryContainer,

                            scheme.tertiaryContainer to
                                    scheme.onTertiaryContainer,

                            scheme.surfaceContainerLow to
                                    scheme.onSurface
                        )
                        ) {
                            for (
                            strength in listOf(
                                0.95f,
                                1f,
                                1.15f,
                                1.2f,
                                1.25f,
                                1.5f
                            )
                            ) {
                                val cached =
                                    cache.accentColors(
                                        bg,
                                        fg,
                                        strength
                                    )

                                assertEquals(
                                    label,
                                    pnrAccentColors(
                                        bg,
                                        fg,
                                        settings,
                                        dark,
                                        strength
                                    ),
                                    cached
                                )

                                assertSame(
                                    cached,
                                    cache.accentColors(
                                        bg,
                                        fg,
                                        strength
                                    )
                                )
                            }
                        }

                        for (screen in PnrScreenType.entries) {
                            val cached =
                                cache.screenColors(screen)

                            assertEquals(
                                label,
                                pnrScreenColors(
                                    screen,
                                    scheme.background,
                                    settings,
                                    dark
                                ),
                                cached
                            )

                            assertEquals(
                                label,
                                previousBackgroundRange(
                                    screen,
                                    scheme.background,
                                    settings,
                                    dark
                                ),
                                cached.contrastRange()
                            )

                            assertSame(
                                cached,
                                cache.screenColors(screen)
                            )
                        }

                        cases++
                    }
                }
            }
        }

        assertEquals(
            144,
            cases
        )
    }

    @Test
    fun actualThemeReusesCacheAndBrushesUntilVisualInputsChange() {
        val settings = mutableStateOf(initial)

        lateinit var latest: Observation

        rule.setContent {
            val current = settings.value

            PnrenqTheme(current) {
                val scheme =
                    MaterialTheme.colorScheme

                val cache = requireNotNull(
                    LocalPnrThemeColorCache.current
                )

                val first = pnrAccentBrush(
                    scheme.primaryContainer,
                    scheme.onPrimaryContainer
                )

                val second = pnrAccentBrush(
                    scheme.primaryContainer,
                    scheme.onPrimaryContainer
                )

                Column {
                    ThemeText(
                        "First example",
                        color = scheme.onBackground
                    )

                    ThemeText(
                        "Second example",
                        color = scheme.onBackground
                    )
                }

                SideEffect {
                    latest = Observation(
                        cache,
                        scheme,
                        scheme.primary,
                        first,
                        second,
                        current.timeFormat
                    )
                }
            }
        }

        val before = rule.runOnIdle {
            assertEquals(
                1,
                latest.cache.textEntryCount
            )

            // Distinct composition sites own distinct gradient brushes.
            assertNotSame(
                latest.first,
                latest.second
            )

            latest
        }

        rule.runOnIdle {
            settings.value = initial.copy(
                timeFormat = AppTimeFormat.HOUR_12
            )
        }

        rule.runOnIdle {
            assertEquals(
                AppTimeFormat.HOUR_12,
                latest.timeFormat
            )

            assertSame(
                before.cache,
                latest.cache
            )

            assertSame(
                before.scheme,
                latest.scheme
            )

            assertSame(
                before.first,
                latest.first
            )

            assertSame(
                before.second,
                latest.second
            )

            assertEquals(
                1,
                latest.cache.textEntryCount
            )
        }

        rule.runOnIdle {
            settings.value = settings.value.copy(
                style = AppThemeStyle.SIMPLIFIED
            )
        }

        val simplified = rule.runOnIdle {
            assertNotSame(
                before.cache,
                latest.cache
            )

            assertTrue(
                latest.first is SolidColor
            )

            assertEquals(
                1,
                latest.cache.textEntryCount
            )

            latest
        }

        rule.runOnIdle {
            settings.value = settings.value.copy(
                palette = AppThemePalette.FOREST
            )
        }

        val forest = rule.runOnIdle {
            assertNotSame(
                simplified.cache,
                latest.cache
            )

            assertNotEquals(
                simplified.primary,
                latest.primary
            )

            assertEquals(
                paletteColors(
                    AppThemePalette.FOREST
                ).light,
                latest.primary
            )

            latest
        }

        rule.runOnIdle {
            settings.value = settings.value.copy(
                mode = AppThemeMode.DARK
            )
        }

        rule.runOnIdle {
            assertNotSame(
                forest.cache,
                latest.cache
            )

            assertEquals(
                paletteColors(
                    AppThemePalette.FOREST
                ).dark,
                latest.primary
            )
        }
    }

    @Test
    fun cacheKeysIncludeResolvedDarknessAndStrengthButNotTimeFormat() {
        val settings = initial.copy(
            mode = AppThemeMode.SYSTEM
        )

        val scheme = pnrColorScheme(
            settings,
            false
        )

        val key = pnrVisualKey(
            settings,
            false
        )

        val cache = PnrThemeColorCache(
            scheme,
            key
        )

        assertTrue(
            cache.matches(
                scheme,
                pnrVisualKey(
                    settings.copy(
                        timeFormat = AppTimeFormat.HOUR_12
                    ),
                    false
                )
            )
        )

        assertFalse(
            cache.matches(
                scheme,
                pnrVisualKey(
                    settings,
                    true
                )
            )
        )

        assertFalse(
            cache.matches(
                pnrColorScheme(settings, true),
                key
            )
        )

        val low = cache.accentColors(
            scheme.primaryContainer,
            scheme.onPrimaryContainer,
            0f
        )

        val high = cache.accentColors(
            scheme.primaryContainer,
            scheme.onPrimaryContainer,
            1.5f
        )

        assertNotSame(
            low,
            high
        )

        assertEquals(
            pnrAccentColors(
                scheme.primaryContainer,
                scheme.onPrimaryContainer,
                settings,
                false,
                0f
            ),
            low
        )

        assertEquals(
            pnrAccentColors(
                scheme.primaryContainer,
                scheme.onPrimaryContainer,
                settings,
                false,
                1.5f
            ),
            high
        )

        assertSame(
            low,
            cache.accentColors(
                scheme.primaryContainer,
                scheme.onPrimaryContainer,
                0f
            )
        )
    }

    @Test
    fun foreignThemeDoesNotPolluteParentCacheAndCallerLayoutCallbackSurvives() {
        lateinit var parent: PnrThemeColorCache

        var matchesParent = true

        var measured: TextLayoutResult? = null

        val foreign = lightColorScheme(
            primary = Color(0xFF112233)
        )

        rule.setContent {
            PnrenqTheme(initial) {
                val cache = requireNotNull(
                    LocalPnrThemeColorCache.current
                )

                val foreground =
                    MaterialTheme.colorScheme.onBackground

                SideEffect {
                    parent = cache
                }

                Column {
                    ThemeText(
                        "Parent example",
                        color = foreground
                    )

                    MaterialTheme(
                        colorScheme = foreign
                    ) {
                        val nested =
                            MaterialTheme.colorScheme

                        SideEffect {
                            matchesParent = cache.matches(
                                nested,
                                pnrVisualKey(
                                    initial,
                                    false
                                )
                            )
                        }

                        ThemeText(
                            "Nested example",
                            color = nested.primary,
                            onTextLayout = {
                                measured = it
                            }
                        )
                    }
                }
            }
        }

        rule.runOnIdle {
            assertFalse(
                matchesParent
            )

            assertEquals(
                1,
                parent.textEntryCount
            )

            val layout = requireNotNull(
                measured
            )

            assertEquals(
                "Nested example",
                layout.layoutInput.text.text
            )

            assertFalse(
                layout.hasVisualOverflow
            )
        }
    }

    private data class Observation(
        val cache: PnrThemeColorCache,
        val scheme: ColorScheme,
        val primary: Color,
        val first: Brush,
        val second: Brush,
        val timeFormat: AppTimeFormat
    )

    /**
     * Independent fixture of the accepted Batch 7 screen color definitions.
     */
    private fun previousBackgroundRange(
        screen: PnrScreenType,
        base: Color,
        settings: ThemeSettings,
        dark: Boolean
    ): List<Color> {
        val accent =
            paletteColors(settings.palette).light

        val high =
            settings.mode == AppThemeMode.HIGH_CONTRAST_LIGHT ||
                    settings.mode == AppThemeMode.HIGH_CONTRAST_DARK

        val amoled =
            settings.mode == AppThemeMode.AMOLED

        if (
            settings.style == AppThemeStyle.SIMPLIFIED
        ) {
            val amount = when (screen) {
                PnrScreenType.HOME -> 0.008f
                PnrScreenType.DETAILS -> 0.015f
                PnrScreenType.ADD_PNR -> 0.023f
                PnrScreenType.SETTINGS -> 0.013f
            }

            return listOf(
                when {
                    amoled -> Color.Black
                    high -> base
                    else -> lerp(
                        base,
                        accent,
                        amount
                    )
                }
            )
        }

        val strength = when {
            high -> 0.12f
            amoled -> 0.24f
            dark -> 0.29f
            else -> 0.26f
        }

        return when (screen) {
            PnrScreenType.HOME -> {
                listOf(
                    lerp(
                        base,
                        accent,
                        strength
                    ),
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
                )
            }

            PnrScreenType.DETAILS -> {
                listOf(
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
                )
            }

            PnrScreenType.ADD_PNR -> {
                listOf(
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
                )
            }

            PnrScreenType.SETTINGS -> {
                val top = accent.copy(
                    alpha = when {
                        high -> 0.13f
                        amoled -> 0.24f
                        dark -> 0.29f
                        else -> 0.25f
                    }
                ).compositeOver(base)

                val bottom = accent.copy(
                    alpha = when {
                        high -> 0.07f
                        amoled -> 0.13f
                        dark -> 0.17f
                        else -> 0.14f
                    }
                )

                listOf(
                    base,
                    top,
                    bottom.compositeOver(base),
                    bottom.compositeOver(top)
                )
            }
        }
    }
}