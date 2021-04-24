package com.example.util.simpletimetracker.utils

import android.view.View
import android.view.ViewGroup
import android.view.ViewParent
import com.google.android.material.appbar.AppBarLayout

internal fun View.collapseAllAppBarsInParent() {
    findViewsInParent(AppBarLayout::class.java)
        .forEach { it.setExpanded(false) }
}

internal fun <T> View.findViewsInParent(
    viewType: Class<T>
): Collection<T> = findViewsInParentRecursively(
    startView = this,
    currentParent = this.parent,
    type = viewType
)

private fun <T> findViewsInParentRecursively(
    startView: View,
    currentParent: ViewParent,
    type: Class<T>,
    result: MutableCollection<T> = mutableSetOf()
): Collection<T> {
    if (currentParent !is ViewGroup) {
        return result
    }

    if (type.isInstance(currentParent)) {
        @Suppress("UNCHECKED_CAST")
        result += currentParent as T
    }

    (0 until currentParent.childCount)
        .map { currentParent.getChildAt(it) }
        .filter { it != startView }
        .filter { type.isInstance(it) }
        .forEach { view ->
            @Suppress("UNCHECKED_CAST")
            result += view as T
        }

    return findViewsInParentRecursively(
        startView = startView,
        currentParent = currentParent.parent,
        type = type,
        result = result
    )
}