
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
    SYSTEM("System default", "Follow device appearance"),
    LIGHT("Light", "Classic bright surfaces"),
    DARK("Dark", "Comfortable dark surfaces"),
    AMOLED("AMOLED", "Black OLED-friendly surfaces"),

    SOFT_LIGHT("Soft Light", "Gentle, warm light surfaces"),
    SOFT_DARK("Soft Dark", "Muted charcoal surfaces"),
    HIGH_CONTRAST_LIGHT(
        "High Contrast Light",
        "Bright white with stronger text contrast"
    ),
    HIGH_CONTRAST_DARK(
        "High Contrast Dark",
        "Near-black with stronger text contrast"
    )
}

enum class AppThemePalette(
    val title: String,
    val description: String
) {
    OCEAN("Ocean", "Deep blue"),
    FOREST("Forest", "Natural green"),
    SUNSET("Sunset", "Warm terracotta"),
    MIDNIGHT("Midnight", "Rich violet"),

    ROSE("Rose", "Soft berry pink"),
    LAGOON("Lagoon", "Fresh teal"),
    AMBER("Amber", "Golden honey"),
    SLATE("Slate", "Cool steel blue")
}

enum class AppThemeStyle(
    val title: String,
    val description: String
) {
    SIMPLIFIED(
        "Simplified",
        "Clean solid-color backgrounds"
    ),
    GRADIENT(
        "Gradient",
        "Blended backgrounds and gradient text"
    )
}

enum class AppTimeFormat(
    val title: String,
    val description: String
) {
    HOUR_12("12-hour", "8:35 PM"),
    HOUR_24("24-hour", "20:35")
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

val LocalPnrVisualSettings = compositionLocalOf {
    ThemeSettings()
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
        val saved = preferences.getString(key, default.name)

        return enumValues<T>().firstOrNull {
            it.name == saved
        } ?: default
    }

    private fun load() = ThemeSettings(
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

    fun setMode(value: AppThemeMode) {
        settings = settings.copy(mode = value)
        preferences.edit()
            .putString("theme_mode", value.name)
            .apply()
    }

    fun setPalette(value: AppThemePalette) {
        settings = settings.copy(palette = value)
        preferences.edit()
            .putString("theme_palette", value.name)
            .apply()
    }

    fun setStyle(value: AppThemeStyle) {
        settings = settings.copy(style = value)
        preferences.edit()
            .putString("theme_style", value.name)
            .apply()
    }

    fun setTimeFormat(value: AppTimeFormat) {
        settings = settings.copy(timeFormat = value)
        preferences.edit()
            .putString("time_format", value.name)
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
