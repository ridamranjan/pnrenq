package com.first.pnrenq.ui.theme

internal const val MIN_CONTROL_CONTRAST = 3.0

internal data class PnrBadgeColors(
    val foreground: Int,
    val background: Int
)

/** Preserve the booking/current hue and its existing 12% tint over the real card. */
internal fun passengerBadgeColors(
    surface: Int,
    dark: Boolean,
    current: Boolean
): PnrBadgeColors {
    val preferred = when {
        dark && current -> 0xFF86EFAC.toInt()
        dark -> 0xFFFFA9A9.toInt()
        current -> 0xFF15803D.toInt()
        else -> 0xFFB42318.toInt()
    }

    val background = blendPnrColor(
        surface,
        preferred,
        0.12
    )

    return PnrBadgeColors(
        foreground = readablePnrForeground(
            preferred,
            listOf(listOf(background))
        ),
        background = background
    )
}

/** Only adjust enabled, opaque control colors. Each range is a real surface. */
internal fun readablePnrControl(
    preferred: Int,
    backgrounds: List<List<Int>>
): Int {
    if (
        (preferred ushr 24) != 255 ||
        backgrounds.isEmpty() ||
        backgrounds.any {
            it.isEmpty() ||
                    it.any { color -> (color ushr 24) != 255 }
        }
    ) {
        return preferred
    }

    fun ratio(color: Int) =
        minimumPnrTextContrast(
            listOf(color),
            backgrounds
        )

    if (ratio(preferred) >= MIN_CONTROL_CONTRAST) {
        return preferred
    }

    val anchor = listOf(
        0xFF000000.toInt(),
        0xFFFFFFFF.toInt()
    ).maxByOrNull {
        ratio(it)
    }!!

    if (ratio(anchor) < MIN_CONTROL_CONTRAST) {
        return preferred
    }

    for (step in 1..20) {
        val candidate = blendPnrColor(
            preferred,
            anchor,
            step / 20.0
        )

        if (ratio(candidate) >= MIN_CONTROL_CONTRAST) {
            return candidate
        }
    }

    return anchor
}