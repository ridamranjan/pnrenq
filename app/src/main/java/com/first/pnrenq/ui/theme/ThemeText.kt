
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

private fun safeGradientBrush(
    baseColor: Color
): Brush {
    val anchor = if (baseColor.luminance() > 0.5f) {
        Color.White
    } else {
        Color.Black
    }

    val midpoint = lerp(
        baseColor,
        anchor,
        0.13f
    )

    return Brush.horizontalGradient(
        listOf(
            baseColor,
            midpoint,
            baseColor
        )
    )
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
    val gradientEnabled =
        LocalPnrVisualSettings.current.style ==
                AppThemeStyle.GRADIENT

    val inherited = LocalContentColor.current

    // Explicit foreground wins, followed by the supplied
    // text style and the current Material surface color.
    val resolvedColor = when {
        color != Color.Unspecified -> color

        style.color != Color.Unspecified -> style.color

        inherited != Color.Unspecified -> inherited

        else -> MaterialTheme.colorScheme.onBackground
    }

    val resolvedStyle = if (gradientEnabled) {
        style.copy(
            brush = safeGradientBrush(resolvedColor)
        )
    } else {
        style
    }

    Text(
        text = text,
        modifier = modifier,
        color = if (gradientEnabled) {
            Color.Unspecified
        } else {
            resolvedColor
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
