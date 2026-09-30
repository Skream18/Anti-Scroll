package com.antiscroll.app

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.antiscroll.app.data.SettingsStore
import com.antiscroll.app.service.GuardAccessibilityService
import com.antiscroll.app.ui.AntiScrollTheme
import com.antiscroll.app.ui.HomeScreen
import com.antiscroll.app.ui.ScrollControlScreen
import com.antiscroll.app.ui.SettingsScreen

private enum class BottomTab { HOME, SCROLL_CONTROL }

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
            var blockSnapchatSpotlight by mutableStateOf(settings.blockSnapchatSpotlight)
            var blockSnapchatStories by mutableStateOf(settings.blockSnapchatStories)
            var darkTheme by mutableStateOf(settings.darkTheme)
            var activeTab by mutableStateOf(BottomTab.HOME)
            var settingsOpen by mutableStateOf(false)
            var scrollControlEnabled by mutableStateOf(settings.scrollControlEnabled)
            var scrollControlLimitMinutes by mutableStateOf(settings.scrollControlLimitMinutes)
            var scrollControlRemainingMillis by mutableStateOf(settings.scrollControlRemainingMillis)
            var scrollControlCooldownUntil by mutableStateOf(settings.scrollControlCooldownUntil)
            val currentLocales = AppCompatDelegate.getApplicationLocales()
            val currentLanguageTag = if (currentLocales.isEmpty) null else currentLocales[0]?.toLanguageTag()

            LifecycleResumeEffect(Unit) {
                serviceEnabled = isAccessibilityServiceEnabled()
                onPauseOrDispose { }
            }

            // Scroll Control's remaining budget and cooldown are ticked down in the
            // background by InstagramGuard (a separate object in the accessibility
            // service), so this screen polls SettingsStore to keep its live countdown
            // display in sync. Handler-based, not a coroutine, to match the rest of the app.
            DisposableEffect(Unit) {
                val pollHandler = Handler(Looper.getMainLooper())
                val poll = object : Runnable {
                    override fun run() {
                        scrollControlRemainingMillis = settings.scrollControlRemainingMillis
                        scrollControlCooldownUntil = settings.scrollControlCooldownUntil
                        pollHandler.postDelayed(this, 1_000L)
                    }
                }
                pollHandler.post(poll)
                onDispose { pollHandler.removeCallbacksAndMessages(null) }
            }

            AntiScrollTheme(darkTheme = darkTheme) {
                if (settingsOpen) {
                    SettingsScreen(
                        onBack = { settingsOpen = false },
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
                    Scaffold(
                        bottomBar = {
                            NavigationBar {
                                NavigationBarItem(
                                    selected = activeTab == BottomTab.HOME,
                                    onClick = { activeTab = BottomTab.HOME },
                                    icon = { Icon(Icons.Filled.Home, contentDescription = null) },
                                    label = { Text(stringResource(R.string.home_nav_label)) },
                                )
                                NavigationBarItem(
                                    selected = activeTab == BottomTab.SCROLL_CONTROL,
                                    onClick = { activeTab = BottomTab.SCROLL_CONTROL },
                                    icon = { Icon(Icons.Filled.Timer, contentDescription = null) },
                                    label = { Text(stringResource(R.string.scroll_control_nav_label)) },
                                )
                            }
                        },
                    ) { innerPadding ->
                        Box(Modifier.padding(innerPadding)) {
                            when (activeTab) {
                                BottomTab.HOME -> HomeScreen(
                                    serviceEnabled = serviceEnabled,
                                    onOpenAccessibilitySettings = {
                                        startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                                    },
                                    onOpenSettings = { settingsOpen = true },
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
                                    blockSnapchatSpotlight = blockSnapchatSpotlight,
                                    onToggleBlockSnapchatSpotlight = { enabled ->
                                        settings.blockSnapchatSpotlight = enabled
                                        blockSnapchatSpotlight = enabled
                                    },
                                    blockSnapchatStories = blockSnapchatStories,
                                    onToggleBlockSnapchatStories = { enabled ->
                                        settings.blockSnapchatStories = enabled
                                        blockSnapchatStories = enabled
                                    },
                                )
                                BottomTab.SCROLL_CONTROL -> ScrollControlScreen(
                                    onOpenSettings = { settingsOpen = true },
                                    fullBlockEnabled = blockInstagramReels,
                                    scrollControlEnabled = scrollControlEnabled,
                                    onToggleScrollControlEnabled = { enabled ->
                                        settings.scrollControlEnabled = enabled
                                        scrollControlEnabled = enabled
                                    },
                                    scrollControlLimitMinutes = scrollControlLimitMinutes,
                                    onSelectLimitMinutes = { minutes ->
                                        settings.scrollControlLimitMinutes = minutes
                                        settings.scrollControlRemainingMillis = minutes * 60_000L
                                        settings.scrollControlCooldownUntil = 0L
                                        scrollControlLimitMinutes = minutes
                                        scrollControlRemainingMillis = minutes * 60_000L
                                        scrollControlCooldownUntil = 0L
                                    },
                                    remainingMillis = scrollControlRemainingMillis,
                                    cooldownUntil = scrollControlCooldownUntil,
                                )
                            }
                        }
                    }
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
