
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

import androidx.compose.runtime.saveable.rememberSaveable

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
import com.first.pnrenq.ui.theme.paletteColors
import com.first.pnrenq.ui.theme.ThemeText as Text

/* =========================================================
   SETTINGS SCREEN

   Appearance:
   - Collapsed by default
   - Shows selected mode
   - Expands when tapped

   Color palette:
   - Collapsed by default
   - Shows selected palette
   - Expands when tapped

   Other settings remain visible.
========================================================= */

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

    /*
     Only one dropdown can be open at a time.

     null = both collapsed
     "appearance" = appearance expanded
     "palette" = palette expanded
    */

    var expandedSection by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    Scaffold(
        modifier = modifier,

        containerColor = Color.Transparent,
        contentColor = scheme.onBackground,

        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        fontWeight = FontWeight.Bold,
                        color = scheme.onBackground
                    )
                },

                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text(
                            text = "← Back",
                            color = scheme.onBackground
                        )
                    }
                },

                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent,
                    titleContentColor = scheme.onBackground,
                    navigationIconContentColor = scheme.onBackground,
                    actionIconContentColor = scheme.onBackground
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

            /* =================================================
               PAGE HEADER
            ================================================= */

            Text(
                text = "Make it yours",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = scheme.onBackground
            )

            Text(
                text = "Customize your appearance, colors, " +
                        "background style and time format.",
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onBackground.copy(alpha = 0.88f)
            )

            /* =================================================
               LIVE PREVIEW
            ================================================= */

            ThemePreviewCard(settings)

            /* =================================================
               APPEARANCE — COLLAPSIBLE
            ================================================= */

            SettingsDropdownSection(
                title = "Appearance",

                selectedTitle = settings.mode.title,

                selectedDescription =
                    settings.mode.description,

                expanded =
                    expandedSection == "appearance",

                onToggle = {
                    expandedSection =
                        if (expandedSection == "appearance") {
                            null
                        } else {
                            "appearance"
                        }
                }
            ) {

                AppThemeMode.entries.forEach { mode ->

                    SettingsOptionRow(
                        title = mode.title,

                        description = mode.description,

                        selected =
                            settings.mode == mode,

                        onClick = {
                            onModeSelected(mode)

                            // Automatically collapse.
                            expandedSection = null
                        }
                    )
                }
            }

            /* =================================================
               COLOR PALETTE — COLLAPSIBLE
            ================================================= */

            SettingsDropdownSection(
                title = "Color palette",

                selectedTitle = settings.palette.title,

                selectedDescription =
                    settings.palette.description,

                expanded =
                    expandedSection == "palette",

                onToggle = {
                    expandedSection =
                        if (expandedSection == "palette") {
                            null
                        } else {
                            "palette"
                        }
                },

                selectedPreview = {
                    PalettePreview(
                        palette = settings.palette
                    )
                }
            ) {

                AppThemePalette.entries.forEach { palette ->

                    SettingsOptionRow(
                        title = palette.title,

                        description = palette.description,

                        selected =
                            settings.palette == palette,

                        onClick = {
                            onPaletteSelected(palette)

                            // Automatically collapse.
                            expandedSection = null
                        },

                        leading = {
                            PalettePreview(
                                palette = palette
                            )
                        }
                    )
                }
            }

            /* =================================================
               BACKGROUND STYLE

               Remains expanded, as before.
            ================================================= */

            SettingsSection(
                title = "Background style",
                subtitle = "Solid or gradient backgrounds"
            ) {

                AppThemeStyle.entries.forEach { style ->

                    SettingsOptionRow(
                        title = style.title,

                        description = style.description,

                        selected =
                            settings.style == style,

                        onClick = {
                            onStyleSelected(style)
                        },

                        leading = {
                            StylePreview(style)
                        }
                    )
                }
            }

            /* =================================================
               TIME FORMAT

               Remains expanded, as before.
            ================================================= */

            SettingsSection(
                title = "Time format",
                subtitle = "Used across the application"
            ) {

                AppTimeFormat.entries.forEach { format ->

                    SettingsOptionRow(
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

            /* =================================================
               CURRENT PREFERENCES
            ================================================= */

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

                        style =
                            MaterialTheme.typography.titleMedium,

                        fontWeight = FontWeight.Bold,

                        color = scheme.onPrimaryContainer
                    )

                    Text(
                        text = "${settings.mode.title} · " +
                                "${settings.palette.title} · " +
                                settings.style.title,

                        style =
                            MaterialTheme.typography.bodyMedium,

                        color = scheme.onPrimaryContainer
                    )

                    Text(
                        text = "Time: ${settings.timeFormat.title}",

                        style =
                            MaterialTheme.typography.bodyMedium,

                        color = scheme.onPrimaryContainer
                    )
                }
            }

            /* =================================================
               RESET ALL SETTINGS
            ================================================= */

            OutlinedButton(
                onClick = {
                    expandedSection = null
                    onReset()
                },

                modifier = Modifier.fillMaxWidth(),

                shape = RoundedCornerShape(16.dp),

                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = scheme.primary
                ),

                border = BorderStroke(
                    1.dp,
                    scheme.outline
                )
            ) {

                Text(
                    text = "Reset to Default",
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.primary
                )
            }

            Text(
                text = "Reset restores System, Ocean, " +
                        "Simplified and 24-hour format.",

                style = MaterialTheme.typography.bodySmall,

                color = scheme.onBackground.copy(alpha = 0.85f)
            )

            Spacer(Modifier.height(12.dp))
        }
    }
}

/* =========================================================
   COLLAPSIBLE SETTINGS CARD

   Only selected option is shown when collapsed.

   Expanding reveals all choices inline.

   No expand/collapse animation.
========================================================= */

