package com.example.util.simpletimetracker.utils

import android.widget.DatePicker
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
import com.example.util.simpletimetracker.feature_dialogs.dateTime.CustomTimePicker
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.equalTo
import com.example.util.simpletimetracker.core.R as coreR
import com.example.util.simpletimetracker.feature_change_activity_filter.R as changeActivityFilterR
import com.example.util.simpletimetracker.feature_change_category.R as changeCategoryR
import com.example.util.simpletimetracker.feature_change_record.R as changeRecordR
import com.example.util.simpletimetracker.feature_change_record_tag.R as changeRecordTagR
import com.example.util.simpletimetracker.feature_change_record_type.R as changeRecordTypeR
import com.example.util.simpletimetracker.feature_dialogs.R as dialogsR
import com.example.util.simpletimetracker.feature_main.R as mainR
import com.example.util.simpletimetracker.feature_records.R as recordsR

object NavUtils {

    fun openRunningRecordsScreen() {
        onView(withId(mainR.id.mainTabs)).perform(selectTabAtPosition(0))
        Thread.sleep(1000)
    }

    fun openRecordsScreen() {
        onView(withId(mainR.id.mainTabs)).perform(selectTabAtPosition(1))
        Thread.sleep(1000)
    }

    fun openStatisticsScreen() {
        onView(withId(mainR.id.mainTabs)).perform(selectTabAtPosition(2))
        Thread.sleep(1000)
    }

    fun openSettingsScreen() {
        onView(withId(mainR.id.mainTabs)).perform(selectTabAtPosition(3))
        Thread.sleep(1000)
    }

    fun openSettingsNotifications() {
        onView(withText(coreR.string.settings_notification_title)).perform(nestedScrollTo(), click())
    }

    fun openSettingsDisplay() {
        onView(withText(coreR.string.settings_display_title)).perform(nestedScrollTo(), click())
    }

    fun openSettingsAdditional() {
        onView(withText(coreR.string.settings_additional_title)).perform(nestedScrollTo(), click())
    }

    fun openSettingsBackup() {
        onView(withText(coreR.string.settings_backup_title)).perform(nestedScrollTo(), click())
    }

    fun openCategoriesScreen() {
        onView(withText(coreR.string.settings_edit_categories)).perform(nestedScrollTo(), click())
    }

    fun openArchiveScreen() {
        onView(withText(coreR.string.settings_archive)).perform(nestedScrollTo(), click())
    }

    fun openDataEditScreen() {
        onView(withText(coreR.string.settings_data_edit)).perform(nestedScrollTo(), click())
    }

    fun openCardSizeScreen() {
        onView(withText(coreR.string.settings_change_card_size)).perform(nestedScrollTo(), click())
    }

    fun addActivity(
        name: String,
        color: Int? = null,
        icon: Int? = null,
        text: String? = null,
        categories: List<String> = emptyList(),
    ) {
        tryAction { clickOnViewWithText(coreR.string.running_records_add_type) }

        // Name
        typeTextIntoView(changeRecordTypeR.id.etChangeRecordTypeName, name)

        // Color
        if (color != null) {
            clickOnViewWithText(coreR.string.change_record_type_color_hint)
            scrollRecyclerToView(changeRecordTypeR.id.rvChangeRecordTypeColor, withCardColor(color))
            clickOnRecyclerItem(changeRecordTypeR.id.rvChangeRecordTypeColor, withCardColor(color))
            clickOnViewWithText(coreR.string.change_record_type_color_hint)
        }

        // Icon
        if (icon != null) {
            clickOnViewWithText(coreR.string.change_record_type_icon_image_hint)
            scrollRecyclerToView(changeRecordTypeR.id.rvChangeRecordTypeIcon, hasDescendant(withTag(icon)))
            clickOnRecyclerItem(changeRecordTypeR.id.rvChangeRecordTypeIcon, withTag(icon))
            clickOnViewWithId(changeRecordTypeR.id.fieldChangeRecordTypeIcon)
        } else if (text != null) {
            clickOnViewWithText(coreR.string.change_record_type_icon_image_hint)
            onView(withId(changeRecordTypeR.id.rvChangeRecordTypeIcon)).perform(collapseToolbar())
            scrollRecyclerToView(changeRecordTypeR.id.rvChangeRecordTypeIcon, hasDescendant(withText(text)))
            clickOnRecyclerItem(changeRecordTypeR.id.rvChangeRecordTypeIcon, withText(text))
            clickOnViewWithId(changeRecordTypeR.id.fieldChangeRecordTypeIcon)
        }

        // Categories
        if (categories.isNotEmpty()) {
            clickOnViewWithText(coreR.string.category_hint)
            categories.forEach { categoryName ->
                scrollRecyclerToView(
                    changeRecordTypeR.id.rvChangeRecordTypeCategories, hasDescendant(withText(categoryName))
                )
                clickOnRecyclerItem(changeRecordTypeR.id.rvChangeRecordTypeCategories, withText(categoryName))
            }
            clickOnViewWithText(coreR.string.category_hint)
        }

        closeSoftKeyboard()
        clickOnViewWithText(coreR.string.change_record_type_save)
    }

