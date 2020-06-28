package com.example.util.simpletimetracker.utils

import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.longClick
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerViewHolder
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
    onView(withId(id)).perform(replaceText(""), typeText(text))

fun clickOnViewWithId(id: Int): ViewInteraction =
    onView(withId(id)).perform(click())

fun clickOnViewWithText(textId: Int): ViewInteraction =
    onView(withText(textId)).perform(click())

fun clickOnView(matcher: Matcher<View>): ViewInteraction =
    onView(matcher).perform(click())

fun longClickOnView(matcher: Matcher<View>): ViewInteraction =
    onView(matcher).perform(longClick())

fun clickOnRecyclerItem(id: Int, matcher: Matcher<View>): ViewInteraction =
    onView(allOf(isDescendantOfA(withId(id)), matcher)).perform(click())

fun scrollToPosition(id: Int, position: Int): ViewInteraction =
    onView(withId(id))
        .perform(RecyclerViewActions.scrollToPosition<BaseRecyclerViewHolder>(position))