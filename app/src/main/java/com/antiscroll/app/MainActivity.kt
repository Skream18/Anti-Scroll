package com.antiscroll.app

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.antiscroll.app.data.SettingsStore
import com.antiscroll.app.service.GuardAccessibilityService
import com.antiscroll.app.ui.AntiScrollTheme
import com.antiscroll.app.ui.HomeScreen

class MainActivity : ComponentActivity() {

    private val settings by lazy { SettingsStore(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var serviceEnabled by mutableStateOf(isAccessibilityServiceEnabled())
            var blockYoutubeShorts by mutableStateOf(settings.blockYoutubeShorts)
            var blockInstagramReelsExplore by mutableStateOf(settings.blockInstagramReelsExplore)
            var blockInstagramStories by mutableStateOf(settings.blockInstagramStories)

            LifecycleResumeEffect(Unit) {
                serviceEnabled = isAccessibilityServiceEnabled()
                onPauseOrDispose { }
            }

            AntiScrollTheme {
                HomeScreen(
                    serviceEnabled = serviceEnabled,
                    onOpenAccessibilitySettings = {
                        startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                    },
                    blockYoutubeShorts = blockYoutubeShorts,
                    onToggleBlockYoutubeShorts = { enabled ->
                        settings.blockYoutubeShorts = enabled
                        blockYoutubeShorts = enabled
                    },
                    blockInstagramReelsExplore = blockInstagramReelsExplore,
                    onToggleBlockInstagramReelsExplore = { enabled ->
                        settings.blockInstagramReelsExplore = enabled
                        blockInstagramReelsExplore = enabled
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

    private fun isAccessibilityServiceEnabled(): Boolean {
        val expectedComponent = "$packageName/${GuardAccessibilityService::class.java.name}"
        val enabledServices = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
        ) ?: return false
        return enabledServices.split(':').any { it.equals(expectedComponent, ignoreCase = true) }
    }
}
