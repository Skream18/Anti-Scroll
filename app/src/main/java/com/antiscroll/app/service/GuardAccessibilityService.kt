package com.antiscroll.app.service

import android.accessibilityservice.AccessibilityService
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import com.antiscroll.app.data.SettingsStore
import com.antiscroll.app.service.instagram.InstagramGuard
import com.antiscroll.app.service.youtube.YouTubeGuard

/**
 * Thin dispatcher only: routes each accessibility event to the right app's guard by
 * package name. All actual detection and blocking logic lives in YouTubeGuard and
 * InstagramGuard, which are fully independent of each other - this class holds no
 * per-app state of its own.
 */
class GuardAccessibilityService : AccessibilityService() {

    private lateinit var youtubeGuard: YouTubeGuard
    private lateinit var instagramGuard: InstagramGuard

    override fun onServiceConnected() {
        super.onServiceConnected()
        val settings = SettingsStore(applicationContext)
        val handler = Handler(Looper.getMainLooper())
        youtubeGuard = YouTubeGuard(this, handler, settings)
        instagramGuard = InstagramGuard(this, handler, settings)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        val pkg = event.packageName?.toString()

        if (event.eventType == AccessibilityEvent.TYPE_VIEW_CLICKED) {
            when (pkg) {
                YouTubeGuard.PACKAGE_NAME -> youtubeGuard.onTabTapped(event)
                InstagramGuard.PACKAGE_NAME -> instagramGuard.onTabTapped(event)
            }
            return
        }

        val root = rootInActiveWindow ?: return
        when (pkg) {
            YouTubeGuard.PACKAGE_NAME -> youtubeGuard.onContentChanged(root)
            InstagramGuard.PACKAGE_NAME -> instagramGuard.onContentChanged(root)
        }
    }

    override fun onInterrupt() {}
}
