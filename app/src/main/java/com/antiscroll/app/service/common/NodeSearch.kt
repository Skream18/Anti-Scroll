package com.antiscroll.app.service.common

import android.view.accessibility.AccessibilityNodeInfo

/**
 * Generic accessibility-tree helper, shared by the YouTube and Instagram guards because
 * it has no app-specific logic in it - unlike detection rules and blocking state, which
 * are kept fully separate per app.
 */

/** Depth-first search over this node and its descendants for one matching [predicate]. */
internal fun AccessibilityNodeInfo.anyDescendant(predicate: (AccessibilityNodeInfo) -> Boolean): Boolean {
    if (predicate(this)) return true
    for (i in 0 until childCount) {
        val child = getChild(i) ?: continue
        if (child.anyDescendant(predicate)) return true
    }
    return false
}
