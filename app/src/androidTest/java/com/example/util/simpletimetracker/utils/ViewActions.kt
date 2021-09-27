package com.example.util.simpletimetracker.utils

import android.view.View
import android.view.ViewConfiguration
import android.widget.HorizontalScrollView
import android.widget.ScrollView
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.PerformException
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.*
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import com.google.android.material.tabs.TabLayout
import org.hamcrest.Matcher
import org.hamcrest.Matchers.*

enum class Direction {
    UP, DOWN, LEFT, RIGHT, COORDINATES
}

fun swipeUp(requiredViewVisibilityPercentage: Int): ViewAction = object : ViewAction {
    override fun getConstraints(): Matcher<View> {
        return isDisplayingAtLeast(requiredViewVisibilityPercentage)
    }

    override fun getDescription(): String {
        return "perform flexible swipe up"
    }

    override fun perform(uiController: UiController?, view: View?) {
        GeneralSwipeAction(
            Swipe.FAST,
            GeneralLocation.VISIBLE_CENTER,
            GeneralLocation.TOP_CENTER,
            Press.FINGER).perform(uiController, view)
    }
}

fun selectTabAtPosition(tabIndex: Int): ViewAction = object : ViewAction {
    override fun getDescription() =
        "with tab at index $tabIndex"

    override fun getConstraints() =
        allOf(isDisplayed(), isAssignableFrom(TabLayout::class.java))

    override fun perform(uiController: UiController, view: View) {
        val tabAtIndex: TabLayout.Tab = (view as TabLayout).getTabAt(tabIndex)
            ?: throw PerformException.Builder()
                .withCause(Throwable("No tab at index $tabIndex"))
                .build()

        tabAtIndex.select()
    }
}

fun unconstrainedClick(): ViewAction = object : ViewAction {
    override fun getDescription(): String =
        "unconstrained click"

    override fun getConstraints(): Matcher<View> =
        ViewMatchers.isEnabled()

    override fun perform(uiController: UiController, view: View) {
        view.performClick()
    }
}

fun nestedScrollTo(): ViewAction = object : ViewAction {
    override fun getDescription(): String =
        "nested scroll to"

    override fun getConstraints(): Matcher<View> = allOf(
        withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE),
        isDescendantOfA(
            anyOf(
                isAssignableFrom(ScrollView::class.java),
                isAssignableFrom(HorizontalScrollView::class.java),
                isAssignableFrom(NestedScrollView::class.java)
            )
        )
    )

    override fun perform(uiController: UiController, view: View) {
        ScrollToAction().perform(uiController, view)
    }
}

fun drag(
    direction: Direction = Direction.COORDINATES,
    offset: Int = 0,
    coordinates: FloatArray = FloatArray(2) { 0.0f }
): ViewAction = object : ViewAction {
    private val SWIPE_EVENT_COUNT = 50

    override fun getDescription(): String = "dragging"

    override fun getConstraints(): Matcher<View> = isDisplayed()

    override fun perform(uiController: UiController, view: View) {
        uiController.loopMainThreadUntilIdle()

        val coordinatesProvider: CoordinatesProvider = getCoordinatesProvider()
        val viewCoordinates = coordinatesProvider.calculateCoordinates(view)
        val destCoordinates = getDestinationCoordinates(viewCoordinates)

        val precision = Press.PINPOINT.describePrecision()
        val downEvent =
            MotionEvents.sendDown(uiController, viewCoordinates, precision).down

        try {
            val longPressTimeout = (ViewConfiguration.getLongPressTimeout() * 1.5).toLong()

            uiController.loopMainThreadForAtLeast(longPressTimeout)

            val steps: Array<FloatArray> = interpolate(
                viewCoordinates,
                destCoordinates
            )

            uiController.loopMainThreadUntilIdle()

            for (step in steps) {
                if (!MotionEvents.sendMovement(uiController, downEvent, step)) {
                    MotionEvents.sendCancel(uiController, downEvent)
                }
            }
            if (!MotionEvents.sendUp(uiController, downEvent, destCoordinates)) {
                MotionEvents.sendCancel(uiController, downEvent)
            }
        } catch (e: Exception) {
            println(e)
        } finally {
            downEvent.recycle()
        }
    }

    private fun getCoordinatesProvider(): CoordinatesProvider {
        return CoordinatesProvider { view ->
            val location = IntArray(2)
            view.getLocationInWindow(location)
            val x = location[0] + (view.measuredWidth / 2).toFloat()
            val y = location[1] + (view.measuredHeight / 2).toFloat()
            floatArrayOf(x, y)
        }
    }

    private fun interpolate(start: FloatArray, end: FloatArray): Array<FloatArray> {
        val res = Array(SWIPE_EVENT_COUNT) { FloatArray(2) }

        for (i in 1..SWIPE_EVENT_COUNT) {
            res[i - 1][0] = start[0] + (end[0] - start[0]) * i / SWIPE_EVENT_COUNT
            res[i - 1][1] = start[1] + (end[1] - start[1]) * i / SWIPE_EVENT_COUNT
        }

        return res
    }

    private fun getDestinationCoordinates(initial: FloatArray): FloatArray {
        var destination = initial.clone()

        when (direction) {
            Direction.UP -> destination[1] = destination[1] - offset
            Direction.DOWN -> destination[1] = destination[1] + offset
            Direction.LEFT -> destination[0] = destination[0] - offset
            Direction.RIGHT -> destination[0] = destination[0] + offset
            Direction.COORDINATES -> destination = coordinates
        }

        return destination
    }
}

fun collapseToolbar(): ViewAction = object : ViewAction {
    override fun getDescription(): String =
        "collapse toolbar"

    override fun getConstraints(): Matcher<View> =
        isDisplayed()

    override fun perform(uiController: UiController?, view: View?) {
        view?.collapseAllAppBarsInParent()
    }
}

fun scrollToBottom(): ViewAction = object : ViewAction {
    override fun getDescription(): String =
        "scroll RecyclerView to bottom"

    override fun getConstraints(): Matcher<View> =
        allOf<View>(isAssignableFrom(RecyclerView::class.java), isDisplayed())

    override fun perform(uiController: UiController?, view: View?) {
        val recyclerView = view as RecyclerView
        val itemCount = recyclerView.adapter?.itemCount
        val position = itemCount?.minus(1) ?: 0
        recyclerView.scrollToPosition(position)
        uiController?.loopMainThreadUntilIdle()
    }
}

fun tryAction(action: () -> Unit) {
    repeat(5) {
        try {
            action()
            return
        } catch (e: Throwable) {
            Thread.sleep(1000)
        }
    }
}