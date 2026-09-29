package com.antiscroll.app.service

import android.accessibilityservice.AccessibilityService
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.antiscroll.app.data.SettingsStore
import com.antiscroll.app.service.detectors.InstagramDetector
import com.antiscroll.app.service.detectors.YouTubeDetector

class GuardAccessibilityService : AccessibilityService() {

    private lateinit var settings: SettingsStore
    private val mainHandler = Handler(Looper.getMainLooper())

    /**
     * Cooldown-based guard against rapid-fire duplicate events during one screen
     * transition (see handleYoutubeContent/handleInstagramContent), reused as a debounce
     * for the click path too so the two don't double-fire for the same tap.
     */
    private var lastYoutubeBlockAtMs = 0L
    private var lastInstagramBlockAtMs = 0L

    override fun onServiceConnected() {
        super.onServiceConnected()
        settings = SettingsStore(applicationContext)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType == AccessibilityEvent.TYPE_VIEW_CLICKED) {
            handleTabTap(event)
            return
        }

        val root = rootInActiveWindow ?: return
        when (event.packageName) {
            YOUTUBE_PACKAGE -> handleYoutubeContent(root)
            INSTAGRAM_PACKAGE -> handleInstagramContent(root)
        }
    }

    /**
     * Bottom-nav tabs (Shorts, Reels, Explore) stay visually "selected" long after Back
     * leaves their content - polling that as a block signal gets stuck permanently true
     * after the first tap, which looked like the app being repeatedly, endlessly exited.
     * A click event is a genuine one-shot signal (fires exactly once per real tap, no
     * stale state), so catch the tap itself, give the app a moment to finish navigating,
     * then reverse it with a single Back.
     */
    private fun handleTabTap(event: AccessibilityEvent) {
        val desc = event.contentDescription?.toString() ?: return
        val pkg = event.packageName?.toString() ?: return

        val isBlockedTab = when (pkg) {
            YOUTUBE_PACKAGE -> settings.blockYoutubeShorts && desc.equals("Shorts", ignoreCase = true)
            INSTAGRAM_PACKAGE -> settings.blockInstagramReelsExplore &&
                INSTAGRAM_TAB_LABELS.any { desc.equals(it, ignoreCase = true) }
            else -> false
        }
        if (!isBlockedTab) return
        if (!tryClaimCooldown(pkg)) return

        mainHandler.postDelayed({
            Log.d(TAG, "Reversing tap on blocked tab: $desc")
            performGlobalAction(GLOBAL_ACTION_BACK)
        }, TAP_REVERSE_DELAY_MS)
    }

    private fun handleYoutubeContent(root: AccessibilityNodeInfo) {
        if (!settings.blockYoutubeShorts) return
        val metrics = resources.displayMetrics
        if (!YouTubeDetector.isShortsScreen(root, metrics.widthPixels, metrics.heightPixels)) return
        if (!tryClaimCooldown(YOUTUBE_PACKAGE)) return

        Log.d(TAG, "Blocking YouTube Shorts (content match)")
        performGlobalAction(GLOBAL_ACTION_BACK)
    }

    private fun handleInstagramContent(root: AccessibilityNodeInfo) {
        if (!settings.blockInstagramReelsExplore) return
        val metrics = resources.displayMetrics
        if (!InstagramDetector.isBlockedScreen(root, metrics.widthPixels, metrics.heightPixels)) return
        if (!tryClaimCooldown(INSTAGRAM_PACKAGE)) return

        Log.d(TAG, "Blocking Instagram Reels (content match)")
        performGlobalAction(GLOBAL_ACTION_BACK)
    }

    /** Returns true and starts a fresh cooldown window if the previous one has elapsed. */
    private fun tryClaimCooldown(pkg: String): Boolean {
        val now = SystemClock.elapsedRealtime()
        val last = if (pkg == YOUTUBE_PACKAGE) lastYoutubeBlockAtMs else lastInstagramBlockAtMs
        if (now - last < BLOCK_COOLDOWN_MS) return false
        if (pkg == YOUTUBE_PACKAGE) lastYoutubeBlockAtMs = now else lastInstagramBlockAtMs = now
        return true
    }

    override fun onInterrupt() {}

    companion object {
        private const val TAG = "AntiScroll"
        private const val YOUTUBE_PACKAGE = "com.google.android.youtube"
        private const val INSTAGRAM_PACKAGE = "com.instagram.android"
        private val INSTAGRAM_TAB_LABELS = setOf("Reels", "Explore", "Search and explore", "Search")

        private const val BLOCK_COOLDOWN_MS = 800L
        private const val TAP_REVERSE_DELAY_MS = 250L
    }
}
