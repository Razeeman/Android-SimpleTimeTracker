package com.example.util.simpletimetracker.core.utils

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.core.view.GestureDetectorCompat
import kotlin.math.atan2

/**
 * Recognizes than user holds finger without lifting up.
 */
class HoldDetector(
    private val onDown: () -> Unit,
    private val onUp: () -> Unit
) {

    fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                onDown()
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                onUp()
                return true
            }
        }
        return false
    }
}

/**
 * Recognizes single fast tap.
 */
class SingleTapDetector(
    context: Context,
    onSingleTap: (MotionEvent) -> Unit
) {

    private val detector = GestureDetectorCompat(
        context,
        object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent): Boolean {
                onSingleTap(e)
                return super.onSingleTapUp(e)
            }
        }
    )

    fun onTouchEvent(event: MotionEvent): Boolean {
        return detector.onTouchEvent(event)
    }
}

/**
 * Recognizes up and down swipes and remembers initial swipe direction.
 */
class SwipeDetector(
    context: Context,
    private val onSlideStart: () -> Unit = {},
    onSlide: (offset: Float, initialDirection: Direction) -> Unit = { _, _ -> Unit },
    private val onSlideStop: () -> Unit = {}
) {

    enum class Direction {
        DOWN, UP
    }

    private var isSliding = false
    private var initialDirection: Direction = Direction.DOWN

    private val detector = GestureDetectorCompat(
        context,
        object : GestureDetector.SimpleOnGestureListener() {
            override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
                val angle = Math.toDegrees(atan2(e1.y - e2.y, e2.x - e1.x).toDouble())

                // Down
                if (angle < -45 && angle >= -135) {
                    if (!isSliding) {
                        startSliding()
                        initialDirection = Direction.DOWN
                    }
                    onSlide(e2.rawY - e1.rawY, initialDirection)
                    return true
                }

                // Up
                if (angle > 45 && angle <= 135) {
                    if (!isSliding) {
                        startSliding()
                        initialDirection = Direction.UP
                    }
                    onSlide(e2.rawY - e1.rawY, initialDirection)
                    return true
                }

                return false
            }
        }
    )

    fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> if (isSliding) stopSliding()
        }
        return detector.onTouchEvent(event)
    }

    private fun startSliding() {
        isSliding = true
        onSlideStart()
    }

    private fun stopSliding() {
        isSliding = false
        onSlideStop()
    }
}
