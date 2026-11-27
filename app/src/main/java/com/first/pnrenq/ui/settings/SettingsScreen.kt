@file:OptIn(ExperimentalMaterial3Api::class)

package com.first.pnrenq.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

import com.first.pnrenq.ui.common.PnrAdaptiveTopBar
import com.first.pnrenq.ui.common.pnrControlColor
import com.first.pnrenq.ui.common.pnrScreenControlColor
import com.first.pnrenq.ui.theme.AppScreenBackground
import com.first.pnrenq.ui.theme.PnrScreenType
import com.first.pnrenq.ui.theme.AppThemeMode
import com.first.pnrenq.ui.theme.AppThemePalette
import com.first.pnrenq.ui.theme.AppThemeStyle
import com.first.pnrenq.ui.theme.AppTimeFormat
import com.first.pnrenq.ui.theme.PnrAccentSurface
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
    onPoliciesClick: () -> Unit,
    onReportBug: () -> Unit,
    onViewSource: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme

    var expandedSection by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        contentColor = scheme.onBackground,
        topBar = {
            PnrAdaptiveTopBar(
                title = "Settings",
                onBack = onBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Text(
                text = "Make it yours",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = scheme.onBackground
            )

            Text(
                text = "Customize appearance, color palette, " +
                        "background style and time format.",
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onBackground
            )

            ThemePreviewCard(settings)

            /* -----------------------------------------
               APPEARANCE
            ----------------------------------------- */

            SettingsDropdownSection(
                title = "Appearance",
                selectedTitle = settings.mode.title,
                selectedDescription = settings.mode.description,
                expanded = expandedSection == "appearance",
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
                        selected = settings.mode == mode,
                        onClick = {
                            onModeSelected(mode)
                            expandedSection = null
                        }
                    )
                }
            }

            /* -----------------------------------------
               COLOR PALETTE
            ----------------------------------------- */

            SettingsDropdownSection(
                title = "Color palette",
                selectedTitle = settings.palette.title,
                selectedDescription = settings.palette.description,
                expanded = expandedSection == "palette",
                onToggle = {
                    expandedSection =
                        if (expandedSection == "palette") {
                            null
                        } else {
                            "palette"
                        }
                },
                selectedPreview = {
                    PalettePreview(settings.palette)
                }
            ) {
                AppThemePalette.entries.forEach { palette ->
                    SettingsOptionRow(
                        title = palette.title,
                        description = palette.description,
                        selected = settings.palette == palette,
                        onClick = {
                            onPaletteSelected(palette)
                            expandedSection = null
                        },
                        leading = {
                            PalettePreview(palette)
                        }
                    )
                }
            }

            /* -----------------------------------------
               BACKGROUND STYLE
            ----------------------------------------- */

            SettingsSection(
                title = "Background style",
                subtitle = "Simplified or Gradient"
            ) {
                AppThemeStyle.entries.forEach { style ->
                    SettingsOptionRow(
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

            /* -----------------------------------------
               TIME FORMAT
            ----------------------------------------- */

            SettingsSection(
                title = "Time format",
                subtitle = "Used across the application"
            ) {
                AppTimeFormat.entries.forEach { format ->
                    SettingsOptionRow(
                        title = format.title,
                        description = format.description,
                        selected = settings.timeFormat == format,
                        onClick = {
                            onTimeFormatSelected(format)
                        }
                    )
                }
            }

            /* -----------------------------------------
               CURRENT PREFERENCES
            ----------------------------------------- */

            PnrAccentSurface(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = scheme.primaryContainer,
                foregroundColor = scheme.onPrimaryContainer,
                shape = RoundedCornerShape(22.dp),
                strength = 1.15f
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
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

            /* -----------------------------------------
               RESTORE DEFAULTS
            ----------------------------------------- */

            OutlinedButton(
                onClick = {
                    expandedSection = null
                    onReset()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = scheme.primary
                ),
                border = BorderStroke(
                    1.dp,
                    pnrScreenControlColor(scheme.outline)
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
                color = scheme.onBackground
            )

            /* -----------------------------------------
               SUPPORT & LEGAL — DROPDOWN
            ----------------------------------------- */

            SettingsDropdownSection(
                title = "Support & legal",
                selectionGroup = false,
                selectedTitle = "Help & information",
                selectedDescription =
                    "Privacy, policies and project support",
                expanded = expandedSection == "support_legal",
                onToggle = {
                    expandedSection =
                        if (expandedSection == "support_legal") {
                            null
                        } else {
                            "support_legal"
                        }
                }
            ) {
                SettingsLinkRow(
                    title = "Policies & Legal",
                    description =
                        "Privacy policy, terms and disclaimer",
                    symbol = "›",
                    onClick = {
                        onPoliciesClick()
                    }
                )

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = scheme.outlineVariant
                )

                SettingsLinkRow(
                    title = "Report a Bug",
                    description = "Open the GitHub issue tracker",
                    symbol = "↗",
                    onClick = {
                        onReportBug()
                    }
                )

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = scheme.outlineVariant
                )

                SettingsLinkRow(
                    title = "Source Code & Licenses",
                    description =
                        "View the project and MIT License",
                    symbol = "↗",
                    onClick = {
                        onViewSource()
                    }
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

/* =========================================================
   SUPPORT / LEGAL ACTION ROW
========================================================= */

@Composable
private fun SettingsLinkRow(
    title: String,
    description: String,
    symbol: String,
    onClick: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                role = Role.Button,
                onClick = onClick
            )
            .heightIn(min = 48.dp)
            .padding(
                horizontal = 16.dp,
                vertical = 16.dp
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = scheme.onSurface
            )

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant
            )
        }

        Text(
            text = symbol,
            modifier = Modifier.clearAndSetSemantics {},
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = scheme.primary
        )
    }
}

/* =========================================================
   SHARED SETTINGS DROPDOWN
========================================================= */

@Composable
private fun SettingsDropdownSection(
    title: String,
    selectedTitle: String,
    selectedDescription: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    selectedPreview: (@Composable () -> Unit)? = null,
    selectionGroup: Boolean = true,
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
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        role = Role.Button,
                        onClickLabel = if (expanded) {
                            "Collapse $title"
                        } else {
                            "Expand $title"
                        },
                        onClick = onToggle
                    )
                    .semantics {
                        heading()
                        stateDescription = if (expanded) {
                            "Expanded"
                        } else {
                            "Collapsed"
                        }
                    }
                    .heightIn(min = 48.dp)
                    .padding(16.dp)
            ) {
                val stacked =
                    maxWidth < 220.dp ||
                            LocalDensity.current.fontScale >= 1.3f

                if (stacked) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SettingsDropdownText(
                            title = title,
                            selectedTitle = selectedTitle,
                            selectedDescription = selectedDescription
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            selectedPreview?.invoke()

                            Spacer(Modifier.weight(1f))

                            DropdownArrow(expanded)
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        selectedPreview?.invoke()

                        SettingsDropdownText(
                            title = title,
                            selectedTitle = selectedTitle,
                            selectedDescription = selectedDescription,
                            modifier = Modifier.weight(1f)
                        )

                        DropdownArrow(expanded)
                    }
                }
            }

            if (expanded) {
                HorizontalDivider(
                    color = scheme.outlineVariant
                )

                Column(
                    modifier = if (selectionGroup) {
                        Modifier.selectableGroup()
                    } else {
                        Modifier
                    },
                    content = content
                )
            }
        }
    }
}

