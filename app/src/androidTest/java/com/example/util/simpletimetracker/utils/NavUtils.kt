package com.example.util.simpletimetracker.utils

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.example.util.simpletimetracker.R

object NavUtils {

    fun openRunningRecordsScreen() {
        onView(withId(R.id.mainTabs)).perform(selectTabAtPosition(0))
    }

    fun openRecordsScreen() {
        onView(withId(R.id.mainTabs)).perform(selectTabAtPosition(1))
    }

    fun openStatisticsScreen() {
        onView(withId(R.id.mainTabs)).perform(selectTabAtPosition(2))
    }

    fun openSettingsScreen() {
        onView(withId(R.id.mainTabs)).perform(selectTabAtPosition(3))
    }
}