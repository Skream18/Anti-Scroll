package com.antiscroll.app.service.detectors

import android.view.accessibility.AccessibilityNodeInfo

/**
 * Heuristic matching against YouTube's view tree. YouTube changes resource IDs across
 * releases, so this matches on several independent signals rather than one exact ID -
 * expect to need adjustments here after real-device testing.
 *
 * - contentDescription "Shorts" on a selected node matches the bottom-nav Shorts tab
 *   being active - this is the signal that actually caught the tab in testing.
 * - "reel" is YouTube's internal codename for Shorts (ReelWatchFragment,
 *   reel_player_page_container, reel_recycler, etc.) and "Remix" is a Shorts-only action -
 *   both fire regardless of how the player was opened (tab, a shared link, search).
 * - any other "shorts"-named node only counts if it actually fills the screen. This is
 *   what catches Shorts opened from a Home-feed shelf thumbnail (which doesn't select the
 *   Shorts tab and may not use "reel" naming) without also matching the small shelf
 *   preview that legitimately sits on the Home feed.
 */
object YouTubeDetector {

    fun isShortsScreen(root: AccessibilityNodeInfo, screenWidth: Int, screenHeight: Int): Boolean =
        root.anyDescendant { node ->
            val id = node.viewIdResourceName.orEmpty()
            val desc = node.contentDescription?.toString().orEmpty()

            val definiteMatch = id.contains("reel", ignoreCase = true) ||
                id.contains("shorts_player", ignoreCase = true) ||
                desc.contains("Remix", ignoreCase = true) ||
                (node.isSelected && (id.contains("shorts", ignoreCase = true) || desc.equals("Shorts", ignoreCase = true)))

            if (definiteMatch) {
                true
            } else if (id.contains("shorts", ignoreCase = true)) {
                node.fillsScreen(screenWidth, screenHeight)
            } else {
                false
            }
        }
}
