package com.example.util.simpletimetracker.utils

import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.longClick
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollTo
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerViewHolder
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Matcher

fun checkViewIsNotDisplayed(matcher: Matcher<View>): ViewInteraction =
    onView(matcher).check(matches(not(isDisplayed())))

fun checkViewDoesNotExist(matcher: Matcher<View>): ViewInteraction =
    onView(matcher).check(doesNotExist())

fun checkViewIsDisplayed(matcher: Matcher<View>): ViewInteraction =
    onView(matcher).check(matches(isDisplayed()))

fun typeTextIntoView(id: Int, text: String): ViewInteraction =
    onView(withId(id)).perform(replaceText(text)) // typeText sometimes misses letters

fun clickOnViewWithId(id: Int): ViewInteraction =
    onView(withId(id)).perform(click())

fun clickOnViewWithIdOnPager(id: Int): ViewInteraction =
    onView(allOf(isCompletelyDisplayed(), withId(id))).perform(click())

fun clickOnViewWithText(textId: Int): ViewInteraction =
    onView(withText(textId)).perform(click())

fun clickOnViewWithText(text: String): ViewInteraction =
    onView(withText(text)).perform(click())

fun clickOnView(matcher: Matcher<View>): ViewInteraction =
    onView(matcher).perform(click())

fun unconstrainedClickOnView(matcher: Matcher<View>): ViewInteraction =
    onView(matcher).perform(unconstrainedClick())

fun longClickOnView(matcher: Matcher<View>): ViewInteraction =
    onView(matcher).perform(longClick())

fun longClickOnViewWithId(id: Int): ViewInteraction =
    onView(withId(id)).perform(longClick())

fun clickOnRecyclerItem(id: Int, matcher: Matcher<View>): ViewInteraction =
    onView(allOf(isDescendantOfA(withId(id)), matcher)).perform(click())

fun scrollRecyclerToPosition(id: Int, position: Int): ViewInteraction =
    onView(withId(id)).perform(scrollToPosition<BaseRecyclerViewHolder>(position))

fun scrollRecyclerToView(id: Int, matcher: Matcher<View>): ViewInteraction =
    onView(withId(id)).perform(scrollTo<BaseRecyclerViewHolder>(matcher))

fun toastTextShowing(textId: Int): ViewInteraction =
    onView(withText(textId)).inRoot(isToast()).check(matches(isDisplayed()))

fun clickOnSpinnerWithId(id: Int) {
    onView(withId(id)).perform(nestedScrollTo())
    // Double click to avoid failure on low api small screens
    clickOnViewWithId(id)
    pressBack()
    clickOnViewWithId(id)
}