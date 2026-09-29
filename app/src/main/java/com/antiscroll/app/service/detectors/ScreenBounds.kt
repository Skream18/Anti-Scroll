package com.antiscroll.app.service.detectors

import android.graphics.Rect
import android.view.accessibility.AccessibilityNodeInfo

/**
 * True if this node's on-screen bounds fill (most of) the display, as opposed to being a
 * small thumbnail/shelf item. Distinguishes an actually-open full-screen video player from
 * a preview of the same content sitting in a feed or grid.
 */
internal fun AccessibilityNodeInfo.fillsScreen(screenWidth: Int, screenHeight: Int): Boolean {
    val bounds = Rect()
    getBoundsInScreen(bounds)
    return bounds.width() >= screenWidth * 0.9 && bounds.height() >= screenHeight * 0.6
}