    fun addCategory(
        name: String,
        color: Int? = null,
        activities: List<String> = emptyList(),
    ) {
        tryAction { clickOnViewWithText(coreR.string.categories_add_category) }

        // Name
        typeTextIntoView(changeCategoryR.id.etChangeCategoryName, name)

        // Color
        if (color != null) {
            clickOnViewWithText(coreR.string.change_category_color_hint)
            scrollRecyclerToView(changeCategoryR.id.rvChangeCategoryColor, withCardColor(color))
            clickOnRecyclerItem(changeCategoryR.id.rvChangeCategoryColor, withCardColor(color))
        }

        // Activities
        clickOnViewWithText(coreR.string.change_category_types_hint)
        activities.forEach { typeName ->
            scrollRecyclerToView(changeCategoryR.id.rvChangeCategoryType, hasDescendant(withText(typeName)))
            clickOnRecyclerItem(changeCategoryR.id.rvChangeCategoryType, withText(typeName))
        }

        clickOnViewWithText(coreR.string.change_category_save)
    }

    fun addRecordTag(
        name: String,
        activity: String? = null,
        color: Int? = null,
    ) {
        tryAction { clickOnViewWithText(coreR.string.categories_add_record_tag) }

        // Name
        typeTextIntoView(changeRecordTagR.id.etChangeRecordTagName, name)

        // Color
        if (color != null) {
            clickOnView(
                allOf(
                    isDescendantOfA(withId(changeRecordTagR.id.buttonsChangeRecordTagType)),
                    withText(coreR.string.change_record_tag_type_general)
                )
            )
            clickOnViewWithId(changeRecordTagR.id.fieldChangeRecordTagColor)
            scrollRecyclerToView(changeRecordTagR.id.rvChangeRecordTagColor, withCardColor(color))
            clickOnRecyclerItem(changeRecordTagR.id.rvChangeRecordTagColor, withCardColor(color))
        }

        // Activity
        if (!activity.isNullOrEmpty()) {
            clickOnView(
                allOf(
                    isDescendantOfA(withId(changeRecordTagR.id.buttonsChangeRecordTagType)),
                    withText(coreR.string.change_record_tag_type_typed)
                )
            )
            clickOnViewWithId(changeRecordTagR.id.fieldChangeRecordTagType)
            scrollRecyclerToView(changeRecordTagR.id.rvChangeRecordTagType, hasDescendant(withText(activity)))
            clickOnRecyclerItem(changeRecordTagR.id.rvChangeRecordTagType, withText(activity))
        }

        closeSoftKeyboard()
        clickOnViewWithText(coreR.string.change_category_save)
    }

