package com.example.util.simpletimetracker.core.extension

import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.LinearInterpolator

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

fun View.animateFlip(duration: Long = 300) {
    ObjectAnimator.ofFloat(
        this,
        "rotation",
        this.rotation,
        this.rotation + 180
    ).apply {
        this.duration = duration
        repeatCount = 0
        interpolator = LinearInterpolator()
        start()
    }
}