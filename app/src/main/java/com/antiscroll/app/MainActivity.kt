package com.antiscroll.app

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.antiscroll.app.data.SettingsStore
import com.antiscroll.app.service.GuardAccessibilityService
import com.antiscroll.app.ui.AntiScrollTheme
import com.antiscroll.app.ui.HomeScreen
import com.antiscroll.app.ui.SettingsScreen

/**
 * Extends AppCompatActivity (not the plain ComponentActivity Compose apps otherwise need)
 * specifically because AppCompatDelegate.setApplicationLocales() - the standard per-app
 * language API, with automatic backward-compat below Android 13 - only auto-recreates the
 * Activity to apply a new language on an AppCompatActivity. Fully supported alongside
 * Compose's setContent {}.
 */
class MainActivity : AppCompatActivity() {

    private val settings by lazy { SettingsStore(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var serviceEnabled by mutableStateOf(isAccessibilityServiceEnabled())
            var blockYoutubeShorts by mutableStateOf(settings.blockYoutubeShorts)
            var blockInstagramReels by mutableStateOf(settings.blockInstagramReels)
            var blockInstagramExplore by mutableStateOf(settings.blockInstagramExplore)
            var blockInstagramStories by mutableStateOf(settings.blockInstagramStories)
            var darkTheme by mutableStateOf(settings.darkTheme)
            var showSettings by mutableStateOf(false)
            val currentLocales = AppCompatDelegate.getApplicationLocales()
            val currentLanguageTag = if (currentLocales.isEmpty) null else currentLocales[0]?.toLanguageTag()

            LifecycleResumeEffect(Unit) {
                serviceEnabled = isAccessibilityServiceEnabled()
                onPauseOrDispose { }
            }

            AntiScrollTheme(darkTheme = darkTheme) {
                if (showSettings) {
                    SettingsScreen(
                        onBack = { showSettings = false },
                        darkTheme = darkTheme,
                        onSelectDarkTheme = { enabled ->
                            settings.darkTheme = enabled
                            darkTheme = enabled
                        },
                        currentLanguageTag = currentLanguageTag,
                        onSelectLanguage = { tag ->
                            val locales = if (tag == null) {
                                LocaleListCompat.getEmptyLocaleList()
                            } else {
                                LocaleListCompat.forLanguageTags(tag)
                            }
                            AppCompatDelegate.setApplicationLocales(locales)
                        },
                    )
                } else {
                    HomeScreen(
                        serviceEnabled = serviceEnabled,
                        onOpenAccessibilitySettings = {
                            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                        },
                        onOpenSettings = { showSettings = true },
                        blockYoutubeShorts = blockYoutubeShorts,
                        onToggleBlockYoutubeShorts = { enabled ->
                            settings.blockYoutubeShorts = enabled
                            blockYoutubeShorts = enabled
                        },
                        blockInstagramReels = blockInstagramReels,
                        onToggleBlockInstagramReels = { enabled ->
                            settings.blockInstagramReels = enabled
                            blockInstagramReels = enabled
                        },
                        blockInstagramExplore = blockInstagramExplore,
                        onToggleBlockInstagramExplore = { enabled ->
                            settings.blockInstagramExplore = enabled
                            blockInstagramExplore = enabled
                        },
                        blockInstagramStories = blockInstagramStories,
                        onToggleBlockInstagramStories = { enabled ->
                            settings.blockInstagramStories = enabled
                            blockInstagramStories = enabled
                        },
                    )
                }
            }
        }
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val expectedComponent = "$packageName/${GuardAccessibilityService::class.java.name}"
        val enabledServices = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
        ) ?: return false
        return enabledServices.split(':').any { it.equals(expectedComponent, ignoreCase = true) }
    }
}
