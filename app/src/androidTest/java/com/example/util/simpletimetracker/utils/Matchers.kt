package com.example.util.simpletimetracker.utils

import android.view.View
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers.withTagValue
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.Description
import org.hamcrest.Matcher

fun withCardColor(expectedId: Int): Matcher<View> =
    object : BoundedMatcher<View, CardView>(CardView::class.java) {
        override fun matchesSafely(view: CardView): Boolean {
            val colorInt: Int = ContextCompat.getColor(view.context, expectedId)
            return view.cardBackgroundColor.defaultColor == colorInt
        }

        override fun describeTo(description: Description) {
            description.appendText("with card color: ")
            description.appendValue(expectedId)
        }
    }

fun withTag(tagValueMatcher: Int): Matcher<View> =
    withTagValue(equalTo(tagValueMatcher))