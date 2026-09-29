package com.antiscroll.app.service.common

import android.graphics.Rect
import android.view.accessibility.AccessibilityNodeInfo

/**
 * Generic accessibility-tree helper, shared by the YouTube and Instagram guards because
 * it has no app-specific logic in it - unlike detection rules and blocking state, which
 * are kept fully separate per app.
 *
 * True if this node is actually visible right now AND its on-screen bounds fill (most of)
 * the display. Both parts matter: view pagers commonly keep an adjacent page's view
 * inflated off-screen for smooth swiping, so a player node can still exist in the tree
 * with stale full-screen bounds long after the user has actually left it - matching on
 * bounds alone let a since-closed Shorts/Reels player keep matching indefinitely on
 * whatever screen came after it, triggering repeated unwanted Back presses.
 */
internal fun AccessibilityNodeInfo.fillsScreen(screenWidth: Int, screenHeight: Int): Boolean {
    if (!isVisibleToUser) return false
    val bounds = Rect()
    getBoundsInScreen(bounds)
    return bounds.width() >= screenWidth * 0.9 && bounds.height() >= screenHeight * 0.6
}
