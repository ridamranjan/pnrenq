
package com.first.pnrenq.ui.theme

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class PnrCardTone {
    DEFAULT,
    PRIMARY,
    SECONDARY,
    TERTIARY
}

private data class SurfaceColors(
    val background: Color,
    val foreground: Color
)

@Composable
private fun cardColorsFor(
    tone: PnrCardTone
): SurfaceColors {
    val colors = MaterialTheme.colorScheme

    return when (tone) {
        PnrCardTone.DEFAULT ->
            SurfaceColors(
                colors.surfaceContainerLow,
                colors.onSurface
            )

        PnrCardTone.PRIMARY ->
            SurfaceColors(
                colors.primaryContainer,
                colors.onPrimaryContainer
            )

        PnrCardTone.SECONDARY ->
            SurfaceColors(
                colors.secondaryContainer,
                colors.onSecondaryContainer
            )

        PnrCardTone.TERTIARY ->
            SurfaceColors(
                colors.tertiaryContainer,
                colors.onTertiaryContainer
            )
    }
}

@Composable
private fun cardBrushFor(
    base: Color
): Brush {
    val settings = LocalPnrVisualSettings.current
    val scheme = MaterialTheme.colorScheme

    if (settings.style == AppThemeStyle.SIMPLIFIED) {
        return Brush.linearGradient(
            listOf(base, base)
        )
    }

    val accent = paletteColors(settings.palette).light
    val dark = scheme.background.luminance() < 0.5f

    val first = lerp(
        base,
        accent,
        if (dark) 0.11f else 0.075f
    )

    val second = lerp(
        base,
        accent,
        if (dark) 0.045f else 0.025f
    )

    return Brush.linearGradient(
        colors = listOf(
            first,
            second,
            base
        )
    )
}

@Composable
fun PnrThemeCard(
    modifier: Modifier = Modifier,
    tone: PnrCardTone = PnrCardTone.DEFAULT,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    shape: Shape = RoundedCornerShape(20.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val colors = cardColorsFor(tone)
    val settings = LocalPnrVisualSettings.current

    val brush = cardBrushFor(colors.background)

    val borderColor = if (
        settings.style == AppThemeStyle.GRADIENT
    ) {
        lerp(
            scheme.outlineVariant,
            scheme.primary,
            0.24f
        )
    } else {
        scheme.outlineVariant
    }

    val cardModifier = modifier
        .clip(shape)
        .background(brush)

    val cardColors = CardDefaults.cardColors(
        containerColor = Color.Transparent,
        contentColor = colors.foreground,
        disabledContainerColor = Color.Transparent,
        disabledContentColor = colors.foreground.copy(
            alpha = 0.65f
        )
    )

    if (onClick != null) {
        Card(
            onClick = onClick,
            enabled = enabled,
            modifier = cardModifier,
            shape = shape,
            colors = cardColors,
            border = BorderStroke(1.dp, borderColor),
            content = content
        )
    } else {
        Card(
            modifier = cardModifier,
            shape = shape,
            colors = cardColors,
            border = BorderStroke(1.dp, borderColor),
            content = content
        )
    }
}

@Composable
fun PnrThemeIconButton(
    @DrawableRes iconRes: Int,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: Dp = 48.dp,
    iconSize: Dp = 22.dp,
    tone: PnrCardTone = PnrCardTone.SECONDARY,
    tint: Color? = null
) {
    val scheme = MaterialTheme.colorScheme
    val settings = LocalPnrVisualSettings.current
    val colors = cardColorsFor(tone)

    val shape = RoundedCornerShape(15.dp)
    val brush = cardBrushFor(colors.background)

    val borderColor =
        if (settings.style == AppThemeStyle.GRADIENT) {
            lerp(
                scheme.outlineVariant,
                scheme.primary,
                0.25f
            )
        } else {
            scheme.outlineVariant
        }

    Surface(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(brush),
        shape = shape,
        color = Color.Transparent,
        contentColor = colors.foreground,
        border = BorderStroke(1.dp, borderColor)
    ) {
        IconButton(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = contentDescription,
                tint = if (enabled) {
                    tint ?: colors.foreground
                } else {
                    colors.foreground.copy(alpha = 0.38f)
                },
                modifier = Modifier.size(iconSize)
            )
        }
    }
}
