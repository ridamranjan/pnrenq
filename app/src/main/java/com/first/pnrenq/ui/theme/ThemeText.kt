
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
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit

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
    val isGradient =
        LocalPnrVisualSettings.current.style ==
                AppThemeStyle.GRADIENT

    val baseColor = when {
        color != Color.Unspecified -> color
        style.color != Color.Unspecified -> style.color
        else -> LocalContentColor.current
    }

    val effectiveStyle = if (isGradient) {
        val primary = MaterialTheme.colorScheme.primary

        val brush = Brush.horizontalGradient(
            listOf(
                baseColor,
                lerp(baseColor, primary, 0.18f),
                baseColor
            )
        )

        style.copy(brush = brush)
    } else {
        style
    }

    Text(
        text = text,
        modifier = modifier,
        color = if (isGradient) {
            Color.Unspecified
        } else {
            color
        },
        style = effectiveStyle,
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
