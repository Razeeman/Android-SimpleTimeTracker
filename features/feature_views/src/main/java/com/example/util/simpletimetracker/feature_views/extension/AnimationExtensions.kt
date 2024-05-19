package com.example.util.simpletimetracker.feature_views.extension

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.annotation.ColorInt

fun View.rotate(from: Float, to: Float, duration: Long = 300) {
    ObjectAnimator.ofFloat(this, "rotation", from, to).apply {
        this.duration = duration
        repeatCount = 0
        interpolator = LinearInterpolator()
        start()
    }
}

fun View.animateAlpha(isVisible: Boolean, duration: Long = 300) {
    val from = alpha
    val to = if (isVisible) 1f else 0f

    ObjectAnimator.ofFloat(this, "alpha", from, to).apply {
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

fun animateColor(
    @ColorInt from: Int,
    @ColorInt to: Int,
    duration: Long = 300,
    doOnUpdate: (value: Int) -> Unit,
): ValueAnimator {
    return ValueAnimator.ofObject(ArgbEvaluator(), from, to).apply {
        this.duration = duration
        addUpdateListener {
            val value = it.animatedValue as? Int ?: return@addUpdateListener
            doOnUpdate(value)
        }
        start()
    }
}