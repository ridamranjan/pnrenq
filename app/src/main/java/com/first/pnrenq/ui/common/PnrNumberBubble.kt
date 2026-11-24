package com.first.pnrenq.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.first.pnrenq.ui.theme.ThemeText as Text

@Composable
fun PnrNumberBubble(
    pnrNumber: String,
    modifier: Modifier = Modifier,
    emphasized: Boolean = false
) {
    val scheme = MaterialTheme.colorScheme

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

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        // PNR label outside the bubble.
        Text(
            text = "PNR",
            style = textStyle,
            fontWeight = FontWeight.SemiBold,
            color = labelColor,
            maxLines = 1,
            softWrap = false
        )

        // Only the 10-digit number inside the bubble.
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
                    horizontal = 12.dp,
                    vertical = 5.dp
                ),
                style = textStyle,
                fontWeight = FontWeight.Bold,
                color = foregroundColor,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}