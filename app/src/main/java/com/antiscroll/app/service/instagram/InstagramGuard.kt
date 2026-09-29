package com.antiscroll.app.service.instagram

import android.accessibilityservice.AccessibilityService
import android.os.Handler
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.antiscroll.app.data.SettingsStore
import com.antiscroll.app.service.common.anyDescendant
import com.antiscroll.app.service.common.fillsScreen

/**
 * Everything about blocking Instagram Reels and Explore lives in this one file: detection
 * rules, tap/content handling, and all of its own state. Nothing here is shared with
 * YouTube's guard (YouTubeGuard, in its own package) - each is fully independent, so a bug
 * or a tuning change on one side can never affect the other.
 *
 * Two independent signals feed the same blocking decision:
 * - [onTabTapped]: the bottom-nav Reels/Explore tabs stay visually "selected" long after
 *   Back leaves their content, so polling that state is unsafe (it never reads false
 *   again). A click event is a genuine one-shot signal instead - it fires exactly once per
 *   real tap - so this catches the tap itself, waits briefly for the app to navigate, then
 *   reverses it with a single Back. This is the only signal for Explore, which has no
 *   equivalent "player" content to detect the other way.
 * - [onContentChanged]: catches Reels entered without tapping the tab (e.g. a Reel opened
 *   from the Explore grid) by matching the actual full-screen Reels player content.
 *
 * Both funnel through [claim] so only one Back is ever sent per real entry, even though
 * both signals can independently notice the same Reels entry.
 */
internal class InstagramGuard(
    private val service: AccessibilityService,
    private val handler: Handler,
    private val settings: SettingsStore,
) {

    private var blocked = false
    private var generation = 0

    fun onTabTapped(event: AccessibilityEvent) {
        if (!settings.blockInstagramReelsExplore) return
        val desc = event.contentDescription?.toString() ?: return
        if (TAB_LABELS.none { desc.equals(it, ignoreCase = true) }) return
        if (!claim()) return

        handler.postDelayed({
            Log.d(TAG, "Reversing tap on blocked tab: $desc")
            service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
        }, TAP_REVERSE_DELAY_MS)
    }

    fun onContentChanged(root: AccessibilityNodeInfo) {
        if (!settings.blockInstagramReelsExplore) {
            release()
            return
        }
        if (isReelsScreen(root)) {
            if (claim()) {
                Log.d(TAG, "Blocking Reels (content match)")
                service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
            }
        } else {
            release()
        }
    }

    /**
     * Claims the block for a fresh entry, returning false if one is already in progress
     * (from either signal above) so only one Back is ever sent. Also arms a safety-net
     * release after [BLOCK_RESET_TIMEOUT_MS]: relying only on [onContentChanged] later
     * observing "not blocked" to release the claim isn't reliable enough on its own - a
     * static screen (e.g. sitting on the Explore grid, which has no content signal of its
     * own at all) may not keep sending content-changed events, so that release could
     * simply never come, permanently blocking any further taps from working. The
     * generation counter stops a stale safety-net release (from an older claim) from
     * firing after a newer claim has already started.
     */
    private fun claim(): Boolean {
        if (blocked) return false
        blocked = true
        generation++
        val myGeneration = generation
        handler.postDelayed({
            if (generation == myGeneration) blocked = false
        }, BLOCK_RESET_TIMEOUT_MS)
        return true
    }

    private fun release() {
        if (blocked) {
            blocked = false
            generation++
        }
    }

    /**
     * `clips_viewer_view_pager` is the actual, confirmed resource ID Instagram uses for
     * the full-screen Reels pager (Reels shipped under the internal codename "Clips") -
     * taken from Scrolless (github.com/duartebarbosadev/Scrolless), another open-source
     * accessibility blocker, rather than guessed. Every match is gated by fillsScreen(),
     * i.e. visible right now AND filling nearly the whole screen - an ID match alone isn't
     * enough, since a view pager can keep an adjacent page's view inflated off-screen for
     * smooth swiping well after the user has left it.
     */
    private fun isReelsScreen(root: AccessibilityNodeInfo): Boolean {
        val metrics = service.resources.displayMetrics
        return root.anyDescendant { node ->
            val id = node.viewIdResourceName.orEmpty()

            val looksLikeReels = id.endsWith(REELS_VIEWER_ID, ignoreCase = true) ||
                id.contains("clips", ignoreCase = true) ||
                id.contains("reel", ignoreCase = true)

            looksLikeReels && node.fillsScreen(metrics.widthPixels, metrics.heightPixels)
        }
    }

    companion object {
        const val PACKAGE_NAME = "com.instagram.android"
        private const val TAG = "AntiScroll-Instagram"
        private const val REELS_VIEWER_ID = "clips_viewer_view_pager"
        private val TAB_LABELS = setOf("Reels", "Explore", "Search and explore", "Search")
        private const val TAP_REVERSE_DELAY_MS = 250L
        private const val BLOCK_RESET_TIMEOUT_MS = 1200L
    }
}
