
package com.first.pnrenq.ui.theme

import android.content.Context
import androidx.compose.runtime.compositionLocalOf
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
        "Always use light mode"
    ),
    DARK(
        "Dark",
        "Always use dark mode"
    ),
    AMOLED(
        "AMOLED",
        "Black surfaces with deep contrast"
    )
}

enum class AppThemePalette(
    val title: String,
    val description: String
) {
    OCEAN("Ocean", "Calm blue"),
    FOREST("Forest", "Natural green"),
    SUNSET("Sunset", "Warm orange"),
    MIDNIGHT("Midnight", "Modern violet")
}

enum class AppThemeStyle(
    val title: String,
    val description: String
) {
    SIMPLIFIED(
        "Simplified",
        "Clean background with solid colors"
    ),
    GRADIENT(
        "Gradient",
        "Soft, blended colors across screens"
    )
}

enum class AppTimeFormat(
    val title: String,
    val description: String
) {
    HOUR_12(
        "12-hour",
        "Example: 8:35 PM"
    ),
    HOUR_24(
        "24-hour",
        "Example: 20:35"
    )
}

data class ThemeSettings(
    val mode: AppThemeMode = AppThemeMode.SYSTEM,
    val palette: AppThemePalette = AppThemePalette.OCEAN,
    val style: AppThemeStyle = AppThemeStyle.SIMPLIFIED,
    val timeFormat: AppTimeFormat = AppTimeFormat.HOUR_24
)

val LocalPnrTimeFormat = compositionLocalOf {
    AppTimeFormat.HOUR_24
}

class ThemePreferences(context: Context) {

    private val preferences =
        context.applicationContext.getSharedPreferences(
            "pnrenq_appearance",
            Context.MODE_PRIVATE
        )

    var settings by mutableStateOf(load())
        private set

    private inline fun <reified T : Enum<T>> readEnum(
        key: String,
        default: T
    ): T {
        val saved = preferences.getString(
            key,
            default.name
        )

        return enumValues<T>().firstOrNull {
            it.name == saved
        } ?: default
    }

    private fun load(): ThemeSettings {
        return ThemeSettings(
            mode = readEnum(
                "theme_mode",
                AppThemeMode.SYSTEM
            ),
            palette = readEnum(
                "theme_palette",
                AppThemePalette.OCEAN
            ),
            style = readEnum(
                "theme_style",
                AppThemeStyle.SIMPLIFIED
            ),
            timeFormat = readEnum(
                "time_format",
                AppTimeFormat.HOUR_24
            )
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

    fun setStyle(style: AppThemeStyle) {
        settings = settings.copy(style = style)

        preferences.edit()
            .putString("theme_style", style.name)
            .apply()
    }

    fun setTimeFormat(format: AppTimeFormat) {
        settings = settings.copy(timeFormat = format)

        preferences.edit()
            .putString("time_format", format.name)
            .apply()
    }

    fun reset() {
        settings = ThemeSettings()

        preferences.edit()
            .remove("theme_mode")
            .remove("theme_palette")
            .remove("theme_style")
            .remove("time_format")
            .apply()
    }
}
