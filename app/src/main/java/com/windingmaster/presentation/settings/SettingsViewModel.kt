package com.windingmaster.presentation.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import com.windingmaster.data.local.SettingsStore
import com.windingmaster.data.local.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

fun ThemeMode.toNightMode(): Int = when (this) {
    ThemeMode.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
    ThemeMode.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
    ThemeMode.DARK -> AppCompatDelegate.MODE_NIGHT_YES
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val store: SettingsStore
) : ViewModel() {

    private val _theme = MutableStateFlow(store.themeMode)
    val theme: StateFlow<ThemeMode> = _theme.asStateFlow()

    private val _language = MutableStateFlow(currentLanguage())
    val language: StateFlow<String> = _language.asStateFlow()

    fun setLanguage(tag: String) {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag))
        _language.value = tag
    }

    fun setTheme(mode: ThemeMode) {
        store.themeMode = mode
        _theme.value = mode
        AppCompatDelegate.setDefaultNightMode(mode.toNightMode())
    }

    private fun currentLanguage(): String =
        AppCompatDelegate.getApplicationLocales().toLanguageTags().ifEmpty { "fa" }.substringBefore('-')
}
