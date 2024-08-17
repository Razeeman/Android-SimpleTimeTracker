package com.example.util.simpletimetracker.core.utils

import android.view.View
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

// Can update padding or margin, which would be more appropriate.
fun View.doOnApplyWindowInsetsListener(block: View.(WindowInsetsCompat) -> Unit) {
    ViewCompat.setOnApplyWindowInsetsListener(this) { _, windowInsets ->
        block(windowInsets)
        windowInsets
    }
}

fun View.applySystemBarInsets() {
    doOnApplyWindowInsetsListener {
        val insets = it.getSystemBarInsets()
        updatePadding(top = insets.top, bottom = insets.bottom)
    }
}

fun View.applyStatusBarInsets() {
    doOnApplyWindowInsetsListener { updatePadding(top = it.getStatusBarInsets().top) }
}

fun View.applyNavBarInsets() {
    doOnApplyWindowInsetsListener { updatePadding(bottom = it.getNavBarInsets().bottom) }
}

fun WindowInsetsCompat.getStatusBarInsets(): Insets {
    return getInsets(WindowInsetsCompat.Type.statusBars())
}

fun WindowInsetsCompat.getNavBarInsets(): Insets {
    return getInsets(WindowInsetsCompat.Type.navigationBars())
}

fun WindowInsetsCompat.getSystemBarInsets(): Insets {
    return getInsets(WindowInsetsCompat.Type.systemBars())
}
