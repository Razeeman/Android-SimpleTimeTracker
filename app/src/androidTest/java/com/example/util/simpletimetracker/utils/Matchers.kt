package com.example.util.simpletimetracker.utils

import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.annotation.PluralsRes
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.test.espresso.Root
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers.withTagValue
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher

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

fun isToast(): Matcher<Root> {
    return object : TypeSafeMatcher<Root>() {
        override fun matchesSafely(item: Root): Boolean {
            val type: Int = item.windowLayoutParams.get().type
            return type == WindowManager.LayoutParams.TYPE_TOAST
        }

        override fun describeTo(description: Description) {
            description.appendText("is toast")
        }
    }
}

fun withPluralText(@PluralsRes expectedId: Int, quantity: Int): Matcher<View> =
    object : BoundedMatcher<View, TextView>(TextView::class.java) {
        override fun matchesSafely(view: TextView): Boolean {
            val text: String = view.context.resources.getQuantityString(expectedId, quantity)
            return view.text == text
        }

        override fun describeTo(description: Description) {
            description.appendText("with plural text: ")
            description.appendValue(expectedId)
            description.appendText(" and quantity: ")
            description.appendValue(quantity)
        }
    }