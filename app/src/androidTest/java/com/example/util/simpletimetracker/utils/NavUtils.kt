package com.example.util.simpletimetracker.utils

import android.widget.TimePicker
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.contrib.PickerActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.example.util.simpletimetracker.R
import org.hamcrest.CoreMatchers

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

    fun openCategoriesScreen() {
        openSettingsScreen()
        clickOnViewWithText(R.string.settings_edit_categories)
    }

    fun addActivity(
        name: String,
        color: Int? = null,
        icon: Int? = null,
        categories: List<String> = emptyList(),
        goalTime: String? = null
    ) {
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

        // Categories
        clickOnViewWithText(R.string.change_record_type_category_hint)
        categories.forEach { categoryName ->
            scrollRecyclerToView(
                R.id.rvChangeRecordTypeCategories, hasDescendant(withText(categoryName))
            )
            clickOnRecyclerItem(R.id.rvChangeRecordTypeCategories, withText(categoryName))
        }
        clickOnViewWithText(R.string.change_record_type_category_hint)

        // Goal time
        if (!goalTime.isNullOrEmpty()) {
            clickOnViewWithId(R.id.groupChangeRecordTypeGoalTime)
            goalTime.forEach { char ->
                clickOnViewWithText(char.toString())
            }
            clickOnViewWithText(R.string.duration_dialog_save)
        }

        clickOnViewWithText(R.string.change_record_type_save)
    }

    fun addCategory(
        name: String,
        color: Int? = null,
        activities: List<String> = emptyList()
    ) {
        Thread.sleep(1000)

        clickOnViewWithText(R.string.categories_add)

        // Name
        typeTextIntoView(R.id.etChangeCategoryName, name)

        // Color
        if (color != null) {
            clickOnViewWithText(R.string.change_category_color_hint)
            scrollRecyclerToView(R.id.rvChangeCategoryColor, withCardColor(color))
            clickOnRecyclerItem(R.id.rvChangeCategoryColor, withCardColor(color))
        }

        // Activities
        clickOnViewWithText(R.string.change_category_type_hint)
        activities.forEach { typeName ->
            scrollRecyclerToView(R.id.rvChangeCategoryType, hasDescendant(withText(typeName)))
            clickOnRecyclerItem(R.id.rvChangeCategoryType, withText(typeName))
        }

        clickOnViewWithText(R.string.change_category_save)
    }

    fun addRecord(name: String) {
        clickOnViewWithId(R.id.btnRecordAdd)

        // Activity
        clickOnViewWithText(R.string.change_record_type_field)
        clickOnRecyclerItem(R.id.rvChangeRecordType, withText(name))

        clickOnViewWithText(R.string.change_record_save)
    }

    fun addRecordWithTime(
        name: String,
        hourStarted: Int,
        minutesStarted: Int,
        hourEnded: Int,
        minutesEnded: Int,
        comment: String? = null
    ) {
        clickOnViewWithId(R.id.btnRecordAdd)

        // Time started
        clickOnViewWithId(R.id.tvChangeRecordTimeStarted)
        onView(ViewMatchers.withClassName(CoreMatchers.equalTo(TimePicker::class.java.name)))
            .perform(PickerActions.setTime(hourStarted, minutesStarted))
        clickOnViewWithId(R.id.btnDateTimeDialogPositive)

        // Time ended
        clickOnViewWithId(R.id.tvChangeRecordTimeEnded)
        onView(ViewMatchers.withClassName(CoreMatchers.equalTo(TimePicker::class.java.name)))
            .perform(PickerActions.setTime(hourEnded, minutesEnded))
        clickOnViewWithId(R.id.btnDateTimeDialogPositive)

        // Comment
        if (!comment.isNullOrEmpty()) {
            typeTextIntoView(R.id.etChangeRecordComment, comment)
            closeSoftKeyboard()
        }

        // Activity
        clickOnViewWithText(R.string.change_record_type_field)
        clickOnRecyclerItem(R.id.rvChangeRecordType, withText(name))

        clickOnViewWithText(R.string.change_record_save)
    }
}