@Composable
private fun SettingsDropdownText(
    title: String,
    selectedTitle: String,
    selectedDescription: String,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = scheme.onSurfaceVariant
        )

        Text(
            text = selectedTitle,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = scheme.onSurface
        )

        Text(
            text = selectedDescription,
            style = MaterialTheme.typography.bodySmall,
            color = scheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DropdownArrow(
    expanded: Boolean
) {
    Text(
        text = if (expanded) "▴" else "▾",
        modifier = Modifier.clearAndSetSemantics {},
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.primary
    )
}

/* =========================================================
   REGULAR SETTINGS SECTION
========================================================= */

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
            fontWeight = FontWeight.Bold,
            color = scheme.onBackground
        )

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = scheme.onBackground
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
                modifier = Modifier.selectableGroup(),
                content = content
            )
        }
    }
}

/* =========================================================
   OPTION ROW

   Keep one selectable action for the entire option.
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
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .selectable(
                    selected = selected,
                    role = Role.RadioButton,
                    onClick = onClick
                )
                .heightIn(min = 48.dp)
                .padding(
                    horizontal = 16.dp,
                    vertical = 12.dp
                )
        ) {
            val stacked =
                maxWidth < 220.dp ||
                        LocalDensity.current.fontScale >= 1.3f

            if (stacked) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        leading?.invoke()

                        Spacer(Modifier.weight(1f))

                        SettingsRadioIndicator(selected)
                    }

                    SettingsOptionText(
                        title = title,
                        description = description,
                        selected = selected
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    leading?.invoke()

                    SettingsOptionText(
                        title = title,
                        description = description,
                        selected = selected,
                        modifier = Modifier.weight(1f)
                    )

                    SettingsRadioIndicator(selected)
                }
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp),
            color = scheme.outlineVariant
        )
    }
}

@Composable
private fun SettingsOptionText(
    title: String,
    description: String,
    selected: Boolean,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (selected) {
                FontWeight.SemiBold
            } else {
                FontWeight.Normal
            },
            color = scheme.onSurface
        )

        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = scheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SettingsRadioIndicator(
    selected: Boolean
) {
    val scheme = MaterialTheme.colorScheme
    val background = scheme.surfaceContainerLow

    Box(
        modifier = Modifier.size(48.dp),
        contentAlignment = Alignment.Center
    ) {
        RadioButton(
            selected = selected,
            // The containing selectable row owns the only action.
            onClick = null,
            colors = RadioButtonDefaults.colors(
                selectedColor = pnrControlColor(
                    scheme.primary,
                    background
                ),
                unselectedColor = pnrControlColor(
                    scheme.onSurfaceVariant,
                    background
                )
            )
        )
    }
}

/* =========================================================
   LIVE PREVIEW
========================================================= */

@Composable
private fun ThemePreviewCard(
    settings: ThemeSettings
) {
    val scheme = MaterialTheme.colorScheme

    PnrAccentSurface(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = scheme.surfaceContainerLow,
        foregroundColor = scheme.onSurface,
        shape = RoundedCornerShape(24.dp),
        strength = 0.95f,
        border = BorderStroke(
            1.dp,
            scheme.outlineVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "LIVE PREVIEW",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = scheme.primary
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 115.dp)
                    .clip(RoundedCornerShape(18.dp))
            ) {
                // The background follows the content's final measured size.
                // It must not force the preview to occupy the available height.
                AppScreenBackground(
                    screen = PnrScreenType.SETTINGS,
                    modifier = Modifier.matchParentSize()
                ) {
                    // Background only.
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
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
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = scheme.onSurface
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = scheme.surface,
                        contentColor = scheme.onSurface
                    ) {
                        Text(
                            text = "${settings.palette.title} · " +
                                    settings.style.title,
                            modifier = Modifier.padding(9.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = scheme.onSurface
                        )
                    }
                }
            }

            Text(
                text = "Preview of the current screen background.",
                style = MaterialTheme.typography.labelSmall,
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
   STYLE PREVIEW
========================================================= */

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