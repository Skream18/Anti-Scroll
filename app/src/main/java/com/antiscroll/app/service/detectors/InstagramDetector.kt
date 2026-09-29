package com.antiscroll.app.service.detectors

import android.view.accessibility.AccessibilityNodeInfo

/**
 * v1 scope: block the bottom-nav Reels tab and the Explore/Search tab. A reel opened from
 * a DM thread does NOT match either check below, so it is intentionally left alone here -
 * that exception (plus stopping the endless scroll on it) is deferred to v2.
 *
 * Reels: `clips_viewer_view_pager` is the actual, confirmed resource ID Instagram uses for
 * the full-screen Reels pager (Reels shipped under the internal codename "Clips") - taken
 * from Scrolless (github.com/duartebarbosadev/Scrolless), another open-source accessibility
 * blocker, rather than guessed. This requires the service's `flagReportViewIds` flag to be
 * set in accessibility_service_config.xml, or Android never populates view IDs at all -
 * that flag was missing before and is why Instagram blocking did nothing.
 *
 * Explore: Instagram doesn't expose a stable ID for this the way it does for Reels
 * (Scrolless doesn't attempt to block it either), so this stays a content-description
 * guess on the bottom-nav tab's selected state, with a couple of likely label variants
 * layered together to raise the odds one of them is right.
 */
object InstagramDetector {

    private const val REELS_VIEWER_ID = "clips_viewer_view_pager"

    fun isBlockedScreen(root: AccessibilityNodeInfo, screenWidth: Int, screenHeight: Int): Boolean =
        root.anyDescendant { node ->
            val id = node.viewIdResourceName.orEmpty()
            val desc = node.contentDescription?.toString().orEmpty()

            val exploreTabSelected = node.isSelected && (
                desc.equals("Explore", ignoreCase = true) ||
                    desc.equals("Search and explore", ignoreCase = true) ||
                    desc.equals("Search", ignoreCase = true)
                )
            val reelsTabSelected = node.isSelected && desc.equals("Reels", ignoreCase = true)

            when {
                exploreTabSelected || reelsTabSelected -> true
                id.endsWith(REELS_VIEWER_ID, ignoreCase = true) -> node.fillsScreen(screenWidth, screenHeight)
                id.contains("clips", ignoreCase = true) || id.contains("reel", ignoreCase = true) ->
                    node.fillsScreen(screenWidth, screenHeight)
                else -> false
            }
        }
}
