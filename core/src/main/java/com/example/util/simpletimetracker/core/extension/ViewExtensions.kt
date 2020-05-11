package com.example.util.simpletimetracker.core.extension

import android.view.View

var View.visible: Boolean
    set(value) {
        visibility = if (value) View.VISIBLE else View.GONE
    }
    get() {
        return visibility == View.VISIBLE
    }

fun View.flipVisibility() {
    visibility = if (this.visibility == View.VISIBLE) View.GONE else View.VISIBLE
}