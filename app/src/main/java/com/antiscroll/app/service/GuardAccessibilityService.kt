package com.antiscroll.app.service

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.antiscroll.app.data.SettingsStore
import com.antiscroll.app.service.detectors.InstagramDetector
import com.antiscroll.app.service.detectors.YouTubeDetector

class GuardAccessibilityService : AccessibilityService() {

    private lateinit var settings: SettingsStore

    /**
     * Edge-triggered guards: a single blocked-screen entry fires many rapid
     * TYPE_WINDOW_CONTENT_CHANGED events while the screen animates in. Without this, each
     * one presses Back again, and those extra presses land past the screen that was
     * underneath (e.g. exiting the app entirely) instead of just going back one step.
     * Once we've pressed Back for an entry, we don't press again until the tree actually
     * shows a non-blocked screen. Separate flags per app since they're independent event
     * streams.
     */
    private var alreadyBlockedYoutube = false
    private var alreadyBlockedInstagram = false

    override fun onServiceConnected() {
        super.onServiceConnected()
        settings = SettingsStore(applicationContext)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        val root = rootInActiveWindow ?: return

        when (event.packageName) {
            YOUTUBE_PACKAGE -> handleYoutube(root)
            INSTAGRAM_PACKAGE -> handleInstagram(root)
        }
    }

    private fun handleYoutube(root: AccessibilityNodeInfo) {
        if (!settings.blockYoutubeShorts) {
            alreadyBlockedYoutube = false
            return
        }

        val metrics = resources.displayMetrics
        if (YouTubeDetector.isShortsScreen(root, metrics.widthPixels, metrics.heightPixels)) {
            if (!alreadyBlockedYoutube) {
                Log.d(TAG, "Blocking YouTube Shorts")
                performGlobalAction(GLOBAL_ACTION_BACK)
                alreadyBlockedYoutube = true
            }
        } else {
            alreadyBlockedYoutube = false
        }
    }

    private fun handleInstagram(root: AccessibilityNodeInfo) {
        if (!settings.blockInstagramReelsExplore) {
            alreadyBlockedInstagram = false
            return
        }

        val metrics = resources.displayMetrics
        if (InstagramDetector.isBlockedScreen(root, metrics.widthPixels, metrics.heightPixels)) {
            if (!alreadyBlockedInstagram) {
                Log.d(TAG, "Blocking Instagram Reels/Explore")
                performGlobalAction(GLOBAL_ACTION_BACK)
                alreadyBlockedInstagram = true
            }
        } else {
            alreadyBlockedInstagram = false
        }
    }

    override fun onInterrupt() {}

    companion object {
        private const val TAG = "AntiScroll"
        private const val YOUTUBE_PACKAGE = "com.google.android.youtube"
        private const val INSTAGRAM_PACKAGE = "com.instagram.android"
    }
}
