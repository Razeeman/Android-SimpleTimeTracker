package com.example.util.simpletimetracker

import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.contrib.RecyclerViewActions.scrollTo
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerViewHolder
import com.example.util.simpletimetracker.utils.clickOnView
import com.example.util.simpletimetracker.utils.collapseToolbar
import com.example.util.simpletimetracker.utils.withTag
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.Matcher
import com.example.util.simpletimetracker.feature_statistics_detail.R as statDetailR

// Scroll

fun scrollStatDetailRecyclerToTag(tag: Any) {
    scrollStatDetailRecyclerToView(withTag(tag))
}

fun scrollStatDetailRecycler(matcher: Matcher<View>) {
    scrollStatDetailRecyclerToView(matcher)
}

// Click

fun clickOnStatDetailRecycler(matcher: Matcher<View>) {
    clickOnStatDetailRecyclerItem(matcher)
}

// Private

private fun scrollStatDetailRecyclerToView(matcher: Matcher<View>) {
    onView(withId(statDetailR.id.rvStatisticsDetailContent))
        .perform(collapseToolbar())
    onView(withId(statDetailR.id.rvStatisticsDetailContent))
        .perform(scrollTo<BaseRecyclerViewHolder>(matcher))
}

private fun clickOnStatDetailRecyclerItem(matcher: Matcher<View>) {
    clickOnView(allOf(isDescendantOfA(withId(statDetailR.id.rvStatisticsDetailContent)), matcher))
}
