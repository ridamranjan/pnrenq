
package com.first.pnrenq.ui.theme

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
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

/* =========================================================
   CONTRAST-SAFE GRADIENT TEXT

   Dark foreground -> subtly darker gradient
   Light foreground -> subtly lighter gradient

   Never mixes text with an unrelated accent color.
========================================================= */

private fun safeGradientBrush(
    baseColor: Color
): Brush {

    val lightForeground =
        baseColor.luminance() > 0.5f

    val contrastAnchor = if (lightForeground) {
        Color.White
    } else {
        Color.Black
    }

    val middleColor = lerp(
        baseColor,
        contrastAnchor,
        0.16f
    )

    return Brush.horizontalGradient(
        colors = listOf(
            baseColor,
            middleColor,
            baseColor
        )
    )
}

/* =========================================================
   SHARED TEXT COMPONENT

   Gradient mode: subtle high-contrast gradient
   Simplified mode: exact foreground color

   Preserves labels, fonts, accessibility and theming.
========================================================= */

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

    /*
     Foreground priority:

     1. Explicit color
     2. Typography style color
     3. Current Material surface's content color

     This respects cards, buttons, alerts and app bars.
    */

    val resolvedColor = when {

        color != Color.Unspecified -> color

        style.color != Color.Unspecified -> style.color

        else -> LocalContentColor.current
    }

    if (gradientEnabled) {

        val brush = safeGradientBrush(
            resolvedColor
        )

        Text(
            text = text,
            modifier = modifier,

            // Brush supplies the foreground paint.
            color = Color.Unspecified,

            style = style.copy(
                brush = brush
            ),

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

    } else {

        Text(
            text = text,
            modifier = modifier,

            color = resolvedColor,

            style = style,

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
}
