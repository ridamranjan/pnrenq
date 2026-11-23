
package com.first.pnrenq.ui.theme

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class AppThemeMode(
    val title: String,
    val description: String
) {
    SYSTEM(
        "System default",
        "Follow your device appearance"
    ),
    LIGHT(
        "Light",
        "Always use the light theme"
    ),
    DARK(
        "Dark",
        "Always use the dark theme"
    ),
    AMOLED(
        "AMOLED",
        "Pure black background"
    )
}

enum class AppThemePalette(
    val title: String,
    val description: String
) {
    OCEAN(
        "Ocean",
        "Calm blue"
    ),
    FOREST(
        "Forest",
        "Natural green"
    ),
    SUNSET(
        "Sunset",
        "Warm orange"
    ),
    MIDNIGHT(
        "Midnight",
        "Modern violet"
    )
}

data class ThemeSettings(
    val mode: AppThemeMode = AppThemeMode.SYSTEM,
    val palette: AppThemePalette = AppThemePalette.OCEAN
)

class ThemePreferences(context: Context) {

    private val preferences =
        context.applicationContext.getSharedPreferences(
            "pnrenq_appearance",
            Context.MODE_PRIVATE
        )

    var settings by mutableStateOf(load())
        private set

    private fun load(): ThemeSettings {
        val savedMode = preferences.getString(
            "theme_mode",
            AppThemeMode.SYSTEM.name
        )

        val savedPalette = preferences.getString(
            "theme_palette",
            AppThemePalette.OCEAN.name
        )

        val mode = AppThemeMode.entries.firstOrNull {
            it.name == savedMode
        } ?: AppThemeMode.SYSTEM

        val palette = AppThemePalette.entries.firstOrNull {
            it.name == savedPalette
        } ?: AppThemePalette.OCEAN

        return ThemeSettings(
            mode = mode,
            palette = palette
        )
    }

    fun setMode(mode: AppThemeMode) {
        settings = settings.copy(mode = mode)

        preferences.edit()
            .putString("theme_mode", mode.name)
            .apply()
    }

    fun setPalette(palette: AppThemePalette) {
        settings = settings.copy(palette = palette)

        preferences.edit()
            .putString("theme_palette", palette.name)
            .apply()
    }

    fun reset() {
        settings = ThemeSettings()

        preferences.edit()
            .remove("theme_mode")
            .remove("theme_palette")
            .apply()
    }
}
