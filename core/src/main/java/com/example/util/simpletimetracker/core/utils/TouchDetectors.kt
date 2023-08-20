package com.example.util.simpletimetracker.core.utils

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener
import androidx.core.view.GestureDetectorCompat
import kotlin.math.atan2

/**
 * Recognizes than user holds finger without lifting up.
 */
class HoldDetector(
    private val onDown: () -> Unit,
    private val onUp: () -> Unit,
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
    onSingleTap: (MotionEvent) -> Unit,
) {

    private val detector = GestureDetectorCompat(
        context,
        object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent): Boolean {
                onSingleTap(e)
                return super.onSingleTapUp(e)
            }
        },
    )

    fun onTouchEvent(event: MotionEvent): Boolean {
        return detector.onTouchEvent(event)
    }
}

/**
 * Recognizes scaling.
 */
class ScaleDetector(
    context: Context,
    onScaleStart: () -> Unit = {},
    onScaleChanged: (scale: Float) -> Unit,
    onScaleStop: () -> Unit = {},
) {

    private val detector = ScaleGestureDetector(
        context,
        object : SimpleOnScaleGestureListener() {
            override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
                onScaleStart()
                return true
            }

            override fun onScale(detector: ScaleGestureDetector): Boolean {
                onScaleChanged(detector.scaleFactor)
                return true
            }

            override fun onScaleEnd(detector: ScaleGestureDetector) {
                onScaleStop()
            }
        },
    )

    fun onTouchEvent(event: MotionEvent): Boolean {
        return detector.onTouchEvent(event)
    }
}

/**
 * Recognizes swipes and remembers initial swipe direction.
 */
class SwipeDetector(
    context: Context,
    private val onSlideStart: () -> Unit = {},
    onSlide: (offset: Float, initialDirection: Direction, event: MotionEvent) -> Unit = { _, _, _ -> },
    private val onSlideStop: () -> Unit = {},
) {

    enum class Direction {
        DOWN, UP, LEFT, RIGHT
    }

    private var isSliding = false
    private var initialDirection: Direction = Direction.DOWN

    private val detector = GestureDetectorCompat(
        context,
        object : GestureDetector.SimpleOnGestureListener() {
            override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
                val angle = Math.toDegrees(atan2(e1.y - e2.y, e2.x - e1.x).toDouble())

                // Right
                if (angle > -45 && angle <= 45) {
                    if (!isSliding) {
                        startSliding()
                        initialDirection = Direction.RIGHT
                    }
                    doSlide(e1, e2)
                    return true
                }

                // Down
                if (angle > -135 && angle <= -45) {
                    if (!isSliding) {
                        startSliding()
                        initialDirection = Direction.DOWN
                    }
                    doSlide(e1, e2)
                    return true
                }

                // Up
                if (angle > 45 && angle <= 135) {
                    if (!isSliding) {
                        startSliding()
                        initialDirection = Direction.UP
                    }
                    doSlide(e1, e2)
                    return true
                }

                // Left
                if (angle > 135 || angle <= -135) {
                    if (!isSliding) {
                        startSliding()
                        initialDirection = Direction.LEFT
                    }
                    doSlide(e1, e2)
                    return true
                }

                return false
            }

            private fun doSlide(e1: MotionEvent, e2: MotionEvent) {
                val offset = when (initialDirection) {
                    Direction.LEFT,
                    Direction.RIGHT,
                    -> e2.rawX - e1.rawX

                    Direction.UP,
                    Direction.DOWN,
                    -> e2.rawY - e1.rawY
                }
                onSlide(offset, initialDirection, e2)
            }
        },
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

fun SwipeDetector.Direction.isHorizontal(): Boolean =
    this == SwipeDetector.Direction.LEFT || this == SwipeDetector.Direction.RIGHT
