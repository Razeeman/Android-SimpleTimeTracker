package com.example.util.simpletimetracker.utils

import android.widget.DatePicker
import android.widget.TimePicker
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.PickerActions.setDate
import androidx.test.espresso.contrib.PickerActions.setTime
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.withClassName
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.example.util.simpletimetracker.R
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.equalTo

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
        onView(withText(R.string.settings_edit_categories)).perform(nestedScrollTo(), click())
    }

    fun openArchiveScreen() {
        onView(withText(R.string.settings_archive)).perform(nestedScrollTo(), click())
    }

    fun openCardSizeScreen() {
        onView(withText(R.string.settings_change_card_size)).perform(nestedScrollTo(), click())
    }

    fun addActivity(
        name: String,
        color: Int? = null,
        icon: Int? = null,
        emoji: String? = null,
        categories: List<String> = emptyList(),
        goalTime: String? = null,
    ) {
        tryAction { clickOnViewWithText(R.string.running_records_add_type) }

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
        } else if (emoji != null) {
            clickOnViewWithText(R.string.change_record_type_icon_hint)
            onView(withId(R.id.rvChangeRecordTypeIcon)).perform(collapseToolbar())
            scrollRecyclerToView(R.id.rvChangeRecordTypeIcon, hasDescendant(withText(emoji)))
            clickOnRecyclerItem(R.id.rvChangeRecordTypeIcon, withText(emoji))
        }

        // Categories
        if (categories.isNotEmpty()) {
            clickOnViewWithText(R.string.change_record_type_category_hint)
            categories.forEach { categoryName ->
                scrollRecyclerToView(
                    R.id.rvChangeRecordTypeCategories, hasDescendant(withText(categoryName))
                )
                clickOnRecyclerItem(R.id.rvChangeRecordTypeCategories, withText(categoryName))
            }
            clickOnViewWithText(R.string.change_record_type_category_hint)
        }

        // Goal time
        if (!goalTime.isNullOrEmpty()) {
            clickOnViewWithId(R.id.groupChangeRecordTypeGoalTime)
            goalTime.forEach { char ->
                clickOnViewWithText(char.toString())
            }
            clickOnViewWithText(R.string.duration_dialog_save)
        }

        closeSoftKeyboard()
        clickOnViewWithText(R.string.change_record_type_save)
    }

    fun addCategory(
        name: String,
        color: Int? = null,
        activities: List<String> = emptyList(),
    ) {
        tryAction { clickOnViewWithText(R.string.categories_add_activity_tag) }

        // Name
        typeTextIntoView(R.id.etChangeCategoryName, name)

        // Color
        if (color != null) {
            clickOnViewWithText(R.string.change_category_color_hint)
            scrollRecyclerToView(R.id.rvChangeCategoryColor, withCardColor(color))
            clickOnRecyclerItem(R.id.rvChangeCategoryColor, withCardColor(color))
        }

        // Activities
        clickOnViewWithText(R.string.change_category_types_hint)
        activities.forEach { typeName ->
            scrollRecyclerToView(R.id.rvChangeCategoryType, hasDescendant(withText(typeName)))
            clickOnRecyclerItem(R.id.rvChangeCategoryType, withText(typeName))
        }

        clickOnViewWithText(R.string.change_category_save)
    }

    fun addRecordTag(
        name: String,
        activity: String? = null,
        color: Int? = null,
    ) {
        tryAction { clickOnViewWithText(R.string.categories_add_record_tag) }

        // Name
        typeTextIntoView(R.id.etChangeRecordTagName, name)

        // Color
        if (color != null) {
            clickOnView(
                allOf(
                    isDescendantOfA(withId(R.id.buttonsChangeRecordTagType)),
                    withText(R.string.change_record_tag_type_general)
                )
            )
            clickOnViewWithId(R.id.fieldChangeRecordTagColor)
            scrollRecyclerToView(R.id.rvChangeRecordTagColor, withCardColor(color))
            clickOnRecyclerItem(R.id.rvChangeRecordTagColor, withCardColor(color))
        }

        // Activity
        if (!activity.isNullOrEmpty()) {
            clickOnView(
                allOf(
                    isDescendantOfA(withId(R.id.buttonsChangeRecordTagType)),
                    withText(R.string.change_record_tag_type_typed)
                )
            )
            clickOnViewWithId(R.id.fieldChangeRecordTagType)
            scrollRecyclerToView(R.id.rvChangeRecordTagType, hasDescendant(withText(activity)))
            clickOnRecyclerItem(R.id.rvChangeRecordTagType, withText(activity))
        }

        clickOnViewWithText(R.string.change_category_save)
    }

    fun addRecord(name: String) {
        tryAction { clickOnViewWithId(R.id.btnRecordAdd) }

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
        comment: String? = null,
        tag: String? = null,
    ) {
        tryAction { clickOnViewWithId(R.id.btnRecordAdd) }

        // Time started
        clickOnViewWithId(R.id.tvChangeRecordTimeStarted)
        onView(withClassName(equalTo(TimePicker::class.java.name)))
            .perform(setTime(hourStarted, minutesStarted))
        clickOnViewWithId(R.id.btnDateTimeDialogPositive)

        // Time ended
        clickOnViewWithId(R.id.tvChangeRecordTimeEnded)
        onView(withClassName(equalTo(TimePicker::class.java.name)))
            .perform(setTime(hourEnded, minutesEnded))
        clickOnViewWithId(R.id.btnDateTimeDialogPositive)

        // Comment
        if (!comment.isNullOrEmpty()) {
            typeTextIntoView(R.id.etChangeRecordComment, comment)
            closeSoftKeyboard()
        }

        // Activity
        clickOnViewWithText(R.string.change_record_type_field)
        clickOnRecyclerItem(R.id.rvChangeRecordType, withText(name))

        // Tag
        if (tag != null) {
            clickOnViewWithText(R.string.change_record_category_field)
            clickOnRecyclerItem(R.id.rvChangeRecordCategories, withText(tag))
        }

        clickOnViewWithText(R.string.change_record_save)
    }

    fun setCustomRange(
        yearStarted: Int,
        monthStarted: Int,
        dayStarted: Int,
        yearEnded: Int,
        monthEnded: Int,
        dayEnded: Int,
    ) {
        // Set time started
        clickOnViewWithId(R.id.tvCustomRangeSelectionTimeStarted)
        onView(withClassName(equalTo(DatePicker::class.java.name)))
            .perform(setDate(yearStarted, monthStarted + 1, dayStarted))
        clickOnViewWithId(R.id.btnDateTimeDialogPositive)

        // Set time ended
        clickOnViewWithId(R.id.tvCustomRangeSelectionTimeEnded)
        onView(withClassName(equalTo(DatePicker::class.java.name)))
            .perform(setDate(yearEnded, monthEnded + 1, dayEnded))
        clickOnViewWithId(R.id.btnDateTimeDialogPositive)

        clickOnViewWithId(R.id.btnCustomRangeSelection)
    }
}