@Composable
private fun SettingsDropdownSection(
    title: String,
    selectedTitle: String,
    selectedDescription: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    selectedPreview: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val scheme = MaterialTheme.colorScheme

    Card(
        modifier = Modifier.fillMaxWidth(),

        shape = RoundedCornerShape(20.dp),

        colors = CardDefaults.cardColors(
            containerColor = scheme.surfaceContainerLow,
            contentColor = scheme.onSurface
        ),

        border = BorderStroke(
            1.dp,
            if (expanded) {
                scheme.primary.copy(alpha = 0.45f)
            } else {
                scheme.outlineVariant
            }
        )
    ) {

        Column {

            /* ---------- Clickable selected option ---------- */

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggle)
                    .padding(16.dp),

                verticalAlignment = Alignment.CenterVertically,

                horizontalArrangement =
                    Arrangement.spacedBy(12.dp)
            ) {

                selectedPreview?.invoke()

                Column(
                    modifier = Modifier.weight(1f),

                    verticalArrangement =
                        Arrangement.spacedBy(4.dp)
                ) {

                    Text(
                        text = title,

                        style =
                            MaterialTheme.typography.labelMedium,

                        fontWeight = FontWeight.SemiBold,

                        color = scheme.onSurfaceVariant
                    )

                    Text(
                        text = selectedTitle,

                        style =
                            MaterialTheme.typography.titleMedium,

                        fontWeight = FontWeight.Bold,

                        color = scheme.onSurface
                    )

                    Text(
                        text = selectedDescription,

                        style =
                            MaterialTheme.typography.bodySmall,

                        color = scheme.onSurfaceVariant
                    )
                }

                Text(
                    text = if (expanded) "▴" else "▾",

                    style =
                        MaterialTheme.typography.headlineSmall,

                    color = scheme.primary
                )
            }

            /* ---------- Expanded options ---------- */

            if (expanded) {

                HorizontalDivider(
                    color = scheme.outlineVariant
                )

                Column(
                    content = content
                )
            }
        }
    }
}

/* =========================================================
   NORMAL SETTINGS SECTION

   Used for background style and time format.
========================================================= */

@Composable
private fun SettingsSection(
    title: String,
    subtitle: String,
    content: @Composable ColumnScope.() -> Unit
) {
    val scheme = MaterialTheme.colorScheme

    Column(
        verticalArrangement =
            Arrangement.spacedBy(9.dp)
    ) {

        Text(
            text = title,

            style =
                MaterialTheme.typography.titleLarge,

            fontWeight = FontWeight.Bold,

            color = scheme.onBackground
        )

        Text(
            text = subtitle,

            style =
                MaterialTheme.typography.bodySmall,

            color = scheme.onBackground.copy(alpha = 0.85f)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),

            shape = RoundedCornerShape(20.dp),

            colors = CardDefaults.cardColors(
                containerColor = scheme.surfaceContainerLow,
                contentColor = scheme.onSurface
            ),

            border = BorderStroke(
                1.dp,
                scheme.outlineVariant
            )
        ) {

            Column(
                content = content
            )
        }
    }
}

/* =========================================================
   INDIVIDUAL SETTING OPTION
========================================================= */

@Composable
private fun SettingsOptionRow(
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

            horizontalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {

            leading?.invoke()

            Column(
                modifier = Modifier.weight(1f),

                verticalArrangement =
                    Arrangement.spacedBy(4.dp)
            ) {

                Text(
                    text = title,

                    style =
                        MaterialTheme.typography.bodyLarge,

                    fontWeight = if (selected) {
                        FontWeight.SemiBold
                    } else {
                        FontWeight.Normal
                    },

                    color = scheme.onSurface
                )

                Text(
                    text = description,

                    style =
                        MaterialTheme.typography.bodySmall,

                    color = scheme.onSurfaceVariant
                )
            }

            RadioButton(
                selected = selected,

                onClick = onClick,

                colors = RadioButtonDefaults.colors(
                    selectedColor = scheme.primary,
                    unselectedColor = scheme.onSurfaceVariant
                )
            )
        }

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp),
            color = scheme.outlineVariant
        )
    }
}

/* =========================================================
   LIVE THEME PREVIEW
========================================================= */

@Composable
private fun ThemePreviewCard(
    settings: ThemeSettings
) {
    val scheme = MaterialTheme.colorScheme

    val palette = paletteColors(
        settings.palette
    )

    val previewBrush =
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

    Card(
        modifier = Modifier.fillMaxWidth(),

        shape = RoundedCornerShape(24.dp),

        colors = CardDefaults.cardColors(
            containerColor = scheme.surfaceContainerLow,
            contentColor = scheme.onSurface
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

                style =
                    MaterialTheme.typography.labelSmall,

                fontWeight = FontWeight.Bold,

                color = scheme.primary
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(115.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(previewBrush)
                    .padding(14.dp)
            ) {

                Column(
                    verticalArrangement =
                        Arrangement.spacedBy(7.dp)
                ) {

                    Surface(
                        shape = RoundedCornerShape(9.dp),

                        color = scheme.surface,

                        contentColor = scheme.onSurface
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
                        color = scheme.surface,

                        contentColor = scheme.onSurface,

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
                text = "Preview of your selected colors.",

                style =
                    MaterialTheme.typography.labelSmall,

                color = scheme.onSurfaceVariant
            )
        }
    }
}

/* =========================================================
   PALETTE PREVIEW
========================================================= */

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

/* =========================================================
   BACKGROUND STYLE PREVIEW
========================================================= */

@Composable
private fun StylePreview(
    style: AppThemeStyle
) {
    val scheme = MaterialTheme.colorScheme

    val brush =
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
            .background(brush)
    )
}
