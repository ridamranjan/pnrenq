package com.first.pnrenq.ui.common

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.first.pnrenq.ui.theme.LocalPnrVisualSettings
import com.first.pnrenq.ui.theme.isDarkAppearance
import com.first.pnrenq.ui.theme.pnrTextBackgroundGroups
import com.first.pnrenq.ui.theme.readablePnrControl
import com.first.pnrenq.ui.theme.ThemeText as Text

/** Resolve an enabled control against a known opaque surface. */
internal fun pnrControlColor(
    preferred: Color,
    background: Color
): Color = pnrControlColor(
    preferred,
    listOf(listOf(background))
)

/** Resolve an enabled control against separate solid or gradient ranges. */
internal fun pnrControlColor(
    preferred: Color,
    backgrounds: List<List<Color>>
): Color = Color(
    readablePnrControl(
        preferred.toArgb(),
        backgrounds.map { range ->
            range.map { it.toArgb() }
        }
    )
)

/** Use the accepted screen-background contrast references for exposed controls. */
@Composable
internal fun pnrScreenControlColor(
    preferred: Color
): Color {
    val scheme = MaterialTheme.colorScheme
    val settings = LocalPnrVisualSettings.current

    val dark = isDarkAppearance(
        settings.mode,
        isSystemInDarkTheme()
    )

    return pnrControlColor(
        preferred,
        pnrTextBackgroundGroups(
            scheme.onBackground,
            scheme,
            settings,
            dark
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PnrAdaptiveTopBar(
    title: String,
    onBack: () -> Unit,
    enabled: Boolean = true
) {
    val scheme = MaterialTheme.colorScheme

    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth()
    ) {
        val expanded =
            maxWidth < 260.dp ||
                    LocalDensity.current.fontScale >= 1.3f

        if (expanded) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .heightIn(min = 64.dp)
                    .padding(end = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PnrBackButton(
                    onClick = onBack,
                    enabled = enabled
                )

                Text(
                    text = title,
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 12.dp),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onBackground
                )
            }
        } else {
            TopAppBar(
                title = {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        color = scheme.onBackground
                    )
                },
                navigationIcon = {
                    PnrBackButton(
                        onClick = onBack,
                        enabled = enabled
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent,
                    titleContentColor = scheme.onBackground,
                    navigationIconContentColor = scheme.onBackground
                )
            )
        }
    }
}