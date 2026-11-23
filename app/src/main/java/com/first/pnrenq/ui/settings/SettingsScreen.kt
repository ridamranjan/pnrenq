
@file:OptIn(ExperimentalMaterial3Api::class)

package com.first.pnrenq.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.first.pnrenq.ui.theme.AppThemeMode
import com.first.pnrenq.ui.theme.AppThemePalette
import com.first.pnrenq.ui.theme.ThemeSettings

@Composable
fun SettingsScreen(
    settings: ThemeSettings,
    onModeSelected: (AppThemeMode) -> Unit,
    onPaletteSelected: (AppThemePalette) -> Unit,
    onReset: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text("Appearance & themes")
                },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = "Make PNR EXPERT yours",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Choose your preferred appearance. " +
                        "Changes apply immediately and are saved " +
                        "for the next time you open the app.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

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

            Card(
                colors = CardDefaults.cardColors(
                    containerColor =
                        MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Your current theme",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = settings.palette.title +
                                " · " + settings.mode.title,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Text(
                        text = "All screens use the selected " +
                                "colors automatically.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            OutlinedButton(
                onClick = onReset,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Reset to default theme")
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
            shape = RoundedCornerShape(20.dp)
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
            if (leadingContent != null) {
                leadingContent()
                Spacer(Modifier.width(14.dp))
            }

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
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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

    Surface(
        modifier = Modifier.size(36.dp),
        shape = RoundedCornerShape(12.dp),
        color = color
    ) {}
}