    fun addRecord(name: String) {
        tryAction { clickOnViewWithId(recordsR.id.btnRecordAdd) }

        // Activity
        clickOnViewWithText(coreR.string.change_record_type_field)
        clickOnRecyclerItem(changeRecordR.id.rvChangeRecordType, withText(name))

        clickOnViewWithText(coreR.string.change_record_save)
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
        tryAction { clickOnViewWithId(recordsR.id.btnRecordAdd) }

        // Time started
        clickOnViewWithId(changeRecordR.id.tvChangeRecordTimeStarted)
        onView(withClassName(equalTo(CustomTimePicker::class.java.name)))
            .perform(setTime(hourStarted, minutesStarted))
        clickOnViewWithId(dialogsR.id.btnDateTimeDialogPositive)

        // Time ended
        clickOnViewWithId(changeRecordR.id.tvChangeRecordTimeEnded)
        onView(withClassName(equalTo(CustomTimePicker::class.java.name)))
            .perform(setTime(hourEnded, minutesEnded))
        clickOnViewWithId(dialogsR.id.btnDateTimeDialogPositive)

        // Comment
        if (!comment.isNullOrEmpty()) {
            clickOnViewWithText(coreR.string.change_record_comment_field)
            typeTextIntoView(changeRecordR.id.etChangeRecordComment, comment)
            clickOnViewWithText(coreR.string.change_record_comment_field)
        }

        // Activity
        clickOnViewWithText(coreR.string.change_record_type_field)
        clickOnRecyclerItem(changeRecordR.id.rvChangeRecordType, withText(name))

        // Tag
        if (tag != null) {
            tryAction { clickOnRecyclerItem(changeRecordR.id.rvChangeRecordCategories, withText(tag)) }
            clickOnViewWithText(coreR.string.change_record_tag_field)
        }

        clickOnViewWithText(coreR.string.change_record_save)
    }

    fun addActivityFilter(
        name: String,
        color: Int? = null,
        activities: List<String> = emptyList(),
        categories: List<String> = emptyList(),
    ) {
        tryAction { clickOnViewWithText(coreR.string.running_records_add_filter) }

        // Name
        typeTextIntoView(changeActivityFilterR.id.etChangeActivityFilterName, name)

        // Color
        if (color != null) {
            clickOnViewWithText(coreR.string.change_category_color_hint)
            scrollRecyclerToView(changeActivityFilterR.id.rvChangeActivityFilterColor, withCardColor(color))
            clickOnRecyclerItem(changeActivityFilterR.id.rvChangeActivityFilterColor, withCardColor(color))
        }

        // Activity
        if (activities.isNotEmpty()) {
            clickOnViewWithId(changeActivityFilterR.id.fieldChangeActivityFilterType)
            clickOnView(
                allOf(
                    isDescendantOfA(withId(changeActivityFilterR.id.buttonsChangeActivityFilterType)),
                    withText(coreR.string.activity_hint)
                )
            )
            activities.forEach {
                scrollRecyclerToView(changeActivityFilterR.id.rvChangeActivityFilterType, hasDescendant(withText(it)))
                clickOnRecyclerItem(changeActivityFilterR.id.rvChangeActivityFilterType, withText(it))
            }
            clickOnViewWithId(changeActivityFilterR.id.fieldChangeActivityFilterType)
        }

        // Category
        if (categories.isNotEmpty()) {
            clickOnViewWithId(changeActivityFilterR.id.fieldChangeActivityFilterType)
            clickOnView(
                allOf(
                    isDescendantOfA(withId(changeActivityFilterR.id.buttonsChangeActivityFilterType)),
                    withText(coreR.string.category_hint)
                )
            )
            categories.forEach {
                scrollRecyclerToView(changeActivityFilterR.id.rvChangeActivityFilterType, hasDescendant(withText(it)))
                clickOnRecyclerItem(changeActivityFilterR.id.rvChangeActivityFilterType, withText(it))
            }
            clickOnViewWithId(changeActivityFilterR.id.fieldChangeActivityFilterType)
        }

        clickOnViewWithText(coreR.string.change_activity_filter_save)
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
        clickOnViewWithId(dialogsR.id.tvCustomRangeSelectionTimeStarted)
        onView(withClassName(equalTo(DatePicker::class.java.name)))
            .perform(setDate(yearStarted, monthStarted + 1, dayStarted))
        clickOnViewWithId(dialogsR.id.btnDateTimeDialogPositive)

        // Set time ended
        clickOnViewWithId(dialogsR.id.tvCustomRangeSelectionTimeEnded)
        onView(withClassName(equalTo(DatePicker::class.java.name)))
            .perform(setDate(yearEnded, monthEnded + 1, dayEnded))
        clickOnViewWithId(dialogsR.id.btnDateTimeDialogPositive)

        clickOnViewWithId(dialogsR.id.btnCustomRangeSelection)
    }
}