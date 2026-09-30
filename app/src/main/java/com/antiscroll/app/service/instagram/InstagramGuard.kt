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
 * here is shared with YouTube's or Snapchat's guards (their own packages) - each is fully
 * independent, so a bug or a tuning change on one side can never affect the others.
 *
 * Reels, Explore, and Stories are three entirely separate, independently toggleable blocks
 * inside this same guard - separate settings, separate claim/release state
 * ([blocked]/[generation] for Reels, [exploreBlocked]/[exploreGeneration] for Explore,
 * [storiesBlocked]/[storiesGeneration] for Stories), separate detection functions. They
 * live in one file because they share the same Instagram event stream, but none of their
 * state can affect the others'.
 *
 * Two independent signals feed the Reels blocking decision:
 * - [onTabTapped]: the bottom-nav Reels tab stays visually "selected" long after Back
 *   leaves its content, so polling that state is unsafe (it never reads false again). A
 *   click event is a genuine one-shot signal instead - it fires exactly once per real tap -
 *   so this catches the tap itself, waits briefly for the app to navigate, then reverses it
 *   with a single Back. The same tap-based approach covers Explore too, independently.
 * - [onContentChanged]: catches Reels entered without tapping the tab (e.g. a Reel opened
 *   from the Explore grid) by matching the actual full-screen Reels player content. This is
 *   also where the one exception lives: a Reel a friend shared in a DM or group chat is
 *   allowed to play (see [isDmSharedReel]), but scrolling from it into the regular
 *   algorithmic feed is blocked exactly as normal.
 *
 * Explore and Stories have no content signal of their own (Explore is a browsable grid, not
 * a player; Stories is covered separately below) - Explore is caught purely by its tab tap,
 * Stories purely by content match, the same way a thumbnail-opened Reel is.
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
 * [claimExplore] and [claimStories] follow the identical pattern for their own blocks.
 */
internal class InstagramGuard(
    private val service: AccessibilityService,
    private val handler: Handler,
    private val settings: SettingsStore,
) {

    private var blocked = false
    private var generation = 0

    private var exploreBlocked = false
    private var exploreGeneration = 0

    private var storiesBlocked = false
    private var storiesGeneration = 0

    fun onTabTapped(event: AccessibilityEvent) {
        val desc = event.contentDescription?.toString() ?: return

        if (settings.blockInstagramReels && desc.equals(REELS_TAB_LABEL, ignoreCase = true) && claim()) {
            handler.postDelayed({
                Log.d(TAG, "Reversing tap on Reels tab")
                service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
            }, TAP_REVERSE_DELAY_MS)
            return
        }

        if (settings.blockInstagramExplore &&
            EXPLORE_TAB_LABELS.any { desc.equals(it, ignoreCase = true) } &&
            claimExplore()
        ) {
            handler.postDelayed({
                Log.d(TAG, "Reversing tap on Explore tab")
                service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
            }, TAP_REVERSE_DELAY_MS)
        }
    }

    fun onContentChanged(root: AccessibilityNodeInfo) {
        if (!settings.blockInstagramReels) {
            release()
        } else if (isReelsScreen(root)) {
            if (isDmSharedReel(root)) {
                // A friend shared this one specific Reel in a DM/group - let it play. The
                // moment the user scrolls past it into the algorithmic continuation, this
                // chrome disappears and isDmSharedReel() stops matching on its own, so
                // blocking resumes with no extra "how many reels seen" tracking needed.
                release()
            } else if (claim()) {
                Log.d(TAG, "Blocking Reels (content match)")
                service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
            }
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
    private fun claimExplore(): Boolean {
        if (exploreBlocked) return false
        exploreBlocked = true
        exploreGeneration++
        val myGeneration = exploreGeneration
        handler.postDelayed({
            if (exploreGeneration == myGeneration) exploreBlocked = false
        }, BLOCK_RESET_TIMEOUT_MS)
        return true
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
     * True if the Reels player currently on screen is showing the one specific Reel a
     * friend shared in a DM or group chat, rather than the main Reels tab or Explore.
     * Instagram overlays a "sent by <name>, <time>" header and a reply text box on top of
     * a message-shared Reel for as long as you're still on it - chrome that doesn't exist
     * on the regular Reels tab. `suggested_title` is Instagram's own label for its "you
     * might also like" algorithmic carousel, forbidden here so that surface can never
     * accidentally qualify. All four IDs are confirmed from Scrolless, same as the player
     * ID above, rather than guessed.
     */
    private fun isDmSharedReel(root: AccessibilityNodeInfo): Boolean {
        fun hasVisibleId(idSuffix: String) = root.anyDescendant { node ->
            node.viewIdResourceName.orEmpty().endsWith(idSuffix, ignoreCase = true) && node.isVisibleToUser
        }
        if (hasVisibleId(SUGGESTED_TITLE_ID)) return false
        return DM_REQUIRED_IDS.all { hasVisibleId(it) }
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
        private val DM_REQUIRED_IDS = setOf(
            "sender_username_or_fullname",
            "sender_timestamp",
            "reply_bar_edittext",
        )
        private const val SUGGESTED_TITLE_ID = "suggested_title"
        private const val REELS_TAB_LABEL = "Reels"
        private val EXPLORE_TAB_LABELS = setOf("Explore", "Search and explore", "Search")
        private const val TAP_REVERSE_DELAY_MS = 250L
        private const val BLOCK_RESET_TIMEOUT_MS = 1500L
    }
}
