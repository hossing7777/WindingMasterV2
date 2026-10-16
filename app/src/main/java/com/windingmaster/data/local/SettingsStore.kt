package com.windingmaster.data.local

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

enum class ThemeMode { SYSTEM, LIGHT, DARK }

@Singleton
class SettingsStore @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    /** پیش‌فرض: تم تیره */
    var themeMode: ThemeMode
        get() = runCatching { ThemeMode.valueOf(prefs.getString(KEY_THEME, null) ?: "") }
            .getOrDefault(ThemeMode.DARK)
        set(value) = prefs.edit().putString(KEY_THEME, value.name).apply()

    /** پیش‌فرض: فارسی */
    var language: String
        get() = prefs.getString(KEY_LANG, null) ?: "fa"
        set(value) = prefs.edit().putString(KEY_LANG, value).apply()

    private companion object {
        const val KEY_THEME = "theme_mode"
        const val KEY_LANG = "app_language"
    }
}
