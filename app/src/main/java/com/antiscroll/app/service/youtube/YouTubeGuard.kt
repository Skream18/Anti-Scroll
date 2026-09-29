package com.antiscroll.app.service.youtube

import android.accessibilityservice.AccessibilityService
import android.os.Handler
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.antiscroll.app.data.SettingsStore
import com.antiscroll.app.service.common.anyDescendant
import com.antiscroll.app.service.common.fillsScreen

/**
 * Everything about blocking YouTube Shorts lives in this one file: detection rules,
 * tap/content handling, and all of its own state. Nothing here is shared with Instagram's
 * guard (InstagramGuard, in its own package) - each is fully independent, so a bug or a
 * tuning change on one side can never affect the other.
 *
 * Two independent signals feed the same blocking decision:
 * - [onTabTapped]: the bottom-nav Shorts tab stays visually "selected" long after Back
 *   leaves its content, so polling that state is unsafe (it never reads false again). A
 *   click event is a genuine one-shot signal instead - it fires exactly once per real tap
 *   - so this catches the tap itself, waits briefly for the app to navigate, then reverses
 *   it with a single Back.
 * - [onContentChanged]: catches Shorts entered without tapping the tab (e.g. a Shorts
 *   thumbnail on the Home feed) by matching the actual full-screen player content.
 *
 * Both funnel through [claim] so only one Back is ever sent per real entry into Shorts,
 * even though both signals can independently notice the same entry. Re-arming after a
 * claim is handled only by [claim]'s own safety-net timer, not by [onContentChanged]
 * observing a "not Shorts" reading - a single content-changed event during a still-playing
 * close animation can transiently read false before the screen has actually settled, and
 * releasing on that reading re-armed the guard early enough for the very next (still part
 * of the same transition, not a new tap) match to trigger a second, spurious Back that
 * exited straight past Home. Waiting out the fixed timer instead avoids that race.
 */
internal class YouTubeGuard(
    private val service: AccessibilityService,
    private val handler: Handler,
    private val settings: SettingsStore,
) {

    private var blocked = false
    private var generation = 0

    fun onTabTapped(event: AccessibilityEvent) {
        if (!settings.blockYoutubeShorts) return
        val desc = event.contentDescription?.toString() ?: return
        if (!desc.equals("Shorts", ignoreCase = true)) return
        if (!claim()) return

        handler.postDelayed({
            Log.d(TAG, "Reversing tap on Shorts tab")
            service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
        }, TAP_REVERSE_DELAY_MS)
    }

    fun onContentChanged(root: AccessibilityNodeInfo) {
        if (!settings.blockYoutubeShorts) {
            release()
            return
        }
        if (isShortsScreen(root) && claim()) {
            Log.d(TAG, "Blocking Shorts (content match)")
            service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
        }
    }

    /**
     * Claims the block for a fresh entry into Shorts, returning false if one is already
     * in progress (from either signal above) so only one Back is ever sent. Re-arms after
     * a fixed [BLOCK_RESET_TIMEOUT_MS] regardless of what the detector reports in the
     * meantime - see the class doc for why re-arming on a detector reading is unsafe. The
     * generation counter stops a stale release (from an older claim) from firing after a
     * newer claim has already started.
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
     * `reel_player_page_container` is the actual, confirmed resource ID for the
     * full-screen Shorts player (taken from Scrolless, github.com/duartebarbosadev/
     * Scrolless, rather than guessed), used alone rather than as part of a broader
     * "reel"/"shorts" substring match. That broader match was tried and confirmed (via
     * real-device logcat) to also fire on the Home feed's own "Shorts" shelf, which
     * appears to reuse "reel"-related component naming - it was added to also catch
     * Shorts opened from a Home-feed thumbnail (not just the bottom-nav tab), but
     * Scrolless's own shipped code relies on this one ID with no special-casing per entry
     * path, which is strong evidence it already covers that case too. "Remix" is a
     * Shorts-only action, a second safe signal. Both are still gated by fillsScreen(),
     * since even a confirmed ID can point at a view a ViewPager keeps inflated off-screen
     * after the user has actually left it.
     */
    private fun isShortsScreen(root: AccessibilityNodeInfo): Boolean {
        val metrics = service.resources.displayMetrics
        return root.anyDescendant { node ->
            val id = node.viewIdResourceName.orEmpty()
            val desc = node.contentDescription?.toString().orEmpty()

            val looksLikeShorts = id.endsWith(SHORTS_PLAYER_ID, ignoreCase = true) ||
                desc.contains("Remix", ignoreCase = true)

            looksLikeShorts && node.fillsScreen(metrics.widthPixels, metrics.heightPixels)
        }
    }

    companion object {
        const val PACKAGE_NAME = "com.google.android.youtube"
        private const val TAG = "AntiScroll-YouTube"
        private const val SHORTS_PLAYER_ID = "reel_player_page_container"
        private const val TAP_REVERSE_DELAY_MS = 250L
        private const val BLOCK_RESET_TIMEOUT_MS = 1500L
    }
}
