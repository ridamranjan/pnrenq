
@file:OptIn(ExperimentalMaterial3Api::class)

package com.first.pnrenq.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

import com.first.pnrenq.ui.theme.AppThemeMode
import com.first.pnrenq.ui.theme.AppThemePalette
import com.first.pnrenq.ui.theme.AppThemeStyle
import com.first.pnrenq.ui.theme.AppTimeFormat
import com.first.pnrenq.ui.theme.ThemeSettings
import com.first.pnrenq.ui.theme.paletteColors
import com.first.pnrenq.ui.theme.ThemeText as Text

@Composable
fun SettingsScreen(
    settings: ThemeSettings,
    onModeSelected: (AppThemeMode) -> Unit,
    onPaletteSelected: (AppThemePalette) -> Unit,
    onStyleSelected: (AppThemeStyle) -> Unit,
    onTimeFormatSelected: (AppTimeFormat) -> Unit,
    onReset: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme

    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,

        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("← Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                )
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),

            verticalArrangement =
                Arrangement.spacedBy(18.dp)
        ) {

            Text(
                text = "Make it yours",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Mix appearance, color palette and " +
                        "background style to create your look.",
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onSurfaceVariant
            )

            /* ---------- Live preview ---------- */

            ThemePreviewCard(settings)

            /* ---------- Appearance ---------- */

            SettingsSection(
                title = "Appearance",
                subtitle = "8 display modes"
            ) {
                AppThemeMode.entries.forEach { mode ->
                    SelectionRow(
                        title = mode.title,
                        description = mode.description,
                        selected = settings.mode == mode,
                        onClick = {
                            onModeSelected(mode)
                        }
                    )
                }
            }

            /* ---------- Palettes ---------- */

            SettingsSection(
                title = "Color palette",
                subtitle = "8 coordinated palettes"
            ) {
                AppThemePalette.entries.forEach { palette ->
                    SelectionRow(
                        title = palette.title,
                        description = palette.description,
                        selected = settings.palette == palette,
                        onClick = {
                            onPaletteSelected(palette)
                        },
                        leading = {
                            PalettePreview(palette)
                        }
                    )
                }
            }

            /* ---------- Background style ---------- */

            SettingsSection(
                title = "Background style",
                subtitle = "Simplified or gradient"
            ) {
                AppThemeStyle.entries.forEach { style ->
                    SelectionRow(
                        title = style.title,
                        description = style.description,
                        selected = settings.style == style,
                        onClick = {
                            onStyleSelected(style)
                        },
                        leading = {
                            StylePreview(style)
                        }
                    )
                }
            }

            /* ---------- Time format ---------- */

            SettingsSection(
                title = "Time format",
                subtitle = "Used on Home and ticket details"
            ) {
                AppTimeFormat.entries.forEach { format ->
                    SelectionRow(
                        title = format.title,
                        description = format.description,
                        selected =
                            settings.timeFormat == format,
                        onClick = {
                            onTimeFormatSelected(format)
                        }
                    )
                }
            }

            /* ---------- Summary ---------- */

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),

                colors = CardDefaults.cardColors(
                    containerColor = scheme.primaryContainer,
                    contentColor = scheme.onPrimaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {

                    Text(
                        text = "Current preferences",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = scheme.onPrimaryContainer
                    )

                    Text(
                        text = "${settings.mode.title} · " +
                                "${settings.palette.title} · " +
                                settings.style.title,
                        style = MaterialTheme.typography.bodyMedium,
                        color = scheme.onPrimaryContainer
                    )

                    Text(
                        text = "Time: ${settings.timeFormat.title}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = scheme.onPrimaryContainer
                    )
                }
            }

            OutlinedButton(
                onClick = onReset,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Reset to Default",
                    fontWeight = FontWeight.SemiBold
                )
            }

            Text(
                text = "Reset restores System, Ocean, " +
                        "Simplified and 24-hour format.",
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant
            )

            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun ThemePreviewCard(
    settings: ThemeSettings
) {
    val scheme = MaterialTheme.colorScheme
    val palette = paletteColors(settings.palette)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),

        colors = CardDefaults.cardColors(
            containerColor = scheme.surfaceContainerLow
        ),

        border = BorderStroke(
            1.dp,
            scheme.outlineVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {

            Text(
                text = "LIVE PREVIEW",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = scheme.primary
            )

            val shape = RoundedCornerShape(18.dp)

            val previewModifier = Modifier
                .fillMaxWidth()
                .height(115.dp)
                .clip(shape)

            val backgroundBrush =
                if (settings.style == AppThemeStyle.GRADIENT) {
                    Brush.linearGradient(
                        listOf(
                            palette.light,
                            palette.dark,
                            scheme.background
                        )
                    )
                } else {
                    Brush.linearGradient(
                        listOf(
                            scheme.primaryContainer,
                            scheme.primaryContainer
                        )
                    )
                }

            Box(
                modifier = previewModifier
                    .background(backgroundBrush)
                    .padding(14.dp)
            ) {
                Column(
                    verticalArrangement =
                        Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(9.dp),
                        color = scheme.surface.copy(alpha = 0.94f)
                    ) {
                        Text(
                            text = "PNR EXPERT",
                            modifier = Modifier.padding(
                                horizontal = 12.dp,
                                vertical = 7.dp
                            ),
                            style =
                                MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = scheme.onSurface
                        )
                    }

                    Surface(
                        color = scheme.surface.copy(alpha = 0.94f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "${settings.palette.title} · " +
                                    settings.style.title,
                            modifier = Modifier.padding(9.dp),
                            style =
                                MaterialTheme.typography.labelSmall,
                            color = scheme.onSurface
                        )
                    }
                }
            }

            Text(
                text = "This is how your selected colors blend.",
                style = MaterialTheme.typography.labelSmall,
                color = scheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    subtitle: String,
    content: @Composable ColumnScope.() -> Unit
) {
    val scheme = MaterialTheme.colorScheme

    Column(
        verticalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = scheme.onSurfaceVariant
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),

            colors = CardDefaults.cardColors(
                containerColor = scheme.surfaceContainerLow
            )
        ) {
            Column(content = content)
        }
    }
}

@Composable
private fun SelectionRow(
    title: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit,
    leading: (@Composable () -> Unit)? = null
) {
    val scheme = MaterialTheme.colorScheme

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(
                    horizontal = 16.dp,
                    vertical = 12.dp
                ),

            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            leading?.invoke()

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (selected) {
                        FontWeight.SemiBold
                    } else {
                        FontWeight.Normal
                    }
                )

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant
                )
            }

            RadioButton(
                selected = selected,
                onClick = onClick
            )
        }

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp),
            color = scheme.outlineVariant
        )
    }
}

@Composable
private fun PalettePreview(
    palette: AppThemePalette
) {
    val colors = paletteColors(palette)

    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        colors.light,
                        colors.dark
                    )
                )
            )
    )
}

@Composable
private fun StylePreview(
    style: AppThemeStyle
) {
    val scheme = MaterialTheme.colorScheme

    val previewBrush =
        if (style == AppThemeStyle.GRADIENT) {
            Brush.linearGradient(
                listOf(
                    scheme.primary,
                    scheme.primaryContainer,
                    scheme.tertiary
                )
            )
        } else {
            Brush.linearGradient(
                listOf(
                    scheme.primaryContainer,
                    scheme.primaryContainer
                )
            )
        }

    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(previewBrush)
    )
}
