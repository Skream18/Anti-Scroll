package com.antiscroll.app.service.detectors

import android.view.accessibility.AccessibilityNodeInfo

/**
 * Heuristic matching against YouTube's view tree, used to catch Shorts entered without
 * tapping the bottom-nav tab (e.g. a Shorts thumbnail on the Home feed) - tapping the tab
 * itself is instead caught as a one-shot click event in GuardAccessibilityService, since a
 * tab's "selected" state stays true long after its content is gone and isn't a safe signal
 * to poll here.
 *
 * - "reel" is YouTube's internal codename for Shorts (ReelWatchFragment,
 *   reel_player_page_container, reel_recycler, etc.) and "Remix" is a Shorts-only action -
 *   both fire regardless of how the player was opened.
 * - any other "shorts"-named node only counts if it actually fills the screen, so this
 *   doesn't also match the small Shorts preview shelf that legitimately sits on Home.
 */
object YouTubeDetector {

    fun isShortsScreen(root: AccessibilityNodeInfo, screenWidth: Int, screenHeight: Int): Boolean =
        root.anyDescendant { node ->
            val id = node.viewIdResourceName.orEmpty()
            val desc = node.contentDescription?.toString().orEmpty()

            val definiteMatch = id.contains("reel", ignoreCase = true) ||
                id.contains("shorts_player", ignoreCase = true) ||
                desc.contains("Remix", ignoreCase = true)

            if (definiteMatch) {
                true
            } else if (id.contains("shorts", ignoreCase = true)) {
                node.fillsScreen(screenWidth, screenHeight)
            } else {
                false
            }
        }
}
