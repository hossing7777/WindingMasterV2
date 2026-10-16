package com.windingmaster

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.os.LocaleListCompat
import com.windingmaster.data.local.SettingsStore
import com.windingmaster.presentation.navigation.MainNavigation
import com.windingmaster.presentation.settings.toNightMode
import com.windingmaster.presentation.theme.WindingMasterTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject lateinit var settingsStore: SettingsStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // اعمال تم پیش‌فرض (تیره)
        AppCompatDelegate.setDefaultNightMode(settingsStore.themeMode.toNightMode())

        // اعمال زبان پیش‌فرض (فارسی) اگر قبلاً ذخیره نشده
        val currentLang = AppCompatDelegate.getApplicationLocales().toLanguageTags()
        if (currentLang.isEmpty()) {
            AppCompatDelegate.setApplicationLocales(
                LocaleListCompat.forLanguageTags(settingsStore.language)
            )
        }

        setContent {
            val language = LocalConfiguration.current.locales[0].language
            val direction = if (language == "en") LayoutDirection.Ltr else LayoutDirection.Rtl
            CompositionLocalProvider(LocalLayoutDirection provides direction) {
                WindingMasterTheme { MainNavigation() }
            }
        }
    }
}
