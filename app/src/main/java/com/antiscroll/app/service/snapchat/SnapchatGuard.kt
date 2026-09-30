package com.antiscroll.app.service.snapchat

import android.accessibilityservice.AccessibilityService
import android.os.Handler
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo
import com.antiscroll.app.data.SettingsStore
import com.antiscroll.app.service.common.anyDescendant
import com.antiscroll.app.service.common.fillsScreen

/**
 * Everything about blocking Snapchat Spotlight and (optionally) Stories lives in this one
 * file, structured exactly like YouTubeGuard/InstagramGuard: its own package, its own
 * state, its own detection rules, nothing shared with the other guards.
 *
 * Spotlight and Stories are two entirely separate, independently toggleable blocks -
 * separate settings, separate claim/release state ([blocked]/[generation] vs
 * [storiesBlocked]/[storiesGeneration]). Spotlight is Snapchat's TikTok-style algorithmic
 * video feed - the actual doomscroll surface this app exists to block, on by default.
 * Stories (friends' updates) are left untouched by default, matching Instagram's model.
 *
 * Unlike YouTube/Instagram, there's no bottom-nav "tab tap" worth chasing here - Spotlight
 * isn't a discrete tab the same way Shorts/Reels are, so both blocks are pure content
 * matches, the same technique already used for Instagram Stories and a thumbnail-opened
 * Reel: re-checked on every event, gated by fillsScreen() (visible right now AND filling
 * nearly the whole screen, not a swipeable pager's off-screen cached page), with a claim/
 * release/generation-counter pair to guarantee exactly one Back per real entry and a fixed
 * safety-net re-arm rather than trusting a possibly-transient "not blocked" reading - see
 * InstagramGuard's class doc for why that distinction matters.
 */
internal class SnapchatGuard(
    private val service: AccessibilityService,
    private val handler: Handler,
    private val settings: SettingsStore,
) {

    private var blocked = false
    private var generation = 0

    private var storiesBlocked = false
    private var storiesGeneration = 0

    fun onContentChanged(root: AccessibilityNodeInfo) {
        if (!settings.blockSnapchatSpotlight) {
            release()
        } else if (isSpotlightScreen(root) && claim()) {
            Log.d(TAG, "Blocking Spotlight (content match)")
            service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
        }

        if (!settings.blockSnapchatStories) {
            releaseStories()
        } else if (isStoriesScreen(root) && claimStories()) {
            Log.d(TAG, "Blocking Stories (content match)")
            service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
        }
    }

    /** Same pattern as InstagramGuard.claim() - see that class's doc for the reasoning. */
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

    /** Same pattern as [claim], entirely separate state. */
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
     * `spotlight_container` is the actual, confirmed resource ID Snapchat uses for the
     * full-screen Spotlight feed - taken from Scrolless (github.com/duartebarbosadev/
     * Scrolless), the same open-source accessibility blocker that gave us YouTube's and
     * Instagram's real IDs, rather than guessed.
     */
    private fun isSpotlightScreen(root: AccessibilityNodeInfo): Boolean {
        val metrics = service.resources.displayMetrics
        return root.anyDescendant { node ->
            node.viewIdResourceName.orEmpty().endsWith(SPOTLIGHT_CONTAINER_ID, ignoreCase = true) &&
                node.fillsScreen(metrics.widthPixels, metrics.heightPixels)
        }
    }

    /**
     * `opera_viewer` is the actual, confirmed resource ID Snapchat uses for its full-screen
     * Story viewer - also taken from Scrolless. This covers all Stories uniformly (friends'
     * and Discover/publisher content alike) - Scrolless doesn't distinguish between them
     * either, so this toggle is all-or-nothing and stays off by default.
     */
    private fun isStoriesScreen(root: AccessibilityNodeInfo): Boolean {
        val metrics = service.resources.displayMetrics
        return root.anyDescendant { node ->
            node.viewIdResourceName.orEmpty().endsWith(STORIES_VIEWER_ID, ignoreCase = true) &&
                node.fillsScreen(metrics.widthPixels, metrics.heightPixels)
        }
    }

    companion object {
        const val PACKAGE_NAME = "com.snapchat.android"
        private const val TAG = "AntiScroll-Snapchat"
        private const val SPOTLIGHT_CONTAINER_ID = "spotlight_container"
        private const val STORIES_VIEWER_ID = "opera_viewer"
        private const val BLOCK_RESET_TIMEOUT_MS = 1500L
    }
}
