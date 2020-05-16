package com.example.util.simpletimetracker.core.extension

import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.LinearInterpolator
import com.google.android.material.tabs.TabLayout

var View.visible: Boolean
    set(value) {
        visibility = if (value) View.VISIBLE else View.GONE
    }
    get() {
        return visibility == View.VISIBLE
    }

fun View.rotate(from: Float, to: Float, duration: Long = 300) {
    ObjectAnimator.ofFloat(this, "rotation", from, to).apply {
        this.duration = duration
        repeatCount = 0
        interpolator = LinearInterpolator()
        start()
    }
}

fun View.rotateDown() {
    this.rotate(from = 0f, to = 180f)
}

fun View.rotateUp() {
    this.rotate(from = 180f, to = 360f)
}

// TODO setOnClick

fun TabLayout.onTabSelected(func: (TabLayout.Tab) -> Unit) {
    this.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
        override fun onTabReselected(tab: TabLayout.Tab?) {
            // Do nothing
        }

        override fun onTabUnselected(tab: TabLayout.Tab?) {
            // Do nothing
        }

        override fun onTabSelected(tab: TabLayout.Tab?) {
            tab?.let(func::invoke)
        }
    })
}