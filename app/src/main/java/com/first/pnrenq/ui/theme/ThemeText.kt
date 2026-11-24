
package com.first.pnrenq.ui.theme

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import kotlin.math.max
import kotlin.math.min

private fun textContrast(
    foreground: Color,
    background: Color
): Float {
    val a = foreground.luminance()
    val b = background.luminance()

    return (max(a, b) + 0.05f) /
            (min(a, b) + 0.05f)
}

@Composable
private fun expectedTextBackground(
    foreground: Color
): Color {
    val c = MaterialTheme.colorScheme

    return when (foreground) {
        c.onBackground -> c.background
        c.onSurface -> c.surfaceContainerLow
        c.onSurfaceVariant -> c.surfaceContainerLow

        c.onPrimary -> c.primary
        c.onPrimaryContainer -> c.primaryContainer

        c.onSecondary -> c.secondary
        c.onSecondaryContainer -> c.secondaryContainer

        c.onTertiary -> c.tertiary
        c.onTertiaryContainer -> c.tertiaryContainer

        c.onError -> c.error
        c.onErrorContainer -> c.errorContainer

        else -> c.surfaceContainerLow
    }
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

    val gradient =
        LocalPnrVisualSettings.current.style ==
                AppThemeStyle.GRADIENT

    val foreground = when {
        color != Color.Unspecified -> color
        style.color != Color.Unspecified -> style.color
        LocalContentColor.current != Color.Unspecified ->
            LocalContentColor.current
        else -> scheme.onBackground
    }

    val referenceBackground =
        expectedTextBackground(foreground)

    val accent = scheme.primary

    val strongAccent = lerp(
        foreground,
        accent,
        0.42f
    )

    val mediumAccent = lerp(
        foreground,
        accent,
        0.26f
    )

    val contrastAnchor =
        if (referenceBackground.luminance() < 0.5f) {
            Color.White
        } else {
            Color.Black
        }

    val fallbackAccent = lerp(
        foreground,
        contrastAnchor,
        0.20f
    )

    val gradientMiddle = when {
        strongAccent != foreground &&
                textContrast(
                    strongAccent,
                    referenceBackground
                ) >= 4.5f -> strongAccent

        mediumAccent != foreground &&
                textContrast(
                    mediumAccent,
                    referenceBackground
                ) >= 4.5f -> mediumAccent

        else -> fallbackAccent
    }

    val resolvedStyle = if (gradient) {
        style.copy(
            brush = Brush.horizontalGradient(
                listOf(
                    foreground,
                    gradientMiddle,
                    foreground
                )
            )
        )
    } else {
        style.copy(color = foreground)
    }

    Text(
        text = text,
        modifier = modifier,
        color = if (gradient) {
            Color.Unspecified
        } else {
            foreground
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
        onTextLayout = onTextLayout
    )
}
