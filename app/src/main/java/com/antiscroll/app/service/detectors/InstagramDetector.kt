package com.antiscroll.app.service.detectors

import android.view.accessibility.AccessibilityNodeInfo

/**
 * Heuristic matching against Instagram's view tree, used to catch Reels entered without
 * tapping the bottom-nav tab (e.g. a Reel opened from the Explore grid) - tapping the
 * Reels/Explore tab itself is instead caught as a one-shot click event in
 * GuardAccessibilityService, since a tab's "selected" state stays true long after its
 * content is gone and isn't a safe signal to poll here.
 *
 * `clips_viewer_view_pager` is the actual, confirmed resource ID Instagram uses for the
 * full-screen Reels pager (Reels shipped under the internal codename "Clips") - taken from
 * Scrolless (github.com/duartebarbosadev/Scrolless), another open-source accessibility
 * blocker, rather than guessed. Explore has no equivalent "player" content to detect this
 * way, so it's covered only by the tab-click path.
 */
object InstagramDetector {

    private const val REELS_VIEWER_ID = "clips_viewer_view_pager"

    fun isBlockedScreen(root: AccessibilityNodeInfo, screenWidth: Int, screenHeight: Int): Boolean =
        root.anyDescendant { node ->
            val id = node.viewIdResourceName.orEmpty()

            when {
                id.endsWith(REELS_VIEWER_ID, ignoreCase = true) -> node.fillsScreen(screenWidth, screenHeight)
                id.contains("clips", ignoreCase = true) || id.contains("reel", ignoreCase = true) ->
                    node.fillsScreen(screenWidth, screenHeight)
                else -> false
            }
        }
}
