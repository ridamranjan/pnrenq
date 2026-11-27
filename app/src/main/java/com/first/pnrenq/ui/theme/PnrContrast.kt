package com.first.pnrenq.ui.theme

import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt

internal const val MIN_TEXT_CONTRAST = 4.5

private fun channel(
    color: Int,
    shift: Int
): Int = (color ushr shift) and 255

private fun opaque(color: Int): Boolean =
    channel(color, 24) == 255

private fun linear(component: Double): Double {
    val s = component / 255.0

    return if (s <= 0.04045) {
        s / 12.92
    } else {
        ((s + 0.055) / 1.055).pow(2.4)
    }
}

private fun luminance(
    red: Double,
    green: Double,
    blue: Double
): Double =
    0.2126 * linear(red) +
            0.7152 * linear(green) +
            0.0722 * linear(blue)

/** Contrast of a foreground over an opaque sRGB background, including its alpha. */
internal fun pnrContrastRatio(
    foreground: Int,
    background: Int
): Double {
    require(opaque(background))

    val alpha = channel(foreground, 24) / 255.0

    fun effective(shift: Int) =
        channel(foreground, shift) * alpha +
                channel(background, shift) * (1.0 - alpha)

    val a = luminance(
        effective(16),
        effective(8),
        effective(0)
    )

    val b = luminance(
        channel(background, 16).toDouble(),
        channel(background, 8).toDouble(),
        channel(background, 0).toDouble()
    )

    return (max(a, b) + 0.05) /
            (min(a, b) + 0.05)
}

/** Opaque sRGB interpolation, matching the channel space used for our gradient stops. */
internal fun blendPnrColor(
    start: Int,
    end: Int,
    fraction: Double
): Int {
    require(opaque(start) && opaque(end))

    val t = fraction.coerceIn(0.0, 1.0)

    fun blend(shift: Int) =
        (
                channel(start, shift) * (1 - t) +
                        channel(end, shift) * t
                ).roundToInt()

    return (255 shl 24) or
            (blend(16) shl 16) or
            (blend(8) shl 8) or
            blend(0)
}

/**
 * Conservative lower bound for opaque sRGB gradients. Channel extrema bound
 * luminance over every interpolated stop, not only at the listed endpoints.
 * If the luminance ranges overlap, no contrast above 1:1 is established.
 */
internal fun minimumPnrGradientContrast(
    foreground: List<Int>,
    background: List<Int>
): Double {
    require(
        foreground.isNotEmpty() &&
                background.isNotEmpty()
    )

    if (
        foreground.any { !opaque(it) } ||
        background.any { !opaque(it) }
    ) {
        return 1.0
    }

    fun bounds(colors: List<Int>): Pair<Double, Double> {
        fun low(shift: Int) =
            colors.minOf {
                channel(it, shift)
            }.toDouble()

        fun high(shift: Int) =
            colors.maxOf {
                channel(it, shift)
            }.toDouble()

        return luminance(
            low(16),
            low(8),
            low(0)
        ) to luminance(
            high(16),
            high(8),
            high(0)
        )
    }

    val (fLow, fHigh) = bounds(foreground)
    val (bLow, bHigh) = bounds(background)

    return when {
        fHigh < bLow ->
            (bLow + 0.05) / (fHigh + 0.05)

        bHigh < fLow ->
            (fLow + 0.05) / (bHigh + 0.05)

        else -> 1.0
    }
}

/** Separate surface ranges must not be combined into an invented gradient. */
internal fun minimumPnrTextContrast(
    foreground: List<Int>,
    backgrounds: List<List<Int>>
): Double {
    require(backgrounds.isNotEmpty())

    return backgrounds.minOf {
        minimumPnrGradientContrast(
            foreground,
            it
        )
    }
}

/** Retain the original color if no contrast-safe accent can be established. */
internal fun safePnrGradientStop(
    base: Int,
    accent: Int,
    oppositeRanges: List<List<Int>>,
    requestedAmount: Double,
    textGradient: Boolean,
    backgroundOverlayFraction: Double = 0.0
): Int {
    if (
        !opaque(base) ||
        !opaque(accent) ||
        oppositeRanges.isEmpty() ||
        oppositeRanges.any {
            it.isEmpty() ||
                    it.any { color -> !opaque(color) }
        }
    ) {
        return base
    }

    // Fixed grid and a monotone search: greater requested strength cannot
    // yield a weaker maximum safe blend than a smaller requested strength.
    fun candidate(step: Int) =
        blendPnrColor(
            base,
            accent,
            step / 1000.0
        )

    fun safe(step: Int): Boolean {
        val originalStops = listOf(
            base,
            candidate(step)
        )

        // Reserve contrast for the train-number surface tinted by its foreground.
        val overlay =
            oppositeRanges.singleOrNull()?.singleOrNull()

        val stops = if (
            !textGradient &&
            overlay != null &&
            backgroundOverlayFraction > 0
        ) {
            originalStops + originalStops.map {
                blendPnrColor(
                    it,
                    overlay,
                    backgroundOverlayFraction
                )
            }
        } else {
            originalStops
        }

        val ratio = if (textGradient) {
            minimumPnrTextContrast(
                stops,
                oppositeRanges
            )
        } else {
            oppositeRanges.minOf {
                minimumPnrGradientContrast(
                    it,
                    stops
                )
            }
        }

        // A small margin covers 8-bit rounding of the actual overlay.
        return ratio >= if (backgroundOverlayFraction > 0) {
            4.6
        } else {
            MIN_TEXT_CONTRAST
        }
    }

    if (!safe(0)) return base

    var low = 0

    var high =
        (requestedAmount.coerceIn(0.0, 1.0) * 1000).toInt()

    while (low < high) {
        val middle = (low + high + 1) / 2

        if (safe(middle)) {
            low = middle
        } else {
            high = middle - 1
        }
    }

    return candidate(low)
}

/** Adjust a known opaque text role only when a safe common color can be found. */
internal fun readablePnrForeground(
    foreground: Int,
    backgrounds: List<List<Int>>
): Int {
    if (
        !opaque(foreground) ||
        backgrounds.isEmpty() ||
        backgrounds.any {
            it.isEmpty() ||
                    it.any { color -> !opaque(color) }
        }
    ) {
        return foreground
    }

    fun ratio(color: Int) =
        minimumPnrTextContrast(
            listOf(color),
            backgrounds
        )

    if (ratio(foreground) >= MIN_TEXT_CONTRAST) {
        return foreground
    }

    val anchor = listOf(
        0xFF000000.toInt(),
        0xFFFFFFFF.toInt()
    ).maxByOrNull {
        ratio(it)
    }!!

    if (ratio(anchor) < MIN_TEXT_CONTRAST) {
        return foreground
    }

    for (step in 1..20) {
        val candidate = blendPnrColor(
            foreground,
            anchor,
            step / 20.0
        )

        if (ratio(candidate) >= MIN_TEXT_CONTRAST) {
            return candidate
        }
    }

    return anchor
}