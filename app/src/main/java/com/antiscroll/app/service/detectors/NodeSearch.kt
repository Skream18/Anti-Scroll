package com.antiscroll.app.service.detectors

import android.view.accessibility.AccessibilityNodeInfo

/** Depth-first search over this node and its descendants for one matching [predicate]. */
internal fun AccessibilityNodeInfo.anyDescendant(predicate: (AccessibilityNodeInfo) -> Boolean): Boolean {
    if (predicate(this)) return true
    for (i in 0 until childCount) {
        val child = getChild(i) ?: continue
        if (child.anyDescendant(predicate)) return true
    }
    return false
}
