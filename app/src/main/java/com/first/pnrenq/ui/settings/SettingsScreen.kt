
@file:OptIn(ExperimentalMaterial3Api::class)

package com.first.pnrenq.ui.settings

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

import com.first.pnrenq.ui.theme.AppThemeMode
import com.first.pnrenq.ui.theme.AppThemePalette
import com.first.pnrenq.ui.theme.AppThemeStyle
import com.first.pnrenq.ui.theme.AppTimeFormat
import com.first.pnrenq.ui.theme.ThemeSettings

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
    Scaffold(
        modifier = modifier,

        // Transparent to reveal the shared theme background.
        containerColor = Color.Transparent,

        topBar = {
            TopAppBar(
                title = {
                    Text("Settings")
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

            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            Text(
                text = "Personalize PNR EXPERT",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Choose your appearance, background " +
                        "style and time format. All changes are " +
                        "saved automatically.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            /* ---------- Appearance ---------- */

            SettingsSection(
                title = "Appearance",
                subtitle = "Control light and dark mode"
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

            /* ---------- Color palette ---------- */

            SettingsSection(
                title = "Color palette",
                subtitle = "Choose your accent colors"
            ) {
                AppThemePalette.entries.forEach { palette ->
                    SelectionRow(
                        title = palette.title,
                        description = palette.description,
                        selected = settings.palette == palette,
                        onClick = {
                            onPaletteSelected(palette)
                        },
                        leadingContent = {
                            PalettePreview(palette)
                        }
                    )
                }
            }

            /* ---------- New style selector ---------- */

            SettingsSection(
                title = "Background style",
                subtitle = "Solid or blended theme colors"
            ) {
                AppThemeStyle.entries.forEach { style ->
                    SelectionRow(
                        title = style.title,
                        description = style.description,
                        selected = settings.style == style,
                        onClick = {
                            onStyleSelected(style)
                        },
                        leadingContent = {
                            StylePreview(style)
                        }
                    )
                }
            }

            /* ---------- Time format ---------- */

            SettingsSection(
                title = "Time format",
                subtitle = "Used across the application"
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

            /* ---------- Current selection ---------- */

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),

                colors = CardDefaults.cardColors(
                    containerColor =
                        MaterialTheme.colorScheme.primaryContainer
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
                        color =
                            MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Text(
                        text = settings.mode.title +
                                " · " + settings.palette.title +
                                " · " + settings.style.title,
                        style = MaterialTheme.typography.bodyMedium,
                        color =
                            MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Text(
                        text = "Time: " + settings.timeFormat.title,
                        style = MaterialTheme.typography.bodyMedium,
                        color =
                            MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            /* ---------- Reset everything ---------- */

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

            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    subtitle: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),

            colors = CardDefaults.cardColors(
                containerColor =
                    MaterialTheme.colorScheme.surfaceContainerLow
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
    leadingContent: (@Composable () -> Unit)? = null
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(
                    horizontal = 16.dp,
                    vertical = 12.dp
                ),

            verticalAlignment = Alignment.CenterVertically
        ) {

            leadingContent?.let {
                it()
                Spacer(Modifier.width(14.dp))
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement =
                    Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight =
                        if (selected) FontWeight.SemiBold
                        else FontWeight.Normal
                )

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color =
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            RadioButton(
                selected = selected,
                onClick = onClick
            )
        }

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp),
            color = MaterialTheme.colorScheme.outlineVariant
        )
    }
}

@Composable
private fun PalettePreview(
    palette: AppThemePalette
) {
    val color = when (palette) {
        AppThemePalette.OCEAN -> Color(0xFF3066C2)
        AppThemePalette.FOREST -> Color(0xFF287D56)
        AppThemePalette.SUNSET -> Color(0xFFC46335)
        AppThemePalette.MIDNIGHT -> Color(0xFF6750B7)
    }

    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(color)
    )
}

@Composable
private fun StylePreview(
    style: AppThemeStyle
) {
    val scheme = MaterialTheme.colorScheme

    val modifier = Modifier
        .size(38.dp)
        .clip(RoundedCornerShape(12.dp))

    if (style == AppThemeStyle.GRADIENT) {
        Box(
            modifier = modifier.background(
                Brush.linearGradient(
                    colors = listOf(
                        scheme.primary,
                        scheme.primaryContainer,
                        scheme.tertiary
                    )
                )
            )
        )
    } else {
        Box(
            modifier = modifier.background(
                scheme.primaryContainer
            )
        )
    }
}
