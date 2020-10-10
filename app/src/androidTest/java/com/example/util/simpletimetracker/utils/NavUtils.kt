package com.example.util.simpletimetracker.utils

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.example.util.simpletimetracker.R

object NavUtils {

    fun openRunningRecordsScreen() {
        onView(withId(R.id.mainTabs)).perform(selectTabAtPosition(0))
        Thread.sleep(1000)
    }

    fun openRecordsScreen() {
        onView(withId(R.id.mainTabs)).perform(selectTabAtPosition(1))
        Thread.sleep(1000)
    }

    fun openStatisticsScreen() {
        onView(withId(R.id.mainTabs)).perform(selectTabAtPosition(2))
        Thread.sleep(1000)
    }

    fun openSettingsScreen() {
        onView(withId(R.id.mainTabs)).perform(selectTabAtPosition(3))
        Thread.sleep(1000)
    }

    fun addActivity(name: String, color: Int? = null, icon: Int? = null) {
        Thread.sleep(1000)

        clickOnViewWithText(R.string.running_records_add_type)

        // Name
        typeTextIntoView(R.id.etChangeRecordTypeName, name)

        // Color
        if (color != null) {
            clickOnViewWithText(R.string.change_record_type_color_hint)
            scrollRecyclerToView(R.id.rvChangeRecordTypeColor, withCardColor(color))
            clickOnRecyclerItem(R.id.rvChangeRecordTypeColor, withCardColor(color))
        }

        // Icon
        if (icon != null) {
            clickOnViewWithText(R.string.change_record_type_icon_hint)
            scrollRecyclerToView(R.id.rvChangeRecordTypeIcon, hasDescendant(withTag(icon)))
            clickOnRecyclerItem(R.id.rvChangeRecordTypeIcon, withTag(icon))
        }

        clickOnViewWithText(R.string.change_record_type_save)
    }

    fun addRecord(name: String) {
        clickOnViewWithId(R.id.btnRecordAdd)

        // Activity
        clickOnViewWithText(R.string.change_record_type_field)
        clickOnRecyclerItem(R.id.rvChangeRecordType, withText(name))

        clickOnViewWithText(R.string.change_record_save)
    }
}