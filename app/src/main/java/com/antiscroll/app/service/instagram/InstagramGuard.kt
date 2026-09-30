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
 * Everything about blocking Instagram Reels, Explore, and (optionally) Stories lives in
 * this one file: detection rules, tap/content handling, and all of its own state. Nothing
 * here is shared with YouTube's guard (YouTubeGuard, in its own package) - each is fully
 * independent, so a bug or a tuning change on one side can never affect the other.
 *
 * Reels/Explore and Stories are two entirely separate, independently toggleable blocks
 * inside this same guard - separate settings, separate claim/release state
 * ([blocked]/[generation] vs [storiesBlocked]/[storiesGeneration]), separate detection
 * functions. They live in one file because they share the same Instagram event stream, but
 * neither's state can affect the other's.
 *
 * Two independent signals feed the Reels/Explore blocking decision:
 * - [onTabTapped]: the bottom-nav Reels/Explore tabs stay visually "selected" long after
 *   Back leaves their content, so polling that state is unsafe (it never reads false
 *   again). A click event is a genuine one-shot signal instead - it fires exactly once per
 *   real tap - so this catches the tap itself, waits briefly for the app to navigate, then
 *   reverses it with a single Back. This is the only signal for Explore, which has no
 *   equivalent "player" content to detect the other way.
 * - [onContentChanged]: catches Reels entered without tapping the tab (e.g. a Reel opened
 *   from the Explore grid) by matching the actual full-screen Reels player content.
 *
 * Stories has no bottom-nav tab at all (it's opened by tapping a profile's story ring), so
 * it's detected purely by content match in [onContentChanged], the same way a
 * thumbnail-opened Reel is.
 *
 * Both Reels signals funnel through [claim] so only one Back is ever sent per real Reels
 * entry, even though both signals can independently notice the same one. Re-arming after a
 * claim is handled only by [claim]'s own safety-net timer, not by [onContentChanged]
 * observing a "not Reels" reading - a single content-changed event during a still-playing
 * close animation can transiently read false before the screen has actually settled, and
 * releasing on that reading re-arms the guard early enough for the very next (still part
 * of the same transition, not a new tap) match to trigger a second, spurious Back. That
 * extra Back doesn't visibly exit Instagram (it takes three Backs in a row to actually
 * leave the app), so this was easy to miss, but it's the same underlying bug this guard
 * shares with YouTube's - just masked by Instagram's deeper back stack rather than fixed.
 * [claimStories] follows the identical pattern for Stories.
 */
internal class InstagramGuard(
    private val service: AccessibilityService,
    private val handler: Handler,
    private val settings: SettingsStore,
) {

    private var blocked = false
    private var generation = 0

    private var storiesBlocked = false
    private var storiesGeneration = 0

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
        } else if (isReelsScreen(root) && claim()) {
            Log.d(TAG, "Blocking Reels (content match)")
            service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
        }

        if (!settings.blockInstagramStories) {
            releaseStories()
        } else if (isStoriesScreen(root) && claimStories()) {
            Log.d(TAG, "Blocking Stories (content match)")
            service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
        }
    }

    /**
     * Claims the block for a fresh entry, returning false if one is already in progress
     * (from either signal above) so only one Back is ever sent. Re-arms after a fixed
     * [BLOCK_RESET_TIMEOUT_MS] regardless of what the detector reports in the meantime -
     * see the class doc for why re-arming on a detector reading is unsafe. The generation
     * counter stops a stale release (from an older claim) from firing after a newer claim
     * has already started.
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

    /** Same pattern as [claim], entirely separate state - see the class doc. */
    private fun claimStories(): Boolean {
        if (storiesBlocked) return false
        storiesBlocked = true
        storiesGeneration++
        val myGeneration = storiesGeneration
        handler.postDelayed({
            if (storiesGeneration == myGeneration) storiesBlocked = false
        }, BLOCK_RESET_TIMEOUT_MS)
        return true
    }

    private fun releaseStories() {
        if (storiesBlocked) {
            storiesBlocked = false
            storiesGeneration++
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
     *
     * There used to also be a broad `id.contains("reel")` fallback here. It was removed
     * because Instagram's Stories viewer's real ID (see [isStoriesScreen]) also contains
     * "reel" - that fallback was matching Stories too and blocking them as an unwanted side
     * effect of Reels/Explore blocking, which is what Stories now has its own separate,
     * independent toggle for instead.
     */
    private fun isReelsScreen(root: AccessibilityNodeInfo): Boolean {
        val metrics = service.resources.displayMetrics
        return root.anyDescendant { node ->
            val id = node.viewIdResourceName.orEmpty()

            val looksLikeReels = id.endsWith(REELS_VIEWER_ID, ignoreCase = true) ||
                id.contains("clips", ignoreCase = true)

            looksLikeReels && node.fillsScreen(metrics.widthPixels, metrics.heightPixels)
        }
    }

    /**
     * `reel_viewer_root` is the actual, confirmed resource ID Instagram uses for the
     * full-screen Stories viewer - also taken from Scrolless, which documents it as a
     * distinct ID from the Reels player above. Matched alone, with no broader substring
     * fallback: that's precisely the mistake being fixed in [isReelsScreen].
     */
    private fun isStoriesScreen(root: AccessibilityNodeInfo): Boolean {
        val metrics = service.resources.displayMetrics
        return root.anyDescendant { node ->
            val id = node.viewIdResourceName.orEmpty()
            id.endsWith(STORIES_VIEWER_ID, ignoreCase = true) &&
                node.fillsScreen(metrics.widthPixels, metrics.heightPixels)
        }
    }

    companion object {
        const val PACKAGE_NAME = "com.instagram.android"
        private const val TAG = "AntiScroll-Instagram"
        private const val REELS_VIEWER_ID = "clips_viewer_view_pager"
        private const val STORIES_VIEWER_ID = "reel_viewer_root"
        private val TAB_LABELS = setOf("Reels", "Explore", "Search and explore", "Search")
        private const val TAP_REVERSE_DELAY_MS = 250L
        private const val BLOCK_RESET_TIMEOUT_MS = 1500L
    }
}
