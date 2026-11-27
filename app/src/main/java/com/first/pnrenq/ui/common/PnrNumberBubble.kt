package com.first.pnrenq.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.first.pnrenq.ui.theme.ThemeText as Text

@Composable
fun PnrNumberBubble(
    pnrNumber: String,
    modifier: Modifier = Modifier,
    emphasized: Boolean = false
) {
    val scheme = MaterialTheme.colorScheme
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()

    val backgroundColor = if (emphasized) {
        scheme.primary
    } else {
        scheme.primaryContainer
    }

    val foregroundColor = if (emphasized) {
        scheme.onPrimary
    } else {
        scheme.onPrimaryContainer
    }

    val labelColor = if (emphasized) {
        scheme.onPrimaryContainer
    } else {
        scheme.onSurfaceVariant
    }

    val borderColor = if (emphasized) {
        scheme.onPrimary.copy(alpha = 0.20f)
    } else {
        scheme.primary.copy(alpha = 0.25f)
    }

    val textStyle = if (emphasized) {
        MaterialTheme.typography.titleSmall
    } else {
        MaterialTheme.typography.labelLarge
    }

    val labelStyle = textStyle.copy(
        fontWeight = FontWeight.SemiBold
    )

    val numberStyle = textStyle.copy(
        fontWeight = FontWeight.Bold
    )

    BoxWithConstraints(
        modifier = modifier
    ) {
        /*
         * At enlarged font scales, give the PNR number
         * its own row rather than competing for horizontal
         * space with the label.
         *
         * At ordinary font scales, use measured text widths
         * to preserve the original horizontal arrangement
         * whenever both elements fit.
         */
        val stacked = if (density.fontScale >= 1.3f) {
            true
        } else if (!constraints.hasBoundedWidth) {
            false
        } else {
            val labelWidth = textMeasurer.measure(
                text = "PNR",
                style = labelStyle,
                softWrap = false,
                maxLines = 1
            ).size.width

            val numberWidth = textMeasurer.measure(
                text = pnrNumber,
                style = numberStyle,
                softWrap = false,
                maxLines = 1
            ).size.width

            val spacingWidth = with(density) {
                8.dp.roundToPx()
            }

            val bubblePaddingWidth = with(density) {
                12.dp.roundToPx() * 2
            }

            // Reserve two additional pixels for rounding differences.
            val requiredWidth =
                labelWidth +
                        numberWidth +
                        spacingWidth +
                        bubblePaddingWidth +
                        2

            requiredWidth > constraints.maxWidth
        }

        val horizontalPadding = if (stacked) {
            8.dp
        } else {
            12.dp
        }

        val labelContent: @Composable () -> Unit = {
            // PNR label outside the bubble.
            Text(
                text = "PNR",
                style = labelStyle,
                color = labelColor
            )
        }

        val numberContent: @Composable () -> Unit = {
            // Preserve the exact PNR, including leading zeros.
            Surface(
                shape = CircleShape,
                color = backgroundColor,
                contentColor = foregroundColor,
                border = BorderStroke(
                    width = 1.dp,
                    color = borderColor
                ),
                tonalElevation = 0.dp,
                shadowElevation = 0.dp
            ) {
                Text(
                    text = pnrNumber,
                    modifier = Modifier.padding(
                        horizontal = horizontalPadding,
                        vertical = 5.dp
                    ),
                    style = numberStyle,
                    color = foregroundColor,
                    textAlign = TextAlign.Center,
                    softWrap = true,
                    maxLines = Int.MAX_VALUE
                )
            }
        }

        if (stacked) {
            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                labelContent()
                numberContent()
            }
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                labelContent()
                numberContent()
            }
        }
    }
}